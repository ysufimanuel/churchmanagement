package com.growthaxys.churchmanagements.feature.common

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import java.io.File

/** Port of web `src/lib/export.ts` (CSV / TXT / XLS(SpreadsheetML) / print-PDF). Files are shared via the Android Sharesheet. */
typealias ExportRows = List<Map<String, Any?>>

object Exporter {
    enum class Format { CSV, XLS, TXT, PDF }

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    fun csv(rows: ExportRows): String {
        val headers = rows.first().keys.toList()
        return (listOf(headers.joinToString(",")) + rows.map { r ->
            headers.joinToString(",") { h -> "\"${(r[h] ?: "").toString().replace("\"", "\"\"")}\"" }
        }).joinToString("\n")
    }

    fun txt(rows: ExportRows): String = rows.joinToString("\n") { r -> r.entries.joinToString(" | ") { "${it.key}: ${it.value ?: "-"}" } }

    fun xls(rows: ExportRows): String {
        val headers = rows.first().keys.toList()
        fun cell(v: String) = "<Cell><Data ss:Type=\"String\">${esc(v)}</Data></Cell>"
        return "<?xml version=\"1.0\"?><Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">" +
            "<Worksheet ss:Name=\"Data\"><Table><Row>${headers.joinToString("") { cell(it) }}</Row>" +
            rows.joinToString("") { r -> "<Row>${headers.joinToString("") { h -> cell((r[h] ?: "").toString()) }}</Row>" } +
            "</Table></Worksheet></Workbook>"
    }

    fun export(context: Context, format: Format, rows: ExportRows, filename: String, title: String) {
        if (rows.isEmpty()) return
        when (format) {
            Format.CSV -> share(context, csv(rows), "$filename.csv", "text/csv")
            Format.TXT -> share(context, txt(rows), "$filename.txt", "text/plain")
            Format.XLS -> share(context, xls(rows), "$filename.xls", "application/vnd.ms-excel")
            Format.PDF -> print(context, title, rows)
        }
    }

    private fun share(context: Context, content: String, filename: String, mime: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, filename).apply { writeText(content, Charsets.UTF_8) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, filename).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Web uses window.print() on a generated HTML table; Android uses PrintManager (also offers "Save as PDF"). */
    private fun print(context: Context, title: String, rows: ExportRows) {
        val headers = rows.first().keys.toList()
        val html = "<html><head><meta charset=\"utf-8\"><title>${esc(title)}</title><style>body{font-family:sans-serif;padding:24px}" +
            "table{width:100%;border-collapse:collapse}th,td{border:1px solid #ccc;padding:6px;text-align:left;font-size:12px}th{background:#f3f4f6}</style></head><body>" +
            "<h2>${esc(title)}</h2><table><tr>${headers.joinToString("") { "<th>${esc(it)}</th>" }}</tr>" +
            rows.joinToString("") { r -> "<tr>${headers.joinToString("") { h -> "<td>${esc((r[h] ?: "").toString())}</td>" }}</tr>" } +
            "</table></body></html>"
        val web = WebView(context) // used only as a print renderer, never to show the product UI
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                pm.print(title, view.createPrintDocumentAdapter(title), PrintAttributes.Builder().build())
            }
        }
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }
}
