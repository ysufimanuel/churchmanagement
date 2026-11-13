package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.*
import com.growthaxys.churchmanagements.data.orNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationRepository @Inject constructor(client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {

    /* ---------- Contacts & chat ---------- */
    suspend fun contacts(): List<ProfileLite> =
        table("profiles").select(raw("id, nama, role")) { filter { eq("status", "aktif") }; order("nama", ASC) }.decodeList()

    suspend fun messages(): List<Message> {
        val me = userId() ?: return emptyList()
        return table("messages").select { filter { or { eq("sender_id", me); eq("receiver_id", me) } }; order("created_at", ASC) }.decodeList()
    }

    suspend fun sendMessage(receiverId: String, content: String): ActionResult {
        val text = content.trim()
        val me = userId()
        if (text.isEmpty()) return ActionResult.fail("Pesan kosong")
        if (receiverId.isEmpty() || receiverId == me) return ActionResult.fail("Penerima pesan tidak valid")
        return action {
            table("messages").insert(jsonOf("sender_id" to me, "receiver_id" to receiverId, "church_id" to churchId(), "content" to text))
        }
    }
    suspend fun markMessageRead(id: Long) = action {
        table("messages").update(jsonOf("read" to true)) { filter { eq("id", id); eq("receiver_id", userId() ?: "") } }
    }

    /** Realtime: new messages (same as web `chat-realtime-<uid>` channel). The caller filters to its conversation. */
    fun incomingMessages(): Flow<Message> = channelFlow {
        val channel = client.channel("chat-realtime-${userId()}")
        val changes = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "messages" }
        channel.subscribe()
        try {
            changes.collect { send(it.decodeRecord<Message>()) }
        } finally {
            withContext(NonCancellable) { runCatching { channel.unsubscribe(); client.realtime.removeChannel(channel) } }
        }
    }

    /* ---------- Announcements ---------- */
    suspend fun announcements(): List<Announcement> = table("announcements").select { order("tanggal", DESC) }.decodeList()
    suspend fun saveAnnouncement(id: Long?, judul: String, konten: String, expiry: String, important: Boolean) = action {
        val base = arrayOf("judul" to judul, "konten" to konten, "expiry" to expiry.orNull(), "important" to important)
        if (id != null) table("announcements").update(jsonOf(*base)) { filter { eq("id", id) } }
        else table("announcements").insert(jsonOf(*base, "author_id" to userId(), "church_id" to churchId()))
    }
    suspend fun deleteAnnouncement(id: Long) = action { table("announcements").delete { filter { eq("id", id) } } }

    /* ---------- Broadcast (only recipients = 'all' is supported by the backend RPC) ---------- */
    suspend fun saveBroadcast(subject: String, message: String, send: Boolean) = action {
        client.postgrest.rpc(
            "create_broadcast_with_notifications",
            jsonOf("p_subject" to subject.trim(), "p_message" to message.trim(), "p_recipients" to "all", "p_send" to send),
        )
    }

    /* ---------- Notifications ---------- */
    suspend fun notifications(): List<Notification> {
        val me = userId() ?: return emptyList()
        return table("notifications").select(raw("id, title, message, type, read, created_at, user_id")) {
            filter { or { filter("user_id", FilterOperator.IS, "null"); eq("user_id", me) } }
            order("created_at", DESC)
            limit(30)
        }.decodeList()
    }
    suspend fun markNotificationRead(id: Long) = action {
        table("notifications").update(jsonOf("read" to true)) { filter { eq("id", id); eq("user_id", userId() ?: "") } }
    }
    suspend fun markAllNotificationsRead() = action {
        table("notifications").update(jsonOf("read" to true)) { filter { eq("user_id", userId() ?: ""); eq("read", false) } }
    }

    /** Realtime INSERT on notifications: broadcast (user_id null) or addressed to me. */
    fun incomingNotifications(): Flow<Notification> = channelFlow {
        val channel = client.channel("notif-realtime")
        val changes = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") { table = "notifications" }
        channel.subscribe()
        try {
            changes.collect {
                val n = it.decodeRecord<Notification>()
                if (n.userId == null || n.userId == userId()) send(n)
            }
        } finally {
            withContext(NonCancellable) { runCatching { channel.unsubscribe(); client.realtime.removeChannel(channel) } }
        }
    }
}
