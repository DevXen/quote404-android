package com.quotebrowser.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DbInfo

/**
 * Lets the user pull quotes from all databases, or pick exactly which ones.
 * Null selection = all.
 */
@Composable
fun DatabaseMultiSelectDialog(
    title: String,
    databases: List<DbInfo>,
    selected: Set<String>?,
    onConfirm: (Set<String>?) -> Unit,
    onDismiss: () -> Unit,
    /** v1.57: chrome accent for the rounded dialog (null = theme primary). */
    accent: androidx.compose.ui.graphics.Color? = null,
    /** v1.57: when set, each database row gets an info button (parity with
     * the old single-select switcher's per-database info panels). */
    onInfoClick: ((String) -> Unit)? = null,
) {
    var all by remember { mutableStateOf(selected == null) }
    // v1.88.20: Browse/Search callers pass browseDatabases(), whose first
    // entry is the synthetic "All databases" row — but this dialog already
    // renders its own "All databases" toggle at the top, so the synthetic
    // entry is filtered out of the per-database list (it used to render a
    // duplicate "All databases" row). Its info affordance moves onto the
    // top toggle row instead.
    val allEntry = databases.firstOrNull { it.id == DatabaseManager.ALL_DBS_ID }
    val listDbs = remember(databases) {
        databases.filter { it.id != DatabaseManager.ALL_DBS_ID }
    }
    var checked by remember { mutableStateOf(selected ?: listDbs.map { it.id }.toSet()) }
    val allIds = remember(listDbs) { listDbs.map { it.id }.toSet() }
    // v1.49: everything counts as selected when "All databases" is on or
    // every database is individually checked.
    val everythingSelected = all || (checked.isNotEmpty() && checked.containsAll(allIds))

    AccentAlertDialog(
        onDismissRequest = onDismiss,
        accent = accent,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // v1.49: top Select all / Deselect all toggle.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (everythingSelected) {
                                // Deselect all: clearing deselects everything.
                                all = false
                                checked = emptySet()
                            } else {
                                all = false
                                checked = allIds
                            }
                        }
                    ) {
                        Text(if (everythingSelected) "Deselect all" else "Select all")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            all = !all
                            // v1.49: clearing "All databases" deselects
                            // everything instead of restoring a stale set.
                            if (all) checked = allIds else checked = emptySet()
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = all,
                        onCheckedChange = {
                            all = it
                            if (all) checked = allIds else checked = emptySet()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        allEntry?.displayTitle() ?: "All databases",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    // v1.88.20: the synthetic entry's (i) button lives on
                    // the top toggle row now that the duplicate list row
                    // is gone. Only shown when a caller actually passed the
                    // synthetic entry and wants info buttons.
                    if (onInfoClick != null && allEntry != null) {
                        IconButton(
                            onClick = { onInfoClick(allEntry.id) }
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Database info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                listDbs.forEach { db ->
                    val isChecked = all || db.id in checked
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !all) {
                                checked = if (db.id in checked) checked - db.id else checked + db.id
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            enabled = !all,
                            onCheckedChange = {
                                checked = if (it) checked + db.id else checked - db.id
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            db.displayTitle(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (onInfoClick != null) {
                            IconButton(
                                onClick = { onInfoClick(db.id) }
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Database info",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (!all && checked.isEmpty()) {
                    Text(
                        "Pick at least one database, or use All databases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(if (all) null else checked)
                    onDismiss()
                },
                enabled = all || checked.isNotEmpty()
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
