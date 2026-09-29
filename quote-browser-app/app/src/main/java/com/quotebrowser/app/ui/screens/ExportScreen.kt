package com.quotebrowser.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.plugins.rememberPluginExportStyleOptions
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.CollectionQuote
import com.quotebrowser.app.ui.drawQuote404LogoPlate
import com.quotebrowser.app.ui.paragraphizeBody
import com.quotebrowser.app.ui.quote404LogoPlateH
import com.quotebrowser.app.ui.MAX_QUOTE_STRIP_QUOTES
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.QuoteCollection
import com.quotebrowser.app.ui.SlideItem
import com.quotebrowser.app.ui.VideoBackground
import com.quotebrowser.app.ui.exportSlideshowVideo
import com.quotebrowser.app.ui.formatQuotesForExport
import com.quotebrowser.app.ui.quote404WatermarkLogo
import com.quotebrowser.app.ui.renderAndShareQuoteStrip
import com.quotebrowser.app.ui.shareCollection
import com.quotebrowser.app.ui.writeAnkiTsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

enum class ExportFormat(val label: String, val ext: String) {
    TXT("Plain text (.txt)", "txt"),
    JSON("JSON (.json)", "json"),
    XML("XML (.xml)", "xml"),
    CSV("CSV (.csv)", "csv"),
    PNG("Image (.png)", "png"),
    PDF("PDF (.pdf)", "pdf"),
    EPUB("E-book (.epub)", "epub"),
    ANKI("Anki flashcards (.tsv)", "tsv"),
    // v1.49: slideshow video export (MP4), gated by the feature toggle.
    MP4("Slideshow video (.mp4)", "mp4"),
    // v1.74.5: narrated audio export (M4A/AAC — Android has no MP3 encoder).
    M4A("Narrated audio (.m4a)", "m4a"),
    // v1.85: standalone web page export, styled by the active quote layout.
    HTML("Web page (.html)", "html")
}

/** MIME type for a SAF document of this format. */
fun exportMimeFor(format: ExportFormat): String = when (format) {
    ExportFormat.JSON -> "application/json"
    ExportFormat.XML -> "text/xml"
    ExportFormat.CSV -> "text/csv"
    ExportFormat.PNG -> "image/png"
    ExportFormat.PDF -> "application/pdf"
    ExportFormat.EPUB -> "application/epub+zip"
    ExportFormat.ANKI -> "text/tab-separated-values"
    ExportFormat.TXT -> "text/plain"
    ExportFormat.MP4 -> "video/mp4"
    ExportFormat.M4A -> "audio/mp4"
    ExportFormat.HTML -> "text/html"
}

/**
 * v1.49: renders a slideshow MP4 to a temp file, streams it into the SAF
 * document, and deletes the temp file. Returns false when the document
 * couldn't be created.
 */
private fun writeSlideshowToUri(
    context: Context,
    resolver: android.content.ContentResolver,
    folderUri: Uri,
    fileName: String,
    slides: List<SlideItem>,
    narrate: Boolean,
    background: VideoBackground = VideoBackground.MIDNIGHT,
    karaoke: Boolean = true
): Boolean {
    if (slides.isEmpty()) return false
    val fileUri = DocumentsContract.createDocument(
        resolver, folderUri, "video/mp4", fileName
    ) ?: return false
    val tmp = exportSlideshowVideo(context, slides, narrate, background, karaoke)
    try {
        resolver.openOutputStream(fileUri)?.use { out ->
            tmp.inputStream().use { it.copyTo(out) }
        }
    } finally {
        tmp.delete()
    }
    return true
}

/**
 * v1.74.5: renders narrated M4A audio to a temp file, streams it into the
 * SAF document, and deletes the temp file. Returns false when the document
 * couldn't be created or encoding failed.
 *
 * v1.74.6: on failure the half-created document is deleted (no 0-byte
 * ghost files) and the reason is reported through [onError] so the UI
 * can show what actually went wrong instead of a generic message.
 */
private fun writeAudioToUri(
    context: Context,
    resolver: android.content.ContentResolver,
    folderUri: Uri,
    fileName: String,
    quotes: List<com.quotebrowser.app.data.Quote>,
    onError: (String) -> Unit = {}
): Boolean {
    if (quotes.isEmpty()) return false
    val fileUri = DocumentsContract.createDocument(
        resolver, folderUri, "audio/mp4", fileName
    ) ?: return false
    val tmp = try {
        com.quotebrowser.app.ui.exportNarrationToM4a(
            context,
            quotes.map {
                com.quotebrowser.app.ui.audioSpokenText(it.number, it.text)
            }
        )
    } catch (e: Exception) {
        try {
            DocumentsContract.deleteDocument(resolver, fileUri)
        } catch (_: Exception) {
        }
        onError(e.message ?: e.toString())
        return false
    }
    try {
        resolver.openOutputStream(fileUri)?.use { out ->
            tmp.inputStream().use { it.copyTo(out) }
        }
    } finally {
        tmp.delete()
    }
    return true
}
/**
 * Binary renderers (PDF, EPUB); null for the text/image formats.
 * Keeps every export path (category, scope, basket) in sync.
 */
fun renderBinaryExport(
    format: ExportFormat,
    quotes: List<com.quotebrowser.app.data.Quote>,
    title: String
): ByteArray? = when (format) {
    ExportFormat.PDF -> com.quotebrowser.app.ui.renderQuotesToPdf(quotes, title)
    ExportFormat.EPUB -> com.quotebrowser.app.ui.renderQuotesToEpub(quotes, title)
    else -> null
}

enum class ExportScope(val label: String) {
    ALL("All quotes"),
    FAVORITES("Favorites only"),
    HISTORY("History only"),
    BASKET("Export tray")
}

enum class ExportMode(val label: String) {
    SINGLE("One file (all quotes together)"),
    PER_QUOTE("Separate file per quote")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    vm: MainViewModel,
    catVm: com.quotebrowser.app.ui.CategoryViewModel,
    initialScope: ExportScope = ExportScope.ALL,
    // v1.67: the Random page's "Export video" menu item opens Export with
    // the slideshow video format preselected.
    initialFormat: ExportFormat = ExportFormat.TXT
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var format by remember(initialFormat) { mutableStateOf(initialFormat) }
    var exportScope by remember(initialScope) { mutableStateOf(initialScope) }
    var mode by remember { mutableStateOf(ExportMode.SINGLE) }
    var exporting by remember { mutableStateOf(false) }
    // v1.49: slideshow narration option + feature gate.
    var narrate by remember { mutableStateOf(false) }
    // v1.63: animated video background + karaoke word highlighting.
    var videoBg by remember { mutableStateOf(VideoBackground.MIDNIGHT) }
    var karaoke by remember { mutableStateOf(true) }
    // v1.85: HTML export element toggles (applied on top of the layout).
    var htmlIncludeUrl by remember { mutableStateOf(true) }
    var htmlIncludeRating by remember { mutableStateOf(true) }
    var htmlIncludeDate by remember { mutableStateOf(true) }
    var htmlIncludeCategories by remember { mutableStateOf(true) }
    var htmlIncludeBranding by remember { mutableStateOf(true) }
    // v1.87: export-style plugin pack id ("" = default styling).
    var htmlExportStyleId by remember { mutableStateOf("") }
    val exportStyleOptions by rememberPluginExportStyleOptions()
    val exportStyleCss =
        com.quotebrowser.app.data.plugins.PluginManager.exportStyles[htmlExportStyleId]?.css.orEmpty()
    val slideshowEnabled by vm.slideshowExportEnabled.collectAsStateWithLifecycle()
    var sharingStrip by remember { mutableStateOf(false) }
    // v1.88.34: strip chat-style username prefixes in bulk TXT exports.
    var stripUsernames by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val basketCount by vm.basketCount.collectAsStateWithLifecycle()

    val categories by catVm.categories.collectAsStateWithLifecycle()
    var selectedCats by remember { mutableStateOf(setOf<Long>()) }
    var exportingCats by remember { mutableStateOf(false) }

    // Favorites folder filter for the export scope.
    val favoriteFolders by vm.favoriteFolders.collectAsStateWithLifecycle()
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var folderMenuExpanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) { catVm.refresh() }

    val dirPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // not fatal; proceed anyway
            }
            exporting = true
            resultMessage = null
            scope.launch {
                val result = if (exportScope == ExportScope.BASKET) {
                    performBasketExport(
                        context, vm, uri, format, mode, narrate, videoBg, karaoke,
                        com.quotebrowser.app.ui.layouts.HtmlExportOptions(
                            includeUrl = htmlIncludeUrl,
                            includeRating = htmlIncludeRating,
                            includeDate = htmlIncludeDate,
                            includeCategories = htmlIncludeCategories,
                            includeBranding = htmlIncludeBranding
                        ),
                        exportStyleCss = exportStyleCss,
                        stripUsernames = stripUsernames
                    )
                } else {
                    performExport(
                        context, vm, uri, format, exportScope, mode, selectedFolder,
                        narrate, videoBg, karaoke,
                        // v1.85: HTML export element toggles.
                        com.quotebrowser.app.ui.layouts.HtmlExportOptions(
                            includeUrl = htmlIncludeUrl,
                            includeRating = htmlIncludeRating,
                            includeDate = htmlIncludeDate,
                            includeCategories = htmlIncludeCategories,
                            includeBranding = htmlIncludeBranding
                        ),
                        exportStyleCss = exportStyleCss,
                        stripUsernames = stripUsernames
                    )
                }
                exporting = false
                resultMessage = result
            }
        }
    }

    val catDirPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // not fatal; proceed anyway
            }
            exportingCats = true
            resultMessage = null
            scope.launch {
                val result = performCategoryExport(
                    context, vm, uri, format, selectedCats, narrate, videoBg, karaoke,
                    com.quotebrowser.app.ui.layouts.HtmlExportOptions(
                        includeUrl = htmlIncludeUrl,
                        includeRating = htmlIncludeRating,
                        includeDate = htmlIncludeDate,
                        includeCategories = htmlIncludeCategories,
                        includeBranding = htmlIncludeBranding
                    ),
                    exportStyleCss = exportStyleCss,
                    stripUsernames = stripUsernames
                )
                exportingCats = false
                resultMessage = result
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EXPORT QUOTES",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Format", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ExportFormat.values()
                    // v1.49: the slideshow format only shows when enabled.
                    .filter { it != ExportFormat.MP4 || slideshowEnabled }
                    .forEach { f ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = format == f,
                                onClick = {
                                    format = f
                                    // PNG only makes sense per-quote;
                                    // MP4 is always one video file.
                                    if (f == ExportFormat.PNG) mode = ExportMode.PER_QUOTE
                                    if (f == ExportFormat.MP4) mode = ExportMode.SINGLE
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == f,
                            onClick = {
                                format = f
                                if (f == ExportFormat.PNG) mode = ExportMode.PER_QUOTE
                                if (f == ExportFormat.MP4) mode = ExportMode.SINGLE
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(f.label)
                    }
                }
                // v1.49: optional narration for the slideshow video.
                if (format == ExportFormat.MP4) {                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = narrate,
                            onCheckedChange = { narrate = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Narrate each quote aloud")
                    }
                    // v1.63: karaoke word highlighting follows the narration.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = karaoke && narrate,
                            enabled = narrate,
                            onCheckedChange = { karaoke = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Highlight each word as it is read",
                            color = if (narrate) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // v1.63: animated background choice.
                    var bgExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = bgExpanded,
                        onExpandedChange = { bgExpanded = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = videoBg.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Video background") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = bgExpanded,
                            onDismissRequest = { bgExpanded = false }
                        ) {
                            VideoBackground.entries.forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg.label) },
                                    onClick = {
                                        videoBg = bg
                                        bgExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        "One slide per quote (5 seconds each, or as long as " +
                            "its narration). Rendering takes a while for " +
                            "large exports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                    )
                }
                // v1.74.5: narrated audio always reads aloud; note the voice.
                if (format == ExportFormat.M4A) {
                    Text(
                        "Read aloud with your device's default text-to-speech voice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // v1.85: HTML export element toggles — applied on top of
                // the active quote layout at export time.
                if (format == ExportFormat.HTML) {
                    Text(
                        "Styled by your active quote layout (Settings → Quote layouts).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    @Composable
                    fun HtmlToggle(
                        label: String,
                        checked: Boolean,
                        onCheckedChange: (Boolean) -> Unit
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = onCheckedChange
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                    HtmlToggle("Include web address link", htmlIncludeUrl) { htmlIncludeUrl = it }
                    HtmlToggle("Include rating", htmlIncludeRating) { htmlIncludeRating = it }
                    HtmlToggle("Include date", htmlIncludeDate) { htmlIncludeDate = it }
                    HtmlToggle("Include categories", htmlIncludeCategories) { htmlIncludeCategories = it }
                    HtmlToggle("Include site branding", htmlIncludeBranding) { htmlIncludeBranding = it }
                    // v1.87: export-style plugin picker.
                    var styleExpanded by remember { mutableStateOf(false) }
                    val styleLabel = if (htmlExportStyleId.isBlank()) "Default"
                    else com.quotebrowser.app.data.plugins.PluginManager.pluginExportStyleDisplayName(
                        htmlExportStyleId
                    )
                    ExposedDropdownMenuBox(
                        expanded = styleExpanded,
                        onExpandedChange = { styleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = styleLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Export style") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(top = 8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = styleExpanded,
                            onDismissRequest = { styleExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default (active quote layout)") },
                                onClick = { htmlExportStyleId = ""; styleExpanded = false }
                            )
                            exportStyleOptions.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = { htmlExportStyleId = id; styleExpanded = false }
                                )
                            }
                        }
                    }
                    if (exportStyleOptions.isEmpty()) {
                        Text(
                            "Install an export-style plugin pack to add more styles here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // v1.88.34: strip chat-style username prefixes (<nick>, nick:) in
        // bulk TXT exports. Text formats only — other formats are untouched.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = stripUsernames,
                        onCheckedChange = { stripUsernames = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Strip usernames (<nick>, nick:)")
                }
                Text(
                    "Removes chat-style username prefixes from each line. TXT exports only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("What to export", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ExportScope.values().forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = exportScope == s,
                                onClick = { exportScope = s }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = exportScope == s,
                            onClick = { exportScope = s }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (s == ExportScope.BASKET) "Export tray ($basketCount)"
                            else s.label
                        )
                    }
                }
            }
        }

        if (exportScope == ExportScope.FAVORITES && favoriteFolders.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Favorites folder", fontWeight = FontWeight.Bold)
                        Text(
                            "Export only the favorites in one folder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.foundation.layout.Box {
                        OutlinedButton(onClick = { folderMenuExpanded = true }) {
                            Text(selectedFolder ?: "All", maxLines = 1)
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Choose favorites folder"
                            )
                        }
                        DropdownMenu(
                            expanded = folderMenuExpanded,
                            onDismissRequest = { folderMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All folders") },
                                onClick = {
                                    folderMenuExpanded = false
                                    selectedFolder = null
                                }
                            )
                            favoriteFolders.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        folderMenuExpanded = false
                                        selectedFolder = name
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // v1.49: MP4 is always one video file, so the layout card is moot.
        if (format != ExportFormat.PNG && format != ExportFormat.MP4) {
            Card(            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
                Column(Modifier.padding(16.dp)) {
                    Text("File layout", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    ExportMode.values().forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = mode == m,
                                    onClick = { mode = m }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == m,
                                onClick = { mode = m }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(m.label)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Image strip (PNG)", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Renders the quotes selected above as one tall image " +
                        "— share cards stacked with dividers (up to " +
                        "$MAX_QUOTE_STRIP_QUOTES) — and opens the share sheet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        sharingStrip = true
                        scope.launch {
                            try {
                                val quotes = if (exportScope == ExportScope.BASKET) {
                                    vm.basketQuotes().map { it.quote }
                                } else {
                                    val scopeStr = when (exportScope) {
                                        ExportScope.ALL -> "all"
                                        ExportScope.FAVORITES -> "favorites"
                                        ExportScope.HISTORY -> "history"
                                        ExportScope.BASKET -> "basket"
                                    }
                                    vm.getQuotesForExport(scopeStr, selectedFolder)
                                }
                                renderAndShareQuoteStrip(context, quotes)
                            } finally {
                                sharingStrip = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !sharingStrip
                ) {
                    if (sharingStrip) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(if (sharingStrip) "Rendering…" else "Share image strip")
                }
            }
        }

        Button(
            onClick = { dirPicker.launch(null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !exporting
        ) {
            if (exporting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(if (exporting) "Exporting…" else "Choose folder and export")
        }

        resultMessage?.let { msg ->
            Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.startsWith("Exported")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Export by category", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Saves one file per selected category (a folder per " +
                        "category for PNG) inside a categories/ folder. The share " +
                        "icon on a row sends that category as a single " +
                        "\".qbcollection.json\" collection file that another " +
                        "QuoteBrowser can import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                if (categories.isEmpty()) {
                    Text(
                        text = "No categories yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = cat.id in selectedCats,
                                    onClick = {
                                        selectedCats =
                                            if (cat.id in selectedCats) selectedCats - cat.id
                                            else selectedCats + cat.id
                                    }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = cat.id in selectedCats,
                                onCheckedChange = { on ->
                                    selectedCats =
                                        if (on) selectedCats + cat.id
                                        else selectedCats - cat.id
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${cat.name} (${cat.quoteCount})",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val quotes = withContext(Dispatchers.IO) {
                                            vm.getQuotesForCategory(cat.id)
                                        }
                                        if (quotes.isEmpty()) {
                                            resultMessage =
                                                "\"${cat.name}\" has no quotes to share."
                                        } else {
                                            shareCollection(
                                                context,
                                                QuoteCollection(
                                                    name = cat.name,
                                                    source = "category",
                                                    quotes = quotes.map { q ->
                                                        CollectionQuote(
                                                            q.number,
                                                            q.text,
                                                            q.rating
                                                        )
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = "Share as collection file"
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { catDirPicker.launch(null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !exportingCats && selectedCats.isNotEmpty()
                ) {
                    if (exportingCats) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        if (exportingCats) "Exporting…"
                        else "Choose folder and export ${selectedCats.size} categor" +
                            if (selectedCats.size == 1) "y" else "ies"
                    )
                }
            }
        }

        Text(
            text = "Exports are saved in a subfolder named by format (e.g. txt/, json/, png/) inside the folder you choose.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Sanitizes a category name for use as a folder/file name. */
private fun sanitizeFileName(name: String): String =
    name.map { c ->
        if (c.isLetterOrDigit() || c == ' ' || c == '-' || c == '_') c else '_'
    }.joinToString("").trim().take(48).ifEmpty { "category" }

/**
 * Exports each selected category into its own folder under categories/ in the
 * chosen folder: one file per category (one file per quote for PNG).
 */
private suspend fun performCategoryExport(
    context: Context,
    vm: MainViewModel,
    treeUri: Uri,
    format: ExportFormat,
    categoryIds: Set<Long>,
    narrate: Boolean = false,
    background: VideoBackground = VideoBackground.MIDNIGHT,
    karaoke: Boolean = true,
    // v1.85: HTML export element toggles.
    htmlOptions: com.quotebrowser.app.ui.layouts.HtmlExportOptions =
        com.quotebrowser.app.ui.layouts.HtmlExportOptions(),
    // v1.87: export-style plugin CSS layered over the layout CSS.
    exportStyleCss: String = "",
    // v1.88.34: strip chat-style username prefixes in TXT exports.
    stripUsernames: Boolean = false
): String = withContext(Dispatchers.IO) {
    try {
        val cats = vm.listCategories().filter { it.id in categoryIds }
        if (cats.isEmpty()) {
            return@withContext "No categories selected."
        }

        val resolver = context.contentResolver
        val parentUri = DocumentsContract.createDocument(
            resolver,
            treeUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            "categories"
        ) ?: return@withContext "Could not create categories/ folder."

        val mime = exportMimeFor(format)

        // v1.61.8: Clint's Quote404 logo watermark for the PNG exports.
        val watermark = quote404WatermarkLogo(context)

        var quoteTotal = 0
        var catCount = 0
        // v1.74.6: first audio failure reason, for the summary line.
        var audioReason: String? = null
        for (cat in cats) {
            val quotes = vm.getQuotesForCategory(cat.id)
            if (quotes.isEmpty()) continue
            val folderUri = DocumentsContract.createDocument(
                resolver,
                parentUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                sanitizeFileName(cat.name)
            ) ?: continue
            if (format == ExportFormat.PNG) {
                for (q in quotes) {
                    val fileUri = DocumentsContract.createDocument(
                        resolver, folderUri, "image/png", "quote_${q.number}.png"
                    ) ?: continue
                    resolver.openOutputStream(fileUri)?.use { out ->
                        val bitmap = renderQuoteAsPng(q, watermark = watermark)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        bitmap.recycle()
                    }
                    quoteTotal++
                }
            } else if (format == ExportFormat.MP4) {
                // v1.49: one slideshow video per category.
                if (writeSlideshowToUri(
                        context, resolver, folderUri, "slideshow.mp4",
                        quotes.map { SlideItem(it, cat.name) }, narrate,
                        background, karaoke
                    )
                ) {
                    quoteTotal += quotes.size
                }
            } else if (format == ExportFormat.M4A) {
                // v1.74.5: one narrated audio file per category.
                // v1.74.6: keep the first failure reason for the summary.
                if (writeAudioToUri(
                        context, resolver, folderUri, "quotes.m4a", quotes,
                        onError = { if (audioReason == null) audioReason = it }
                    )
                ) {
                    quoteTotal += quotes.size
                }
            } else if (format == ExportFormat.HTML) {
                // v1.85: one styled web page per category, using the
                // current database's layout and the export-dialog toggles.
                val dbId = vm.currentDbId.value
                val layout = com.quotebrowser.app.data.layouts.LayoutStore.layoutForDb(
                    context, dbId,
                    com.quotebrowser.app.data.plugins.PluginManager.pluginLayouts
                )
                val siteName = vm.labelForDb(dbId)
                val contexts = quotes.map { q ->
                    com.quotebrowser.app.ui.layouts.QuoteExportContext(
                        quote = q,
                        siteName = siteName,
                        categories = if (htmlOptions.includeCategories)
                            listOf(cat.name) else emptyList()
                    )
                }
                val html = com.quotebrowser.app.ui.layouts.renderQuotesPageHtml(
                    "Quotes in category \"${cat.name}\"",
                    contexts, layout, htmlOptions,
                    // v1.86: classified per-category quote-text spans.
                    vm.htmlExportTextStyle(),
                    // v1.87: export-style plugin CSS.
                    extraCss = exportStyleCss
                )
                val fileUri = DocumentsContract.createDocument(
                    resolver, folderUri, "text/html", "quotes.html"
                ) ?: continue
                resolver.openOutputStream(fileUri)?.use { out ->
                    out.write(html.toByteArray(Charsets.UTF_8))
                }
                quoteTotal += quotes.size
            } else {
                val fileUri = DocumentsContract.createDocument(
                    resolver, folderUri, mime, "quotes.${format.ext}"
                ) ?: continue
                resolver.openOutputStream(fileUri)?.use { out ->
                    val binary = renderBinaryExport(
                        format, quotes, "Quotes in category \"${cat.name}\""
                    )
                    if (binary != null) {
                        out.write(binary)
                    } else if (format == ExportFormat.ANKI) {
                        val dbId = vm.currentDbId.value
                        writeAnkiTsv(
                            out,
                            quotes.map {
                                com.quotebrowser.app.data.SourcedQuote(dbId, it)
                            }
                        ) { vm.labelForDb(it) }
                    } else {
                        // v1.88.34: strip username prefixes for TXT text output only.
                        val txtQuotes = if (stripUsernames) quotes.map {
                            it.copy(text = com.quotebrowser.app.ui.stripUsernames(it.text))
                        } else quotes
                        val content = when (format) {
                            ExportFormat.TXT -> formatQuotesForExport(
                                txtQuotes, "Quotes in category \"${cat.name}\""
                            )
                            ExportFormat.JSON -> quotesToJson(quotes)
                            ExportFormat.XML -> quotesToXml(quotes)
                            ExportFormat.CSV -> quotesToCsv(quotes)
                            else -> ""
                        }
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
                quoteTotal += quotes.size
            }
            catCount++
        }
        if (catCount == 0) {
            "No quotes to export in the selected categories."
        } else if (format == ExportFormat.M4A && quoteTotal == 0 && audioReason != null) {
            // v1.74.6: every category's audio export failed — say why.
            "Audio export failed: $audioReason"
        } else {
            "Exported $quoteTotal quotes across $catCount categor" +
                (if (catCount == 1) "y" else "ies") + " to categories/ folder."
        }
    } catch (e: Exception) {
        "Export failed: ${e.message}"
    }
}

/**
 * Performs the export. Returns a user-facing result message.
 */
private suspend fun performExport(
    context: Context,
    vm: MainViewModel,
    treeUri: Uri,
    format: ExportFormat,
    scope: ExportScope,
    mode: ExportMode,
    folder: String? = null,
    narrate: Boolean = false,
    background: VideoBackground = VideoBackground.MIDNIGHT,
    karaoke: Boolean = true,
    // v1.85: HTML export element toggles.
    htmlOptions: com.quotebrowser.app.ui.layouts.HtmlExportOptions =
        com.quotebrowser.app.ui.layouts.HtmlExportOptions(),
    // v1.87: export-style plugin CSS layered over the layout CSS.
    exportStyleCss: String = "",
    // v1.88.34: strip chat-style username prefixes in TXT exports.
    stripUsernames: Boolean = false
): String = withContext(Dispatchers.IO) {
    try {
        val scopeStr = when (scope) {
            ExportScope.ALL -> "all"
            ExportScope.FAVORITES -> "favorites"
            ExportScope.HISTORY -> "history"
            ExportScope.BASKET -> "basket" // handled by performBasketExport; never reached here
        }
        val quotes = vm.getQuotesForExport(scopeStr, folder)
        if (quotes.isEmpty()) {
            return@withContext "No quotes to export."
        }

        val resolver = context.contentResolver
        // Create a subfolder for the format inside the chosen folder.
        val subfolderUri = DocumentsContract.createDocument(
            resolver,
            treeUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            format.ext
        ) ?: return@withContext "Could not create ${format.ext}/ subfolder."

        // v1.61.8: Clint's Quote404 logo watermark for the PNG exports.
        val watermark = quote404WatermarkLogo(context)

        var count = 0
        if (format == ExportFormat.MP4) {
            // v1.49: slideshow video — always a single MP4 file.
            val dbLabel = vm.labelForDb(vm.currentDbId.value)
            val ok = writeSlideshowToUri(
                context, resolver, subfolderUri, "quotes_${scopeStr}.mp4",
                quotes.map { SlideItem(it, dbLabel) }, narrate,
                background, karaoke
            )
            count = if (ok) quotes.size else 0
            return@withContext if (ok) "Exported $count quotes to ${format.ext}/ folder as a slideshow video."
            else "Could not create output file."
        }
        if (format == ExportFormat.M4A) {
            // v1.74.5: narrated audio — one M4A per quote, or a single
            // file when the mode is SINGLE.
            // v1.74.6: surface the real failure reason.
            if (mode == ExportMode.SINGLE) {
                var reason: String? = null
                val ok = writeAudioToUri(
                    context, resolver, subfolderUri, "quotes_${scopeStr}.m4a", quotes,
                    onError = { reason = it }
                )
                count = if (ok) quotes.size else 0
                return@withContext if (ok) "Exported $count quotes to ${format.ext}/ folder as narrated audio."
                else "Audio export failed: ${reason ?: "could not create output file."}"
            }
            var reason: String? = null
            for (q in quotes) {
                val ok = writeAudioToUri(
                    context, resolver, subfolderUri, "quote_${q.number}.m4a", listOf(q),
                    onError = { if (reason == null) reason = it }
                )
                if (ok) count++
            }
            return@withContext if (count > 0 || reason == null) "Exported $count quotes to ${format.ext}/ folder as narrated audio."
            else "Audio export failed: $reason"
        }
        if (format == ExportFormat.HTML) {
            // v1.85: standalone web page, styled by the active quote
            // layout (per-db override or global). Export-dialog toggles
            // ride on top of the layout.
            val dbId = vm.currentDbId.value
            val layout = com.quotebrowser.app.data.layouts.LayoutStore.layoutForDb(
                context, dbId, com.quotebrowser.app.data.plugins.PluginManager.pluginLayouts
            )
            val siteName = vm.labelForDb(dbId)
            suspend fun ctxFor(q: com.quotebrowser.app.data.Quote) =
                com.quotebrowser.app.ui.layouts.QuoteExportContext(
                    quote = q,
                    siteName = siteName,
                    categories = if (htmlOptions.includeCategories)
                        vm.categoryNamesForQuote(dbId, q.number) else emptyList()
                )
            if (mode == ExportMode.SINGLE) {
                val html = com.quotebrowser.app.ui.layouts.renderQuotesPageHtml(
                    "Quote Export", quotes.map { ctxFor(it) }, layout, htmlOptions,
                    extraCss = exportStyleCss
                )
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, "text/html", "quotes_${scopeStr}.html"
                ) ?: return@withContext "Could not create output file."
                resolver.openOutputStream(fileUri)?.use { out ->
                    out.write(html.toByteArray(Charsets.UTF_8))
                }
                count = quotes.size
            } else {
                for (q in quotes) {
                    val html = com.quotebrowser.app.ui.layouts.renderQuotesPageHtml(
                        "Quote #${q.number}", listOf(ctxFor(q)), layout, htmlOptions,
                        extraCss = exportStyleCss
                    )
                    val fileUri = DocumentsContract.createDocument(
                        resolver, subfolderUri, "text/html", "quote_${q.number}.html"
                    ) ?: continue
                    resolver.openOutputStream(fileUri)?.use { out ->
                        out.write(html.toByteArray(Charsets.UTF_8))
                    }
                    count++
                }
            }
            return@withContext "Exported $count quotes to ${format.ext}/ folder as web pages."
        }
        if (format == ExportFormat.PNG) {
            // PNG: always one file per quote
            for (q in quotes) {
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, "image/png", "quote_${q.number}.png"
                ) ?: continue
                resolver.openOutputStream(fileUri)?.use { out ->
                    val bitmap = renderQuoteAsPng(q, watermark = watermark)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    bitmap.recycle()
                }
                count++
            }
        } else if (mode == ExportMode.SINGLE) {
            // Single file with all quotes
            val binary = renderBinaryExport(format, quotes, "Quote Export")
            if (binary != null) {
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, exportMimeFor(format), "quotes_${scopeStr}.${format.ext}"
                ) ?: return@withContext "Could not create output file."
                resolver.openOutputStream(fileUri)?.use { out -> out.write(binary) }
                count = quotes.size
            } else {
                // v1.88.34: strip username prefixes for TXT text output only.
                val txtQuotes = if (stripUsernames) quotes.map {
                    it.copy(text = com.quotebrowser.app.ui.stripUsernames(it.text))
                } else quotes
                val content = when (format) {
                    ExportFormat.TXT -> formatQuotesForExport(txtQuotes, "Quote Export")
                    ExportFormat.JSON -> quotesToJson(quotes)
                    ExportFormat.XML -> quotesToXml(quotes)
                    ExportFormat.CSV -> quotesToCsv(quotes)
                    else -> ""
                }
                val mime = when (format) {
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.XML -> "text/xml"
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.ANKI -> "text/tab-separated-values"
                    else -> "text/plain"
                }
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, mime, "quotes_${scopeStr}.${format.ext}"
                ) ?: return@withContext "Could not create output file."
                resolver.openOutputStream(fileUri)?.use { out ->
                    if (format == ExportFormat.ANKI) {
                        val dbId = vm.currentDbId.value
                        writeAnkiTsv(
                            out,
                            quotes.map {
                                com.quotebrowser.app.data.SourcedQuote(dbId, it)
                            }
                        ) { vm.labelForDb(it) }
                    } else {
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
                count = quotes.size
            }
        } else {
            // One file per quote
            for (q in quotes) {
                val binary = renderBinaryExport(format, listOf(q), "Quote #${q.number}")
                if (binary != null) {
                    val fileUri = DocumentsContract.createDocument(
                        resolver, subfolderUri, exportMimeFor(format), "quote_${q.number}.${format.ext}"
                    ) ?: continue
                    resolver.openOutputStream(fileUri)?.use { out -> out.write(binary) }
                    count++
                    continue
                }
                val content = when (format) {
                    // v1.88.34: strip username prefixes for TXT text output only.
                    ExportFormat.TXT -> quoteToText(
                        if (stripUsernames) q.copy(
                            text = com.quotebrowser.app.ui.stripUsernames(q.text)
                        ) else q
                    )
                    ExportFormat.JSON -> quotesToJson(listOf(q))
                    ExportFormat.XML -> quotesToXml(listOf(q))
                    ExportFormat.CSV -> quotesToCsv(listOf(q))
                    else -> ""
                }
                val mime = when (format) {
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.XML -> "text/xml"
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.ANKI -> "text/tab-separated-values"
                    else -> "text/plain"
                }
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, mime, "quote_${q.number}.${format.ext}"
                ) ?: continue
                resolver.openOutputStream(fileUri)?.use { out ->
                    if (format == ExportFormat.ANKI) {
                        val dbId = vm.currentDbId.value
                        writeAnkiTsv(
                            out,
                            listOf(com.quotebrowser.app.data.SourcedQuote(dbId, q))
                        ) { vm.labelForDb(it) }
                    } else {
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
                count++
            }
        }
        "Exported $count quotes to ${format.ext}/ folder."
    } catch (e: Exception) {
        "Export failed: ${e.message}"
    }
}

/** Exports the export-queue contents; quotes can come from several databases. */
private suspend fun performBasketExport(
    context: Context,
    vm: MainViewModel,
    treeUri: Uri,
    format: ExportFormat,
    mode: ExportMode,
    narrate: Boolean = false,
    background: VideoBackground = VideoBackground.MIDNIGHT,
    karaoke: Boolean = true,
    // v1.85: HTML export element toggles.
    htmlOptions: com.quotebrowser.app.ui.layouts.HtmlExportOptions =
        com.quotebrowser.app.ui.layouts.HtmlExportOptions(),
    // v1.87: export-style plugin CSS layered over the layout CSS.
    exportStyleCss: String = "",
    // v1.88.34: strip chat-style username prefixes in TXT exports.
    stripUsernames: Boolean = false
): String = withContext(Dispatchers.IO) {
    try {
        val quotes = vm.basketQuotes()
        if (quotes.isEmpty()) {
            return@withContext "Export tray is empty."
        }
        val labelFor: (String) -> String = { vm.labelForDb(it) }

        val resolver = context.contentResolver
        val subfolderUri = DocumentsContract.createDocument(
            resolver,
            treeUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            format.ext
        ) ?: return@withContext "Could not create ${format.ext}/ subfolder."

        // v1.61.8: Clint's Quote404 logo watermark for the PNG exports.
        val watermark = quote404WatermarkLogo(context)

        fun fileStem(sq: com.quotebrowser.app.data.SourcedQuote): String {
            val safe = labelFor(sq.dbId).replace(Regex("[^A-Za-z0-9]+"), "_")
                .trim('_').ifEmpty { "db" }
            return "quote_${safe}_${sq.quote.number}"
        }

        var count = 0
        // v1.74.6: first audio failure reason, for the summary line.
        var audioReason: String? = null
        if (format == ExportFormat.PNG) {
            for (sq in quotes) {
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, "image/png", "${fileStem(sq)}.png"
                ) ?: continue
                resolver.openOutputStream(fileUri)?.use { out ->
                    val bitmap = renderQuoteAsPng(sq.quote, watermark = watermark)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    bitmap.recycle()
                }
                count++
            }
        } else if (format == ExportFormat.HTML) {
            // v1.85: web pages. Single-file mode uses the current
            // database's layout for the whole page; per-quote mode uses
            // each quote's own database layout.
            // v1.86: quote text renders with classified per-category
            // spans (qb-said-name, qb-said-text, qb-action-line, …).
            val textStyle = vm.htmlExportTextStyle()
            val pluginLayouts = com.quotebrowser.app.data.plugins.PluginManager.pluginLayouts
            suspend fun ctxFor(sq: com.quotebrowser.app.data.SourcedQuote) =
                com.quotebrowser.app.ui.layouts.QuoteExportContext(
                    quote = sq.quote,
                    siteName = labelFor(sq.dbId),
                    categories = if (htmlOptions.includeCategories)
                        vm.categoryNamesForQuote(sq.dbId, sq.quote.number) else emptyList()
                )
            if (mode == ExportMode.SINGLE) {
                val layout = com.quotebrowser.app.data.layouts.LayoutStore.layoutForDb(
                    context, vm.currentDbId.value, pluginLayouts
                )
                val html = com.quotebrowser.app.ui.layouts.renderQuotesPageHtml(
                    "Quote Export", quotes.map { ctxFor(it) }, layout, htmlOptions,
                    textStyle,
                    // v1.87: export-style plugin CSS.
                    extraCss = exportStyleCss
                )
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, "text/html", "quotes_basket.html"
                ) ?: return@withContext "Could not create output file."
                resolver.openOutputStream(fileUri)?.use { out ->
                    out.write(html.toByteArray(Charsets.UTF_8))
                }
                count = quotes.size
            } else {
                for (sq in quotes) {
                    val layout = com.quotebrowser.app.data.layouts.LayoutStore.layoutForDb(
                        context, sq.dbId, pluginLayouts
                    )
                    val html = com.quotebrowser.app.ui.layouts.renderQuotesPageHtml(
                        "Quote #${sq.quote.number}", listOf(ctxFor(sq)),
                        layout, htmlOptions, textStyle,
                        // v1.87: export-style plugin CSS.
                        extraCss = exportStyleCss
                    )
                    val fileUri = DocumentsContract.createDocument(
                        resolver, subfolderUri, "text/html", "${fileStem(sq)}.html"
                    ) ?: continue
                    resolver.openOutputStream(fileUri)?.use { out ->
                        out.write(html.toByteArray(Charsets.UTF_8))
                    }
                    count++
                }
            }
            return@withContext "Exported $count quotes to ${format.ext}/ folder as web pages."
        } else if (mode == ExportMode.SINGLE) {
            // v1.49: slideshow video for the export queue.
            if (format == ExportFormat.MP4) {
                val ok = writeSlideshowToUri(
                    context, resolver, subfolderUri, "quotes_basket.mp4",
                    quotes.map { SlideItem(it.quote, labelFor(it.dbId)) }, narrate,
                    background, karaoke
                )
                count = if (ok) quotes.size else 0
                return@withContext if (ok) "Exported $count quotes to ${format.ext}/ folder as a slideshow video."
                else "Could not create output file."
            }
            // v1.74.5: narrated audio for the export queue.
            // v1.74.6: surface the real failure reason.
            if (format == ExportFormat.M4A) {
                var reason: String? = null
                val ok = writeAudioToUri(
                    context, resolver, subfolderUri, "quotes_basket.m4a",
                    quotes.map { it.quote },
                    onError = { reason = it }
                )
                count = if (ok) quotes.size else 0
                return@withContext if (ok) "Exported $count quotes to ${format.ext}/ folder as narrated audio."
                else "Audio export failed: ${reason ?: "could not create output file."}"
            }
            val binary = renderBinaryExport(format, quotes.map { it.quote }, "Quote Export")
            val fileUri = DocumentsContract.createDocument(
                resolver, subfolderUri,
                exportMimeFor(format),
                "quotes_basket.${format.ext}"
            ) ?: return@withContext "Could not create output file."
            resolver.openOutputStream(fileUri)?.use { out ->
                if (binary != null) {
                    out.write(binary)
                } else if (format == ExportFormat.ANKI) {
                    writeAnkiTsv(out, quotes, labelFor)
                } else {
                    // v1.88.34: strip username prefixes for TXT text output only.
                    val txtQuotes = if (stripUsernames) quotes.map { sq ->
                        sq.copy(
                            quote = sq.quote.copy(
                                text = com.quotebrowser.app.ui.stripUsernames(sq.quote.text)
                            )
                        )
                    } else quotes
                    val content = when (format) {
                        ExportFormat.TXT -> formatSourcedQuotesForExport(txtQuotes, "Quote Export", labelFor)
                        ExportFormat.JSON -> quotesToJson(quotes, labelFor)
                        ExportFormat.XML -> quotesToXml(quotes, labelFor)
                        ExportFormat.CSV -> quotesToCsv(quotes, labelFor)
                        else -> ""
                    }
                    out.write(content.toByteArray(Charsets.UTF_8))
                }
            }
            count = quotes.size
        } else {
            for (sq in quotes) {
                // v1.74.5: one narrated M4A per quote.
                if (format == ExportFormat.M4A) {
                    if (writeAudioToUri(
                            context, resolver, subfolderUri,
                            "${fileStem(sq)}.m4a", listOf(sq.quote),
                            onError = { if (audioReason == null) audioReason = it }
                        )
                    ) count++
                    continue
                }
                val binary = renderBinaryExport(
                    format, listOf(sq.quote), "Quote #${sq.quote.number}"
                )
                if (binary != null) {
                    val fileUri = DocumentsContract.createDocument(
                        resolver, subfolderUri, exportMimeFor(format),
                        "${fileStem(sq)}.${format.ext}"
                    ) ?: continue
                    resolver.openOutputStream(fileUri)?.use { out -> out.write(binary) }
                    count++
                    continue
                }
                val content = when (format) {
                    // v1.88.34: strip username prefixes for TXT text output only.
                    ExportFormat.TXT -> quoteToText(
                        if (stripUsernames) sq.copy(
                            quote = sq.quote.copy(
                                text = com.quotebrowser.app.ui.stripUsernames(sq.quote.text)
                            )
                        ) else sq,
                        labelFor
                    )
                    ExportFormat.JSON -> quotesToJson(listOf(sq), labelFor)
                    ExportFormat.XML -> quotesToXml(listOf(sq), labelFor)
                    ExportFormat.CSV -> quotesToCsv(listOf(sq), labelFor)
                    else -> ""
                }
                val mime = when (format) {
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.XML -> "text/xml"
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.ANKI -> "text/tab-separated-values"
                    else -> "text/plain"
                }
                val fileUri = DocumentsContract.createDocument(
                    resolver, subfolderUri, mime, "${fileStem(sq)}.${format.ext}"
                ) ?: continue
                resolver.openOutputStream(fileUri)?.use { out ->
                    if (format == ExportFormat.ANKI) {
                        writeAnkiTsv(out, listOf(sq), labelFor)
                    } else {
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
                count++
            }
        }
        // v1.86: the export tray is staging — it persists until the user
        // clears it from the tray screen.
        // v1.74.6: when every per-quote audio export failed, say why.
        if (format == ExportFormat.M4A && count == 0 && audioReason != null) {
            "Audio export failed: $audioReason"
        } else {
            "Exported $count tray quotes to ${format.ext}/ folder."
        }
    } catch (e: Exception) {
        "Export failed: ${e.message}"
    }
}

/** Renders a quote as a PNG Bitmap. */
fun renderQuoteAsPng(
    quote: Quote,
    width: Int = 1080,
    showNumber: Boolean = true,
    /**
     * v1.61.8: Clint's Quote404 logo, drawn as a faint centered watermark
     * over the finished image. Null = no watermark.
     */
    watermark: Bitmap? = null
): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 48f
    }
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFBB86FC.toInt()
        textSize = 56f
        isFakeBoldText = true
    }
    // Simple word wrap, preserving the quote's own line breaks.
    // v1.88.21: "mind breaks" — each line is its own paragraph, and blank
    // paragraphs are kept as empty lines so the vertical breathing room
    // survives the wrap (matches the on-screen cards).
    val lines = mutableListOf<String>()
    for (paragraph in paragraphizeBody(quote.text).split("\n")) {
        var currentLine = ""
        var added = false
        for (word in paragraph.split(" ").filter { it.isNotEmpty() }) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > width - 80) {
                lines.add(currentLine)
                added = true
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
            added = true
        }
        if (!added) lines.add("")
    }

    val lineHeight = 70
    val titleHeight = if (showNumber) 200 else 80
    // v1.61.15: slim Quote404 logo plate flush at the top — no floating
    // strip — with the logo large enough to read.
    val plateH =
        quote404LogoPlateH(watermark, width.toFloat()).toInt()
    val height = plateH + titleHeight + lines.size * lineHeight
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(0xFF121212.toInt())
    // v1.61.15: the logo plate replaces the old strip.
    canvas.drawQuote404LogoPlate(watermark, plateH = plateH.toFloat())

    var y = 120f + plateH
    if (showNumber) {
        canvas.drawText("#${quote.number}", 40f, y, titlePaint)
        y += 80f
    }
    for (line in lines) {
        canvas.drawText(line, 40f, y, paint)
        y += lineHeight
    }
    return bitmap
}

/** Formats quotes as JSON. */
fun quotesToJson(quotes: List<Quote>): String {
    val array = JSONArray()
    for (q in quotes) {
        array.put(JSONObject().apply {
            put("number", q.number)
            put("rating", q.rating ?: JSONObject.NULL)
            put("text", q.text)
            put("url", q.url)
        })
    }
    return array.toString(2)
}

/** Formats quotes as XML. */
fun quotesToXml(quotes: List<Quote>): String = buildString {
    appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    appendLine("<quotes count=\"${quotes.size}\" exported=\"${LocalDate.now()}\">")
    for (q in quotes) {
        appendLine("  <quote number=\"${q.number}\" rating=\"${q.rating ?: ""}\">")
        appendLine("    <text><![CDATA[${q.text}]]></text>")
        appendLine("    <url>${q.url}</url>")
        appendLine("  </quote>")
    }
    appendLine("</quotes>")
}

/** Formats quotes as CSV. */
fun quotesToCsv(quotes: List<Quote>): String = buildString {
    appendLine("number,rating,text,url")
    for (q in quotes) {
        val text = "\"" + q.text.replace("\"", "\"\"") + "\""
        val url = "\"" + q.url.replace("\"", "\"\"") + "\""
        appendLine("${q.number},${q.rating ?: ""},$text,$url")
    }
}

/** Formats a single quote as text. */
fun quoteToText(q: Quote): String = buildString {
    appendLine("#${q.number} (rating ${q.rating?.toString() ?: "unrated"})")
    appendLine()
    appendLine(q.text)
    appendLine()
    appendLine(q.url)
}

/** Formats sourced quotes (possibly from several databases) as text. */
fun formatSourcedQuotesForExport(
    quotes: List<com.quotebrowser.app.data.SourcedQuote>,
    title: String,
    labelFor: (String) -> String
): String = buildString {
    appendLine(title)
    appendLine("Exported ${LocalDate.now()} · ${quotes.size} quotes")
    quotes.forEach { sq ->
        appendLine()
        appendLine("------------------------")
        appendLine(
            "[${labelFor(sq.dbId)}] #${sq.quote.number} " +
                "(rating ${sq.quote.rating?.toString() ?: "unrated"})"
        )
        appendLine(sq.quote.text)
        appendLine(sq.quote.url)
    }
}

/** Formats a single sourced quote as text. */
fun quoteToText(
    sq: com.quotebrowser.app.data.SourcedQuote,
    labelFor: (String) -> String
): String = buildString {
    appendLine(
        "[${labelFor(sq.dbId)}] #${sq.quote.number} " +
            "(rating ${sq.quote.rating?.toString() ?: "unrated"})"
    )
    appendLine()
    appendLine(sq.quote.text)
    appendLine()
    appendLine(sq.quote.url)
}

/** Formats sourced quotes as JSON, including each quote's database label. */
fun quotesToJson(
    quotes: List<com.quotebrowser.app.data.SourcedQuote>,
    labelFor: (String) -> String
): String {
    val array = JSONArray()
    for (sq in quotes) {
        array.put(JSONObject().apply {
            put("source", labelFor(sq.dbId))
            put("number", sq.quote.number)
            put("rating", sq.quote.rating ?: JSONObject.NULL)
            put("text", sq.quote.text)
            put("url", sq.quote.url)
        })
    }
    return array.toString(2)
}

/** Formats sourced quotes as XML. */
fun quotesToXml(
    quotes: List<com.quotebrowser.app.data.SourcedQuote>,
    labelFor: (String) -> String
): String = buildString {
    appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    appendLine("<quotes count=\"${quotes.size}\" exported=\"${LocalDate.now()}\">")
    for (sq in quotes) {
        appendLine(
            "  <quote source=\"${labelFor(sq.dbId)}\" " +
                "number=\"${sq.quote.number}\" rating=\"${sq.quote.rating ?: ""}\">"
        )
        appendLine("    <text><![CDATA[${sq.quote.text}]]></text>")
        appendLine("    <url>${sq.quote.url}</url>")
        appendLine("  </quote>")
    }
    appendLine("</quotes>")
}

/** Formats sourced quotes as CSV. */
fun quotesToCsv(
    quotes: List<com.quotebrowser.app.data.SourcedQuote>,
    labelFor: (String) -> String
): String = buildString {
    appendLine("source,number,rating,text,url")
    for (sq in quotes) {
        val source = "\"" + labelFor(sq.dbId).replace("\"", "\"\"") + "\""
        val text = "\"" + sq.quote.text.replace("\"", "\"\"") + "\""
        val url = "\"" + sq.quote.url.replace("\"", "\"\"") + "\""
        appendLine("$source,${sq.quote.number},${sq.quote.rating ?: ""},$text,$url")
    }
}
