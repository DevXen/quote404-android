package com.quotebrowser.app.ui.layouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.layouts.LayoutStore
import com.quotebrowser.app.data.plugins.PluginManager

/**
 * v1.85: the Settings → Quote layouts section content. Global default
 * layout, per-database overrides, and the custom HTML/CSS editor entry.
 */
@Composable
fun QuoteLayoutsSettingsContent(
    themeDbs: List<DbInfo>,
    onLayoutEditorClick: () -> Unit
) {
    val context = LocalContext.current
    // Bumped whenever a layout choice changes so names re-resolve.
    var version by remember { mutableStateOf(0) }
    var globalPickerOpen by remember { mutableStateOf(false) }
    var dbPickerFor by remember { mutableStateOf<DbInfo?>(null) }

    val layouts = remember(version) {
        LayoutStore.allLayouts(context, PluginManager.pluginLayouts)
    }
    fun layoutName(id: String?): String =
        layouts.firstOrNull { it.id == id }?.name ?: "Classic"
    val globalId = remember(version) { LayoutStore.globalLayoutId(context) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "A layout arranges the elements of a quote card — header, text, " +
                        "author, rating, details, actions. Colors and fonts still come " +
                        "from the theme. The layout also drives HTML export.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { globalPickerOpen = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Default layout", style = MaterialTheme.typography.titleSmall)
                        Text(
                            layoutName(globalId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text("Change", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onLayoutEditorClick) {
                    Text("Customize HTML / CSS…")
                }
            }
        }

        if (themeDbs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Per-database overrides",
                        style = MaterialTheme.typography.titleSmall
                    )
                    themeDbs.forEach { db ->
                        val overrideId = remember(version) {
                            LayoutStore.dbLayoutId(context, db.id)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dbPickerFor = db }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(db.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (overrideId == null) "Default (${layoutName(globalId)})"
                                    else layoutName(overrideId),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text("Change", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (globalPickerOpen) {
        LayoutPickerDialog(
            layouts = layouts,
            selectedId = globalId,
            title = "Default quote layout",
            onSelect = {
                LayoutStore.setGlobalLayoutId(context, it)
                version++
                globalPickerOpen = false
            },
            onDismiss = { globalPickerOpen = false }
        )
    }
    dbPickerFor?.let { db ->
        LayoutPickerDialog(
            layouts = layouts,
            selectedId = LayoutStore.dbLayoutId(context, db.id),
            title = "Layout for ${db.label}",
            defaultOptionLabel = "Default (follows the global layout)",
            onSelect = {
                LayoutStore.setDbLayoutId(context, db.id, it.ifBlank { null })
                version++
                dbPickerFor = null
            },
            onDismiss = { dbPickerFor = null }
        )
    }
}
