package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.BuildConfig
import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class MobileUserInput(
    val id: String? = null,
    val nama: String,
    val username: String,
    val email: String,
    val password: String = "",
    val role: String,
    val status: String = "aktif",
)

@Singleton
class MobileAdminRepository @Inject constructor(private val client: SupabaseClient) {
    private val codec = Json { ignoreUnknownKeys = true }
    private val baseUrl get() = BuildConfig.WEB_BASE_URL.trimEnd('/')

    private suspend fun request(path: String, method: String, body: JSONObject? = null, authenticated: Boolean = true): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            }
            if (authenticated) {
                val token = client.auth.currentSessionOrNull()?.accessToken
                    ?: throw IOException("Sesi login tidak ditemukan. Silakan login ulang.")
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            try {
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = if (response.isBlank()) JSONObject() else JSONObject(response)
                if (code !in 200..299 || !json.optBoolean("ok", false)) {
                    throw IOException(json.optString("error").ifBlank { "Permintaan gagal (HTTP $code)." })
                }
                json
            } finally {
                connection.disconnect()
            }
        }

    suspend fun registerChurch(
        churchName: String, address: String, city: String, phone: String, churchEmail: String,
        adminName: String, username: String, email: String, password: String,
    ): ActionResult = runCatching {
        request("/api/mobile/register", "POST", JSONObject()
            .put("church_name", churchName).put("church_address", address).put("church_city", city)
            .put("church_phone", phone).put("church_email", churchEmail).put("admin_name", adminName)
            .put("username", username).put("admin_email", email).put("password", password), authenticated = false)
    }.fold({ ActionResult.OK }, { ActionResult.fail(it.message) })

    suspend fun users(): List<Profile> {
        val response = request("/api/mobile/users", "GET")
        val array = response.optJSONArray("users") ?: JSONArray()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(codec.decodeFromString(Profile.serializer(), item.toString()))
            }
        }
    }

    suspend fun saveUser(input: MobileUserInput): ActionResult = runCatching {
        val body = JSONObject()
            .put("nama", input.nama).put("username", input.username).put("email", input.email)
            .put("password", input.password).put("role", input.role).put("status", input.status)
        val method = if (input.id.isNullOrBlank()) "POST" else "PATCH"
        if (!input.id.isNullOrBlank()) body.put("id", input.id)
        request("/api/mobile/users", method, body)
    }.fold({ ActionResult.OK }, { ActionResult.fail(it.message) })

    suspend fun deleteUser(id: String): ActionResult = runCatching {
        request("/api/mobile/users", "DELETE", JSONObject().put("id", id))
    }.fold({ ActionResult.OK }, { ActionResult.fail(it.message) })
}
