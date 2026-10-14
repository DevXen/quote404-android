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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.CatalogEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.NewContentOffer
import com.quotebrowser.app.data.PluginCatalogEntry
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.ui.rememberCappedFlingBehavior
import kotlinx.coroutines.launch

/** Friendly display name for a catalog plugin's pack type. */
private fun catalogPluginTypeLabel(type: String): String = when (type) {
    PluginManager.TYPE_QUOTE_PACK -> "Quote pack"
    PluginManager.TYPE_THEME_PACK -> "Theme pack"
    PluginManager.TYPE_LAYOUT_PACK -> "Quote layout"
    PluginManager.TYPE_EXPORT_STYLE -> "Export style"
    PluginManager.TYPE_GAME_PACK -> "Game pack"
    PluginManager.TYPE_VOICE_PACK -> "Voice pack"
    PluginManager.TYPE_VIDEO_BACKGROUND -> "Video background"
    else -> type.ifBlank { "Plugin" }
}

private fun dbKey(entry: CatalogEntry): String = "db:${entry.file}"
private fun pluginKey(entry: PluginCatalogEntry): String = "plugin:${entry.id}"
private fun updateKey(entry: PluginCatalogEntry): String = "update:${entry.id}"

/**
 * v1.89.4: the community-catalog offer. Shown once per cold start when
 * the startup check found new databases, new plugins, or plugin
 * updates (and from the manual "Check for new databases & plugins"
 * action, which ignores dismissals). Nothing installs without a tap:
 * "Install selected" downloads the checked items one by one with
 * per-item progress; "Not now" remembers every shown item as
 * dismissed so startup stays quiet until a newer version appears.
 *
 * Rendered as a full-screen overlay (AppNav) or inside a dialog (the
 * manual check); the root consumes stray taps either way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewContentScreen(
    manager: DatabaseManager,
    offer: NewContentOffer,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val installedPlugins by PluginManager.plugins
        .collectAsStateWithLifecycle()
    val selected = remember {
        mutableStateMapOf<String, Boolean>().apply {
            offer.newDatabases.forEach { put(dbKey(it), true) }
            offer.newPlugins.forEach { put(pluginKey(it), true) }
            offer.pluginUpdates.forEach { put(updateKey(it), true) }
        }
    }
    // key -> progress fraction while installing; key -> failure text;
    // keys that installed successfully.
    val progress = remember { mutableStateMapOf<String, Float>() }
    val failures = remember { mutableStateMapOf<String, String>() }
    val done = remember { mutableStateMapOf<String, Boolean>() }
    var installing by remember { mutableStateOf(false) }

    fun installSelected() {
        if (installing) return
        installing = true
        scope.launch {
            for (entry in offer.newDatabases) {
                val key = dbKey(entry)
                if (selected[key] != true || done[key] == true) continue
                failures.remove(key)
                progress[key] = 0f
                val result = manager.installDb(entry.file) { _, d, total ->
                    progress[key] =
                        if (total > 0) (d.toFloat() / total).coerceIn(0f, 1f)
                        else 0f
                }
                progress.remove(key)
                result.onSuccess {
                    done[key] = true
                    selected[key] = false
                }.onFailure { e ->
                    failures[key] = e.message ?: "Download failed."
                }
            }
            val pluginItems =
                offer.newPlugins.map { pluginKey(it) to it } +
                    offer.pluginUpdates.map { updateKey(it) to it }
            for ((key, entry) in pluginItems) {
                if (selected[key] != true || done[key] == true) continue
                failures.remove(key)
                progress[key] = 0f
                val result = manager.installCatalogPlugin(entry) { d, total ->
                    progress[key] =
                        if (total > 0) (d.toFloat() / total).coerceIn(0f, 1f)
                        else 0f
                }
                progress.remove(key)
                result.onSuccess {
                    done[key] = true
                    selected[key] = false
                }.onFailure { e ->
                    failures[key] = e.message ?: "Install failed."
                }
            }
            installing = false
        }
    }

    fun notNow() {
        manager.dismissNewContent(offer.dismissalKeys())
        onClose()
    }

    val chosenSize =
        offer.newDatabases.filter { selected[dbKey(it)] == true }
            .sumOf { it.size } +
            offer.newPlugins.filter { selected[pluginKey(it)] == true }
                .sumOf { it.size } +
            offer.pluginUpdates.filter { selected[updateKey(it)] == true }
                .sumOf { it.size }
    val chosenCount =
        offer.newDatabases.count { selected[dbKey(it)] == true } +
            offer.newPlugins.count { selected[pluginKey(it)] == true } +
            offer.pluginUpdates.count { selected[updateKey(it)] == true }
    val allDone = done.size > 0 &&
        offer.newDatabases.all { done[dbKey(it)] == true } &&
        offer.newPlugins.all { done[pluginKey(it)] == true } &&
        offer.pluginUpdates.all { done[updateKey(it)] == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New databases & plugins") },
                actions = {
                    IconButton(onClick = onClose, enabled = !installing) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when {
                            allDone -> "Everything installed"
                            chosenCount == 0 -> "Nothing selected"
                            else -> "$chosenCount selected · " +
                                formatDbSize(chosenSize)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { notNow() },
                        enabled = !installing
                    ) { Text("Not now") }
                    Button(
                        onClick = { installSelected() },
                        enabled = chosenCount > 0 && !installing
                    ) {
                        Text(if (installing) "Installing…" else "Install selected")
                    }
                }
                Text(
                    "\"Not now\" hides these until a newer version is " +
                        "published. Nothing installs without your tap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout
                    .PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                flingBehavior = rememberCappedFlingBehavior()
            ) {
                item {
                    Text(
                        "Fresh from the Quote404 community catalog on " +
                            "GitHub — pick what you want to add.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                if (offer.newDatabases.isNotEmpty()) {
                    item { OfferSectionHeader("New databases") }
                    items(offer.newDatabases, key = { dbKey(it) }) { entry ->
                        val key = dbKey(entry)
                        OfferRow(
                            title = entry.label
                                .ifBlank { entry.file.removeSuffix(".db") },
                            subtitle = buildString {
                                append(
                                    "${formatDbCount(entry.count)} quotes"
                                )
                                append(" · ${formatDbSize(entry.size)}")
                                entry.date?.takeIf { it.isNotBlank() }
                                    ?.let { append(" · $it") }
                            },
                            description = null,
                            checked = selected[key] == true,
                            onCheckedChange = { selected[key] = it },
                            enabled = !installing && done[key] != true,
                            progressFraction = progress[key],
                            failure = failures[key],
                            installed = done[key] == true
                        )
                    }
                }
                if (offer.newPlugins.isNotEmpty()) {
                    item { OfferSectionHeader("New plugins") }
                    items(offer.newPlugins, key = { pluginKey(it) }) { entry ->
                        val key = pluginKey(entry)
                        OfferRow(
                            title = entry.name,
                            subtitle = buildString {
                                if (entry.author.isNotBlank()) {
                                    append("by ${entry.author} · ")
                                }
                                append(catalogPluginTypeLabel(entry.type))
                                append(" · ${formatDbSize(entry.size)}")
                            },
                            description = entry.description
                                .takeIf { it.isNotBlank() },
                            checked = selected[key] == true,
                            onCheckedChange = { selected[key] = it },
                            enabled = !installing && done[key] != true,
                            progressFraction = progress[key],
                            failure = failures[key],
                            installed = done[key] == true
                        )
                    }
                }
                if (offer.pluginUpdates.isNotEmpty()) {
                    item { OfferSectionHeader("Plugin updates") }
                    items(offer.pluginUpdates, key = { updateKey(it) }) { entry ->
                        val key = updateKey(entry)
                        val installedVersion = installedPlugins
                            .firstOrNull { it.manifest.id == entry.id }
                            ?.manifest?.version.orEmpty()
                        OfferRow(
                            title = entry.name,
                            subtitle = buildString {
                                if (entry.author.isNotBlank()) {
                                    append("by ${entry.author} · ")
                                }
                                append(catalogPluginTypeLabel(entry.type))
                                append(" · ")
                                if (installedVersion.isNotBlank()) {
                                    append(
                                        "v$installedVersion → " +
                                            "v${entry.version}"
                                    )
                                } else {
                                    append("v${entry.version}")
                                }
                                append(" · ${formatDbSize(entry.size)}")
                            },
                            description = entry.description
                                .takeIf { it.isNotBlank() },
                            checked = selected[key] == true,
                            onCheckedChange = { selected[key] = it },
                            enabled = !installing && done[key] != true,
                            progressFraction = progress[key],
                            failure = failures[key],
                            installed = done[key] == true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferSectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun OfferRow(
    title: String,
    subtitle: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    progressFraction: Float?,
    failure: String?,
    installed: Boolean
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
                    checked = checked || installed,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (description != null) {
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (installed) {
                    Text(
                        "Installed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
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
            if (failure != null && !installed) {
                Spacer(Modifier.height(8.dp))
                Text(
                    failure,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
