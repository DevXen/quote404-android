package com.quotebrowser.app.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.layouts.LayoutBlock
import com.quotebrowser.app.data.layouts.LayoutBlockType
import com.quotebrowser.app.data.layouts.LayoutDef
import com.quotebrowser.app.data.layouts.LayoutStore
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.layouts.HTML_TOKENS
import com.quotebrowser.app.ui.layouts.HtmlExportOptions
import com.quotebrowser.app.ui.layouts.QuoteExportContext
import com.quotebrowser.app.ui.layouts.quoteTextExportStyle
import com.quotebrowser.app.ui.layouts.renderQuotesPageHtml

/**
 * v1.85: the custom style editor. Write your own per-quote HTML with
 * {{tokens}} plus your own CSS, preview it live, and save it as a named
 * style — it then appears in the layout picker next to the premades.
 */
@Composable
fun LayoutEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var styleName by remember { mutableStateOf("") }
    var htmlTemplate by remember { mutableStateOf(DEFAULT_CUSTOM_HTML) }
    var css by remember { mutableStateOf(DEFAULT_CUSTOM_CSS) }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var customsVersion by remember { mutableStateOf(0) }

    // Live preview: render the sample quote with the current template+css.
    val previewDef = remember(htmlTemplate, css) {
        LayoutDef(
            id = "preview",
            name = "Preview",
            blocks = listOf(LayoutBlock(LayoutBlockType.QUOTE_TEXT)),
            css = css,
            htmlTemplate = htmlTemplate,
            source = "custom"
        )
    }

    val customs = remember(customsVersion) { LayoutStore.customLayouts(context) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScreenHeader(title = "Customize HTML / CSS") {
                TextButton(onClick = onBack) { Text("Back") }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            Text(
                "Design your own quote style. Arrange the {{tokens}} however you want, " +
                    "style them with CSS, and save it — your style joins the layout picker. " +
                    "Your template is used exactly as written: the export screen's " +
                    "element toggles (URL, rating, date, categories, branding) apply " +
                    "to the premade layouts, not to your custom HTML.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Tokens: " + HTML_TOKENS.joinToString(" "),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = styleName,
                onValueChange = { styleName = it },
                label = { Text("Style name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = htmlTemplate,
                onValueChange = { htmlTemplate = it },
                label = { Text("HTML template (per quote)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
            OutlinedTextField(
                value = css,
                onValueChange = { css = it },
                label = { Text("CSS") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )

            Text("Live preview", style = MaterialTheme.typography.titleSmall)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                // v1.86: the preview renders quote text with the same
                // classified per-category spans as a real export.
                val previewStyle = remember {
                    quoteTextExportStyle(
                        (context.applicationContext as com.quotebrowser.app.QuoteApp).dbManager
                    )
                }
                val pageHtml = renderQuotesPageHtml(
                    "Preview",
                    listOf(SAMPLE_CONTEXT),
                    previewDef,
                    HtmlExportOptions(),
                    previewStyle
                )
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = false
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                null, pageHtml, "text/html", "utf-8", null
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(320.dp)
                    )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val name = styleName.trim()
                        if (name.isEmpty()) {
                            savedMessage = "Give the style a name first."
                            return@Button
                        }
                        val id = name.lowercase()
                            .replace(Regex("[^a-z0-9]+"), "-")
                            .trim('-')
                            .ifEmpty { "custom" }
                        LayoutStore.saveCustomLayout(context, id, name, htmlTemplate, css)
                        customsVersion++
                        savedMessage = "Saved \"$name\" — it now appears in the layout picker."
                    }
                ) { Text("Save style") }
                OutlinedButton(onClick = {
                    htmlTemplate = DEFAULT_CUSTOM_HTML
                    css = DEFAULT_CUSTOM_CSS
                }) { Text("Reset") }
            }
            savedMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary)
            }

            if (customs.isNotEmpty()) {
                Text("Your styles", style = MaterialTheme.typography.titleSmall)
                customs.forEach { custom ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(custom.name, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = {
                            LayoutStore.deleteCustomLayout(context, custom.id)
                            customsVersion++
                        }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
}

private val SAMPLE_QUOTE = Quote(
    number = 42,
    rating = 137,
    text = "<alice> my code finally compiled on the first try\n<bob> that's the most suspicious thing you've ever said",
    url = "https://example.com/quotes/42"
)

private val SAMPLE_CONTEXT = QuoteExportContext(
    quote = SAMPLE_QUOTE,
    siteName = "Example Quotes",
    categories = listOf("programming", "funny"),
    date = "2026-09-19"
)

private const val DEFAULT_CUSTOM_HTML = """<div class="my-quote">
  <div class="my-number">#{{quote_number}}</div>
  <div class="my-text">{{quote_text}}</div>
  <div class="my-author">— {{author}}</div>
  <div class="my-meta">{{site_name}} · {{rating}} · <a href="{{source_url}}">source</a></div>
</div>"""

private const val DEFAULT_CUSTOM_CSS = """.my-quote {
  background: #202020;
  border-left: 4px solid #0090ff;
  border-radius: 8px;
  padding: 16px;
  margin: 12px 0;
  font-family: sans-serif;
  color: #eee;
}
.my-number { font-size: 0.8em; color: #0090ff; }
.my-text { font-size: 1.1em; line-height: 1.5; margin: 8px 0; white-space: pre-wrap; }
.my-author { text-align: right; font-style: italic; color: #aaa; }
.my-meta { margin-top: 8px; font-size: 0.8em; color: #888; }
.my-meta a { color: #0090ff; }"""
