package com.growthaxys.churchmanagements.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Column names/types come from supabase/schema.sql + migrations of the web repo (see docs/DATABASE_MAPPING.md). */

@Serializable
data class Profile(
    val id: String,
    val nama: String = "",
    val username: String? = null,
    val email: String? = null,
    val role: String = "user",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val status: String = "aktif",
    @SerialName("church_id") val churchId: String? = null,
    @SerialName("last_login") val lastLogin: String? = null,
)

@Serializable
data class ProfileLite(val id: String, val nama: String = "", val role: String = "user")

@Serializable
data class Member(
    val id: Long,
    val nama: String = "",
    val email: String? = null,
    val telepon: String? = null,
    val jk: String? = null,
    @SerialName("tempat_lahir") val tempatLahir: String? = null,
    @SerialName("tgl_lahir") val tglLahir: String? = null,
    val alamat: String? = null,
    val kota: String? = null,
    val kodepos: String? = null,
    val status: String = "aktif",
    @SerialName("group_id") val groupId: Long? = null,
    @SerialName("join_date") val joinDate: String? = null,
)

@Serializable data class NamedRef(val id: Long, val nama: String = "")

@Serializable
data class Family(
    val id: Long, val nama: String = "",
    @SerialName("kepala_id") val kepalaId: Long? = null,
    val alamat: String? = null, val kota: String? = null,
)
@Serializable data class FamilyMember(@SerialName("family_id") val familyId: Long, @SerialName("member_id") val memberId: Long)

@Serializable
data class Group(
    val id: Long, val nama: String = "", val deskripsi: String? = null,
    @SerialName("leader_id") val leaderId: Long? = null, val jadwal: String? = null,
    @SerialName("show_phone") val showPhone: Boolean = true,
)
@Serializable data class GroupMember(@SerialName("group_id") val groupId: Long, @SerialName("member_id") val memberId: Long)

@Serializable
data class Event(
    val id: Long, val nama: String = "", val tipe: String = "ibadah",
    @SerialName("custom_type") val customType: String? = null,
    @SerialName("start_at") val startAt: String = "",
    @SerialName("end_at") val endAt: String? = null,
    val lokasi: String? = null, val deskripsi: String? = null,
    val kapasitas: Int? = null, val status: String = "upcoming",
    @SerialName("image_url") val imageUrl: String? = null,
)
@Serializable
data class EventParticipant(
    val id: Long, @SerialName("event_id") val eventId: Long,
    @SerialName("member_id") val memberId: Long? = null,
    @SerialName("nama_manual") val namaManual: String? = null,
    @SerialName("registered_at") val registeredAt: String? = null,
)
@Serializable
data class EventLite(val id: Long, val nama: String = "", val status: String = "upcoming")

@Serializable
data class Attendance(
    val id: Long, @SerialName("event_id") val eventId: Long, @SerialName("member_id") val memberId: Long,
    val tanggal: String = "", val waktu: String? = null, val status: String = "hadir",
)

@Serializable
data class Donor(
    val id: Long, val nama: String = "", val email: String? = null, val telepon: String? = null,
    @SerialName("total_donasi") val totalDonasi: Double = 0.0,
    @SerialName("terakhir_donasi") val terakhirDonasi: String? = null,
)
@Serializable
data class Donation(
    val id: Long, @SerialName("donor_id") val donorId: Long? = null,
    val tipe: String = "", @SerialName("custom_type") val customType: String? = null,
    val jumlah: Double = 0.0, val tanggal: String = "", val keterangan: String? = null,
    val donors: DonorName? = null, // from select("*, donors(nama)")
)
@Serializable data class DonorName(val nama: String = "")

@Serializable
data class Volunteer(
    val id: Long, @SerialName("member_id") val memberId: Long? = null,
    @SerialName("external_nama") val externalNama: String? = null,
    @SerialName("external_email") val externalEmail: String? = null,
    @SerialName("external_telepon") val externalTelepon: String? = null,
    val area: String = "", val jadwal: List<String> = emptyList(), val status: String = "aktif",
)
@Serializable
data class VolunteerAssignment(
    val id: Long, @SerialName("volunteer_id") val volunteerId: Long,
    @SerialName("event_id") val eventId: Long? = null,
    val tanggal: String = "", val waktu: String? = null, val tempat: String? = null,
    val tugas: String? = null, val catatan: String? = null, val status: String = "assigned",
)

@Serializable
data class Announcement(
    val id: Long, val judul: String = "", val konten: String = "", val tanggal: String = "",
    val expiry: String? = null, val important: Boolean = false,
    @SerialName("author_id") val authorId: String? = null,
)
@Serializable
data class Message(
    val id: Long = 0,
    @SerialName("sender_id") val senderId: String,
    @SerialName("receiver_id") val receiverId: String,
    val content: String = "", val read: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)
@Serializable
data class Notification(
    val id: Long, val title: String = "", val message: String = "", val type: String = "info",
    val read: Boolean = false, @SerialName("created_at") val createdAt: String = "",
    @SerialName("user_id") val userId: String? = null,
)
@Serializable
data class Activity(val id: Long, val type: String = "", val action: String = "", val detail: String? = null,
                    @SerialName("created_at") val createdAt: String = "")

@Serializable
data class FinanceTransaction(
    val id: Long, val tipe: String = "pemasukan",
    @SerialName("kategori_id") val kategoriId: Long? = null,
    val jumlah: Double = 0.0, val tanggal: String = "", val keterangan: String? = null,
    @SerialName("donatur_id") val donaturId: Long? = null, val status: String = "pending",
)
@Serializable
data class FinanceCategory(val id: Long, val nama: String = "", val tipe: String = "pemasukan", val deskripsi: String? = null)
@Serializable data class FinanceSettings(@SerialName("saldo_awal") val saldoAwal: Double = 0.0)
@Serializable
data class ApprovalHistory(val id: Long, @SerialName("transaction_id") val transactionId: Long? = null,
                           val action: String = "", @SerialName("created_at") val createdAt: String = "")

@Serializable data class AttendanceDay(val tanggal: String)
@Serializable data class DonationAmount(val jumlah: Double = 0.0, val tanggal: String = "")
@Serializable data class IdOnly(val id: Long)
@Serializable data class MemberGender(val id: Long, val jk: String? = null, val status: String = "aktif")
