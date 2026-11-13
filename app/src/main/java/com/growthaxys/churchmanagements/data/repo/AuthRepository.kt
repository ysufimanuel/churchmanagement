package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.BuildConfig
import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val profile: Profile) : SessionState
}

enum class LoginError { Required, Invalid, UsernameUnconfigured, Unavailable }
class LoginException(val kind: LoginError) : Exception(kind.name)

@Singleton
class AuthRepository @Inject constructor(private val client: SupabaseClient) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    /** Observes the SDK session (restore / refresh / expiry / sign-out) and loads the profile row. */
    suspend fun observe() {
        client.auth.sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val uid = status.session.user?.id
                    _state.value = if (uid == null) SessionState.SignedOut else loadProfile(uid)?.let { SessionState.SignedIn(it) } ?: SessionState.SignedOut
                }
                is SessionStatus.NotAuthenticated -> _state.value = SessionState.SignedOut
                is SessionStatus.LoadingFromStorage -> _state.value = SessionState.Loading
                else -> Unit // NetworkError: keep current state; SDK retries
            }
        }
    }

    private suspend fun loadProfile(uid: String): Profile? = runCatching {
        client.from("profiles").select { filter { eq("id", uid) } }.decodeSingleOrNull<Profile>()
    }.getOrNull()

    suspend fun refreshProfile() {
        val uid = client.auth.currentUserOrNull()?.id ?: return
        loadProfile(uid)?.let { _state.value = SessionState.SignedIn(it) }
    }

    /**
     * Web flow: username -> email lookup happens server-side with the service role (never exposed to clients).
     * Android supports email directly. A username is resolved only through an optional trusted endpoint
     * configured via LOGIN_RESOLVER_URL (see docs/BACKEND_INTEGRATION_AUDIT.md); otherwise the web's
     * `login-username-unconfigured` message is shown.
     */
    suspend fun signIn(usernameOrEmail: String, password: String) {
        val id = usernameOrEmail.trim()
        if (id.isEmpty() || password.isEmpty()) throw LoginException(LoginError.Required)
        val email = if ("@" in id) id else resolveUsername(id)
        try {
            client.auth.signInWith(Email) { this.email = email; this.password = password }
        } catch (e: Exception) {
            throw LoginException(LoginError.Invalid)
        }
        // last_login must never block access (web logs and continues)
        runCatching {
            val uid = client.auth.currentUserOrNull()?.id
            if (uid != null) client.from("profiles").update(jsonOf("last_login" to java.time.Instant.now().toString())) { filter { eq("id", uid) } }
        }
    }

    private suspend fun resolveUsername(username: String): String = withContext(Dispatchers.IO) {
        val url = BuildConfig.LOGIN_RESOLVER_URL
        if (url.isBlank()) throw LoginException(LoginError.UsernameUnconfigured)
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            conn.doOutput = true
            conn.outputStream.use { it.write("""{"username":${Json.encodeToString(kotlinx.serialization.serializer<String>(), username)}}""".toByteArray()) }
            if (conn.responseCode != 200) throw LoginException(LoginError.Invalid)
            val body = conn.inputStream.bufferedReader().readText()
            val obj: JsonObject = Json.parseToJsonElement(body).jsonObject
            obj["email"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: throw LoginException(LoginError.Invalid)
        } catch (e: LoginException) { throw e } catch (e: Exception) { throw LoginException(LoginError.Unavailable) }
    }

    suspend fun signOut() { runCatching { client.auth.signOut() }; _state.value = SessionState.SignedOut }

    /** Web changePasswordAction: verify the current password, then update. */
    suspend fun changePassword(email: String, current: String, next: String): ActionResult {
        try {
            client.auth.signInWith(Email) { this.email = email; this.password = current }
        } catch (e: Exception) { return ActionResult.fail(PASSWORD_WRONG) }
        return action { client.auth.modifyUser { password = next } }
    }

    companion object { const val PASSWORD_WRONG = "__password_wrong__" }
}
