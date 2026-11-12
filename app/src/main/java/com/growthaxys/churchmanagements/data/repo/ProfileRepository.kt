package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Own-profile editing. Web uses the service role to update `profiles`; on Android the same whitelisted
 * columns (nama, username, avatar_url) are updated through the `profiles_self_update` RLS policy
 * (id = auth.uid() AND role unchanged), so no elevated key is needed.
 */
@Singleton
class ProfileRepository @Inject constructor(private val client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {
    private val bucket get() = client.storage.from("avatars")

    suspend fun update(nama: String, username: String): ActionResult {
        val id = userId() ?: return ActionResult.fail("Unauthorized")
        val n = nama.trim().take(100)
        val u = username.trim().lowercase().take(40)
        if (n.isEmpty()) return ActionResult.fail(INVALID_NAME)
        if (u.isNotEmpty() && !Regex("^[a-z0-9._-]{3,40}$").matches(u)) return ActionResult.fail(INVALID_USERNAME)
        return action {
            table("profiles").update(jsonOf("nama" to n, "username" to u.ifEmpty { null })) { filter { eq("id", id) } }
            auth.refreshProfile()
        }.let { if (!it.ok && it.error?.contains("23505") == true) ActionResult.fail(USERNAME_TAKEN) else it }
    }

    /** Upload to avatars/<uid>/avatar-<ts>.jpg (bucket policy: only own folder; 2 MB; jpeg/png/webp), then save avatar_url. */
    suspend fun uploadAvatar(bytes: ByteArray): ActionResult {
        val id = userId() ?: return ActionResult.fail("Unauthorized")
        return action {
            val path = "$id/avatar-${System.currentTimeMillis()}.jpg"
            bucket.upload(path, bytes) { upsert = false }
            val url = bucket.publicUrl(path)
            table("profiles").update(jsonOf("avatar_url" to url)) { filter { eq("id", id) } }
            runCatching { bucket.list(id).map { "$id/${it.name}" }.filter { it != path }.let { old -> if (old.isNotEmpty()) bucket.delete(old) } }
            auth.refreshProfile()
        }
    }

    suspend fun removeAvatar(): ActionResult {
        val id = userId() ?: return ActionResult.fail("Unauthorized")
        return action {
            table("profiles").update(jsonOf("avatar_url" to null)) { filter { eq("id", id) } }
            runCatching { bucket.list(id).map { "$id/${it.name}" }.let { old -> if (old.isNotEmpty()) bucket.delete(old) } }
            auth.refreshProfile()
        }
    }

    /** Superadmin: list accounts of the same church (RLS: profiles_tenant_read). */
    suspend fun users(): List<Profile> = table("profiles").select { order("nama", ASC) }.decodeList()

    companion object {
        const val INVALID_NAME = "__invalid_name__"
        const val INVALID_USERNAME = "__invalid_username__"
        const val USERNAME_TAKEN = "__username_taken__"
    }
}
