package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.Rbac
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

data class SearchHit(val id: String, val group: String, val title: String, val subtitle: String?, val route: String, val query: String? = null)
data class SearchOutcome(val hits: List<SearchHit>, val failed: Boolean)

/** Port of web `searchDashboard`: same tables/columns, LIMIT 6 each; only TEXT columns are used in ilike filters. */
@Singleton
class SearchRepository @Inject constructor(client: SupabaseClient, private val auth: AuthRepository) : BaseRepo(client, auth) {

    private fun safeLike(q: String) = q.replace(Regex("[%_\\\\,()*:\"'`]"), " ").replace(Regex("\\s+"), " ").trim().take(64)

    private class Spec(
        val group: String, val table: String, val cols: String, val filterCols: List<String>, val route: String,
        val title: (JsonObject) -> String, val subtitle: (JsonObject) -> String?, val prefix: String,
    )

    private fun JsonObject.s(k: String): String? = this[k]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

    suspend fun search(raw: String, role: Role): SearchOutcome {
        val q = safeLike(raw)
        if (q.length < 2) return SearchOutcome(emptyList(), false)
        val like = "%$q%"
        val me = userId()
        val specs = buildList {
            add(Spec("members", "members", "id,nama,email,telepon,alamat,kota,status", listOf("nama", "email", "telepon", "alamat", "kota"), "members", { it.s("nama") ?: "" }, { o -> listOfNotNull(o.s("telepon"), o.s("email"), o.s("status")).joinToString(" · ") }, "m"))
            add(Spec("families", "families", "id,nama,alamat,kota", listOf("nama", "alamat", "kota"), "families", { it.s("nama") ?: "" }, { o -> listOfNotNull(o.s("alamat"), o.s("kota")).joinToString(" · ") }, "f"))
            add(Spec("groups", "groups", "id,nama,deskripsi,jadwal", listOf("nama", "deskripsi", "jadwal"), "groups", { it.s("nama") ?: "" }, { o -> o.s("deskripsi") ?: o.s("jadwal") }, "g"))
            add(Spec("events", "events", "id,nama,tipe,custom_type,lokasi,deskripsi", listOf("nama", "tipe", "custom_type", "lokasi", "deskripsi"), "events", { it.s("nama") ?: "" }, { o -> listOfNotNull(o.s("custom_type") ?: o.s("tipe"), o.s("lokasi")).joinToString(" · ") }, "e"))
            add(Spec("donations", "donations", "id,tipe,custom_type,keterangan,tanggal,jumlah", listOf("tipe", "custom_type", "keterangan"), "donations", { it.s("keterangan") ?: it.s("custom_type") ?: it.s("tipe") ?: "" }, { o -> listOfNotNull(o.s("tipe"), o.s("tanggal"), o.s("jumlah")?.let { "Rp $it" }).joinToString(" · ") }, "dn"))
            add(Spec("donations", "donors", "id,nama,email,telepon", listOf("nama", "email", "telepon"), "donations", { it.s("nama") ?: "" }, { o -> listOfNotNull(o.s("telepon"), o.s("email")).joinToString(" · ") }, "dr"))
            add(Spec("finance", "finance_categories", "id,nama,tipe,deskripsi", listOf("nama", "deskripsi"), "finance", { it.s("nama") ?: "" }, { o -> listOfNotNull(o.s("tipe"), o.s("deskripsi")).joinToString(" · ") }, "fc"))
            add(Spec("finance", "finance_transactions", "id,keterangan,tipe,tanggal,jumlah,status", listOf("keterangan"), "finance", { it.s("keterangan") ?: if (it.s("tipe") == "pemasukan") "Pemasukan" else "Pengeluaran" }, { o -> listOfNotNull(o.s("tipe"), o.s("tanggal"), o.s("status"), o.s("jumlah")?.let { "Rp $it" }).joinToString(" · ") }, "t"))
            add(Spec("volunteers", "volunteers", "id,external_nama,external_email,external_telepon,area,status", listOf("external_nama", "external_email", "external_telepon", "area", "status"), "volunteers", { it.s("external_nama") ?: it.s("area") ?: "" }, { o -> listOfNotNull(o.s("area"), o.s("external_telepon"), o.s("status")).joinToString(" · ") }, "v"))
            add(Spec("communication", "announcements", "id,judul,konten,tanggal", listOf("judul", "konten"), "communication", { it.s("judul") ?: "" }, { it.s("konten")?.take(90) }, "a"))
            add(Spec("volunteers", "volunteer_assignments", "id,tempat,tugas,catatan,status,tanggal", listOf("tempat", "tugas", "catatan", "status"), "volunteers", { it.s("tugas") ?: it.s("tempat") ?: "" }, { o -> listOfNotNull(o.s("tempat"), o.s("tanggal"), o.s("status")).joinToString(" · ") }, "va"))
            if (Rbac.canManageUsers(role)) add(Spec("user-management", "profiles", "id,nama,username,email", listOf("nama", "username", "email"), "users", { it.s("nama") ?: "" }, { it.s("username") ?: it.s("email") }, "u"))
        }
        var failed = false
        val hits = coroutineScope {
            specs.map { spec ->
                async {
                    runCatching {
                        table(spec.table).select(Columns.raw(spec.cols)) {
                            filter { or { spec.filterCols.forEach { ilike(it, like) } } }
                            limit(6)
                        }.decodeList<JsonObject>().map {
                            SearchHit(spec.prefix + (it.s("id") ?: ""), spec.group, spec.title(it), spec.subtitle(it)?.takeIf { s -> s.isNotBlank() }, spec.route, q)
                        }
                    }.getOrElse { failed = true; emptyList() }
                }
            }.let { jobs ->
                val contacts = async {
                    runCatching {
                        table("profiles").select(Columns.raw("id,nama,role")) { filter { eq("status", "aktif"); ilike("nama", like) }; limit(6) }
                            .decodeList<JsonObject>().filter { it.s("id") != me }
                            .map { SearchHit("c" + it.s("id"), "contacts", it.s("nama") ?: "", it.s("role"), "communication", q) }
                    }.getOrElse { failed = true; emptyList() }
                }
                jobs.map { it.await() }.flatten() + contacts.await()
            }
        }
        return SearchOutcome(hits, failed)
    }
}
