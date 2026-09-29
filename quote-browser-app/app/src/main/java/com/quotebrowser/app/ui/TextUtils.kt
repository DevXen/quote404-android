package com.quotebrowser.app.ui

/**
 * Small shared text helpers.
 */

/** Number of whitespace-separated words in [text]. */
fun wordCount(text: String): Int {
    var count = 0
    var inWord = false
    for (c in text) {
        if (c.isWhitespace()) {
            inWord = false
        } else if (!inWord) {
            inWord = true
            count++
        }
    }
    return count
}

/**
 * Whether [text] passes the quote-length filter. Word counts come from
 * [wordCount]; thresholds are user-configurable in Settings.
 */
fun lengthFilterOk(
    text: String,
    mode: com.quotebrowser.app.data.LengthMode,
    customMin: Int?,
    customMax: Int?,
    longerWords: Int,
    shorterWords: Int,
    /**
     * v1.51: whether the "Normalize / line breaks" QuoteClean toggle is on.
     * The ONE_LINE check judges what the user actually sees: IRC
     * `" \"` separators become real line breaks in the display pipeline
     * (unconditional there), and `/` separators do too when this toggle
     * is on. Raw stored text can contain zero `\n` yet render as many
     * lines, so checking the raw text undercounts.
     */
    normalizeSlashes: Boolean = true
): Boolean {
    val wc = wordCount(text)
    return when (mode) {
        com.quotebrowser.app.data.LengthMode.ALL -> true
        com.quotebrowser.app.data.LengthMode.LONGER -> wc >= longerWords
        com.quotebrowser.app.data.LengthMode.SHORTER -> wc <= shorterWords
        com.quotebrowser.app.data.LengthMode.ONE_LINE -> {
            val shown = applyIrcLineBreaks(text).let {
                if (normalizeSlashes) normalizeSlashBreaks(it) else it
            }.trim()
            '\n' !in shown && '\r' !in shown
        }
        com.quotebrowser.app.data.LengthMode.CUSTOM ->
            (customMin == null || wc >= customMin) &&
                (customMax == null || wc <= customMax)
    }
}
