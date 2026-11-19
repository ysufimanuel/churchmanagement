package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.*
import com.growthaxys.churchmanagements.data.orNull
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

data class MemberInput(
    val id: Long? = null, val nama: String, val email: String = "", val telepon: String = "", val jk: String? = null,
    val tempatLahir: String = "", val tglLahir: String = "", val alamat: String = "", val kota: String = "", val kodepos: String = "",
    val status: String = "aktif", val groupId: Long? = null, val joinDate: String = "",
)
data class FamilyInput(val id: Long? = null, val nama: String, val kepalaId: Long?, val alamat: String, val kota: String, val anggota: List<Long>)
data class GroupInput(val id: Long? = null, val nama: String, val deskripsi: String, val leaderId: Long?, val jadwal: String, val showPhone: Boolean)

@Singleton
class PeopleRepository @Inject constructor(client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {

    /* ---------- Members ---------- */
    suspend fun members(): List<Member> = table("members").select { order("nama", ASC) }.decodeList()
    suspend fun memberNames(): List<NamedRef> = table("members").select(raw("id, nama")) { order("nama", ASC) }.decodeList()
    suspend fun groupNames(): List<NamedRef> = table("groups").select(raw("id, nama")) { order("nama", ASC) }.decodeList()

    suspend fun saveMember(i: MemberInput): ActionResult = action {
        val payload = jsonOf(
            "nama" to i.nama, "email" to i.email.orNull(), "telepon" to i.telepon.orNull(), "jk" to i.jk,
            "tempat_lahir" to i.tempatLahir.orNull(), "tgl_lahir" to i.tglLahir.orNull(),
            "alamat" to i.alamat.orNull(), "kota" to i.kota.orNull(), "kodepos" to i.kodepos.orNull(),
            "status" to i.status, "group_id" to i.groupId, "join_date" to i.joinDate.orNull(),
        )
        if (i.id != null) table("members").update(payload) { filter { eq("id", i.id) } }
        else table("members").insert(jsonOf(*payload.entries.map { it.key to it.value }.toTypedArray(), "church_id" to churchId()))
    }
    suspend fun deleteMember(id: Long) = action { table("members").delete { filter { eq("id", id) } } }

    /* ---------- Families ---------- */
    suspend fun families(): List<Family> = table("families").select { order("nama", ASC) }.decodeList()
    suspend fun familyMembers(): List<FamilyMember> = table("family_members").select().decodeList()

    /**
     * Same rules as web saveFamily: admins may only ADD relations; removals are superadmin-only
     * (RLS forbids delete for admins). No delete+reinsert (would violate RLS / leave partial data).
     */
    suspend fun saveFamily(i: FamilyInput, isSuperadmin: Boolean): ActionResult = action {
        val base = arrayOf("nama" to i.nama, "kepala_id" to i.kepalaId, "alamat" to i.alamat.orNull(), "kota" to i.kota.orNull())
        val familyId: Long = if (i.id != null) {
            table("families").update(jsonOf(*base)) { filter { eq("id", i.id) } }; i.id
        } else {
            table("families").insert(jsonOf(*base, "church_id" to churchId())) { select() }.decodeSingle<IdOnly>().id
        }
        val existing = table("family_members").select { filter { eq("family_id", familyId) } }.decodeList<FamilyMember>().map { it.memberId }
        val requested = i.anggota.filter { it > 0 }.distinct()
        if (isSuperadmin) {
            val removals = existing.filter { it !in requested }
            if (removals.isNotEmpty()) table("family_members").delete { filter { eq("family_id", familyId); isIn("member_id", removals) } }
        }
        val additions = requested.filter { it !in existing }
        if (additions.isNotEmpty()) table("family_members").insert(additions.map { jsonOf("family_id" to familyId, "member_id" to it, "church_id" to churchId()) })
    }
    suspend fun deleteFamily(id: Long) = action { table("families").delete { filter { eq("id", id) } } }

    /* ---------- Groups ---------- */
    suspend fun groups(): List<Group> = table("groups").select { order("nama", ASC) }.decodeList()
    suspend fun groupMembers(): List<GroupMember> = table("group_members").select().decodeList()

    suspend fun saveGroup(i: GroupInput): ActionResult = action {
        val base = arrayOf("nama" to i.nama, "deskripsi" to i.deskripsi.orNull(), "leader_id" to i.leaderId, "jadwal" to i.jadwal.orNull(), "show_phone" to i.showPhone)
        if (i.id != null) table("groups").update(jsonOf(*base)) { filter { eq("id", i.id) } }
        else table("groups").insert(jsonOf(*base, "church_id" to churchId()))
    }
    suspend fun deleteGroup(id: Long) = action { table("groups").delete { filter { eq("id", id) } } }

    suspend fun addGroupMember(groupId: Long, memberId: Long) = action {
        val exists = table("group_members").select { filter { eq("group_id", groupId); eq("member_id", memberId) } }.decodeList<GroupMember>().isNotEmpty()
        if (!exists) table("group_members").insert(jsonOf("group_id" to groupId, "member_id" to memberId, "church_id" to churchId()))
    }
    suspend fun removeGroupMember(groupId: Long, memberId: Long) =
        action { table("group_members").delete { filter { eq("group_id", groupId); eq("member_id", memberId) } } }
}
