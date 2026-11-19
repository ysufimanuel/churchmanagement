package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.*
import com.growthaxys.churchmanagements.data.orNull
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

data class VolunteerInput(
    val id: Long? = null, val memberId: Long?, val externalNama: String?, val externalEmail: String?, val externalTelepon: String?,
    val area: String, val jadwal: List<String>, val status: String,
)
data class AssignmentInput(
    val id: Long? = null, val volunteerId: Long, val eventId: Long?, val tanggal: String, val waktu: String,
    val tempat: String, val tugas: String, val catatan: String,
)

@Singleton
class VolunteerRepository @Inject constructor(client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {
    suspend fun volunteers(): List<Volunteer> = table("volunteers").select { order("created_at", DESC) }.decodeList()
    suspend fun assignments(): List<VolunteerAssignment> = table("volunteer_assignments").select { order("tanggal", DESC) }.decodeList()
    suspend fun memberNames(): List<NamedRef> = table("members").select(raw("id, nama")) { order("nama", ASC) }.decodeList()
    suspend fun eventNames(): List<NamedRef> = table("events").select(raw("id, nama")) { order("start_at", DESC) }.decodeList()

    suspend fun saveVolunteer(i: VolunteerInput) = action {
        val base = arrayOf(
            "member_id" to i.memberId, "external_nama" to i.externalNama.orNull(), "external_email" to i.externalEmail.orNull(),
            "external_telepon" to i.externalTelepon.orNull(), "area" to i.area, "jadwal" to i.jadwal, "status" to i.status,
        )
        if (i.id != null) table("volunteers").update(jsonOf(*base)) { filter { eq("id", i.id) } }
        else table("volunteers").insert(jsonOf(*base, "church_id" to churchId()))
    }
    suspend fun deleteVolunteer(id: Long) = action { table("volunteers").delete { filter { eq("id", id) } } }

    suspend fun saveAssignment(i: AssignmentInput) = action {
        val base = arrayOf(
            "volunteer_id" to i.volunteerId, "event_id" to i.eventId, "tanggal" to i.tanggal, "waktu" to i.waktu.orNull(),
            "tempat" to i.tempat.orNull(), "tugas" to i.tugas.orNull(), "catatan" to i.catatan.orNull(),
        )
        if (i.id != null) table("volunteer_assignments").update(jsonOf(*base)) { filter { eq("id", i.id) } }
        else table("volunteer_assignments").insert(jsonOf(*base, "status" to "assigned", "church_id" to churchId()))
    }
    suspend fun deleteAssignment(id: Long) = action { table("volunteer_assignments").delete { filter { eq("id", id) } } }
}
