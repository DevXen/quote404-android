package com.quotebrowser.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.DuplicateHit
import com.quotebrowser.app.ui.MyQuotesViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyQuotesScreen(
    vm: MyQuotesViewModel,
    onBack: () -> Unit,
    inboxText: String? = null,
    onInboxTextConsumed: () -> Unit = {},
    // v1.88.36: reused for user-created databases — the heading follows the DB label.
    title: String = "My Quotes"
) {
    val quotes by vm.quotes.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Quote?>(null) }
    var deleting by remember { mutableStateOf<Quote?>(null) }
    var importResult by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var collectionImportName by remember { mutableStateOf<String?>(null) }
    // Parsed collection file awaiting the user's destination choice.
    var pendingCollection by remember {
        mutableStateOf<com.quotebrowser.app.ui.QuoteCollection?>(null)
    }
    var showLinkDialog by remember { mutableStateOf(false) }
    var prefillText by remember { mutableStateOf("") }
    // v1.60: source URL pulled out of shared text (kept separate so the
    // quote text stays clean and "View original" works).
    var prefillUrl by remember { mutableStateOf("") }
    // Text shared in from another app: open the editor prefilled with it.
    LaunchedEffect(inboxText) {
        if (inboxText != null) {
            val (text, url) = extractSourceUrl(inboxText)
            prefillText = text
            prefillUrl = url
            editing = null
            showEditor = true
            onInboxTextConsumed()
        }
    }
    // (text, rating, url, existing) + duplicate hits awaiting confirmation.
    var pendingDupe by remember {
        mutableStateOf<Pair<PendingSave, List<DuplicateHit>>?>(null)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader()?.use { it.readText() }
                    } catch (e: Exception) {
                        null
                    }
                }
                importResult = if (text != null) vm.importText(text) else (0 to 0)
            }
        }
    }

    // Shared collection files (*.qbcollection.json): parse first, then ask
    // where the quotes should go (My Quotes, or My Quotes + a new category).
    val collectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val collection = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader()?.use { it.readText() }
                            ?.let { com.quotebrowser.app.ui.parseCollection(it) }
                    } catch (e: Exception) {
                        null
                    }
                }
                if (collection != null) {
                    pendingCollection = collection
                } else {
                    collectionImportName = null
                    // Signal "not a collection" with a distinctive pair.
                    importResult = (-1 to -1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEditor = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add a quote")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("text/plain")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Import from text file")
            }
            OutlinedButton(
                onClick = {
                    collectionImportName = null
                    collectionLauncher.launch(arrayOf("application/json", "*/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Import collection file")
            }
            OutlinedButton(
                onClick = { showLinkDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Import from bash.org link")
            }
            Text(
                "Blank lines (or --- lines) separate quotes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(4.dp))
            if (quotes.isEmpty()) {
                EmptyState("Nothing here yet.\nTap + to add a quote you found.")
            } else {
                LazyColumn(Modifier.fillMaxSize(),
                    flingBehavior = rememberCappedFlingBehavior()) {
                    items(quotes, key = { it.number }) { quote ->
                        MyQuoteRow(
                            quote = quote,
                            onEdit = { editing = quote; showEditor = true },
                            onDelete = { deleting = quote }
                        )
                    }
                }
            }
        }
    }

    if (showLinkDialog) {
        var linkUrl by remember { mutableStateOf("") }
        var fetching by remember { mutableStateOf(false) }
        var linkError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!fetching) showLinkDialog = false },
            title = { Text("Import from bash.org link") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it; linkError = null },
                        label = { Text("bash.org link or quote number") },
                        singleLine = true,
                        enabled = !fetching,
                        modifier = Modifier.fillMaxWidth()
                    )
                    linkError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (fetching) {
                        Text("Fetching…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        fetching = true
                        linkError = null
                        scope.launch {
                            val result =
                                com.quotebrowser.app.data.fetchBashOrgQuote(linkUrl)
                            fetching = false
                            result.onSuccess { fetched ->
                                prefillText = fetched.text
                                showLinkDialog = false
                                editing = null
                                showEditor = true
                            }.onFailure {
                                linkError = it.message ?: "Fetch failed."
                            }
                        }
                    },
                    enabled = !fetching && linkUrl.isNotBlank()
                ) {
                    Text("Fetch")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLinkDialog = false },
                    enabled = !fetching
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditor) {
        QuoteEditorDialog(
            initial = editing,
            prefill = prefillText,
            prefillUrl = prefillUrl,
            onDismiss = {
                showEditor = false
                editing = null
                prefillText = ""
                prefillUrl = ""
            },
            onSave = { text, rating, url, existing ->
                scope.launch {
                    val dupes = vm.findDuplicates(text)
                        // v1.88.36: exclude the quote being edited in THIS
                        // database (vm.dbId), not just the built-in My Quotes.
                        .filter { it.dbId != vm.dbId ||
                            it.number != existing?.number }
                    if (dupes.isEmpty()) {
                        if (existing == null) vm.addQuote(text, rating, url)
                        else vm.updateQuote(existing.copy(text = text, rating = rating, url = url))
                        showEditor = false
                        editing = null
                    } else {
                        pendingDupe = PendingSave(text, rating, url, existing) to dupes
                    }
                }
            }
        )
    }

    val dupe = pendingDupe
    if (dupe != null) {
        val (save, hits) = dupe
        AlertDialog(
            onDismissRequest = { pendingDupe = null },
            title = { Text("Already exists") },
            text = {
                Text(
                    "This exact text is already in:\n" +
                        hits.joinToString("\n") { "• ${it.label} (#${it.number})" } +
                        "\n\nAdd it anyway?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        if (save.existing == null) vm.addQuote(save.text, save.rating, save.url)
                        else vm.updateQuote(
                            save.existing.copy(
                                text = save.text,
                                rating = save.rating,
                                url = save.url
                            )
                        )
                        pendingDupe = null
                        showEditor = false
                        editing = null
                    }
                }) {
                    Text("Add anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDupe = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val del = deleting
    if (del != null) {
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete quote?") },
            text = { Text("Delete #${del.number} from My Quotes? This can't be undone.") },
            confirmButton = {
                Button(onClick = { vm.deleteQuote(del.number); deleting = null }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val pc = pendingCollection
    if (pc != null) {
        AlertDialog(
            onDismissRequest = { pendingCollection = null },
            title = { Text("Import collection") },
            text = {
                Text(
                    "\"${pc.name}\" has ${pc.quotes.size} quote(s). Import them " +
                        "into My Quotes, optionally creating a category to keep " +
                        "them grouped. Quotes you already have are skipped."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingCollection = null
                        scope.launch {
                            collectionImportName = pc.name
                            importResult = vm.importCollectionTexts(
                                pc.name,
                                pc.quotes.map { it.text }
                            )
                        }
                    }
                ) {
                    Text("Create category \"${pc.name}\"")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingCollection = null
                        scope.launch {
                            collectionImportName = null
                            importResult = vm.importCollectionTexts(
                                null,
                                pc.quotes.map { it.text }
                            )
                        }
                    }
                ) {
                    Text("Import into My Quotes")
                }
            }
        )
    }

    val result = importResult
    if (result != null) {
        AlertDialog(
            onDismissRequest = { importResult = null; collectionImportName = null },
            title = { Text("Import finished") },
            text = {
                Text(
                    when {
                        result.first < 0 ->
                            "That file isn't a valid QuoteBrowser collection."
                        collectionImportName != null ->
                            "Imported ${result.first} quote(s) from " +
                                "\"$collectionImportName\". " +
                                if (result.second > 0) "${result.second} skipped (blank or duplicate)."
                                else "No duplicates skipped."
                        else ->
                            "Imported ${result.first} quote(s). " +
                                if (result.second > 0) "${result.second} skipped (blank or duplicate)."
                                else "No duplicates skipped."
                    }
                )
            },
            confirmButton = {
                Button(onClick = { importResult = null; collectionImportName = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun MyQuoteRow(
    quote: Quote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuoteNumberBadge(number = quote.number)
                if (quote.rating != null) {
                    Spacer(Modifier.width(8.dp))
                    RatingBadge(quote.rating)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit quote")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete quote",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6
            )
        }
    }
}

@Composable
private fun QuoteEditorDialog(
    initial: Quote?,
    prefill: String = "",
    prefillUrl: String = "",
    onDismiss: () -> Unit,
    onSave: (text: String, rating: Int?, url: String, existing: Quote?) -> Unit
) {
    var text by remember { mutableStateOf(initial?.text ?: prefill) }
    var ratingText by remember {
        mutableStateOf(initial?.rating?.toString() ?: "")
    }
    // v1.60: source URL — prefilled from shared text, editable, powers
    // "View original".
    var url by remember { mutableStateOf(initial?.url ?: prefillUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add a quote" else "Edit quote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Quote text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Source URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                OutlinedTextField(
                    value = ratingText,
                    onValueChange = { input ->
                        ratingText = input.filter { it.isDigit() || it == '-' }.take(6)
                    },
                    label = { Text("Rating (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(text.trim(), ratingText.toIntOrNull(), url.trim(), initial)
                },
                enabled = text.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/** v1.60: a quote save awaiting duplicate confirmation. */
private data class PendingSave(
    val text: String,
    val rating: Int?,
    val url: String,
    val existing: Quote?
)

/**
 * v1.60: pulls the first http(s) URL out of shared text, returning the
 * remaining text and the URL. The URL becomes the quote's source link
 * instead of cluttering the quote body.
 */
private val SourceUrlRegex = Regex("""https?://[^\s)>\]"]+""")

private fun extractSourceUrl(text: String): Pair<String, String> {
    val m = SourceUrlRegex.find(text) ?: return text to ""
    val url = m.value.trimEnd('.', ',', ';', ':', '!', '?')
    val remaining = text.removeRange(m.range).trim()
        .replace(Regex("""\n{3,}"""), "\n\n")
    return remaining to url
}
