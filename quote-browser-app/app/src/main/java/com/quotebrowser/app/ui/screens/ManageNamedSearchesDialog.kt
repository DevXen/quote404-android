package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import com.quotebrowser.app.data.NamedSearch

/**
 * Rename/delete management for named saved searches.
 */
@Composable
fun ManageNamedSearchesDialog(
    searches: List<NamedSearch>,
    onRename: (oldName: String, newName: String) -> Boolean,
    onDelete: (name: String) -> Unit,
    onAlertToggle: (name: String, alert: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var editing by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Saved searches",
                    style = MaterialTheme.typography.titleLarge
                )
                if (searches.isEmpty()) {
                    Text(
                        "No saved searches yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searches, key = { it.name }) { ns ->
                            if (editing == ns.name) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editText,
                                        onValueChange = { editText = it },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            if (onRename(ns.name, editText)) {
                                                editing = null
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Name taken or blank",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    ) { Text("OK") }
                                }
                            } else {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                ns.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                describeNamedSearch(ns),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                editing = ns.name
                                                editText = ns.name
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "Rename ${ns.name}"
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (editing == ns.name) editing = null
                                                onDelete(ns.name)
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Delete ${ns.name}"
                                            )
                                        }
                                    }
                                    // v1.48 search alerts: opt-in per search,
                                    // persisted immediately on change.
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = ns.alertOnNewMatch,
                                            onCheckedChange = { checked ->
                                                onAlertToggle(ns.name, checked)
                                            }
                                        )
                                        Text(
                                            "Alert me when new quotes match",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
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

private fun describeNamedSearch(ns: NamedSearch): String {
    val parts = mutableListOf<String>()
    if (ns.query.isNotBlank()) {
        val q = if (ns.query.length > 32) ns.query.take(32) + "…" else ns.query
        parts += "\"$q\""
    }
    parts += when {
        ns.dbIds == null -> "This database"
        ns.dbIds.size == 1 -> "1 database"
        else -> "${ns.dbIds.size} databases"
    }
    if (ns.categoryIds.isNotEmpty()) {
        parts += if (ns.categoryIds.size == 1) "1 category"
        else "${ns.categoryIds.size} categories"
    }
    if (ns.minRating != null) parts += "rating ≥ ${ns.minRating}"
    if (ns.notesScope) parts += "notes"
    if (ns.useRegex) parts += "regex"
    if (ns.lengthMode.name != "ALL") parts += "length: ${ns.lengthMode.label}"
    parts += "sort: ${ns.sortOrder.label}"
    return parts.joinToString(" • ")
}
