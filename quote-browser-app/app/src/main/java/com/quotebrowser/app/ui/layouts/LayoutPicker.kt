package com.quotebrowser.app.ui.layouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.quotebrowser.app.data.layouts.LayoutDef
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * v1.85: layout picker. Shows every available layout (bundled, plugin
 * packs, custom styles) with a mini schematic of its blocks. The picked
 * layout changes the on-screen cards AND becomes the default for HTML
 * export — one definition, both surfaces.
 */
@Composable
fun LayoutPickerDialog(
    layouts: List<LayoutDef>,
    selectedId: String?,
    title: String = "Quote layout",
    /** When non-null, a leading "follow the default" row (used for per-db overrides). */
    defaultOptionLabel: String? = null,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "The layout arranges every element of a quote card — and is also used for HTML export.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    flingBehavior = rememberCappedFlingBehavior()) {
                    if (defaultOptionLabel != null) {
                        item(key = "__default__") {
                            DefaultLayoutRow(
                                label = defaultOptionLabel,
                                selected = selectedId.isNullOrBlank(),
                                onClick = { onSelect("") }
                            )
                        }
                    }
                    items(layouts, key = { it.id }) { layout ->
                        LayoutPickerRow(
                            layout = layout,
                            selected = layout.id == selectedId,
                            onClick = { onSelect(layout.id) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun DefaultLayoutRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun LayoutPickerRow(
    layout: LayoutDef,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = layout.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    layoutSourceBadge(layout)?.let { badge ->
                        Text(
                            text = "  ·  $badge",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (layout.description.isNotEmpty()) {
                    Text(
                        text = layout.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Mini schematic: one chip per block.
                Text(
                    text = layout.blocks.joinToString(" → ") { blockLabel(it.type) },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun layoutSourceBadge(layout: LayoutDef): String? = when {
    layout.id.startsWith("plugin:") -> "Plugin pack"
    layout.id.startsWith("custom:") -> "Custom"
    layout.source == "custom" -> "Custom"
    else -> null
}
