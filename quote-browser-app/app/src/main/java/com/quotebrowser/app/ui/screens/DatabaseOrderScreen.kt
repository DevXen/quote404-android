package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Arrange the order databases appear in the switcher and lists. "All
 * databases" is always pinned first and isn't listed here.
 */
@Composable
fun DatabaseOrderScreen(vm: MainViewModel) {
    val databases by vm.databases.collectAsStateWithLifecycle()
    val list = remember(databases) {
        databases.filter { it.id != DatabaseManager.ALL_DBS_ID }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Database order")
        if (list.isEmpty()) {
            EmptyState("No databases yet.")
        } else {
            Text(
                "Use the arrows to arrange how databases appear. " +
                    "The default order is Bash.org, #CrapShack Quotes, QDB.us, " +
                    "then the rest alphabetically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(Modifier.fillMaxSize(),
                flingBehavior = rememberCappedFlingBehavior()) {
                itemsIndexed(list, key = { _, db -> db.id }) { index, db ->
                    ListItem(
                        headlineContent = { Text(db.displayTitle()) },
                        supportingContent = {
                            if (db.quoteCount != null) Text("${db.quoteCount} quotes")
                        },
                        leadingContent = {
                            Icon(Icons.Filled.Storage, contentDescription = null)
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { vm.moveDatabase(index, index - 1) },
                                    enabled = index > 0
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowUpward,
                                        contentDescription = "Move up"
                                    )
                                }
                                IconButton(
                                    onClick = { vm.moveDatabase(index, index + 1) },
                                    enabled = index < list.lastIndex
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowDownward,
                                        contentDescription = "Move down"
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
