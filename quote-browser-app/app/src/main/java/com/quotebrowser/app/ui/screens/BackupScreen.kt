package com.quotebrowser.app.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.quotebrowser.app.data.BackupEngine
import com.quotebrowser.app.data.DatabaseManager
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Full backup & restore:
 * - "Back up now" saves one zip through SAF (CreateDocument).
 * - "Restore" previews a zip (createdAt, app version, entry list) before
 *   restoring; a restart is required afterwards and offered on-screen.
 * - "Automatic backups" mirrors the schedule setting; the checkbox decides
 *   whether the scheduled run also writes a full zip into the folder.
 */
@Composable
fun BackupScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }

    val autoBackupFreq by manager.autoBackupFreq.collectAsStateWithLifecycle()
    var includeFull by remember {
        mutableStateOf(BackupEngine.isFullBackupInAuto(context))
    }

    // Restore flow state: temp zip file + its preview + dialog visibility.
    var restoreTemp by remember { mutableStateOf<File?>(null) }
    var restorePreview by remember { mutableStateOf<BackupEngine.BackupPreview?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    val createLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                working = true
                try {
                    val zip = BackupEngine.createFullBackup(context)
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            zip.inputStream().use { it.copyTo(out) }
                        } ?: throw IllegalStateException("Couldn't open the destination.")
                        status = "Full backup saved."
                    } finally {
                        zip.delete()
                    }
                } catch (e: Exception) {
                    status = "Backup failed: ${e.message ?: "unknown error"}"
                    Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                }
                working = false
            }
        }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                working = true
                val temp = File(
                    context.cacheDir,
                    "restore-pick-${System.currentTimeMillis()}.zip"
                )
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        temp.outputStream().use { input.copyTo(it) }
                    } ?: throw IllegalStateException("Couldn't read the file.")
                    val preview = BackupEngine.previewBackup(temp)
                    restoreTemp = temp
                    restorePreview = preview
                    showPreviewDialog = true
                    status = null
                } catch (e: IllegalArgumentException) {
                    temp.delete()
                    status = "Not a QuoteBrowser backup: ${e.message}"
                    Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    temp.delete()
                    status = "Couldn't read the backup: ${e.message ?: "unknown error"}"
                    Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                }
                working = false
            }
        }

    Column(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            flingBehavior = rememberCappedFlingBehavior()) {
            item {
                SettingsRowHeader(text = "Back up now")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Saves everything — settings, themes, favorites, " +
                                "categories, notes, reading history, My Quotes, and " +
                                "imported databases — into one zip file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                val stamp = LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                                )
                                createLauncher.launch("quotebrowser-full-$stamp.zip")
                            },
                            enabled = !working
                        ) {
                            Text(if (working) "Working…" else "Back up now")
                        }
                    }
                }
            }
            item {
                SettingsRowHeader(text = "Restore")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Restore replaces all current data with the backup. " +
                                "The app restarts afterwards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { restoreLauncher.launch(arrayOf("application/zip")) },
                            enabled = !working
                        ) {
                            Text("Choose backup file…")
                        }
                    }
                }
            }
            item {
                SettingsRowHeader(text = "Automatic backups")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "The scheduled backup always copies the databases. " +
                                "Also include a full zip (settings, themes, favorites, " +
                                "history, My Quotes) in each run. The backup folder is " +
                                "chosen in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("off" to "Off", "daily" to "Daily", "weekly" to "Weekly")
                                .forEach { (value, label) ->
                                    FilterChip(
                                        selected = autoBackupFreq == value,
                                        onClick = { manager.setAutoBackupFreq(value) },
                                        label = { Text(label) }
                                    )
                                }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeFull,
                                onCheckedChange = { checked ->
                                    includeFull = checked
                                    BackupEngine.setFullBackupInAuto(context, checked)
                                }
                            )
                            Text(
                                "Include full backup in automatic backups",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            if (status != null) {
                item {
                    Text(
                        text = status!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Preview dialog: shows what the restore would replace.
    if (showPreviewDialog && restorePreview != null && restoreTemp != null) {
        val preview = restorePreview!!
        AlertDialog(
            onDismissRequest = {
                showPreviewDialog = false
                restoreTemp?.delete()
                restoreTemp = null
                restorePreview = null
            },
            title = { Text("Restore this backup?") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Text(
                        "Created: ${preview.createdAt}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "App version: ${preview.appVersion}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Size: ${(preview.sizeBytes / 1024).coerceAtLeast(1)} KB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Contents (${preview.entries.size} entries)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.weight(1f, fill = false),
                        flingBehavior = rememberCappedFlingBehavior()) {
                        items(preview.entries) { entry ->
                            Text(
                                entry,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "All current data will be replaced.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPreviewDialog = false
                        val temp = restoreTemp
                        restoreTemp = null
                        restorePreview = null
                        if (temp == null) return@TextButton
                        scope.launch {
                            working = true
                            try {
                                BackupEngine.restoreFullBackup(context, temp)
                                showRestartDialog = true
                            } catch (e: Exception) {
                                status = "Restore failed: ${e.message ?: "unknown error"}"
                                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                            } finally {
                                temp.delete()
                                working = false
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPreviewDialog = false
                        restoreTemp?.delete()
                        restoreTemp = null
                        restorePreview = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Post-restore: the process must die so Room reopens restored files.
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { /* must pick an action */ },
            title = { Text("Restart required") },
            text = {
                Text("The backup was restored. Restart the app now so it loads the restored data.")
            },
            confirmButton = {
                TextButton(onClick = { BackupEngine.restartApp(context) }) {
                    Text("Restart now")
                }
            }
        )
    }
}

/** Small section header matching the settings style. */
@Composable
private fun SettingsRowHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}
