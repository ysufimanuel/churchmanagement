package com.growthaxys.churchmanagements.feature.webmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.BuildConfig
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.core.tr
import com.growthaxys.churchmanagements.data.jsonOf
import com.growthaxys.churchmanagements.data.repo.SectionDraft
import com.growthaxys.churchmanagements.data.repo.ThemeValues
import com.growthaxys.churchmanagements.data.repo.WmData
import com.growthaxys.churchmanagements.data.repo.WmSection
import com.growthaxys.churchmanagements.data.repo.bool
import com.growthaxys.churchmanagements.data.repo.int
import com.growthaxys.churchmanagements.data.repo.long
import com.growthaxys.churchmanagements.data.repo.obj
import com.growthaxys.churchmanagements.data.repo.str
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/* ------------------------------------------------------------------ meta (web visual-editor-config.ts) */

private data class SectionMeta(val label: String, val hint: String, val defaults: () -> JsonObject)

private fun newId(prefix: String) = "$prefix-${System.currentTimeMillis().toString(36)}-${(1000..9999).random()}"

private val SECTION_ORDER = listOf("hero", "card_grid", "event_feed", "group_feed", "info_lokasi", "media", "map", "richtext")

private val SECTION_META: Map<String, SectionMeta> = mapOf(
    "hero" to SectionMeta("Banner Utama", "Bagian paling atas: judul besar, kalimat sambutan, tombol, dan foto latar.") {
        jsonOf("title" to "Selamat Datang", "subtitle" to "Tulis kalimat sambutan singkat di sini.", "cta_label" to "Kenali Kami", "cta_href" to "/tentang", "bg_image" to "", "title_font_size" to 40)
    },
    "card_grid" to SectionMeta("Kartu Informasi", "Beberapa kartu berisi foto, judul, dan penjelasan. Cocok untuk pelayanan atau program.") {
        jsonOf("title" to "Pelayanan Kami", "subtitle" to "", "grid_columns" to 3, "cards" to JsonArray((1..3).map { n ->
            jsonOf("id" to newId("card"), "title" to "Kartu $n", "body" to "Tulis penjelasan singkat di sini.", "image_url" to "", "href" to "")
        }))
    },
    "event_feed" to SectionMeta("Acara Mendatang", "Otomatis menampilkan acara yang sudah kamu buat di menu Event. Tidak perlu diisi ulang.") {
        jsonOf("title" to "Acara Mendatang", "limit" to 3)
    },
    "group_feed" to SectionMeta("Komunitas", "Otomatis menampilkan daftar komunitas/kelompok dari menu Groups.") {
        jsonOf("title" to "Komunitas & Kelompok", "subtitle" to "", "limit" to 6)
    },
    "info_lokasi" to SectionMeta("Alamat & Jam Ibadah", "Alamat gereja, jadwal ibadah, dan peta kecil.") {
        jsonOf("title" to "Info & Lokasi", "address" to "", "hours" to "", "lat" to null, "lng" to null)
    },
    "media" to SectionMeta("Galeri Foto / Video", "Tampilkan satu atau beberapa foto atau video.") {
        jsonOf("title" to "Galeri", "media_items" to JsonArray(emptyList()), "media_layout" to "stack")
    },
    "map" to SectionMeta("Peta Lokasi", "Peta besar dengan titik lokasi gereja.") {
        jsonOf("title" to "Lokasi Kami", "lat" to null, "lng" to null, "maps_embed" to "", "ratio" to "1:1")
    },
    "richtext" to SectionMeta("Teks", "Judul dan paragraf bebas. Cocok untuk sejarah, visi misi, atau pengumuman.") {
        jsonOf("title" to "Judul", "body" to "Tulis isi di sini.")
    },
)

private fun metaOf(type: String) = SECTION_META[type] ?: SectionMeta(type, "") { JsonObject(emptyMap()) }

private val PAGE_LABELS = mapOf(
    "home" to "Beranda", "new" to "Pengunjung Baru", "connect" to "Komunitas", "grow" to "Bertumbuh", "ministries" to "Pelayanan",
    "events" to "Acara", "worship" to "Ibadah", "tentang" to "Tentang Kami", "kontak" to "Kontak",
)

private fun pageLabel(slug: String) = PAGE_LABELS[slug] ?: (slug.replaceFirstChar { it.uppercase() }.replace('-', ' ').replace('_', ' '))

private val FONTS = listOf("Plus Jakarta Sans", "Poppins", "Inter", "Roboto", "Montserrat")
private val RATIOS = listOf("19:9", "16:9", "1:1", "4:3", "4:5", "2:3", "9:16", "2:9", "32:9")
private val POSITIONS = listOf("center", "top", "bottom", "left", "right")

private val COLOR_KEYS = listOf(
    "background_color", "overlay_color", "title_color", "subtitle_color", "body_color", "text_color", "accent_color",
    "cta_color", "cta_text_color", "card_background_color", "card_title_color", "card_text_color", "card_border_color", "date_color",
)
private val SIZE_KEYS = listOf("title_font_size", "subtitle_font_size", "body_font_size", "cta_font_size")

private fun sectionSummary(type: String, c: JsonObject): String {
    val text = listOf("title", "address", "body", "subtitle").firstNotNullOfOrNull { c.str(it)?.trim()?.takeIf { s -> s.isNotEmpty() } } ?: ""
    return text.take(80)
}

private fun String.parseHex(): Color? = runCatching {
    val h = trim().removePrefix("#")
    when (h.length) {
        3 -> Color(android.graphics.Color.parseColor("#" + h.map { "$it$it" }.joinToString("")))
        6, 8 -> Color(android.graphics.Color.parseColor("#$h"))
        else -> null
    }
}.getOrNull()

private fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255); val g = (green * 255).toInt().coerceIn(0, 255); val b = (blue * 255).toInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}

/* ------------------------------------------------------------------ screen */

@Composable
fun WebManagerScreen(role: Role, padding: PaddingValues, vm: WebManagerViewModel = hiltViewModel()) {
    LoadGate(vm, padding, SkeletonKind.Tabs) { data ->
        if (!Rbac.canUseWebManager(role)) { EmptyText(t("view-only")); return@LoadGate }
        WebManagerContent(role, padding, data, vm)
    }
}

@Composable
private fun WebManagerContent(role: Role, padding: PaddingValues, data: WmData, vm: WebManagerViewModel) {
    val ctx = LocalContext.current
    val busy by vm.busy.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf("visual") }
    var page by remember { mutableStateOf("home") }
    var confirmPublishAll by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<String?>(null) }

    val pages = (PAGE_LABELS.keys + data.sections.filter { !it.deleted }.map { it.page }).filter { it.isNotBlank() }.distinct()
    val publicBase = data.slug?.takeIf { it.isNotBlank() }?.let { "${BuildConfig.WEB_BASE_URL}/site/$it" }
    fun pageUrl(p: String) = publicBase?.let { it + if (p == "home") "" else "/$p" }
    fun openUrl(u: String) = runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

    ModulePage(padding) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) { PageHeader(t("web-manager"), tr("Kelola header, tema, halaman, draft, dan preview website publik.")) }
                AppButton(t("wm-publish"), { confirmPublishAll = true }, icon = Icons.Outlined.RocketLaunch, loading = busy)
            }
        }
        item {
            AppCard {
                CardHeaderRow {
                    Column(Modifier.weight(1f)) {
                        Text(tr("Website Publik Gereja"), fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 16.sp)
                        Text(publicBase ?: tr("Slug gereja belum diatur."), fontSize = 12.sp, color = Tokens.MutedForeground)
                    }
                }
                CardBody {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (publicBase != null) {
                            AppButton(tr("Buka website"), { openUrl(publicBase) }, variant = BtnVariant.Secondary, icon = Icons.Outlined.OpenInNew)
                            AppButton(tr("Pratinjau"), { preview = pageUrl("home") }, variant = BtnVariant.Outline, icon = Icons.Outlined.Visibility)
                        }
                    }
                }
            }
        }
        item {
            AppTabs(
                listOf(TabItem("visual", tr("Editor Visual")), TabItem("nav", tr("Header & Navigasi")), TabItem("theme", tr("Tema")), TabItem("adv", tr("Section Lanjutan"))),
                tab, { tab = it },
            )
        }
        when (tab) {
            "visual" -> visualTab(role, data, vm, page, pages, { page = it }, ::pageUrl, { preview = it }, ::openUrl)
            "nav" -> navTab(role, data, vm)
            "theme" -> themeTab(data, vm)
            else -> advTab(role, data, vm, pages)
        }
    }

    if (confirmPublishAll) AppDialog(t("wm-publish"), { confirmPublishAll = false }) {
        Text(tr("Semua perubahan Draft (header, tema, dan halaman) akan dipublikasikan ke website publik."), color = Tokens.MutedForeground, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            AppButton(t("cancel"), { confirmPublishAll = false }, variant = BtnVariant.Secondary)
            AppButton(t("wm-publish"), { confirmPublishAll = false; vm.publishAll() }, icon = Icons.Outlined.RocketLaunch)
        }
    }
    preview?.let { u -> WebPreviewDialog(u, onOpen = { openUrl(it) }) { preview = null } }
}

/* ------------------------------------------------------------------ Visual editor tab */

private fun androidx.compose.foundation.lazy.LazyListScope.visualTab(
    role: Role, data: WmData, vm: WebManagerViewModel, page: String, pages: List<String>, setPage: (String) -> Unit,
    pageUrl: (String) -> String?, preview: (String?) -> Unit, openUrl: (String) -> Unit,
) {
    val list = data.sections.filter { !it.deleted && it.page == page }.sortedBy { it.sort }
    item {
        VisualHeader(role, data, vm, page, pages, setPage, list, pageUrl, preview, openUrl)
    }
    if (list.isEmpty()) item { EmptyText(tr("Belum ada bagian di halaman ini. Tekan Tambah untuk membuat.")) }
    items(list, key = { it.id }) { s ->
        SectionRow(role, vm, s, list)
    }
}

@Composable
private fun VisualHeader(
    role: Role, data: WmData, vm: WebManagerViewModel, page: String, pages: List<String>, setPage: (String) -> Unit,
    list: List<WmSection>, pageUrl: (String) -> String?, preview: (String?) -> Unit, openUrl: (String) -> Unit,
) {
    var adding by remember { mutableStateOf(false) }
    var newType by remember { mutableStateOf<String?>(null) }
    var confirm by remember { mutableStateOf(false) }
    val unpublished = list.count { it.hasUnpublished() } + data.sections.count { it.deleted && it.page == page && it.hasUnpublished() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LabeledField(tr("Halaman")) { AppSelect(page, pages.map { Option(it, pageLabel(it)) }, setPage) }
        FilterRow {
            AppButton(tr("Tambah"), { adding = true }, icon = Icons.Outlined.Add)
            AppButton("${tr("Publish Halaman")}${if (unpublished > 0) " ($unpublished)" else ""}", { confirm = true }, variant = BtnVariant.Secondary, icon = Icons.Outlined.RocketLaunch)
            pageUrl(page)?.let { u ->
                AppButton(tr("Pratinjau"), { preview(u) }, variant = BtnVariant.Outline, icon = Icons.Outlined.Visibility)
                val dt = if (data.defaultTheme) "default" else "published"
                AppButton(tr("Preview Draft"), { openUrl("$u?preview=draft&theme=$dt") }, variant = BtnVariant.Outline, icon = Icons.Outlined.OpenInNew)
            }
        }
        Text(tr("Pratinjau menampilkan versi yang sudah tayang. Preview Draft dibuka di browser dan membutuhkan login web."), fontSize = 12.sp, color = Tokens.MutedForeground)
    }
    if (adding) AppDialog(tr("Tambah Bagian"), { adding = false }) {
        SECTION_ORDER.forEach { type ->
            val m = metaOf(type)
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tr(m.label), color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(tr(m.hint), fontSize = 12.sp, color = Tokens.MutedForeground)
                    AppButton(tr("Tambah"), { adding = false; newType = type }, Modifier.fillMaxWidth(), variant = BtnVariant.Secondary, icon = Icons.Outlined.Add)
                }
            }
        }
    }
    newType?.let { type ->
        SectionEditor(null, type, page, ((list.maxOfOrNull { it.sort } ?: 0) + 10), data, vm, onClose = { newType = null })
    }
    if (confirm) AppDialog(tr("Publish Halaman"), { confirm = false }) {
        Text(if (unpublished > 0) "$unpublished ${tr("bagian punya perubahan Draft yang belum dipublikasikan.")}" else tr("Tidak ada perubahan Draft pada halaman ini."),
            color = Tokens.MutedForeground, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            AppButton(t("cancel"), { confirm = false }, variant = BtnVariant.Secondary)
            AppButton(tr("Publish Halaman"), { confirm = false; vm.publishPage(page) })
        }
    }
}

@Composable
private fun SectionRow(role: Role, vm: WebManagerViewModel, s: WmSection, list: List<WmSection>) {
    var editing by remember { mutableStateOf(false) }
    var confirmDel by remember { mutableStateOf(false) }
    val data = (vm.state.collectAsStateWithLifecycle().value as? Load.Ready)?.data
    val idx = list.indexOf(s)
    fun move(d: Int) {
        val j = idx + d
        if (j !in list.indices) return
        val ids = list.map { it.id }.toMutableList()
        ids[idx] = ids[j].also { ids[j] = ids[idx] }
        vm.reorder(ids)
    }
    AppCard {
        CardHeaderRow {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tr(metaOf(s.type).label), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                val sum = sectionSummary(s.type, s.content)
                if (sum.isNotEmpty()) Text(sum, fontSize = 12.sp, color = Tokens.MutedForeground, maxLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (s.hasUnpublished()) AppBadge(tr("Draft belum publish"), BadgeVariant.Warning)
                    else AppBadge(tr("Published"), BadgeVariant.Success)
                    if (!s.enabled) AppBadge(tr("Nonaktif"), BadgeVariant.Secondary)
                }
            }
            AppSwitch(s.enabled) { vm.setEnabled(s.id, it) }
        }
        CardBody {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                AppIconButton(Icons.Outlined.ArrowUpward, tr("Naik"), { move(-1) })
                AppIconButton(Icons.Outlined.ArrowDownward, tr("Turun"), { move(1) })
                AppIconButton(Icons.Outlined.ContentCopy, tr("Duplikat"), {
                    vm.saveSection(SectionDraft(null, s.page, s.type, s.sort + 5, s.content, s.enabled)) { nid ->
                        if (nid != null) {
                            val ids = list.map { it.id }.toMutableList()
                            ids.add(idx + 1, nid)
                            vm.reorder(ids)
                        }
                    }
                })
                AppIconButton(Icons.Outlined.Edit, t("act-edit"), { editing = true })
                if (Rbac.canDelete(role)) AppIconButton(Icons.Outlined.Delete, t("act-delete"), { confirmDel = true }, tint = Tokens.Red400)
            }
        }
    }
    if (editing && data != null) SectionEditor(s, s.type, s.page, s.sort, data, vm, onClose = { editing = false })
    if (confirmDel) ConfirmDialog({ confirmDel = false; vm.deleteSection(s) }, { confirmDel = false })
}

/* ------------------------------------------------------------------ Navigation tab */

private fun androidx.compose.foundation.lazy.LazyListScope.navTab(role: Role, data: WmData, vm: WebManagerViewModel) {
    item { NavHeader(data, vm) }
    items(data.nav, key = { it.long("id") ?: it.hashCode().toLong() }) { row -> NavRow(role, row, vm) }
    if (data.nav.isEmpty()) item { EmptyText() }
}

private fun JsonObject.eff(k: String): String? = (obj("draft") ?: this).str(k) ?: str(k)

@Composable
private fun NavHeader(data: WmData, vm: WebManagerViewModel) {
    var editing by remember { mutableStateOf(false) }
    FilterRow {
        AppButton(tr("Menu Baru"), { editing = true }, icon = Icons.Outlined.Add)
        AppButton(tr("Publish Navigasi"), { vm.publishNav() }, variant = BtnVariant.Secondary, icon = Icons.Outlined.RocketLaunch)
    }
    if (editing) NavEditor(null, ((data.nav.maxOfOrNull { it.int("sort_order") ?: 0 } ?: 0) + 10), vm) { editing = false }
}

@Composable
private fun NavRow(role: Role, row: JsonObject, vm: WebManagerViewModel) {
    var editing by remember { mutableStateOf(false) }
    var confirmDel by remember { mutableStateOf(false) }
    val draft = row.obj("draft")
    val visible = draft?.bool("visible") ?: row.bool("visible") ?: true
    val isDraft = row.str("state") == "draft"
    AppCard {
        CardHeaderRow {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(row.eff("label").orEmpty(), color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(row.eff("href").orEmpty(), fontSize = 12.sp, color = Tokens.MutedForeground)
                AppBadge(if (isDraft) tr("Draft") else tr("Published"), if (isDraft) BadgeVariant.Warning else BadgeVariant.Success)
            }
            AppSwitch(visible) { vm.toggleNav(row, it) }
        }
        CardBody {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                AppIconButton(Icons.Outlined.Edit, t("act-edit"), { editing = true })
                if (Rbac.canDelete(role)) AppIconButton(Icons.Outlined.Delete, t("act-delete"), { confirmDel = true }, tint = Tokens.Red400)
            }
        }
    }
    if (editing) NavEditor(row, row.eff("sort_order")?.toIntOrNull() ?: (row.int("sort_order") ?: 0), vm) { editing = false }
    if (confirmDel) ConfirmDialog({ confirmDel = false; vm.deleteNav(row) }, { confirmDel = false })
}

@Composable
private fun NavEditor(row: JsonObject?, order: Int, vm: WebManagerViewModel, onClose: () -> Unit) {
    var label by remember { mutableStateOf(row?.eff("label").orEmpty()) }
    var href by remember { mutableStateOf(row?.eff("href").orEmpty()) }
    var visible by remember { mutableStateOf((row?.obj("draft")?.bool("visible") ?: row?.bool("visible")) ?: true) }
    var ord by remember { mutableStateOf(order.toString()) }
    AppDialog(if (row == null) tr("Menu Baru") else tr("Ubah Menu"), onClose) {
        LabeledField(tr("Label")) { AppTextField(label, { label = it }) }
        LabeledField(tr("Tautan")) { AppTextField(href, { href = it }, placeholder = "/tentang") }
        LabeledField(tr("Urutan")) { AppTextField(ord, { ord = it }, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppSwitch(visible) { visible = it }; Text(tr("Tampilkan"), color = Color.White, fontSize = 14.sp)
        }
        DialogActions(onClose, { vm.saveNav(row?.long("id"), label.trim(), href.trim(), visible, ord.toIntOrNull() ?: 0) { onClose() } })
    }
}

/* ------------------------------------------------------------------ Theme tab */

private fun androidx.compose.foundation.lazy.LazyListScope.themeTab(data: WmData, vm: WebManagerViewModel) {
    item { ThemeCard(data, vm) }
}

@Composable
private fun ThemeCard(data: WmData, vm: WebManagerViewModel) {
    val th = data.theme
    val draft = th?.obj("draft")
    fun cur(k: String, def: String) = draft?.str(k) ?: th?.str(k) ?: def
    var primary by remember(th) { mutableStateOf(cur("primary_color", "#0F1117")) }
    var secondary by remember(th) { mutableStateOf(cur("secondary_color", "#F8FAFC")) }
    var accent by remember(th) { mutableStateOf(cur("accent_color", "#00E5FF")) }
    var font by remember(th) { mutableStateOf(cur("font_family", FONTS.first())) }
    var confirmRestore by remember { mutableStateOf(false) }
    AppCard {
        CardHeaderRow { Text(tr("Tampilan & Tema"), fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 16.sp) }
        CardBody {
            Text(tr("Palette 60 / 30 / 10: Primary, Secondary, dan Accent adalah palette global. Rasionya panduan visual, bukan pembatas."), fontSize = 12.sp, color = Tokens.MutedForeground)
            ColorField("Primary · 60%", primary) { primary = it }
            ColorField("Secondary · 30%", secondary) { secondary = it }
            ColorField("Accent · 10%", accent) { accent = it }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Tokens.BorderSubtle, RoundedCornerShape(12.dp))) {
                Box(Modifier.weight(3f).height(72.dp).background(primary.parseHex() ?: Color.Black).padding(10.dp)) { Text("Primary", color = secondary.parseHex() ?: Color.White, fontSize = 12.sp) }
                Box(Modifier.weight(1.5f).height(72.dp).background(secondary.parseHex() ?: Color.White).padding(10.dp)) { Text("Secondary", color = primary.parseHex() ?: Color.Black, fontSize = 12.sp) }
                Box(Modifier.weight(0.5f).height(72.dp).background(accent.parseHex() ?: Color.Cyan))
            }
            LabeledField(tr("Font")) { AppSelect(font, FONTS.map { Option(it, it) }, { font = it }) }
            FilterRow {
                AppButton(t("wm-draft"), { vm.saveTheme(ThemeValues(primary, secondary, accent, font)) }, variant = BtnVariant.Secondary)
                AppButton(tr("Publish Tema"), { vm.publishTheme() }, icon = Icons.Outlined.RocketLaunch)
                AppButton(tr("Pulihkan Default"), { confirmRestore = true }, variant = BtnVariant.Outline)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppSwitch(data.defaultTheme) { vm.setDefaultThemePref(it) }
                Column {
                    Text(tr("Default Theme"), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(tr("Saat aktif, preview untuk akun ini memakai Default Theme tanpa mengganti konten, halaman, atau media."), fontSize = 12.sp, color = Tokens.MutedForeground)
                }
            }
        }
    }
    if (confirmRestore) AppDialog(tr("Pulihkan Default"), { confirmRestore = false }) {
        Text(tr("Draft tema akan dikembalikan ke Default Theme."), color = Tokens.MutedForeground, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            AppButton(t("cancel"), { confirmRestore = false }, variant = BtnVariant.Secondary)
            AppButton(tr("Pulihkan Default"), { confirmRestore = false; vm.restoreDefaultTheme { } })
        }
    }
}

/** Swatch + hex field + HSV sliders (web ColorField). */
@Composable
private fun ColorField(label: String, value: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val color = value.parseHex() ?: Color.Black
    val hsv = remember(value) { FloatArray(3).also { android.graphics.Color.colorToHSV(android.graphics.Color.rgb((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt()), it) } }
    fun emit(h: Float, s: Float, v: Float) = onChange(Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))).toHex())
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color).border(1.dp, Tokens.InputBorder, RoundedCornerShape(10.dp))
                .clickable { open = !open })
            AppTextField(value, onChange, Modifier.weight(1f), placeholder = "#RRGGBB")
        }
        if (open) {
            Slider(hsv[0], { emit(it, hsv[1], hsv[2]) }, valueRange = 0f..360f)
            Slider(hsv[1], { emit(hsv[0], it, hsv[2]) }, valueRange = 0f..1f)
            Slider(hsv[2], { emit(hsv[0], hsv[1], it) }, valueRange = 0f..1f)
        }
    }
}

/* ------------------------------------------------------------------ Advanced tab: all sections of all pages */

private fun androidx.compose.foundation.lazy.LazyListScope.advTab(role: Role, data: WmData, vm: WebManagerViewModel, pages: List<String>) {
    item { AdvHeader(data, vm) }
    val all = data.sections.filter { !it.deleted }.sortedWith(compareBy({ it.page }, { it.sort }))
    if (all.isEmpty()) item { EmptyText() }
    items(all, key = { it.id }) { s ->
        val list = all.filter { it.page == s.page }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(pageLabel(s.page), fontSize = 11.sp, color = Tokens.MutedForeground)
            SectionRow(role, vm, s, list)
        }
    }
}

@Composable
private fun AdvHeader(data: WmData, vm: WebManagerViewModel) {
    var page by remember { mutableStateOf("home") }
    var type by remember { mutableStateOf("richtext") }
    var go by remember { mutableStateOf(false) }
    val pages = (PAGE_LABELS.keys + data.sections.map { it.page }).filter { it.isNotBlank() }.distinct()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledField(tr("Halaman")) { AppSelect(page, pages.map { Option(it, pageLabel(it)) }, { page = it }) }
        LabeledField(tr("Tipe")) { AppSelect(type, SECTION_ORDER.map { Option(it, tr(metaOf(it).label)) }, { type = it }) }
        AppButton(tr("Tambah"), { go = true }, icon = Icons.Outlined.Add)
    }
    if (go) {
        val max = data.sections.filter { it.page == page }.maxOfOrNull { it.sort } ?: 0
        SectionEditor(null, type, page, max + 10, data, vm, onClose = { go = false })
    }
}

/* ------------------------------------------------------------------ Section editor (full form) */

@Composable
private fun SectionEditor(section: WmSection?, initialType: String, initialPage: String, initialOrder: Int, data: WmData, vm: WebManagerViewModel, onClose: () -> Unit) {
    val base = remember { section?.content ?: metaOf(initialType).defaults() }
    var type by remember { mutableStateOf(initialType) }
    var page by remember { mutableStateOf(initialPage) }
    var order by remember { mutableStateOf(initialOrder.toString()) }
    var enabled by remember { mutableStateOf(section?.enabled ?: true) }
    val fields = remember {
        mutableStateMapOf<String, String>().also { m ->
            listOf("title", "subtitle", "body", "cta_label", "cta_href", "bg_image", "anchor_id", "address", "hours", "maps_embed", "ratio",
                "media_layout", "grid_columns", "limit", "lat", "lng").plus(COLOR_KEYS).plus(SIZE_KEYS).forEach { k ->
                m[k] = (base[k] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content.orEmpty()
            }
        }
    }
    val cards = remember { mutableStateListOf<Map<String, String>>().also { l ->
        (base["cards"] as? JsonArray)?.forEach { e -> (e as? JsonObject)?.let { o -> l.add(listOf("id", "title", "body", "image_url", "href").associateWith { o.str(it).orEmpty() }) } }
    } }
    val media = remember { mutableStateListOf<Map<String, String>>().also { l ->
        (base["media_items"] as? JsonArray)?.forEach { e -> (e as? JsonObject)?.let { o ->
            l.add(MEDIA_KEYS.associateWith { k -> (o[k] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content.orEmpty() })
        } }
    } }
    var showStyle by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    fun f(k: String) = fields[k].orEmpty()
    fun set(k: String, v: String) { fields[k] = v }
    val has = { k: String -> when (type) {
        "hero" -> k in setOf("title", "subtitle", "cta_label", "cta_href", "bg_image")
        "card_grid" -> k in setOf("title", "subtitle", "grid_columns")
        "event_feed" -> k in setOf("title", "limit")
        "group_feed" -> k in setOf("title", "subtitle", "limit")
        "info_lokasi" -> k in setOf("title", "address", "hours", "lat", "lng")
        "media" -> k in setOf("title", "media_layout")
        "map" -> k in setOf("title", "lat", "lng", "maps_embed", "ratio")
        "richtext" -> k in setOf("title", "body")
        else -> k == "title"
    } }

    fun build(): JsonObject {
        val out = LinkedHashMap<String, JsonElement>(base)
        fields.forEach { (k, v) ->
            val numeric = k in SIZE_KEYS || k in setOf("grid_columns", "limit", "lat", "lng")
            when {
                numeric -> {
                    val n = v.trim().toDoubleOrNull()
                    if (n == null) { if (k == "lat" || k == "lng") out[k] = JsonNull else out.remove(k) }
                    else out[k] = if (n % 1.0 == 0.0 && k != "lat" && k != "lng") JsonPrimitive(n.toLong()) else JsonPrimitive(n)
                }
                v.isBlank() && k in COLOR_KEYS || v.isBlank() && k == "anchor_id" -> out.remove(k)
                else -> out[k] = JsonPrimitive(v)
            }
        }
        if (type == "card_grid") out["cards"] = JsonArray(cards.map { c -> JsonObject(c.mapValues { JsonPrimitive(it.value) }) })
        if (type == "media") out["media_items"] = JsonArray(media.map { m ->
            val o = LinkedHashMap<String, JsonElement>()
            m.forEach { (k, v) -> when (k) {
                "autoplay", "muted" -> o[k] = JsonPrimitive(v == "true")
                "resource_id" -> v.toLongOrNull()?.let { o[k] = JsonPrimitive(it) }
                else -> if (v.isNotBlank()) o[k] = JsonPrimitive(v)
            } }
            JsonObject(o)
        })
        return JsonObject(out)
    }

    AppDialog(if (section == null) tr("Tambah Bagian") else tr("Ubah Bagian"), onClose, wide = true) {
        Text(tr(metaOf(type).hint), fontSize = 12.sp, color = Tokens.MutedForeground)
        LabeledField(tr("Tipe")) { AppSelect(type, SECTION_ORDER.map { Option(it, tr(metaOf(it).label)) }, { type = it }) }
        LabeledField(tr("Halaman")) { AppTextField(page, { page = it.lowercase() }) }
        LabeledField(tr("Urutan")) { AppTextField(order, { order = it }, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) }
        LabeledField(tr("ID Anchor")) { AppTextField(f("anchor_id"), { set("anchor_id", it) }, placeholder = "contoh: pelayanan") }

        if (has("title")) LabeledField(tr("Judul")) { AppTextField(f("title"), { set("title", it) }) }
        if (has("subtitle")) LabeledField(tr("Sub-judul")) { AppTextField(f("subtitle"), { set("subtitle", it) }, singleLine = false, minHeight = 72.dp) }
        if (has("body")) LabeledField(tr("Isi")) { AppTextField(f("body"), { set("body", it) }, singleLine = false, minHeight = 120.dp) }
        if (has("cta_label")) LabeledField(tr("Teks Tombol")) { AppTextField(f("cta_label"), { set("cta_label", it) }) }
        if (has("cta_href")) LabeledField(tr("Tautan Tombol")) { AppTextField(f("cta_href"), { set("cta_href", it) }, placeholder = "/tentang") }
        if (has("bg_image")) LabeledField(tr("Gambar Latar (URL)")) { AppTextField(f("bg_image"), { set("bg_image", it) }, placeholder = "https://") }
        if (has("grid_columns")) LabeledField(tr("Jumlah Kolom")) { AppSelect(f("grid_columns").ifBlank { "3" }, listOf("1", "2", "3", "4").map { Option(it, it) }, { set("grid_columns", it) }) }
        if (has("limit")) LabeledField(tr("Batas Tampil")) { AppTextField(f("limit"), { set("limit", it) }, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) }
        if (has("address")) LabeledField(tr("Alamat")) { AppTextField(f("address"), { set("address", it) }, singleLine = false, minHeight = 72.dp) }
        if (has("hours")) LabeledField(tr("Jam Ibadah")) { AppTextField(f("hours"), { set("hours", it) }, singleLine = false, minHeight = 72.dp) }
        if (has("lat")) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledField(tr("Lintang"), Modifier.weight(1f)) { AppTextField(f("lat"), { set("lat", it) }, keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal) }
            LabeledField(tr("Bujur"), Modifier.weight(1f)) { AppTextField(f("lng"), { set("lng", it) }, keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal) }
        }
        if (has("maps_embed")) LabeledField(tr("Embed Google Maps (URL)")) { AppTextField(f("maps_embed"), { set("maps_embed", it) }, placeholder = "https://") }
        if (has("ratio")) LabeledField(tr("Rasio")) { AppSelect(f("ratio").ifBlank { "1:1" }, RATIOS.map { Option(it, it) }, { set("ratio", it) }) }
        if (has("media_layout")) LabeledField(tr("Tata Letak Media")) {
            AppSelect(f("media_layout").ifBlank { "stack" }, listOf("stack", "grid", "carousel").map { Option(it, it) }, { set("media_layout", it) })
        }

        if (type == "card_grid") {
            FieldLabel(tr("Kartu"))
            cards.forEachIndexed { i, c ->
                AppCard {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("title" to "Judul", "body" to "Isi", "image_url" to "Gambar (URL)", "href" to "Tautan").forEach { (k, lbl) ->
                            LabeledField(tr(lbl)) { AppTextField(c[k].orEmpty(), { v -> cards[i] = c + (k to v) }, singleLine = k != "body", minHeight = if (k == "body") 72.dp else 40.dp) }
                        }
                        AppButton(t("act-delete"), { cards.removeAt(i) }, variant = BtnVariant.Destructive, icon = Icons.Outlined.Delete)
                    }
                }
            }
            AppButton(tr("Tambah Kartu"), { cards.add(mapOf("id" to newId("card"), "title" to "", "body" to "", "image_url" to "", "href" to "")) }, variant = BtnVariant.Secondary, icon = Icons.Outlined.Add)
        }

        if (type == "media") {
            FieldLabel(tr("Item Media"))
            media.forEachIndexed { i, m ->
                fun mv(k: String) = m[k].orEmpty()
                fun ms(k: String, v: String) { media[i] = m + (k to v) }
                AppCard {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledField(tr("Jenis")) { AppSelect(mv("kind").ifBlank { "image" }, listOf("image", "video").map { Option(it, it) }, { ms("kind", it) }) }
                        LabeledField("URL") { AppTextField(mv("url"), { ms("url", it) }, placeholder = "https://") }
                        LabeledField(tr("Rasio")) { AppSelect(mv("ratio").ifBlank { "16:9" }, RATIOS.map { Option(it, it) }, { ms("ratio", it) }) }
                        LabeledField(tr("Posisi")) { AppSelect(mv("position").ifBlank { "center" }, POSITIONS.map { Option(it, it) }, { ms("position", it) }) }
                        LabeledField(tr("Ukuran")) { AppTextField(mv("size"), { ms("size", it) }) }
                        LabeledField(tr("Radius")) { AppTextField(mv("radius"), { ms("radius", it) }) }
                        LabeledField(tr("Layer")) { AppTextField(mv("layer"), { ms("layer", it) }) }
                        LabeledField(tr("Tautkan Sumber Daya")) {
                            AppSelect(mv("resource_type").ifBlank { "none" }, listOf(Option("none", "-"), Option("event", t("events")), Option("group", t("groups"))), { ms("resource_type", if (it == "none") "" else it); if (it == "none") ms("resource_id", "") })
                        }
                        val rt = mv("resource_type")
                        if (rt == "event" || rt == "group") {
                            val opts = (if (rt == "event") data.events else data.groups).map { Option(it.id.toString(), it.nama) }
                            AppSelect(mv("resource_id"), opts, { ms("resource_id", it) }, placeholder = "-")
                        }
                        if (mv("kind") == "video") {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppSwitch(mv("autoplay") == "true") { ms("autoplay", it.toString()) }; Text("Autoplay", color = Color.White, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppSwitch(mv("muted") == "true") { ms("muted", it.toString()) }; Text("Muted", color = Color.White, fontSize = 14.sp)
                            }
                        }
                        AppButton(t("act-delete"), { media.removeAt(i) }, variant = BtnVariant.Destructive, icon = Icons.Outlined.Delete)
                    }
                }
            }
            AppButton(tr("Tambah Media"), { media.add(MEDIA_KEYS.associateWith { "" } + mapOf("id" to newId("media"), "kind" to "image", "ratio" to "16:9", "position" to "center")) }, variant = BtnVariant.Secondary, icon = Icons.Outlined.Add)
        }

        AppButton(if (showStyle) tr("Sembunyikan Gaya") else tr("Gaya & Tipografi"), { showStyle = !showStyle }, Modifier.fillMaxWidth(), variant = BtnVariant.Outline)
        if (showStyle) {
            COLOR_KEYS.forEach { k -> ColorField(k.replace('_', ' '), f(k)) { set(k, it) } }
            SIZE_KEYS.forEach { k ->
                LabeledField(k.replace('_', ' ')) { AppTextField(f(k), { set(k, it) }, keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppSwitch(enabled) { enabled = it }; Text(tr("Aktif"), color = Color.White, fontSize = 14.sp)
        }
        error?.let { Text(it, color = Tokens.Red400, fontSize = 12.sp) }
        DialogActions(onClose, {
            val pg = page.trim().lowercase()
            if (pg.isBlank()) { error = tr("Halaman wajib diisi"); return@DialogActions }
            vm.saveSection(SectionDraft(section?.id, pg, type, order.toIntOrNull() ?: 0, build(), enabled)) { onClose() }
        })
    }
}

private val MEDIA_KEYS = listOf("id", "kind", "url", "ratio", "size", "radius", "position", "layer", "autoplay", "muted", "resource_type", "resource_id")

/* ------------------------------------------------------------------ Published-site preview */

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebPreviewDialog(url: String, onOpen: (String) -> Unit, onClose: () -> Unit) {
    AppDialog(tr("Pratinjau"), onClose, wide = true) {
        AndroidView(
            factory = { c -> WebView(c).apply { settings.javaScriptEnabled = true; settings.domStorageEnabled = true; setBackgroundColor(0xFF0F1117.toInt()); loadUrl(url) } },
            modifier = Modifier.fillMaxWidth().height(480.dp).clip(RoundedCornerShape(12.dp)),
            onRelease = { it.destroy() },
        )
        AppButton(tr("Buka di browser"), { onOpen(url) }, Modifier.fillMaxWidth(), variant = BtnVariant.Secondary, icon = Icons.Outlined.OpenInNew)
    }
}
