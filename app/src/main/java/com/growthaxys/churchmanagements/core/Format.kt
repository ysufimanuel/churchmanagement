package com.growthaxys.churchmanagements.core

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** Port of web `src/lib/utils.ts` formatters. */
object Format {
    private val idLocale = Locale("id", "ID")

    fun rupiah(amount: Double): String = "Rp " + NumberFormat.getNumberInstance(idLocale).format(amount)
    fun rupiah(amount: Long): String = rupiah(amount.toDouble())

    /** Rp 1,2 jt / Rp 350 rb / Rp 2,5 M (id) — Rp 1.2 M / K / B (en). */
    fun rupiahCompact(amount: Double, lang: Lang): String {
        val n = abs(amount)
        val id = lang == Lang.ID
        val nf = NumberFormat.getNumberInstance(lang.locale).apply { maximumFractionDigits = 1 }
        val sign = if (amount < 0) "-" else ""
        return when {
            n >= 1e9 -> "${sign}Rp ${nf.format(n / 1e9)} ${if (id) "M" else "B"}"
            n >= 1e6 -> "${sign}Rp ${nf.format(n / 1e6)} ${if (id) "jt" else "M"}"
            n >= 1e3 -> "${sign}Rp ${nf.format(n / 1e3)} ${if (id) "rb" else "K"}"
            else -> "${sign}Rp ${nf.format(n)}"
        }
    }

    private fun parseDate(raw: String): LocalDate? = runCatching {
        if (raw.length <= 10) LocalDate.parse(raw) else OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    }.getOrNull()

    private fun parseInstant(raw: String): LocalDateTime? = runCatching {
        if (raw.length <= 10) LocalDate.parse(raw).atStartOfDay()
        else OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    }.getOrNull()

    /** "12 November 2026" */
    fun date(raw: String?, lang: Lang): String {
        if (raw.isNullOrBlank()) return "-"
        val d = parseDate(raw) ?: return raw
        return d.format(DateTimeFormatter.ofPattern("d MMMM yyyy", lang.locale))
    }

    /** "12 Nov 2026 14.30" */
    fun dateTime(raw: String?, lang: Lang): String {
        if (raw.isNullOrBlank()) return "-"
        val d = parseInstant(raw) ?: return raw
        return d.format(DateTimeFormatter.ofPattern("d MMM yyyy HH.mm", lang.locale))
    }

    fun timeAgo(raw: String?, lang: Lang): String {
        if (raw.isNullOrBlank()) return "-"
        val then = runCatching { Instant.parse(raw) }.getOrElse {
            runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
        } ?: return "-"
        val en = lang == Lang.EN
        val m = ((System.currentTimeMillis() - then.toEpochMilli()) / 60000).toInt()
        if (m < 1) return if (en) "just now" else "baru saja"
        if (m < 60) return if (en) "$m min ago" else "$m menit lalu"
        val h = m / 60
        if (h < 24) return if (en) "$h hr ago" else "$h jam lalu"
        val d = h / 24
        return if (en) "$d day${if (d == 1) "" else "s"} ago" else "$d hari lalu"
    }

    fun age(birth: String?): String {
        if (birth.isNullOrBlank()) return "-"
        val b = parseDate(birth) ?: return "-"
        return java.time.Period.between(b, LocalDate.now()).years.toString()
    }

    fun today(): String = LocalDate.now().toString()
    fun monthShort(ym: String, lang: Lang): String =
        LocalDate.parse("$ym-01").format(DateTimeFormatter.ofPattern("MMM", lang.locale))

}
