package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.CatalogEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.rememberCappedFlingBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * v1.89: first-run database setup. The databases are downloads now, so
 * on a fresh install (nothing installed, setup not skipped) this screen
 * offers the catalog: pick any subset, watch per-file progress, retry
 * failures individually. "Skip for now" is persisted — the main UI works
 * without any catalog database (My Quotes only), and everything here
 * stays available later in Settings → Databases.
 *
 * Rendered as a full-screen overlay over the main UI (AppNav); the root
 * consumes stray taps so nothing underneath can be hit through it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSetupScreen(
    manager: DatabaseManager,
    onFinished: () -> Unit,
    onOpenOwnDatabases: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<CatalogEntry>>(emptyList()) }
    var fetchAttempt by remember { mutableStateOf(0) }
    val selected = remember { mutableStateMapOf<String, Boolean>() }
    // file -> progress fraction while installing.
    val progress = remember { mutableStateMapOf<String, Float>() }
    val failures = remember { mutableStateMapOf<String, String>() }
    var installing by remember { mutableStateOf(false) }

    LaunchedEffect(fetchAttempt) {
        loading = true
        loadError = null
        val fetched = withContext(Dispatchers.IO) { manager.refreshCatalog() }
        val catalog = fetched ?: withContext(Dispatchers.IO) {
            manager.catalogEntries()
        }
        if (catalog.isEmpty()) {
            loadError = "Couldn't download the database list. Check " +
                "your connection and try again — or skip for now and " +
                "pick databases later in Settings → Databases."
        } else {
            entries = catalog
            catalog.forEach { e ->
                if (e.file !in selected) selected[e.file] = true
            }
        }
        loading = false
    }

    fun installFiles(files: List<String>) {
        if (installing || files.isEmpty()) return
        installing = true
        scope.launch {
            for (file in files) {
                failures.remove(file)
                progress[file] = 0f
                val result = manager.installDb(file) { _, done, total ->
                    progress[file] =
                        if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f)
                        else 0f
                }
                progress.remove(file)
                result.onSuccess {
                    selected[file] = false
                }.onFailure { e ->
                    failures[file] = e.message ?: "Download failed."
                }
            }
            installing = false
            // Everything requested is on the device: done. When some
            // failed, stay so the failures + Retry stay visible.
            if (failures.isEmpty()) onFinished()
        }
    }

    fun skip() {
        manager.setDbSetupSkipped()
        onFinished()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Choose your databases") })
        },
        bottomBar = {
            if (!loading && loadError == null && entries.isNotEmpty()) {
                val chosen = entries.filter { selected[it.file] == true }
                val totalSize = chosen.sumOf { it.size }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (chosen.isEmpty()) {
                                "Nothing selected"
                            } else {
                                "${chosen.size} selected · ${formatDbSize(totalSize)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { skip() },
                            enabled = !installing
                        ) { Text("Skip for now") }
                        Button(
                            onClick = {
                                installFiles(chosen.map { it.file })
                            },
                            enabled = chosen.isNotEmpty() && !installing
                        ) {
                            Text(if (installing) "Installing…" else "Install")
                        }
                    }
                    TextButton(
                        onClick = onOpenOwnDatabases,
                        enabled = !installing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("I have my own chat files / databases")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                // The setup overlays the main UI: eat taps that land
                // between controls so nothing underneath can be hit.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            when {
                loading -> {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Loading the database list…")
                    }
                }
                loadError != null -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            loadError!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { fetchAttempt++ }) {
                            Text("Retry")
                        }
                        TextButton(onClick = { skip() }) {
                            Text("Skip for now")
                        }
                        TextButton(onClick = onOpenOwnDatabases) {
                            Text("I have my own chat files / databases")
                        }
                    }
                }
                else -> {
                    val allSelected = entries.isNotEmpty() &&
                        entries.all { selected[it.file] == true }
                    Column(Modifier.fillMaxSize()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Quote databases are separate downloads — " +
                                    "pick the ones you want. They update " +
                                    "automatically, and you can change your " +
                                    "mind any time in Settings → Databases.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    val target = !allSelected
                                    entries.forEach { selected[it.file] = target }
                                },
                                enabled = !installing
                            ) {
                                Text(if (allSelected) "Select none" else "Select all")
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            flingBehavior = rememberCappedFlingBehavior()
                        ) {
                            items(entries, key = { it.file }) { entry ->
                                SetupDbRow(
                                    entry = entry,
                                    checked = selected[entry.file] == true,
                                    onCheckedChange = { checked ->
                                        selected[entry.file] = checked
                                    },
                                    enabled = !installing &&
                                        !progress.containsKey(entry.file),
                                    progressFraction = progress[entry.file],
                                    failure = failures[entry.file],
                                    onRetry = { installFiles(listOf(entry.file)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupDbRow(
    entry: CatalogEntry,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    progressFraction: Float?,
    failure: String?,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.label.ifBlank { entry.file.removeSuffix(".db") },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val sub = buildString {
                        append("${formatDbCount(entry.count)} quotes")
                        append(" · ${formatDbSize(entry.size)}")
                        entry.date?.takeIf { it.isNotBlank() }
                            ?.let { append(" · $it") }
                    }
                    Text(
                        sub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (progressFraction != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Downloading… ${(progressFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (failure != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        failure,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry) { Text("Retry") }
                }
            }
        }
    }
}
