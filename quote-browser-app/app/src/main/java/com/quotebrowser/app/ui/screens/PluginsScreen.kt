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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * v1.83: the plugin framework UI. Lists every discovered pack (installed,
 * rejected, and reserved-future-type) with its manifest details, checksum,
 * and any error/skip reason; toggles packs on/off; uninstalls with a
 * confirm; and imports new packs from a .zip with an install-time approval
 * dialog (name, author, version, type, permissions, checksum) — nothing
 * lands in the plugins dir until the user approves.
 *
 * Route string: "plugins" ([PLUGINS_ROUTE]); the Settings entry point is
 * the "Plugins" row.
 *
 * v1.87: packs that declare an update URL get a "Check for updates"
 * button with one-tap update; packs that declare a settings schema get
 * a "Settings" button opening their own settings page. Only packs with
 * an update URL can be checked — the rest update by re-importing.
 */
const val PLUGINS_ROUTE = "plugins"

/** Friendly display name for a pack type. */
private fun pluginTypeLabel(type: String): String = when (type) {
    PluginManager.TYPE_QUOTE_PACK -> "Quote pack"
    PluginManager.TYPE_THEME_PACK -> "Theme pack"
    PluginManager.TYPE_LAYOUT_PACK -> "Quote layout"
    PluginManager.TYPE_EXPORT_STYLE -> "Export style"
    PluginManager.TYPE_GAME_PACK -> "Game pack"
    PluginManager.TYPE_VOICE_PACK -> "Voice pack"
    PluginManager.TYPE_VIDEO_BACKGROUND -> "Video background"
    else -> type.ifBlank { "Unknown type" }
}

/** Friendly display name for a declared permission. */
private fun pluginPermissionLabel(permission: String): String = when (permission) {
    PluginManager.PERM_QUOTES_READ -> "Read your quote library (text, numbers, ratings)"
    PluginManager.PERM_STORAGE -> "Use its own private storage"
    PluginManager.PERM_NETWORK -> "Access the internet"
    PluginManager.PERM_TTS -> "Text-to-speech / audio playback"
    PluginManager.PERM_AUDIO -> "Play audio"
    PluginManager.PERM_EXPORT -> "Hook into the export pipeline"
    else -> permission
}

@Composable
fun PluginsScreen(
    manager: DatabaseManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val plugins by PluginManager.plugins.collectAsStateWithLifecycle()

    var staged by remember { mutableStateOf<PluginManager.StagedPack?>(null) }
    var staging by remember { mutableStateOf(false) }
    var installing by remember { mutableStateOf(false) }
    var stageError by remember { mutableStateOf<String?>(null) }
    var uninstallTarget by remember { mutableStateOf<PluginManager.PluginInfo?>(null) }
    // v1.87: one-tap updates — latest check result per pack id, plus
    // in-flight check/apply sets. Per-plugin settings target.
    var updateChecks by remember {
        mutableStateOf<Map<String, PluginManager.UpdateCheck>>(emptyMap())
    }
    var checkingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var applyingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var settingsTarget by remember { mutableStateOf<PluginManager.PluginInfo?>(null) }

    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    /** v1.87: run an update check for one pack, off the main thread. */
    fun checkUpdate(id: String) {
        if (id in checkingIds || id in applyingIds) return
        checkingIds = checkingIds + id
        scope.launch(Dispatchers.IO) {
            val check = PluginManager.checkForUpdate(context, id)
            withContext(Dispatchers.Main) {
                checkingIds = checkingIds - id
                updateChecks = updateChecks + (id to check)
            }
        }
    }

    /** v1.87: drop a staged update the user doesn't want. */
    fun dismissUpdate(id: String) {
        val check = updateChecks[id] ?: return
        scope.launch(Dispatchers.IO) {
            PluginManager.discardStagedPack(check.staged)
            withContext(Dispatchers.Main) {
                updateChecks = updateChecks - id
            }
        }
    }

    /** v1.87: one-tap apply of a staged update (Room evict for quote packs). */
    fun applyUpdate(id: String, check: PluginManager.UpdateCheck) {
        if (id in applyingIds) return
        applyingIds = applyingIds + id
        scope.launch(Dispatchers.IO) {
            val info = PluginManager.plugins.value
                .firstOrNull { it.manifest.id == id }
            if (info?.manifest?.type == PluginManager.TYPE_QUOTE_PACK) {
                manager.evictQuoteDatabase(PluginManager.DB_ID_PREFIX + id)
            }
            val error = PluginManager.applyUpdate(context, check)
            PluginManager.discardStagedPack(check.staged)
            withContext(Dispatchers.Main) {
                applyingIds = applyingIds - id
                // Clear the check so the row returns to its idle state;
                // rescan already refreshed the pack list.
                updateChecks = updateChecks - id
                if (error == null) toast("Pack updated")
                else toast(error)
            }
        }
    }

    val zipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        staging = true
        stageError = null
        scope.launch(Dispatchers.IO) {
            val (pack, error) = PluginManager.stagePackZip(context, uri)
            withContext(Dispatchers.Main) {
                staging = false
                if (pack != null) {
                    staged = pack
                } else {
                    stageError = error ?: "Couldn't import that zip"
                }
            }
        }
    }

    // Install-time approval dialog: the user sees exactly what the pack
    // declares before anything is installed.
    staged?.let { pack ->
        val m = pack.manifest
        AlertDialog(
            onDismissRequest = {
                PluginManager.discardStagedPack(pack)
                staged = null
            },
            title = { Text("Install pack?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PluginDetailRow("Name", m.name)
                    PluginDetailRow("Author", m.author)
                    PluginDetailRow("Version", m.version)
                    PluginDetailRow("Type", pluginTypeLabel(m.type))
                    if (m.description.isNotBlank()) {
                        PluginDetailRow("About", m.description)
                    }
                    PluginDetailRow(
                        "Permissions",
                        if (m.permissions.isEmpty()) "None — data only"
                        else m.permissions.joinToString("\n") {
                            "• ${pluginPermissionLabel(it)}"
                        }
                    )
                    PluginDetailRow(
                        "Checksum (SHA-256)",
                        pack.entryChecksum.ifBlank { "n/a" }
                    )
                    Text(
                        "Nothing is installed until you approve.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !installing,
                    onClick = {
                        installing = true
                        scope.launch(Dispatchers.IO) {
                            val result = PluginManager.installStagedPack(context, pack)
                            PluginManager.discardStagedPack(pack)
                            withContext(Dispatchers.Main) {
                                installing = false
                                staged = null
                                if (result == null) toast("\"${m.name}\" installed")
                                else toast(result)
                            }
                        }
                    }
                ) { Text(if (installing) "Installing…" else "Install") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        PluginManager.discardStagedPack(pack)
                        staged = null
                    }
                ) { Text("Cancel") }
            }
        )
    }

    // Uninstall confirm: evict the cached Room handle for quote packs first
    // so no stale database survives the pack's removal.
    uninstallTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { uninstallTarget = null },
            title = { Text("Uninstall pack?") },
            text = {
                Text(
                    "\"${target.manifest.name}\" will be removed everywhere. " +
                        "Its quotes and themes disappear from the app."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = target.manifest.id
                        uninstallTarget = null
                        scope.launch(Dispatchers.IO) {
                            if (target.manifest.type == PluginManager.TYPE_QUOTE_PACK) {
                                manager.evictQuoteDatabase(PluginManager.DB_ID_PREFIX + id)
                            }
                            val ok = PluginManager.uninstall(context, id)
                            withContext(Dispatchers.Main) {
                                toast(if (ok) "\"${target.manifest.name}\" uninstalled" else "Uninstall failed")
                            }
                        }
                    }
                ) { Text("Uninstall") }
            },
            dismissButton = {
                TextButton(onClick = { uninstallTarget = null }) { Text("Cancel") }
            }
        )
    }

    // v1.87: per-plugin settings — each pack with a settings schema gets
    // its own settings page (toggle → Switch, text → field, choice →
    // dropdown), persisted through PluginManager.setSetting.
    settingsTarget?.let { target ->
        PluginSettingsDialog(
            pluginId = target.manifest.id,
            title = target.manifest.name.ifBlank { target.dir.name },
            onDismiss = { settingsTarget = null }
        )
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Plugins") {
            TextButton(onClick = onBack) { Text("Back") }
        }
        if (staging) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text("Checking pack…", style = MaterialTheme.typography.bodyMedium)
            }
        }
        stageError?.let { err ->
            Text(
                err,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { zipPicker.launch(arrayOf("application/zip", "*/*")) },
                    enabled = !staging && !installing
                ) { Text("Import pack (.zip)") }
            }
            if (plugins.isEmpty()) {
                item {
                    EmptyState(
                        "No packs installed.\n\nQuoteBrowser supports seven pack types:\n" +
                            "quote packs (extra databases), theme packs (extra UI themes), " +
                            "layout packs (extra quote layouts), " +
                            "export styles (HTML export themes), voice packs (extra read-aloud voices), " +
                            "and game packs and video backgrounds " +
                            "(reserved for a future update).\n\n" +
                            "Import a pack with the button above, or place a pack folder " +
                            "(containing plugin.json) in the app's private plugins folder. " +
                            "Five sample packs are bundled with the app to show how packs work."
                    )
                }
            }
            items(plugins, key = { it.manifest.id.ifBlank { it.dir.name } }) { info ->
                val id = info.manifest.id
                val check = updateChecks[id]
                PluginRow(
                    info = info,
                    onToggle = { enabled ->
                        scope.launch(Dispatchers.IO) {
                            if (!enabled && info.manifest.type == PluginManager.TYPE_QUOTE_PACK) {
                                manager.evictQuoteDatabase(
                                    PluginManager.DB_ID_PREFIX + info.manifest.id
                                )
                            }
                            withContext(Dispatchers.Main) {
                                PluginManager.setEnabled(context, info.manifest.id, enabled)
                            }
                        }
                    },
                    onUninstall = { uninstallTarget = info },
                    updateCheck = check,
                    checkingUpdate = id in checkingIds,
                    applyingUpdate = id in applyingIds,
                    onCheckUpdate = { checkUpdate(id) },
                    onApplyUpdate = { check?.let { applyUpdate(id, it) } },
                    onDismissUpdate = { dismissUpdate(id) },
                    onOpenSettings = { settingsTarget = info }
                )
            }
            item {
                Text(
                    "Packs that declare an update address in their manifest show " +
                        "a \"Check for updates\" button above; other packs update " +
                        "by re-importing a newer zip.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PluginDetailRow(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PluginRow(
    info: PluginManager.PluginInfo,
    onToggle: (Boolean) -> Unit,
    onUninstall: () -> Unit,
    // v1.87: one-tap updates + per-plugin settings.
    updateCheck: PluginManager.UpdateCheck?,
    checkingUpdate: Boolean,
    applyingUpdate: Boolean,
    onCheckUpdate: () -> Unit,
    onApplyUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val m = info.manifest
    val broken = info.error != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (broken)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        m.name.ifBlank { info.dir.name },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        buildString {
                            if (m.author.isNotBlank()) append("by ${m.author}")
                            if (m.version.isNotBlank()) {
                                if (isNotEmpty()) append(" · ")
                                append("v${m.version}")
                            }
                        }.ifBlank { pluginTypeLabel(m.type) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Rejected packs can't be enabled; reserved future types
                // explain themselves via the error note below.
                Switch(
                    checked = info.enabled && !broken,
                    enabled = !broken && m.type.isNotBlank(),
                    onCheckedChange = onToggle
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Type: ${pluginTypeLabel(m.type)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (m.permissions.isNotEmpty()) {
                Text(
                    "Permissions: " +
                        m.permissions.joinToString(", ") { pluginPermissionLabel(it) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (info.entryChecksum.isNotBlank()) {
                Text(
                    "Checksum: ${info.entryChecksum.take(16)}…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (m.type == PluginManager.TYPE_QUOTE_PACK && info.quoteCount != null) {
                Text(
                    "${info.quoteCount} quotes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (m.type == PluginManager.TYPE_THEME_PACK && info.themes.isNotEmpty()) {
                Text(
                    "Themes: " + info.themes.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (m.type == PluginManager.TYPE_LAYOUT_PACK && info.layouts.isNotEmpty()) {
                Text(
                    "Layouts: " + info.layouts.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (m.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(m.description, style = MaterialTheme.typography.bodySmall)
            }
            info.error?.let { err ->
                Spacer(Modifier.height(4.dp))
                Text(
                    err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (!broken && m.id.isNotBlank()) {
                // v1.87: update status line for packs with an update URL.
                if (m.updateUrl.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    PluginUpdateStatus(
                        check = updateCheck,
                        checking = checkingUpdate,
                        applying = applyingUpdate,
                        installedVersion = m.version
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (m.updateUrl.isNotBlank()) {
                        when {
                            checkingUpdate || applyingUpdate -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            updateCheck?.status ==
                                PluginManager.UpdateStatus.AVAILABLE -> {
                                TextButton(onClick = onDismissUpdate) { Text("Dismiss") }
                                TextButton(onClick = onApplyUpdate) { Text("Update") }
                            }
                            else -> {
                                TextButton(onClick = onCheckUpdate) {
                                    Text("Check for updates")
                                }
                            }
                        }
                    }
                    // v1.87: per-plugin settings page.
                    if (PluginManager.settingDefs(m.id).isNotEmpty()) {
                        TextButton(onClick = onOpenSettings) { Text("Settings") }
                    }
                    OutlinedButton(onClick = onUninstall) { Text("Uninstall") }
                }
            }
        }
    }
}

/** v1.87: one line describing the latest update check for a pack. */
@Composable
private fun PluginUpdateStatus(
    check: PluginManager.UpdateCheck?,
    checking: Boolean,
    applying: Boolean,
    installedVersion: String
) {
    val small = MaterialTheme.typography.bodySmall
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    when {
        applying -> Text("Updating…", style = small, color = dim)
        checking -> Text("Checking for updates…", style = small, color = dim)
        check == null -> {
            // Idle: no check run yet this session.
        }
        check.status == PluginManager.UpdateStatus.AVAILABLE -> {
            Column {
                Text(
                    "Update available: " +
                        "v${installedVersion.ifBlank { "?" }} → v${check.latestVersion}",
                    style = small,
                    color = MaterialTheme.colorScheme.primary
                )
                if (check.newPermissions.isNotEmpty()) {
                    Text(
                        "Adds permissions: " + check.newPermissions.joinToString(", ") {
                            pluginPermissionLabel(it)
                        },
                        style = small,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        check.status == PluginManager.UpdateStatus.UP_TO_DATE ->
            Text("Up to date", style = small, color = dim)
        check.status == PluginManager.UpdateStatus.ERROR ->
            Text(
                check.error ?: "Update check failed",
                style = small,
                color = MaterialTheme.colorScheme.error
            )
        else -> {
            // NO_SOURCE can't happen here — the button is hidden then.
        }
    }
}

/**
 * v1.87: a pack's own settings page. Toggle → Switch, text → field,
 * choice → dropdown; every change persists through
 * PluginManager.setSetting immediately.
 */
@Composable
private fun PluginSettingsDialog(
    pluginId: String,
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val defs = remember(pluginId) { PluginManager.settingDefs(pluginId) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title — settings") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                defs.forEach { def ->
                    val initial =
                        PluginManager.getSettingOrDefault(context, pluginId, def)
                    PluginSettingRow(
                        def = def,
                        initial = initial,
                        onCommit = { value ->
                            PluginManager.setSetting(context, pluginId, def.key, value)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun PluginSettingRow(
    def: PluginManager.PluginSettingDef,
    initial: String,
    onCommit: (String) -> Unit
) {
    val small = MaterialTheme.typography.bodySmall
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    when (def.type) {
        "toggle" -> {
            var checked by remember(initial) { mutableStateOf(initial == "true") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(def.title, style = MaterialTheme.typography.bodyMedium)
                    if (def.description.isNotBlank()) {
                        Text(def.description, style = small, color = dim)
                    }
                }
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        onCommit(if (it) "true" else "false")
                    }
                )
            }
        }
        "choice" -> {
            var expanded by remember { mutableStateOf(false) }
            var current by remember(initial) { mutableStateOf(initial) }
            Column {
                Text(def.title, style = MaterialTheme.typography.bodyMedium)
                if (def.description.isNotBlank()) {
                    Text(def.description, style = small, color = dim)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { expanded = true }) { Text(current) }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        def.options.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    current = opt
                                    onCommit(opt)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        else -> {
            // "text"
            var text by remember(initial) { mutableStateOf(initial) }
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onCommit(it)
                    },
                    label = { Text(def.title) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (def.description.isNotBlank()) {
                    Text(
                        def.description,
                        style = small,
                        color = dim,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
