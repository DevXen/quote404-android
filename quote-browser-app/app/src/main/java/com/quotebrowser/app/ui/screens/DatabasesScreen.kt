package com.quotebrowser.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.CatalogEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DbFileLoad
import com.quotebrowser.app.ui.MyQuotesViewModel
import com.quotebrowser.app.ui.rememberCappedFlingBehavior
import java.text.NumberFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Human size for a database file / download ("850 KB", "56.5 MB"). */
internal fun formatDbSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.0f KB".format(kb)
    val mb = kb / 1024.0
    return if (mb < 100) "%.1f MB".format(mb) else "%.0f MB".format(mb)
}

internal fun formatDbCount(count: Long): String =
    NumberFormat.getIntegerInstance().format(count)

/**
 * v1.89: Databases manager (Settings → Databases). The quote databases
 * are downloads now, not part of the app:
 *
 * - "Download": catalog databases that aren't installed yet.
 * - "Installed": live count, size on disk, revision, update chip when
 *   the catalog has a newer revision, re-download, uninstall.
 * - "Your own databases": routes into the existing personal flows —
 *   My Databases (create/rename/delete), importing a chat or text file
 *   into My Quotes / one of your databases / a new database, and
 *   importing a .db file — nothing is reimplemented here.
 *
 * The manifest is refreshed in the background when the screen opens so
 * newly published databases show up without an app update.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabasesScreen(
    manager: DatabaseManager,
    onMyDatabasesClick: () -> Unit,
    onBack: () -> Unit,
    // v1.89.4: manual "what's new" check (community catalogs).
    onCheckNewContentClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userDbs by manager.userDbs.collectAsStateWithLifecycle()
    val currentDbId by manager.currentDbId.collectAsStateWithLifecycle()

    var entries by remember { mutableStateOf<List<CatalogEntry>>(emptyList()) }
    var refreshTick by remember { mutableStateOf(0) }
    var totalBytes by remember { mutableStateOf(0L) }
    // Installed live counts, loaded off the main thread per refresh.
    val counts = remember { mutableStateMapOf<String, Long>() }
    // file -> progress fraction while a download is running.
    val progress = remember { mutableStateMapOf<String, Float>() }
    val errors = remember { mutableStateMapOf<String, String>() }
    var statusLine by remember { mutableStateOf<String?>(null) }
    var uninstallTarget by remember { mutableStateOf<CatalogEntry?>(null) }

    LaunchedEffect(refreshTick) {
        // Refresh the manifest in the background; the cached catalog is
        // shown either way, so this also works offline.
        withContext(Dispatchers.IO) { manager.refreshCatalog() }
        val catalog = manager.catalogEntries()
        entries = catalog
        totalBytes = withContext(Dispatchers.IO) {
            manager.totalInstalledCatalogBytes()
        }
        val freshCounts = withContext(Dispatchers.IO) {
            catalog.filter { manager.isCatalogDbInstalled(it.file) }
                .associate {
                    it.file to manager.quoteCountOnFile(
                        manager.catalogDeviceFile(it.file)
                    )
                }
        }
        counts.clear()
        counts.putAll(freshCounts)
    }

    fun runDownload(entry: CatalogEntry, force: Boolean) {
        if (progress.containsKey(entry.file)) return
        errors.remove(entry.file)
        statusLine = null
        progress[entry.file] = 0f
        scope.launch {
            val result = if (force) {
                manager.redownloadDb(entry.file) { _, done, total ->
                    progress[entry.file] =
                        if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f)
                        else 0f
                }
            } else {
                manager.installDb(entry.file) { _, done, total ->
                    progress[entry.file] =
                        if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f)
                        else 0f
                }
            }
            progress.remove(entry.file)
            result.onSuccess {
                statusLine = "${entryLabel(entry)} is ready."
                refreshTick++
            }.onFailure { e ->
                errors[entry.file] = e.message ?: "Download failed."
            }
        }
    }

    // ---- "Your own databases": import a chat / text file ----
    var pendingImportText by remember { mutableStateOf<String?>(null) }
    var importTargetId by remember { mutableStateOf(DatabaseManager.MY_QUOTES_ID) }
    var newDbName by remember { mutableStateOf("") }
    var createNewForImport by remember { mutableStateOf(false) }

    val textImportLauncher = rememberLauncherForActivityResult(
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
                if (text != null) {
                    importTargetId = DatabaseManager.MY_QUOTES_ID
                    createNewForImport = false
                    newDbName = ""
                    pendingImportText = text
                } else {
                    statusLine = "Couldn't read that file."
                }
            }
        }
    }

    // ---- Load a .db file (v1.89.1): a file whose name matches a
    // catalog database installs AS that database; any other valid
    // quote database is imported as a personal database. Used by both
    // the prominent button at the top and the "Your own databases" row.
    var loadInProgress by remember { mutableStateOf(false) }
    val dbImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && !loadInProgress) {
            loadInProgress = true
            statusLine = null
            scope.launch {
                val result = manager.loadDbFile(uri)
                loadInProgress = false
                result.onSuccess { load ->
                    when (load) {
                        is DbFileLoad.CatalogInstalled -> {
                            statusLine = "Installed as ${load.label}."
                            refreshTick++
                        }
                        is DbFileLoad.Imported -> {
                            manager.switchDatabase(load.info.id)
                            statusLine =
                                "Imported \"${load.info.label}\" as " +
                                    "your own database and switched to it."
                            refreshTick++
                        }
                    }
                }.onFailure { e ->
                    statusLine = e.message ?: "Couldn't load that file."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Databases") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val notInstalled = entries.filter { !manager.isCatalogDbInstalled(it.file) }
        val installed = entries.filter { manager.isCatalogDbInstalled(it.file) }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            flingBehavior = rememberCappedFlingBehavior()
        ) {
            item {
                Text(
                    "The quote databases are downloads — the app stays " +
                        "small, and each database updates on its own when " +
                        "a new edition is published.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = {
                            dbImportLauncher.launch(arrayOf("*/*"))
                        },
                        enabled = !loadInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (loadInProgress) {
                                "Loading…"
                            } else {
                                "Load a .db file"
                            }
                        )
                    }
                    Text(
                        "Already have a database file saved on this " +
                            "phone? Load it directly — a file whose name " +
                            "matches one of the online databases installs " +
                            "as that database (works offline too); any " +
                            "other quote database is added as your own.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (loadInProgress) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
            statusLine?.let { line ->
                item {
                    Text(
                        line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // v1.89.4: manual check of the community catalogs (new
            // databases, new plugins, plugin updates).
            item {
                OutlinedButton(
                    onClick = onCheckNewContentClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Check for new databases & plugins")
                }
            }

            if (notInstalled.isNotEmpty()) {
                item {
                    SectionTitle("Download")
                }
                items(notInstalled, key = { "dl:" + it.file }) { entry ->
                    DownloadRow(
                        entry = entry,
                        progressFraction = progress[entry.file],
                        error = errors[entry.file],
                        onInstall = { runDownload(entry, force = false) }
                    )
                }
            }

            if (installed.isNotEmpty()) {
                item {
                    SectionTitle("Installed")
                }
                items(installed, key = { "in:" + it.file }) { entry ->
                    InstalledRow(
                        entry = entry,
                        liveCount = counts[entry.file],
                        sizeOnDisk = manager.catalogDeviceFile(entry.file).length(),
                        installedRevision = manager.installedDbRevision(entry.file),
                        isCurrent = currentDbId == entry.id,
                        progressFraction = progress[entry.file],
                        error = errors[entry.file],
                        onUpdate = { runDownload(entry, force = false) },
                        onRedownload = { runDownload(entry, force = true) },
                        onUninstall = { uninstallTarget = entry }
                    )
                }
            }

            if (entries.isEmpty()) {
                item {
                    Text(
                        "The database list hasn't loaded yet — it " +
                            "downloads the first time you open this screen " +
                            "with a connection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                SectionTitle("Your own databases")
            }
            item {
                OwnDbRow(
                    title = "My databases",
                    subtitle = "Create your own quote databases, add " +
                        "quotes, rename or delete them",
                    onClick = onMyDatabasesClick
                )
            }
            item {
                OwnDbRow(
                    title = "Import a chat or text file",
                    subtitle = "Turn a chat log or text file into quotes " +
                        "in My Quotes or one of your databases",
                    onClick = { textImportLauncher.launch(arrayOf("text/plain")) }
                )
            }
            item {
                OwnDbRow(
                    title = "Import a .db file",
                    subtitle = "Same as Load a .db file above — a file " +
                        "matching an online database installs as that " +
                        "database, anything else is added as your own",
                    onClick = {
                        dbImportLauncher.launch(arrayOf("*/*"))
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (installed.isEmpty()) {
                        "No databases installed yet."
                    } else {
                        "Installed databases use ${formatDbSize(totalBytes)}."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    uninstallTarget?.let { entry ->
        val isCurrent = currentDbId == entry.id
        AlertDialog(
            onDismissRequest = { uninstallTarget = null },
            title = { Text("Remove ${entryLabel(entry)}?") },
            text = {
                Text(
                    "The database file is deleted from this device. " +
                        "Favorites, categories, and ratings stored inside " +
                        "it are removed with it." +
                        if (isCurrent) {
                            " This is the database you're browsing now — " +
                                "the app will switch to another installed " +
                                "database, or My Quotes."
                        } else {
                            ""
                        } +
                        " You can download it again any time."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    uninstallTarget = null
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            manager.uninstallDb(entry.file)
                        }
                        if (ok) {
                            statusLine = "${entryLabel(entry)} removed."
                            refreshTick++
                        }
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { uninstallTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingImportText?.let { text ->
        AlertDialog(
            onDismissRequest = { pendingImportText = null },
            title = { Text("Import into…") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Each block of text becomes a quote (blank lines " +
                            "separate quotes). Where should they go?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    ImportTargetRow(
                        label = "My Quotes",
                        selected = !createNewForImport &&
                            importTargetId == DatabaseManager.MY_QUOTES_ID,
                        onClick = {
                            createNewForImport = false
                            importTargetId = DatabaseManager.MY_QUOTES_ID
                        }
                    )
                    userDbs.forEach { info ->
                        ImportTargetRow(
                            label = info.label,
                            selected = !createNewForImport &&
                                importTargetId == info.id,
                            onClick = {
                                createNewForImport = false
                                importTargetId = info.id
                            }
                        )
                    }
                    ImportTargetRow(
                        label = "New database…",
                        selected = createNewForImport,
                        onClick = { createNewForImport = true }
                    )
                    if (createNewForImport) {
                        OutlinedTextField(
                            value = newDbName,
                            onValueChange = { newDbName = it },
                            label = { Text("New database name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val content = text
                        pendingImportText = null
                        scope.launch {
                            val targetId = if (createNewForImport) {
                                withContext(Dispatchers.IO) {
                                    manager.createUserDb(newDbName)
                                }.id
                            } else {
                                importTargetId
                            }
                            val (imported, skipped) =
                                MyQuotesViewModel(manager, targetId)
                                    .importText(content)
                            statusLine = when {
                                imported > 0 ->
                                    "Imported $imported quotes" +
                                        if (skipped > 0) {
                                            " ($skipped skipped as duplicates/blanks)."
                                        } else {
                                            "."
                                        }
                                else ->
                                    "Nothing imported — the file had no " +
                                        "new quotes for that database."
                            }
                            refreshTick++
                        }
                    },
                    enabled = !createNewForImport || newDbName.isNotBlank()
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportText = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private fun entryLabel(entry: CatalogEntry): String =
    entry.label.ifBlank { entry.file.removeSuffix(".db") }

@Composable
private fun DownloadRow(
    entry: CatalogEntry,
    progressFraction: Float?,
    error: String?,
    onInstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        entryLabel(entry),
                        style = MaterialTheme.typography.titleMedium
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
                if (progressFraction == null) {
                    Button(onClick = onInstall) { Text("Install") }
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
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun InstalledRow(
    entry: CatalogEntry,
    liveCount: Long?,
    sizeOnDisk: Long,
    installedRevision: Int,
    isCurrent: Boolean,
    progressFraction: Float?,
    error: String?,
    onUpdate: () -> Unit,
    onRedownload: () -> Unit,
    onUninstall: () -> Unit
) {
    val updateAvailable = entry.revision > installedRevision
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                entryLabel(entry),
                style = MaterialTheme.typography.titleMedium
            )
            val sub = buildString {
                append(
                    when (liveCount) {
                        null -> "…"
                        1L -> "1 quote"
                        else -> "${formatDbCount(liveCount)} quotes"
                    }
                )
                append(" · ${formatDbSize(sizeOnDisk)} on disk")
                append(" · revision $installedRevision")
                if (isCurrent) append(" · in use")
            }
            Text(
                sub,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (updateAvailable && progressFraction == null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Update available (revision ${entry.revision})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onUpdate) { Text("Update") }
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
            } else {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onRedownload) {
                        Text("Re-download")
                    }
                    OutlinedButton(onClick = onUninstall) {
                        Text("Uninstall")
                    }
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun OwnDbRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImportTargetRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
