package com.quotebrowser.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.quotebrowser.app.data.Quote
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Which optional parts to include when sharing/copying a quote. Text is always included. */
data class ShareOptions(
    val includeNumber: Boolean = false,
    val includeRating: Boolean = false,
    val includeUrl: Boolean = false,
    val stripUsernames: Boolean = false,
    val includeCategories: Boolean = false,
    val spaceOutLines: Boolean = false
)

/**
 * Removes chat-style username prefixes from the start of each line:
 *   <nick> message        (IRC style, ~78% of lines)
 *   nick: message          (IM style, e.g. bloodninja:)
 *   (nick) message / (nick): message
 */
private val nickPrefixPatterns = listOf(
    // v1.88.36: "nick1 | nick2:" compound speaker prefix (bash.org #448982).
    Regex("^[A-Za-z0-9_\\-\\[\\]{}^`|\\\\]{1,39}\\s*\\|\\s*[A-Za-z0-9_\\-\\[\\]{}^`|\\\\]{1,39}:\\s"),
    Regex("^<[^<>\\n]{1,48}>\\s?"),
    Regex("^\\([^()\\n]{1,32}\\)\\s*:?\\s?"),
    Regex("^[A-Za-z0-9_\\-\\[\\]{}^`|\\\\]{2,32}:\\s")
)

fun stripUsernames(text: String): String =
    text.lines().joinToString("\n") { line ->
        var s = line
        for (pattern in nickPrefixPatterns) {
            val stripped = pattern.replaceFirst(s, "")
            if (stripped != s) {
                s = stripped
                break
            }
        }
        s
    }

/** Builds the shared/copied text for a quote. Text first, then optional metadata. */
fun formatQuoteForSharing(
    quote: Quote,
    options: ShareOptions = ShareOptions(),
    categories: List<String> = emptyList()
): String =
    buildString {
        if (options.includeNumber) {
            append("#${quote.number}")
            if (options.includeRating) {
                append(" (rating ${quote.rating?.toString() ?: "unrated"})")
            }
            appendLine()
            appendLine()
        } else if (options.includeRating) {
            append("Rating: ${quote.rating?.toString() ?: "unrated"}")
            appendLine()
            appendLine()
        }
        var text = if (options.stripUsernames) stripUsernames(quote.text) else quote.text
        // v1.88.34: uniform single blank line between every non-empty body line.
        // Applies ONLY to the quote body, never to the number/rating/URL/category metadata.
        if (options.spaceOutLines) {
            text = text.lines().filter { it.isNotBlank() }.joinToString("\n\n")
        }
        append(text)
        if (options.includeUrl) {
            appendLine()
            appendLine()
            append(quote.url)
        }
        if (options.includeCategories && categories.isNotEmpty()) {
            appendLine()
            appendLine()
            append("Categories: ${categories.joinToString(", ")}")
        }
    }

/** Copies the quote text (plus any selected extras) to the clipboard. */
fun copyQuote(
    context: Context,
    quote: Quote,
    options: ShareOptions = ShareOptions(),
    categories: List<String> = emptyList()
) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(
        ClipData.newPlainText("quote", formatQuoteForSharing(quote, options, categories))
    )
    Toast.makeText(context, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
}

/** Shares the quote text (plus any selected extras) through the system chooser. */
fun shareQuote(
    context: Context,
    quote: Quote,
    options: ShareOptions = ShareOptions(),
    categories: List<String> = emptyList()
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, formatQuoteForSharing(quote, options, categories))
    }
    context.startActivity(Intent.createChooser(intent, "Share quote"))
}

/** Opens a web link in the user's browser. Best-effort: no-ops on bad URLs. */
fun openWebLink(context: Context, url: String) {
    val clean = url.trim()
    if (clean.isEmpty()) return
    try {
        val uri = Uri.parse(if ("://" in clean) clean else "https://$clean")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't open link", Toast.LENGTH_SHORT).show()
    }
}

/** Formats a quote list as plain text for .txt export. */
fun formatQuotesForExport(quotes: List<Quote>, title: String): String = buildString {
    appendLine(title)
    appendLine("Exported ${LocalDate.now()} · ${quotes.size} quotes")
    quotes.forEach { q ->
        appendLine()
        appendLine("------------------------")
        appendLine("#${q.number} (rating ${q.rating?.toString() ?: "unrated"})")
        appendLine(q.text)
        appendLine(q.url)
    }
}

/** Writes the export text to a Storage Access Framework Uri. */
suspend fun exportQuotesToUri(
    context: Context,
    uri: Uri,
    quotes: List<Quote>,
    title: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(formatQuotesForExport(quotes, title).toByteArray(Charsets.UTF_8))
        } != null
    } catch (e: Exception) {
        false
    }
}
