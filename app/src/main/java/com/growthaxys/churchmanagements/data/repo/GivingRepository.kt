package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.action
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.*
import com.growthaxys.churchmanagements.data.orNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton

data class DonationInput(val id: Long? = null, val donorId: Long?, val tipe: String, val customType: String, val jumlah: Double, val tanggal: String, val keterangan: String)
data class TrxInput(val id: Long? = null, val tipe: String, val kategoriId: Long?, val jumlah: Double, val tanggal: String, val keterangan: String, val donaturId: Long?)
data class FinanceData(
    val transactions: List<FinanceTransaction>, val categories: List<FinanceCategory>, val saldoAwal: Double,
    val history: List<ApprovalHistory>, val donors: List<NamedRef>,
)

@Singleton
class GivingRepository @Inject constructor(client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {

    /* ---------- Donations ---------- */
    suspend fun donations(): List<Donation> = table("donations").select { order("tanggal", DESC) }.decodeList()
    suspend fun donors(): List<Donor> = table("donors").select { order("nama", ASC) }.decodeList()

    /** After every change the donor aggregates (total_donasi / terakhir_donasi) are recomputed like the web does. */
    private suspend fun refreshDonorTotals(donorId: Long): String? = try {
        val rows = table("donations").select(raw("jumlah, tanggal")) { filter { eq("donor_id", donorId) } }.decodeList<DonationAmount>()
        table("donors").update(jsonOf("total_donasi" to rows.sumOf { it.jumlah }, "terakhir_donasi" to rows.maxOfOrNull { it.tanggal })) { filter { eq("id", donorId) } }
        null
    } catch (e: Exception) { e.message ?: "error" }

    suspend fun saveDonation(i: DonationInput): ActionResult {
        var previousDonor: Long? = null
        try {
            if (i.id != null) previousDonor = table("donations").select(raw("donor_id")) { filter { eq("id", i.id) } }
                .decodeList<DonorIdOnly>().firstOrNull()?.donorId
            val base = arrayOf("donor_id" to i.donorId, "tipe" to i.tipe, "custom_type" to i.customType.orNull(), "jumlah" to i.jumlah, "tanggal" to i.tanggal, "keterangan" to i.keterangan.orNull())
            if (i.id != null) table("donations").update(jsonOf(*base)) { filter { eq("id", i.id) } }
            else table("donations").insert(jsonOf(*base, "church_id" to churchId()))
        } catch (e: Exception) { return ActionResult.fail(e.message) }
        for (d in setOfNotNull(previousDonor, i.donorId)) {
            refreshDonorTotals(d)?.let { return ActionResult.fail("Donasi tersimpan, tetapi total donatur gagal diperbarui: $it") }
        }
        return ActionResult.OK
    }

    suspend fun deleteDonation(id: Long): ActionResult {
        val donor: Long? = try {
            table("donations").select(raw("donor_id")) { filter { eq("id", id) } }.decodeList<DonorIdOnly>().firstOrNull()?.donorId
        } catch (e: Exception) { return ActionResult.fail(e.message) }
        try { table("donations").delete { filter { eq("id", id) } } } catch (e: Exception) { return ActionResult.fail(e.message) }
        if (donor != null) refreshDonorTotals(donor)?.let { return ActionResult.fail("Donasi terhapus, tetapi total donatur gagal diperbarui: $it") }
        return ActionResult.OK
    }

    /* ---------- Finance ---------- */
    suspend fun finance(): FinanceData {
        val trx = table("finance_transactions").select { order("tanggal", DESC) }.decodeList<FinanceTransaction>()
        val cats = table("finance_categories").select { order("nama", ASC) }.decodeList<FinanceCategory>()
        val settings = table("finance_settings").select { filter { eq("id", 1) } }.decodeList<FinanceSettings>().firstOrNull()
        val history = table("approval_history").select { order("created_at", DESC); limit(50) }.decodeList<ApprovalHistory>()
        val donors = table("donors").select(raw("id, nama")) { order("nama", ASC) }.decodeList<NamedRef>()
        return FinanceData(trx, cats, settings?.saldoAwal ?: 0.0, history, donors)
    }

    /** Superadmin => approved immediately; admin => pending (RLS also forces status='pending' for admins). */
    suspend fun saveTransaction(i: TrxInput, autoApprove: Boolean) = action {
        if (i.id != null) {
            table("finance_transactions").update(jsonOf("tipe" to i.tipe, "kategori_id" to i.kategoriId, "jumlah" to i.jumlah, "tanggal" to i.tanggal, "keterangan" to i.keterangan.orNull(), "donatur_id" to i.donaturId)) { filter { eq("id", i.id) } }
        } else {
            table("finance_transactions").insert(jsonOf(
                "tipe" to i.tipe, "kategori_id" to i.kategoriId, "jumlah" to i.jumlah, "tanggal" to i.tanggal,
                "keterangan" to i.keterangan.orNull(), "donatur_id" to i.donaturId,
                "status" to if (autoApprove) "approved" else "pending",
                "approved_by" to if (autoApprove) userId() else null,
                "approved_at" to if (autoApprove) java.time.Instant.now().toString() else null,
                "created_by" to userId(), "church_id" to churchId(),
            ))
        }
    }

    /** Server-side RPC (atomic approve/reject + approval_history row). */
    suspend fun process(id: Long, approve: Boolean) = action {
        client.postgrest.rpc("process_finance_transaction", jsonOf("p_transaction_id" to id, "p_action" to if (approve) "approved" else "rejected"))
    }
    suspend fun deleteTransaction(id: Long) = action { table("finance_transactions").delete { filter { eq("id", id) } } }

    suspend fun saveCategory(id: Long?, nama: String, tipe: String, deskripsi: String) = action {
        val base = arrayOf("nama" to nama, "tipe" to tipe, "deskripsi" to deskripsi.orNull())
        if (id != null) table("finance_categories").update(jsonOf(*base)) { filter { eq("id", id) } }
        else table("finance_categories").insert(jsonOf(*base, "church_id" to churchId()))
    }
    suspend fun deleteCategory(id: Long) = action { table("finance_categories").delete { filter { eq("id", id) } } }
    suspend fun updateSaldoAwal(saldo: Double) = action { table("finance_settings").update(jsonOf("saldo_awal" to saldo)) { filter { eq("id", 1) } } }
}

@kotlinx.serialization.Serializable
private data class DonorIdOnly(@kotlinx.serialization.SerialName("donor_id") val donorId: Long? = null)
