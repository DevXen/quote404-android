package com.quotebrowser.app.data

import android.text.Html
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Result of fetching a quote from a bash.org link. */
data class FetchedBashQuote(val number: Int, val text: String)

/**
 * Fetches a quote from a bash.org link (or a bare quote number).
 *
 * Accepts forms like:
 * - https://bash.org/?244321
 * - http://bash.org/?244321
 * - 244321
 *
 * The parser is deliberately tolerant: it grabs the first
 * `<p class="qt">…</p>` block, turns `<br>` into line breaks, strips the
 * remaining tags, and unescapes HTML entities.
 */
suspend fun fetchBashOrgQuote(input: String): Result<FetchedBashQuote> =
    withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return@withContext Result.failure(
            IllegalArgumentException("Paste a bash.org link first.")
        )
        val numberMatch = Regex("""(\d{4,8})""").find(trimmed)
            ?: return@withContext Result.failure(
                IllegalArgumentException("No quote number found in that link.")
            )
        val number = numberMatch.groupValues[1].toInt()
        try {
            val url = URL("https://bash.org/?$number")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 16) QuoteBrowser/1.23"
                )
            }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    IllegalStateException("bash.org returned HTTP $code.")
                )
            }
            val html = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            conn.disconnect()
            val text = parseBashOrgQuote(html)
                ?: return@withContext Result.failure(
                    IllegalStateException("Couldn't find a quote on that page.")
                )
            Result.success(FetchedBashQuote(number, text))
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Couldn't reach bash.org: ${e.message}"))
        }
    }

/** Extracts the quote body from a bash.org quote page, or null. */
internal fun parseBashOrgQuote(html: String): String? {
    // Primary: the classic <p class="qt"> block.
    val blocks = Regex(
        """<p\s+class="qt">(.*?)</p>""",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
    ).findAll(html).map { it.groupValues[1] }.toList()
    // Fallback: some mirrors wrap quotes in <div class="quote">.
    val raw = blocks.firstOrNull()
        ?: Regex(
            """<div\s+class="quote">(.*?)</div>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        ).find(html)?.groupValues?.get(1)
        ?: return null
    var text = raw
        .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("""<[^>]+>"""), "")
    // Unescape entities (&lt; &gt; &amp; &quot; …).
    text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
    val lines = text.lines()
        .map { it.trimEnd() }
        // Drop the "#12345 …" header line bash.org puts first.
        .dropWhile { it.matches(Regex("""#\d+.*""")) }
        .dropWhile { it.isBlank() }
    val cleaned = lines.joinToString("\n").trim()
    return cleaned.ifBlank { null }
}
