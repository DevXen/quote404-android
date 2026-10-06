package com.quotebrowser.app.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.quotebrowser.app.ui.MyQuotesViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import kotlinx.coroutines.launch
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

private sealed interface OcrState {
    data object Idle : OcrState
    data object Scanning : OcrState
    data class Done(val blocks: List<String>) : OcrState
    data class Error(val message: String) : OcrState
}

/**
 * Photograph-a-page import: pick a photo, run on-device OCR (ML Kit,
 * bundled model — works offline), then pick which recognized paragraphs
 * to import as My Quotes.
 */
@Composable
fun OcrImportScreen(
    myQuotesVm: MyQuotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var state by remember { mutableStateOf<OcrState>(OcrState.Idle) }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var importing by remember { mutableStateOf(false) }

    val recognizer = remember {
        // ML Kit's auto-init provider is stripped from the manifest (it
        // crashes hand-built APKs at launch), so initialize lazily here.
        com.google.mlkit.common.MlKit.initialize(context)
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun scan(uri: Uri) {
        state = OcrState.Scanning
        selected = emptySet()
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            state = OcrState.Error("Couldn't read that image.")
            return
        }
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val blocks = visionText.textBlocks
                    .map { it.text.trim() }
                    .filter { it.isNotEmpty() }
                state = if (blocks.isEmpty())
                    OcrState.Error("No text found in that photo.")
                else
                    OcrState.Done(blocks)
            }
            .addOnFailureListener {
                state = OcrState.Error("OCR failed — try a clearer photo.")
            }
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) scan(uri) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenHeader(title = "Import from photo") {
                TextButton(onClick = onBack) { Text("Back") }
            }
            when (val s = state) {
                OcrState.Idle -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Pick a photo of a quotes page — the text is read on-device and never leaves your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { picker.launch("image/*") }) {
                        Text("Pick a photo")
                    }
                }
                OcrState.Scanning -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Reading text…")
                }
                is OcrState.Error -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { picker.launch("image/*") }) {
                        Text("Try another photo")
                    }
                }
                is OcrState.Done -> {
                    val blocks = s.blocks
                    val allSelected = selected.size == blocks.size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${blocks.size} paragraphs found",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            selected = if (allSelected) emptySet()
                            else blocks.indices.toSet()
                        }) {
                            Text(if (allSelected) "Deselect all" else "Select all")
                        }
                    }
                    LazyColumn(modifier = Modifier.weight(1f),
                        flingBehavior = rememberCappedFlingBehavior()) {
                        items(blocks.size) { i ->
                            val checked = i in selected
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                onClick = {
                                    selected = if (checked) selected - i
                                    else selected + i
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            selected = if (it) selected + i
                                            else selected - i
                                        }
                                    )
                                    Text(
                                        blocks[i],
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 8,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { picker.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) { Text("New photo") }
                        Button(
                            onClick = {
                                importing = true
                                scope.launch {
                                    var n = 0
                                    for (i in selected.sorted()) {
                                        runCatching {
                                            myQuotesVm.addQuote(blocks[i], null)
                                            n++
                                        }
                                    }
                                    importing = false
                                    selected = emptySet()
                                    snackbar.showSnackbar(
                                        if (n == 1) "Imported 1 quote"
                                        else "Imported $n quotes"
                                    )
                                }
                            },
                            enabled = selected.isNotEmpty() && !importing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (importing) "Importing…"
                                else "Import (${selected.size})"
                            )
                        }
                    }
                }
            }
        }
    }
}
