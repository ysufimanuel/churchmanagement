package com.growthaxys.churchmanagements.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

enum class Lang(val code: String, val locale: java.util.Locale) {
    ID("id", java.util.Locale("id", "ID")), EN("en", java.util.Locale("en", "US"));

    companion object {
        fun from(code: String?) = entries.firstOrNull { it.code == code } ?: EN // web default is 'en'
    }
}

/** Dictionaries are generated 1:1 from web `src/lib/i18n.ts` into assets/i18n/{id,en}.json. */
@Immutable
class Strings(val lang: Lang, private val dict: Map<String, String>, private val wm: Map<String, String> = emptyMap()) {
    fun t(key: String): String = dict[key] ?: key

    /** Web `tr()`: Web Manager copy is authored in Indonesian; English comes from `wm-en.json` (generated from `wm-en.ts`). */
    fun tr(indonesian: String): String = if (lang == Lang.EN) wm[indonesian] ?: indonesian else indonesian

    /** Translates server/Supabase error messages the same way web `tm()` does for known messages. */
    fun tm(message: String?): String {
        if (message.isNullOrBlank()) return t("upload-failed")
        if (lang == Lang.ID) return EN_TO_ID[message] ?: message
        return ID_TO_EN[message] ?: PATTERNS.firstNotNullOfOrNull { (re, f) -> re.matchEntire(message)?.let(f) } ?: message
    }

    companion object {
        private val serializer = MapSerializer(String.serializer(), String.serializer())
        fun load(context: Context, lang: Lang): Strings {
            val text = context.assets.open("i18n/${lang.code}.json").bufferedReader().use { it.readText() }
            // extra-<lang>.json = Android-only strings (not in the web dictionary); web keys win on conflict.
            val extra = runCatching { context.assets.open("i18n/extra-${lang.code}.json").bufferedReader().use { it.readText() } }.getOrNull()
                ?.let { Json.decodeFromString(serializer, it) }.orEmpty()
            val wm = runCatching { context.assets.open("i18n/wm-en.json").bufferedReader().use { Json.decodeFromString(serializer, it.readText()) } }.getOrDefault(emptyMap())
            return Strings(lang, extra + Json.decodeFromString(serializer, text), wm)
        }

        private val ID_TO_EN = mapOf(
            "Hanya Superadmin yang dapat menghapus data" to "Only a Super Admin can delete data",
            "Hanya Superadmin yang dapat menghapus peserta event" to "Only a Super Admin can remove event participants",
            "Hanya Superadmin yang dapat menghapus anggota komunitas" to "Only a Super Admin can remove group members",
            "Hanya Superadmin yang dapat memproses transaksi" to "Only a Super Admin can process transactions",
            "Akses ditolak" to "Access denied",
            "Pesan kosong" to "Message is empty",
            "Penerima pesan tidak valid" to "Invalid message recipient",
        )
        private val EN_TO_ID = mapOf("Unknown server error" to "Terjadi kesalahan pada server")
        private val PATTERNS: List<Pair<Regex, (MatchResult) -> String>> = listOf(
            Regex("^Donasi tersimpan, tetapi total donatur gagal diperbarui: ([\\s\\S]*)$") to
                { m -> "Donation saved, but the donor total failed to update: ${m.groupValues[1]}" },
            Regex("^Donasi terhapus, tetapi total donatur gagal diperbarui: ([\\s\\S]*)$") to
                { m -> "Donation deleted, but the donor total failed to update: ${m.groupValues[1]}" },
        )
    }
}

val LocalStrings = compositionLocalOf { Strings(Lang.EN, emptyMap()) }

/** Shorthand used in composables: `t("members")`. */
@Composable
fun t(key: String): String = LocalStrings.current.t(key)

/** Web Manager copy (`tr('…Indonesian…')`). */
@Composable
fun tr(indonesian: String): String = LocalStrings.current.tr(indonesian)
