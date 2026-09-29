package com.quotebrowser.app.ui.layouts

import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.layouts.LayoutBlock
import com.quotebrowser.app.data.layouts.LayoutBlockType
import com.quotebrowser.app.data.layouts.LayoutDef
import com.quotebrowser.app.ui.NickPalette
import com.quotebrowser.app.ui.QuoteCleanOptions
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.quoteTextHtmlWithClasses
import com.quotebrowser.app.ui.theme.resolveNickColors

/**
 * v1.86: builds the [NickPalette] + [QuoteCleanOptions] the standalone
 * HTML export renders quote text with, mirroring the widget providers
 * (same theme resolution, same per-category colors). Null when nick
 * coloring is off — the export then falls back to plain text.
 */
fun quoteTextExportStyle(
    manager: DatabaseManager
): Pair<NickPalette, QuoteCleanOptions>? {
    if (!manager.nickColoringEnabled.value) return null
    val resolved = resolveNickColors(manager.themeId.value, manager.customThemes.value)
    val palette = NickPalette(
        enabled = true,
        brackets = resolved.brackets,
        names = resolved.names,
        bracketPalette = resolved.bracketPalette,
        action = resolved.action,
        actionOverridesName = resolved.actionOverridesName,
        colorMessageWithSpeaker = resolved.colorMessageWithSpeaker,
        rules = manager.nickRules.value,
        ircLineBreaks = manager.ircLineBreaks.value,
        dialogueLineBreaks = manager.dialogueLineBreaks.value
    )
    val opts = QuoteCleanOptions(
        colorNames = manager.qcColorNames.value,
        colorActions = manager.qcColorActions.value,
        normalizeBreaks = manager.qcNormalizeBreaks.value,
        showTimestamps = manager.qcShowTimestamps.value,
        colorMessage = manager.qcColorMessage.value,
        nameColorHex = manager.qcNameColor.value,
        messageColorHex = manager.qcMessageColor.value,
        actionColorHex = manager.qcActionColor.value,
        timestampColorHex = manager.qcTimestampColor.value,
        colorOther = manager.qcColorOther.value,
        otherColorHex = manager.qcOtherColor.value
    )
    return palette to opts
}

/**
 * v1.85: HTML export. One [LayoutDef] drives the on-screen card and the
 * exported page: each block becomes a `<div class="qb-…">`, styled by the
 * layout's embedded CSS. A layout may instead ship a custom [LayoutDef.htmlTemplate]
 * using {{tokens}}, which is substituted per quote.
 */

/** Export-dialog toggles (Clint's refinement): applied on top of the layout at export time. */
data class HtmlExportOptions(
    val includeUrl: Boolean = true,
    val includeRating: Boolean = true,
    val includeDate: Boolean = true,
    val includeCategories: Boolean = true,
    val includeBranding: Boolean = true
)

/** The {{token}} vocabulary for custom HTML templates. */
val HTML_TOKENS = listOf(
    "{{quote_text}}", "{{author}}", "{{quote_number}}", "{{site_name}}",
    "{{source_url}}", "{{date}}", "{{rating}}", "{{categories}}"
)

fun escapeHtml(s: String): String = buildString(s.length + 16) {
    for (c in s) {
        when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(c)
        }
    }
}

/** Best-effort author: first nick mentioned in the text, else blank. */
fun authorFor(quote: Quote): String =
    try {
        extractNicks(quote.text).firstOrNull() ?: ""
    } catch (e: Exception) {
        ""
    }

data class QuoteExportContext(
    val quote: Quote,
    val siteName: String,
    val categories: List<String> = emptyList(),
    /** Quote.date doesn't exist on the model; empty unless a caller supplies one. */
    val date: String = ""
)

/** Raw (unescaped) token values for a quote. */
fun quoteTokenMap(ctx: QuoteExportContext): Map<String, String> {
    val q = ctx.quote
    return mapOf(
        "quote_text" to q.text,
        "author" to authorFor(q),
        "quote_number" to q.number.toString(),
        "site_name" to ctx.siteName,
        "source_url" to q.url,
        "date" to ctx.date,
        "rating" to (q.rating?.toString() ?: ""),
        "categories" to ctx.categories.joinToString(", ")
    )
}

/**
 * Substitutes {{tokens}} in [template]. Values are HTML-escaped; newlines
 * in quote_text become <br>. Unknown tokens are left untouched so typos
 * are visible instead of silently vanishing.
 */
fun substituteTokens(template: String, tokens: Map<String, String>): String {
    var out = template
    for ((key, raw) in tokens) {
        var v = escapeHtml(raw)
        if (key == "quote_text") v = v.replace("\n", "<br>")
        out = out.replace("{{$key}}", v)
    }
    return out
}

private fun ratingHtml(rating: Int?): String = when {
    rating == null -> ""
    rating > 0 -> "(+$rating)"
    rating < 0 -> "($rating)"
    else -> "(0)"
}

/** Renders one block of a layout as HTML. Null = block suppressed by toggles. */
private fun renderBlock(
    block: LayoutBlock,
    ctx: QuoteExportContext,
    tokens: Map<String, String>,
    options: HtmlExportOptions,
    /** v1.86: when non-null, quote text renders with classified per-category spans. */
    textStyle: Pair<NickPalette, QuoteCleanOptions>? = null
): String? {
    val q = ctx.quote
    return when (block.type) {
        LayoutBlockType.SITE_HEADER -> {
            if (!options.includeBranding) {
                // Without branding the header still shows the number —
                // it's the quote's identity, not the site's logo.
                "<div class=\"qb-site-header\"><span class=\"qb-quote-number\">#${q.number}</span></div>"
            } else {
                "<div class=\"qb-site-header\">" +
                    "<span class=\"qb-site-name\">${escapeHtml(ctx.siteName)}</span>" +
                    "<span class=\"qb-quote-number\">#${q.number}</span></div>"
            }
        }
        LayoutBlockType.QUOTE_TEXT -> {
            val inner = if (textStyle != null) {
                // v1.86: classified spans with per-category CSS classes
                // (qb-said-name, qb-said-text, qb-action-line, …).
                quoteTextHtmlWithClasses(q.text, textStyle.first, textStyle.second)
            } else {
                escapeHtml(q.text).replace("\n", "<br>")
            }
            "<div class=\"qb-quote-text\">$inner</div>"
        }
        LayoutBlockType.AUTHOR -> {
            val author = tokens["author"] ?: ""
            if (author.isEmpty()) null
            else "<div class=\"qb-author\">${escapeHtml(block.prefix + author)}</div>"
        }
        LayoutBlockType.RATING -> {
            if (!options.includeRating || q.rating == null) null
            else "<div class=\"qb-rating\">${escapeHtml(ratingHtml(q.rating))}</div>"
        }
        LayoutBlockType.META -> {
            val parts = mutableListOf<String>()
            if (options.includeDate && ctx.date.isNotEmpty() && "date" in block.show) {
                parts.add(escapeHtml(ctx.date))
            }
            if (options.includeCategories && ctx.categories.isNotEmpty() && "categories" in block.show) {
                parts.add(escapeHtml(ctx.categories.joinToString(", ")))
            }
            if ("url" in block.show && options.includeUrl && q.url.isNotEmpty()) {
                parts.add("<a href=\"${escapeHtml(q.url)}\">${escapeHtml(q.url)}</a>")
            }
            if (parts.isEmpty()) null
            else "<div class=\"qb-meta\">${parts.joinToString(" · ")}</div>"
        }
        LayoutBlockType.ACTIONS -> null // actions are interactive; a static page has none
        LayoutBlockType.DIVIDER -> "<hr class=\"qb-divider\">"
        LayoutBlockType.SPACER ->
            "<div class=\"qb-spacer\" style=\"height:${block.paddingDp.coerceIn(4, 64)}px\"></div>"
    }
}

/** Renders one quote to an HTML fragment (no <html> wrapper). */
fun renderQuoteHtml(
    ctx: QuoteExportContext,
    layout: LayoutDef,
    options: HtmlExportOptions,
    /** v1.86: when non-null, quote text renders with classified per-category spans. */
    textStyle: Pair<NickPalette, QuoteCleanOptions>? = null
): String {
    val tokens = quoteTokenMap(ctx)
    val inner = if (layout.htmlTemplate.isNotBlank()) {
        substituteTokens(layout.htmlTemplate, tokens)
    } else {
        val blocks = layout.blocks.mapNotNull { renderBlock(it, ctx, tokens, options, textStyle) }
        val extras = buildString {
            // Export-dialog toggles that aren't layout blocks: the web
            // address link and the branding line are appended after the
            // layout's own blocks.
            val hasUrlInMeta = layout.blocks.any {
                it.type == LayoutBlockType.META && "url" in it.show
            }
            if (options.includeUrl && ctx.quote.url.isNotEmpty() && !hasUrlInMeta) {
                val u = escapeHtml(ctx.quote.url)
                append("<div class=\"qb-source-url\"><a href=\"$u\">$u</a></div>")
            }
            if (options.includeBranding) {
                append("<div class=\"qb-branding\">Exported from Quote404</div>")
            }
        }
        (blocks + extras).joinToString("\n")
    }
    return "<article class=\"qb-quote qb-layout-${escapeHtml(layout.id)}\">\n$inner\n</article>"
}

/** A full standalone .html page for one or more quotes. */
fun renderQuotesPageHtml(
    title: String,
    contexts: List<QuoteExportContext>,
    layout: LayoutDef,
    options: HtmlExportOptions,
    /** v1.86: when non-null, quote text renders with classified per-category spans. */
    textStyle: Pair<NickPalette, QuoteCleanOptions>? = null,
    /**
     * v1.87: extra CSS layered on top of the layout's CSS — used for
     * export-style plugin packs chosen in the export dialog.
     */
    extraCss: String = ""
): String {
    val css = if (layout.css.isNotBlank()) layout.css else DEFAULT_EXPORT_CSS
    val body = contexts.joinToString("\n") { renderQuoteHtml(it, layout, options, textStyle) }
    val extra = if (extraCss.isNotBlank()) "\n/* export style plugin */\n$extraCss" else ""
    return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${escapeHtml(title)}</title>
<style>
$css$extra
</style>
</head>
<body>
<h1 class="qb-page-title">${escapeHtml(title)}</h1>
$body
</body>
</html>
"""
}

private const val DEFAULT_EXPORT_CSS = """
body { font-family: sans-serif; background: #111; color: #eee; margin: 0; padding: 16px; }
.qb-page-title { font-size: 1.2em; }
.qb-quote { background: #1e1e1e; border-radius: 12px; padding: 16px; margin: 12px 0; }
.qb-site-header { display: flex; justify-content: space-between; font-size: 0.8em; color: #888; margin-bottom: 8px; }
.qb-quote-text { line-height: 1.5; white-space: pre-wrap; }
.qb-author { text-align: right; font-style: italic; color: #aaa; margin-top: 8px; }
.qb-rating { margin-top: 8px; color: #ffd54f; font-weight: bold; }
.qb-meta { margin-top: 6px; font-size: 0.8em; color: #888; }
.qb-source-url { margin-top: 8px; font-size: 0.8em; }
.qb-source-url a { color: #7ab8ff; }
.qb-branding { margin-top: 10px; font-size: 0.75em; color: #666; text-align: center; }
/* v1.86: per-category quote-text classes. Colors are inline on each
   span; these selectors let custom layout CSS retarget the categories
   (use !important to beat the inline colors).
   .qb-said-name   speaker names (<nick>, Nick:, [nick], nick | …)
   .qb-said-bracket the brackets/marks around speaker names (<>, ??, [])
   .qb-said-text   the words a speaker says
   .qb-action-line action lines (* action, + nick, join/part notices)
   .qb-timestamp   [HH:MM] timestamp prefixes
   .qb-other-line  narration / system lines (neither speech nor action) */
.qb-said-name, .qb-said-bracket, .qb-said-text,
.qb-action-line, .qb-timestamp, .qb-other-line { }
"""
