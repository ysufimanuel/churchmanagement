package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.model.NamedRef
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject
import javax.inject.Singleton

/* ---- tiny JSON accessors (rows are kept as JsonObject: draft/published payloads are free-form) ---- */
fun JsonObject.str(k: String): String? = (this[k] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull
fun JsonObject.long(k: String): Long? = (this[k] as? JsonPrimitive)?.longOrNull
fun JsonObject.int(k: String): Int? = (this[k] as? JsonPrimitive)?.intOrNull
fun JsonObject.bool(k: String): Boolean? = (this[k] as? JsonPrimitive)?.booleanOrNull
fun JsonObject.obj(k: String): JsonObject? = this[k] as? JsonObject
fun JsonObject.has(k: String): Boolean = this[k]?.let { it !is JsonNull } == true

/** `site_sections` row with the web `effective()` Draft semantics (visual-editor-config.ts). */
data class WmSection(val raw: JsonObject) {
    val id: Long get() = raw.long("id") ?: 0
    val page: String get() = (raw.str("draft_page") ?: raw.str("page") ?: "").trim().lowercase()
    val type: String get() = raw.str("draft_type") ?: raw.str("type") ?: ""
    val sort: Int get() = raw.int("draft_sort_order") ?: raw.int("sort_order") ?: 0
    val enabled: Boolean get() = raw.bool("draft_enabled") ?: raw.bool("enabled") ?: false
    val deleted: Boolean get() = raw.bool("draft_deleted") == true
    val content: JsonObject get() = raw.obj("content") ?: JsonObject(emptyMap())
    val published: Boolean get() = raw.has("published")

    /** True if the Draft differs from what is live (port of hasUnpublished). */
    fun hasUnpublished(): Boolean {
        if (deleted) return raw.has("published") || raw.bool("enabled") == true || raw.bool("draft_enabled") == true
        if (!raw.has("published")) return true
        if (page != (raw.str("page") ?: "").trim().lowercase()) return true
        if (type != (raw.str("type") ?: "")) return true
        if (sort != (raw.int("sort_order") ?: 0)) return true
        if (enabled != (raw.bool("enabled") ?: false)) return true
        return raw["content"] != raw["published"]
    }
}

data class WmData(
    val nav: List<JsonObject>, val theme: JsonObject?, val sections: List<WmSection>, val slug: String?,
    val events: List<NamedRef>, val groups: List<NamedRef>, val defaultTheme: Boolean,
)

data class SectionDraft(val id: Long? = null, val page: String, val type: String, val sortOrder: Int, val content: JsonObject, val enabled: Boolean)
data class ThemeValues(val primary: String, val secondary: String, val accent: String, val font: String)
data class IdResult(val ok: Boolean, val error: String? = null, val id: Long? = null)

private val SECTION_TYPES = setOf("hero", "card_grid", "event_feed", "group_feed", "info_lokasi", "media", "map", "richtext")
private val PAGE_RE = Regex("^[a-z0-9][a-z0-9_-]{0,79}$")

/**
 * Port of `src/lib/actions/webmanager.ts`. The web actions only use the signed-in user's Supabase client
 * (RLS + publish_* RPCs), so the same calls run natively — no service-role key involved.
 */
@Singleton
class WebManagerRepository @Inject constructor(client: SupabaseClient, auth: AuthRepository) : BaseRepo(client, auth) {

    suspend fun load(): WmData {
        val cid = churchId() ?: error("No church")
        val nav = table("site_nav_items").select { filter { eq("church_id", cid) }; order("sort_order", ASC) }.decodeList<JsonObject>()
        val theme = table("site_theme").select { filter { eq("church_id", cid); eq("id", 1) } }.decodeList<JsonObject>().firstOrNull()
        val sections = table("site_sections").select { filter { eq("church_id", cid) }; order("sort_order", ASC) }.decodeList<JsonObject>().map(::WmSection)
        val slug = table("churches").select(raw("slug")) { filter { eq("id", cid) } }.decodeList<JsonObject>().firstOrNull()?.str("slug")
        val events = table("events").select(raw("id,nama")) { filter { eq("church_id", cid) }; order("nama", ASC) }.decodeList<NamedRef>()
        val groups = table("groups").select(raw("id,nama")) { filter { eq("church_id", cid) }; order("nama", ASC) }.decodeList<NamedRef>()
        val pref = runCatching { preferences() }.getOrDefault(JsonObject(emptyMap()))
        return WmData(nav, theme, sections, slug, events, groups, pref.str("website_theme") == "default")
    }

    private suspend fun preferences(): JsonObject {
        val uid = userId() ?: return JsonObject(emptyMap())
        val row = table("user_preferences").select(raw("preferences")) { filter { eq("user_id", uid) } }.decodeList<JsonObject>().firstOrNull()
        return row?.obj("preferences") ?: JsonObject(emptyMap())
    }


    /* ---------------- navigation ---------------- */
    suspend fun saveNavItem(id: Long?, label: String, href: String, visible: Boolean, sortOrder: Int) = action {
        val cid = churchId()
        val draft = jsonOf("label" to label, "href" to href, "visible" to visible, "sort_order" to sortOrder)
        val update = JsonObject(draft + mapOf("draft" to draft, "state" to JsonPrimitive("draft")))
        if (id != null) table("site_nav_items").update(update) { filter { eq("id", id); eq("church_id", cid ?: "") } }
        else table("site_nav_items").insert(JsonObject(update + mapOf("published" to JsonNull, "church_id" to JsonPrimitive(cid)))
        )
    }

    suspend fun toggleNavVisibility(row: JsonObject, visible: Boolean) = action {
        val cur = row.obj("draft") ?: row
        val draft = jsonOf("label" to cur.str("label"), "href" to cur.str("href"), "visible" to visible, "sort_order" to (cur.int("sort_order") ?: 0))
        table("site_nav_items").update(JsonObject(draft + mapOf("draft" to draft, "state" to JsonPrimitive("draft")))) {
            filter { eq("id", row.long("id") ?: 0L); eq("church_id", churchId() ?: "") }
        }
    }

    suspend fun deleteNavItem(row: JsonObject) = action {
        val id = row.long("id") ?: 0L
        val cid = churchId() ?: ""
        if (row.has("published")) table("site_nav_items").update(jsonOf("draft" to null, "state" to "draft")) { filter { eq("id", id); eq("church_id", cid) } }
        else table("site_nav_items").delete { filter { eq("id", id); eq("church_id", cid) } }
    }

    suspend fun publishNav() = action { client.postgrest.rpc("publish_site_navigation") }

    /* ---------------- theme ---------------- */
    suspend fun saveThemeDraft(t: ThemeValues) = action {
        val cid = churchId() ?: ""
        val cur = table("site_theme").select { filter { eq("church_id", cid); eq("id", 1) } }.decodeList<JsonObject>().firstOrNull() ?: error("Tema Draft tidak ditemukan")
        val draft = JsonObject((cur.obj("draft") ?: JsonObject(emptyMap())) + mapOf(
            "primary_color" to JsonPrimitive(t.primary), "secondary_color" to JsonPrimitive(t.secondary),
            "accent_color" to JsonPrimitive(t.accent), "font_family" to JsonPrimitive(t.font),
        ))
        // canonical columns stay untouched so unpublished changes never leak to the public site
        table("site_theme").update(jsonOf("draft" to draft, "state" to "draft")) { filter { eq("id", 1); eq("church_id", cid) } }
    }

    suspend fun restoreDefaultThemeDraft() = action {
        val cid = churchId() ?: ""
        val cur = table("site_theme").select { filter { eq("church_id", cid); eq("id", 1) } }.decodeList<JsonObject>().firstOrNull() ?: error("Tema Draft tidak ditemukan")
        val draft = JsonObject((cur.obj("draft") ?: JsonObject(emptyMap())) + mapOf(
            "primary_color" to JsonPrimitive("#0F1117"), "secondary_color" to JsonPrimitive("#FFFFFF"), "accent_color" to JsonPrimitive("#00E5FF"),
            "font_family" to JsonPrimitive("Plus Jakarta Sans"), "website_theme" to JsonPrimitive("default"),
        ))
        table("site_theme").update(jsonOf("draft" to draft, "state" to "draft")) { filter { eq("id", 1); eq("church_id", cid) } }
    }

    suspend fun publishTheme() = action {
        val cid = churchId() ?: ""
        val theme = table("site_theme").select { filter { eq("church_id", cid); eq("id", 1) } }.decodeList<JsonObject>().firstOrNull() ?: error("Tema tidak ditemukan")
        val prefs = preferences()
        val existing = theme.obj("published") ?: JsonObject(emptyMap())
        val draftValues = theme.obj("draft") ?: JsonObject(emptyMap())
        val published = linkedMapOf<String, JsonElement>(
            "primary_color" to JsonPrimitive(theme.str("primary_color")), "secondary_color" to JsonPrimitive("#1F2937"),
            "accent_color" to JsonPrimitive("#22C55E"), "font_family" to JsonPrimitive(theme.str("font_family")),
        )
        published.putAll(existing); published.putAll(draftValues)
        val draftPreset = draftValues.str("website_theme")
        if (draftPreset == "default" || (draftPreset == null && (prefs.str("website_theme") == "default" || existing.str("website_theme") == "default"))) {
            published["website_theme"] = JsonPrimitive("default")
        } else published.remove("website_theme")
        val pub = JsonObject(published)
        table("site_theme").update(jsonOf(
            "primary_color" to (pub.str("primary_color") ?: theme.str("primary_color")), "font_family" to (pub.str("font_family") ?: theme.str("font_family")),
            "published" to pub, "draft" to pub, "state" to "published",
        )) { filter { eq("id", 1); eq("church_id", cid) } }
    }

    /** Account-level "Default Theme" preview preference (user_preferences, own-user RLS). */
    suspend fun saveWebsiteThemePreference(default: Boolean) = action {
        val uid = userId() ?: error("Not signed in")
        val existing = table("user_preferences").select(raw("preferences")) { filter { eq("user_id", uid) } }.decodeList<JsonObject>().firstOrNull()
        val prefs = LinkedHashMap<String, JsonElement>(existing?.obj("preferences") ?: JsonObject(emptyMap()))
        if (default) prefs["website_theme"] = JsonPrimitive("default") else prefs.remove("website_theme")
        val now = java.time.Instant.now().toString()
        if (existing != null) table("user_preferences").update(jsonOf("preferences" to JsonObject(prefs), "updated_at" to now)) { filter { eq("user_id", uid) } }
        else table("user_preferences").insert(jsonOf("user_id" to uid, "preferences" to JsonObject(prefs), "updated_at" to now))
    }

    /* ---------------- sections ---------------- */
    private fun isSafeUrl(v: String?): Boolean {
        val u = v?.trim().orEmpty()
        if (u.isEmpty()) return true
        if (u.startsWith("/") && !u.startsWith("//")) return true
        return u.startsWith("http://") || u.startsWith("https://")
    }

    /** Server-boundary validation port (types, 256 KB cap, safe URLs, event/group resource links). */
    private suspend fun validate(type: String, content: JsonObject): String? {
        if (type !in SECTION_TYPES) return "Tipe section tidak valid"
        if (content.toString().length > 262144) return "Konten section terlalu besar (maksimal 256 KB)"
        val media = (content["media_items"] as? JsonArray)?.mapNotNull { it as? JsonObject } ?: emptyList()
        val cards = (content["cards"] as? JsonArray)?.mapNotNull { it as? JsonObject } ?: emptyList()
        val urls = listOf(content.str("bg_image"), content.str("media_url"), content.str("maps_embed")) + media.map { it.str("url") } + cards.map { it.str("image_url") }
        if (urls.any { !isSafeUrl(it) }) return "URL media harus menggunakan HTTP/HTTPS atau path lokal yang valid."
        val eventIds = mutableSetOf<Long>(); val groupIds = mutableSetOf<Long>()
        for (m in media) {
            val rt = m.str("resource_type")
            if (rt.isNullOrEmpty()) { if (m.has("resource_id") && m.str("resource_id").orEmpty().isNotEmpty()) return "Jenis resource media belum dipilih."; continue }
            val rid = m.long("resource_id") ?: 0L
            if ((rt != "event" && rt != "group") || rid <= 0) return "Resource media tidak valid."
            (if (rt == "event") eventIds else groupIds).add(rid)
        }
        if (eventIds.isNotEmpty() && type != "event_feed") return "Media event hanya dapat ditautkan pada section Event."
        if (groupIds.isNotEmpty() && type != "group_feed") return "Media komunitas hanya dapat ditautkan pada section Komunitas."
        val cid = churchId() ?: ""
        if (eventIds.isNotEmpty()) {
            val n = table("events").select(raw("id")) { filter { eq("church_id", cid); isIn("id", eventIds.toList()) } }.decodeList<JsonObject>().size
            if (n != eventIds.size) return "Event media tidak ditemukan pada gereja ini."
        }
        if (groupIds.isNotEmpty()) {
            val n = table("groups").select(raw("id")) { filter { eq("church_id", cid); isIn("id", groupIds.toList()) } }.decodeList<JsonObject>().size
            if (n != groupIds.size) return "Komunitas media tidak ditemukan pada gereja ini."
        }
        return null
    }

    suspend fun saveSectionDraft(i: SectionDraft): IdResult = try {
        val page = i.page.trim().lowercase()
        if (!PAGE_RE.matches(page)) IdResult(false, "Slug halaman tidak valid")
        else validate(i.type, i.content)?.let { IdResult(false, it) } ?: run {
            val cid = churchId() ?: ""
            val sort = i.sortOrder.coerceIn(0, 10000)
            val payload = mapOf(
                "draft_page" to JsonPrimitive(page), "draft_type" to JsonPrimitive(i.type), "draft_sort_order" to JsonPrimitive(sort),
                "content" to i.content, "draft_enabled" to JsonPrimitive(i.enabled), "draft_deleted" to JsonPrimitive(false),
                "updated_at" to JsonPrimitive(java.time.Instant.now().toString()),
            )
            if (i.id != null) {
                table("site_sections").update(JsonObject(payload)) { filter { eq("id", i.id); eq("church_id", cid) } }
                IdResult(true, id = i.id)
            } else {
                val row = table("site_sections").insert(JsonObject(payload + mapOf(
                    "page" to JsonPrimitive(page), "type" to JsonPrimitive(i.type), "sort_order" to JsonPrimitive(sort),
                    "enabled" to JsonPrimitive(false), "church_id" to JsonPrimitive(cid),
                ))) { select() }.decodeList<JsonObject>().firstOrNull()
                if (row == null) IdResult(false, "Section baru gagal dibuat.") else IdResult(true, id = row.long("id"))
            }
        }
    } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Throwable) { IdResult(false, e.message) }

    suspend fun saveSectionContent(s: WmSection, content: JsonObject): ActionResult {
        validate(s.type, content)?.let { return ActionResult.fail(it) }
        return action {
            table("site_sections").update(jsonOf("content" to content, "updated_at" to java.time.Instant.now().toString())) {
                filter { eq("id", s.id); eq("church_id", churchId() ?: "") }
            }
        }
    }

    suspend fun reorderSections(ids: List<Long>): ActionResult {
        if (ids.isEmpty() || ids.size > 200 || ids.toSet().size != ids.size) return ActionResult.fail("Daftar urutan tidak valid")
        return action {
            val cid = churchId() ?: ""
            ids.forEachIndexed { idx, id ->
                table("site_sections").update(jsonOf("draft_sort_order" to (idx + 1) * 10, "updated_at" to java.time.Instant.now().toString())) { filter { eq("id", id); eq("church_id", cid) } }
            }
        }
    }

    suspend fun setSectionEnabled(id: Long, enabled: Boolean) = action {
        table("site_sections").update(jsonOf("draft_enabled" to enabled, "updated_at" to java.time.Instant.now().toString())) { filter { eq("id", id); eq("church_id", churchId() ?: "") } }
    }

    suspend fun deleteSection(s: WmSection) = action {
        val cid = churchId() ?: ""
        if (s.published) table("site_sections").update(jsonOf("draft_deleted" to true)) { filter { eq("id", s.id); eq("church_id", cid) } }
        else table("site_sections").delete { filter { eq("id", s.id); eq("church_id", cid) } }
    }

    suspend fun publishSections(page: String?) = action {
        if (page != null && !PAGE_RE.matches(page.trim().lowercase())) error("Page tidak valid")
        client.postgrest.rpc("publish_site_sections", jsonOf("p_page" to page?.trim()?.lowercase()))
    }

    suspend fun publishWebManager() = action { client.postgrest.rpc("publish_web_manager") }
}
