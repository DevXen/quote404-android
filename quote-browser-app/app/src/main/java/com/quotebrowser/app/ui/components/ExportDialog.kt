package com.quotebrowser.app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.data.quoteBackgroundForDb
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.data.siteStyleFor
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.LocalSiteShareCardsEnabled
import com.quotebrowser.app.ui.QuoteCardStyleDialog
import com.quotebrowser.app.ui.SlideItem
import com.quotebrowser.app.ui.VideoBackground
import com.quotebrowser.app.ui.exportNarrationToM4a
import com.quotebrowser.app.ui.exportGeminiNarrationToM4a
import com.quotebrowser.app.ui.exportSlideshowVideo
import com.quotebrowser.app.ui.renderSiteQuoteCard
import com.quotebrowser.app.ui.isNoveltyTheme
import com.quotebrowser.app.ui.renderNoveltyQuoteCard
import com.quotebrowser.app.ui.rememberNoveltyExportLocals
import com.quotebrowser.app.ui.noveltyCardThemeKey
import com.quotebrowser.app.ui.ShareOptions
import com.quotebrowser.app.ui.formatQuoteForSharing
import com.quotebrowser.app.ui.quote404WatermarkLogo
import com.quotebrowser.app.ui.stripUsernames
import com.quotebrowser.app.ui.screens.ExportFormat
import com.quotebrowser.app.ui.screens.renderBinaryExport
import com.quotebrowser.app.ui.screens.quotesToCsv
import com.quotebrowser.app.ui.screens.quotesToJson
import com.quotebrowser.app.ui.screens.quotesToXml
import com.quotebrowser.app.ui.screens.renderQuoteAsPng
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Export dialog for a single quote: pick a format plus the same text
 * options as the share dialog, then save through the system file picker.
 * The number/rating/URL toggles shape the human-readable formats (TXT, PNG);
 * JSON/XML/CSV always carry the full record.
 *
 * v1.74.7: MP4 now renders a real narrated slideshow video (with narrate /
 * word-highlight / animated-background options) and M4A a real narrated
 * audio file — both used to write the raw quote text into the media
 * container, producing a corrupt, unplayable file. PNG keeps the
 * site-styled card option and gains the share dialog's "Colorize
 * nicknames" toggle.
 */
@Composable
fun SingleQuoteExportDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dbId: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var format by remember { mutableStateOf(ExportFormat.TXT) }
    var includeNumber by remember { mutableStateOf(true) }
    var includeRating by remember { mutableStateOf(true) }
    var includeUrl by remember { mutableStateOf(true) }
    var stripNames by remember { mutableStateOf(false) }
    // v1.74.7: MP4 options — the same slideshow knobs the Export screen
    // offers, so a single quote can become a narrated video too.
    var narrate by remember { mutableStateOf(true) }
    var karaoke by remember { mutableStateOf(true) }
    var videoBg by remember { mutableStateOf(VideoBackground.MIDNIGHT) }
    var bgMenuOpen by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    // v1.74.9: M4A narration options — announce nicknames ("X says:" /
    // "Y replies:") and include the quote number or not. The export uses
    // the Gemini voices already saved for the quote.
    var announceNicks by remember { mutableStateOf(true) }
    // v1.75: designed-image export — the same style picker Share uses,
    // so Export PNG offers the same backgrounds/pics/options.
    var showDesignedPicker by remember { mutableStateOf(false) }
    var designedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var designedFileName by remember { mutableStateOf("") }
    // v1.53: site-styled PNG card alternative, from the quote's database site
    // theme; only offered when the feature is on and a site style resolves.
    val siteCardsOn = LocalSiteShareCardsEnabled.current
    val outsideBlack = LocalBlackOutsideQuotes.current
    val dbTotal = LocalDbTotals.current[dbId]
    val themeSnap = LocalDbThemeSnapshots.current
    val resolvedTheme = remember(dbId, siteCardsOn, themeSnap) {
        if (siteCardsOn && dbId != null) resolveDbTheme(themeSnap, dbId) else null
    }
    val siteStyle = remember(resolvedTheme) {
        val r = resolvedTheme
        if (r != null && !r.bypassed) siteStyleFor(r.structureThemeId)
        else SiteStyle.MODERN_CARD
    }
    val showSiteCardOption = format == ExportFormat.PNG &&
        siteStyle != SiteStyle.MODERN_CARD
    var siteStyled by remember(showSiteCardOption) { mutableStateOf(showSiteCardOption) }
    // v1.74.7: "Colorize nicknames" on the site card, like the share dialog
    // — defaults to the global nick-coloring setting.
    val nickPalette = LocalNickPalette.current
    var colorizeNicks by remember { mutableStateOf(nickPalette.enabled) }
    // v1.88.24: ambient composition locals for the novelty-theme export
    // path, so the exported card matches the on-screen one.
    val exportNoveltyLocals = rememberNoveltyExportLocals()
    // v1.88.32: "remove nicks from message text" for the chat themes, so
    // the direct PNG export matches the on-screen card.
    val nickStripStore = com.quotebrowser.app.ui.LocalThemeNickPaletteStore.current
    val nickStripSet by (nickStripStore?.nickStrip
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptySet<String>()) })
        .collectAsState()
    val quoteBackgroundArgb = remember(resolvedTheme, dbId, themeSnap) {
        if (resolvedTheme != null && dbId != null) {
            quoteBackgroundForDb(resolvedTheme, dbId, themeSnap).toArgb()
        } else {
            null
        }
    }

    fun doExport(uri: Uri) {
        working = true
        errorText = null
        scope.launch(Dispatchers.IO) {
            var failure: String? = null
            try {
                val q = if (stripNames) quote.copy(text = stripUsernames(quote.text))
                else quote
                when (format) {
                    ExportFormat.PNG -> {
                        // v1.61.8: Clint's Quote404 logo watermark for the export.
                        val watermark = quote404WatermarkLogo(context)
                        val bitmap = if (showSiteCardOption && siteStyled) {
                            // v1.88.24: novelty themes render the real
                            // on-screen composables off-screen — the export
                            // is pixel-identical to the card.
                            if (siteStyle.isNoveltyTheme()) {
                                renderNoveltyQuoteCard(
                                    context, q, siteStyle,
                                    includeNumber = includeNumber,
                                    includeRating = includeRating,
                                    includeUrl = includeUrl,
                                    colorizeNicks = colorizeNicks,
                                    locals = exportNoveltyLocals,
                                    dbId = dbId,
                                    // v1.88.32: "remove nicks from message
                                    // text" for the chat themes.
                                    stripNicks = nickStripSet.contains(
                                        siteStyle.noveltyCardThemeKey()
                                    ),
                                    // v1.89.2: dual-palette cards (Friends
                                    // sunset vs wall) match the screen.
                                    structureThemeId =
                                        resolvedTheme?.structureThemeId
                                )
                            } else {
                                // The database's site theme: header, site
                                // background, footer, and optional nick colors.
                                renderSiteQuoteCard(
                                    q, siteStyle,
                                    includeNumber = includeNumber,
                                    includeRating = includeRating,
                                    includeUrl = includeUrl,
                                    outsideBlack = outsideBlack,
                                    dbTotal = dbTotal,
                                    quoteBackgroundArgb = quoteBackgroundArgb,
                                    colorizeNicks = colorizeNicks,
                                    nickPalette = nickPalette,
                                    hideTldInHeader = resolvedTheme?.hideTldInHeader == true,
                                    watermark = watermark,
                                    // v1.89.6: the rating format the
                                    // database carries, so the exported
                                    // rating text matches the screen.
                                    ratingFormat = dbId?.let {
                                        themeSnap.builtInDbRatingFormats[it]
                                    }
                                )
                            }
                        } else {
                            renderQuoteAsPng(q, showNumber = includeNumber, watermark = watermark)
                        }
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        bitmap.recycle()
                    }
                    ExportFormat.MP4 -> {
                        // A real 720x1280 narrated slideshow video, not the
                        // raw text with an .mp4 extension.
                        val rendered = exportSlideshowVideo(
                            context,
                            listOf(SlideItem(q, dbId ?: "Quote")),
                            narrate = narrate,
                            background = videoBg,
                            karaoke = karaoke
                        )
                        try {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                FileInputStream(rendered).use { it.copyTo(out) }
                            }
                        } finally {
                            rendered.delete()
                        }
                    }
                    ExportFormat.M4A -> {
                        // v1.74.9: narrated M4A using the Gemini voices saved
                        // for the quote, with nick-announcement and quote-number
                        // options. Falls back to system TTS when Gemini isn't
                        // available.
                        val rendered = exportGeminiNarrationToM4a(
                            context,
                            q,
                            dbId ?: "bash",
                            announceNicks = announceNicks,
                            includeNumber = includeNumber
                        )
                        try {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                FileInputStream(rendered).use { it.copyTo(out) }
                            }
                        } catch (e: Exception) {
                            rendered.delete()
                            throw e
                        }
                        rendered.delete()
                    }
                    ExportFormat.PDF, ExportFormat.EPUB -> {
                        val bytes = renderBinaryExport(format, listOf(q), "Quote #${q.number}")
                            ?: q.text.toByteArray(Charsets.UTF_8)
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(bytes)
                        }
                    }
                    ExportFormat.HTML -> {
                        // v1.85: standalone web page styled by the active
                        // quote layout (per-db override or global). The
                        // dialog's URL/rating toggles map onto the HTML
                        // export options.
                        val layout = com.quotebrowser.app.data.layouts.LayoutStore.layoutForDb(
                            context, dbId,
                            com.quotebrowser.app.data.plugins.PluginManager.pluginLayouts
                        )
                        // v1.86: quote text renders with classified
                        // per-category spans (qb-said-name, qb-said-text,
                        // qb-action-line, …), matching the in-app colors.
                        val manager =
                            (context.applicationContext as com.quotebrowser.app.QuoteApp).dbManager
                        val html = com.quotebrowser.app.ui.layouts.renderQuotesPageHtml(
                            "Quote #${q.number}",
                            listOf(
                                com.quotebrowser.app.ui.layouts.QuoteExportContext(
                                    quote = q,
                                    siteName = dbId ?: "Quote"
                                )
                            ),
                            layout,
                            com.quotebrowser.app.ui.layouts.HtmlExportOptions(
                                includeUrl = includeUrl,
                                includeRating = includeRating
                            ),
                            com.quotebrowser.app.ui.layouts.quoteTextExportStyle(manager)
                        )
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(html.toByteArray(Charsets.UTF_8))
                        }
                    }
                    else -> {
                        val content = when (format) {
                            ExportFormat.TXT -> formatQuoteForSharing(
                                q,
                                ShareOptions(includeNumber, includeRating, includeUrl, stripNames)
                            )
                            ExportFormat.JSON -> quotesToJson(listOf(q))
                            ExportFormat.XML -> quotesToXml(listOf(q))
                            ExportFormat.CSV -> quotesToCsv(listOf(q))
                            else -> q.text
                        }
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(content.toByteArray(Charsets.UTF_8))
                        }
                    }
                }
            } catch (e: Exception) {
                failure = e.message ?: e.javaClass.simpleName
            }
            withContext(Dispatchers.Main) {
                working = false
                if (failure != null) {
                    errorText = failure
                } else {
                    Toast.makeText(
                        context,
                        "Exported quote #${quote.number}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                }
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        if (uri != null) doExport(uri)
    }

    // v1.75: save-file picker for the designed-image path — writes the
    // bitmap the style picker rendered (same renderers Share uses).
    val designedFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        val bmp = designedBitmap
        designedBitmap = null
        if (uri != null && bmp != null && !bmp.isRecycled) {
            working = true
            errorText = null
            scope.launch(Dispatchers.IO) {
                var failure: String? = null
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                } catch (e: Exception) {
                    failure = e.message ?: e.javaClass.simpleName
                } finally {
                    bmp.recycle()
                }
                withContext(Dispatchers.Main) {
                    working = false
                    if (failure != null) {
                        errorText = failure
                    } else {
                        Toast.makeText(
                            context,
                            "Exported quote #${quote.number}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                }
            }
        } else {
            bmp?.recycle()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        modifier = modifier,
        title = { Text("Export quote #${quote.number}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Format",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExportFormat.values().forEach { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = format == f,
                                onClick = { format = f }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == f,
                            onClick = { format = f }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(f.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (format == ExportFormat.MP4) {
                    // v1.74.7: the video actually gets options now.
                    ExportCheckRow(
                        label = "Narrate the quote aloud",
                        checked = narrate,
                        onCheckedChange = { narrate = it }
                    )
                    ExportCheckRow(
                        label = "Highlight each word as it's spoken",
                        checked = karaoke,
                        onCheckedChange = { karaoke = it },
                        enabled = narrate
                    )
                    Box {
                        OutlinedButton(onClick = { bgMenuOpen = true }) {
                            Text("Background: ${videoBg.label}")
                        }
                        DropdownMenu(
                            expanded = bgMenuOpen,
                            onDismissRequest = { bgMenuOpen = false }
                        ) {
                            VideoBackground.values().forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg.label) },
                                    onClick = { videoBg = bg; bgMenuOpen = false }
                                )
                            }
                        }
                    }
                } else {
                    ExportCheckRow(
                        label = "Include quote number",
                        checked = includeNumber,
                        onCheckedChange = { includeNumber = it }
                    )
                    ExportCheckRow(
                        label = "Include rating",
                        checked = includeRating,
                        onCheckedChange = { includeRating = it }
                    )
                    ExportCheckRow(
                        label = "Include web address",
                        checked = includeUrl,
                        onCheckedChange = { includeUrl = it }
                    )
                    ExportCheckRow(
                        label = "Strip usernames (<nick>, nick:)",
                        checked = stripNames,
                        onCheckedChange = { stripNames = it }
                    )
                }
                // v1.53 site-styled PNG card option; v1.74.7 adds the
                // share dialog's "Colorize nicknames" toggle.
                if (showSiteCardOption) {
                    ExportCheckRow(
                        label = "Site-styled card (database site theme)",
                        checked = siteStyled,
                        onCheckedChange = { siteStyled = it }
                    )
                    ExportCheckRow(
                        label = "Colorize nicknames",
                        checked = colorizeNicks,
                        onCheckedChange = { colorizeNicks = it },
                        enabled = siteStyled
                    )
                }
                // v1.75: designed-image export — the exact same style
                // picker Share uses (Quote theme, Quote404, Midnight,
                // DevXen…), with preview before saving.
                if (format == ExportFormat.PNG) {
                    OutlinedButton(
                        onClick = { showDesignedPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Designed image…")
                    }
                    Text(
                        text = "Same card styles as Share — pick a style, " +
                            "preview it, then save the file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                // v1.74.9: M4A uses the Gemini voices saved for the quote,
                // with options for nick announcements and quote number.
                if (format == ExportFormat.M4A) {
                    ExportCheckRow(
                        label = "Announce nicknames (\"X says:\", \"Y replies:\")",
                        checked = announceNicks,
                        onCheckedChange = { announceNicks = it }
                    )
                    ExportCheckRow(
                        label = "Include quote number",
                        checked = includeNumber,
                        onCheckedChange = { includeNumber = it }
                    )
                    Text(
                        text = "Uses the Gemini voices saved for this quote. " +
                            "Saves the quote read aloud as an audio file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (format != ExportFormat.MP4) {
                    Text(
                        text = "Number/rating/link options apply to TXT and PNG; JSON, XML, CSV and PDF always carry the full record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (working) {
                    Text(
                        text = "Rendering…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (errorText != null) {
                    Text(
                        text = "Export failed: $errorText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    errorText = null
                    filePicker.launch("quote_${quote.number}.${format.ext}")
                },
                enabled = !working
            ) {
                Text("Save file…")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) {
                Text("Cancel")
            }
        }
    )

    // v1.75: the Share style picker in export mode — same styles, same
    // preview; "Save" hands the rendered bitmap back for the file picker.
    if (showDesignedPicker) {
        val q = if (stripNames) quote.copy(text = stripUsernames(quote.text))
        else quote
        QuoteCardStyleDialog(
            quote = q,
            includeNumber = includeNumber,
            includeRating = includeRating,
            includeUrl = includeUrl,
            ircLineBreaks = true,
            dialogueLineBreaks = true,
            onDismiss = { showDesignedPicker = false },
            dbId = dbId,
            shareScope = scope,
            onExportBitmap = { bmp, name ->
                designedBitmap = bmp
                designedFileName = name
                showDesignedPicker = false
                designedFilePicker.launch(name)
            }
        )
    }
}

@Composable
private fun ExportCheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
