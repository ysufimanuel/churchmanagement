package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.*
import com.growthaxys.churchmanagements.data.orNull
import io.github.jan.supabase.SupabaseClient
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class EventInput(
    val id: Long? = null, val nama: String, val tipe: String, val customType: String, val startAt: String, val endAt: String,
    val lokasi: String, val deskripsi: String, val kapasitas: Int?, val status: String,
)

@Singleton
class EventsRepository @Inject constructor(client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {
    suspend fun events(): List<Event> = table("events").select { order("start_at", DESC) }.decodeList()
    suspend fun eventLites(): List<EventLite> = table("events").select(raw("id, nama, status")) { order("start_at", DESC) }.decodeList()
    suspend fun participants(): List<EventParticipant> = table("event_participants").select().decodeList()

    /** datetime-local ("2026-11-12T09:00") is interpreted in the device zone and sent as an ISO instant. */
    private fun toInstant(local: String): String? =
        local.orNull()?.let { runCatching { LocalDateTime.parse(it).atZone(ZoneId.systemDefault()).toOffsetDateTime().toString() }.getOrDefault(it) }

    suspend fun saveEvent(i: EventInput) = action {
        val base = arrayOf(
            "nama" to i.nama, "tipe" to i.tipe, "custom_type" to i.customType.orNull(),
            "start_at" to toInstant(i.startAt), "end_at" to toInstant(i.endAt),
            "lokasi" to i.lokasi.orNull(), "deskripsi" to i.deskripsi.orNull(), "kapasitas" to i.kapasitas, "status" to i.status,
        )
        if (i.id != null) table("events").update(jsonOf(*base)) { filter { eq("id", i.id) } }
        else table("events").insert(jsonOf(*base, "church_id" to churchId()))
    }
    suspend fun deleteEvent(id: Long) = action { table("events").delete { filter { eq("id", id) } } }

    suspend fun addParticipant(eventId: Long, memberId: Long?, namaManual: String?) = action {
        table("event_participants").insert(jsonOf("event_id" to eventId, "member_id" to memberId, "nama_manual" to namaManual, "church_id" to churchId()))
    }
    suspend fun removeParticipant(id: Long) = action { table("event_participants").delete { filter { eq("id", id) } } }

    /* ---------- Attendance ---------- */
    suspend fun attendance(): List<Attendance> = table("attendance").select { order("tanggal", DESC) }.decodeList()

    /** Mirrors web checkInMember: one row per (event, member, day); the day key is the UTC date like the web. */
    suspend fun checkIn(eventId: Long, memberId: Long) = action {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val tanggal = now.toLocalDate().toString()
        val waktu = LocalTime.now().withNano(0).toString().let { if (it.length == 5) "$it:00" else it }
        val existing = table("attendance").select { filter { eq("event_id", eventId); eq("member_id", memberId); eq("tanggal", tanggal) } }
            .decodeList<Attendance>().firstOrNull()
        if (existing != null) table("attendance").update(jsonOf("waktu" to waktu, "status" to "hadir")) { filter { eq("id", existing.id) } }
        else table("attendance").insert(jsonOf("event_id" to eventId, "member_id" to memberId, "church_id" to churchId(), "tanggal" to tanggal, "waktu" to waktu, "status" to "hadir"))
    }
    suspend fun deleteAttendance(id: Long) = action { table("attendance").delete { filter { eq("id", id) } } }
}
