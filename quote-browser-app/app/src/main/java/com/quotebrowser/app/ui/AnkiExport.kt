package com.quotebrowser.app.ui

import com.quotebrowser.app.data.SourcedQuote
import java.io.OutputStream

/**
 * Writes quotes as a tab-separated file Anki can import via File > Import.
 *
 * Each card: Front = the quote text collapsed to a single line,
 * Back = "#<number> — <database name>".
 *
 * The leading comment lines are Anki's import-header format: tab separator,
 * plain text (not HTML), and the column names.
 */
suspend fun writeAnkiTsv(
    out: OutputStream,
    quotes: List<SourcedQuote>,
    dbNameFor: (String) -> String
) {
    val sb = StringBuilder()
    sb.appendLine("#separator:tab")
    sb.appendLine("#html:false")
    sb.appendLine("#columns:Front\tBack")
    for (sq in quotes) {
        val front = singleLine(sq.quote.text)
        val back = singleLine("#${sq.quote.number} — ${dbNameFor(sq.dbId)}")
        sb.append(front).append('\t').append(back).append('\n')
    }
    out.write(sb.toString().toByteArray(Charsets.UTF_8))
}

/** Collapses tabs, newlines, and runs of whitespace to single spaces. */
private fun singleLine(text: String): String =
    text.replace('\t', ' ')
        .replace('\n', ' ')
        .replace('\r', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
