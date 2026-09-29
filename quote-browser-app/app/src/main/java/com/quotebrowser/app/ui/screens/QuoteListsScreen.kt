package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.QuoteList
import com.quotebrowser.app.data.QuoteListStore
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * v1.66 "My Collections": named, reorderable, persistent quote lists.
 * Create lists, rename/delete them, open one to reorder or prune its
 * quotes, play a list as audio, or send it to the export queue (which
 * includes the narrated slideshow-video export).
 */
@Composable
fun QuoteListsScreen(
    manager: DatabaseManager,
    listStore: QuoteListStore,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onPlay: (List<SourcedQuote>) -> Unit,
    onExportList: (QuoteList) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshTick by remember { mutableStateOf(0) }
    val lists = remember(refreshTick) { listStore.lists() }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<QuoteList?>(null) }
    var deleteTarget by remember { mutableStateOf<QuoteList?>(null) }
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    val selected = lists.firstOrNull { it.id == selectedId }
    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (selected == null) "My Collections" else selected.name,
            actions = {
                if (selected != null) {
                    IconButton(onClick = { selectedId = null }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back to lists")
                    }
                } else {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
        )
        if (selected == null) {
            if (lists.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            "No collections yet.\nLong-press any quote and pick “Add to list…”\nto start one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { showCreate = true }) {
                            Text("Create a list")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(lists) { list ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { selectedId = list.id },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        list.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${list.items.size} quote" +
                                            if (list.items.size == 1) "" else "s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { renameTarget = list }) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Rename list",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { deleteTarget = list }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete list",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = { showCreate = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "New list")
                    }
                }
            }
        } else {
            QuoteListDetail(
                manager = manager,
                listStore = listStore,
                list = selected,
                onChanged = { refreshTick++ },
                onQuoteClick = onQuoteClick,
                onPlay = onPlay,
                onExportList = { onExportList(selected) }
            )
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New list") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val created = listStore.create(name)
                    if (created == null) {
                        toast("Enter a name")
                    } else {
                        refreshTick++
                        showCreate = false
                        selectedId = created.id
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            }
        )
    }

    renameTarget?.let { target ->
        var name by remember(target.id) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename list") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (listStore.rename(target.id, name)) {
                        refreshTick++
                        renameTarget = null
                    } else {
                        toast("Enter a name")
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete “${target.name}”?") },
            text = { Text("The quotes stay in their databases — only the list goes away.") },
            confirmButton = {
                TextButton(onClick = {
                    listStore.delete(target.id)
                    if (selectedId == target.id) selectedId = null
                    refreshTick++
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

/** One open list: its quotes in order, with reorder/prune/play/export. */
@Composable
private fun QuoteListDetail(
    manager: DatabaseManager,
    listStore: QuoteListStore,
    list: QuoteList,
    onChanged: () -> Unit,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onPlay: (List<SourcedQuote>) -> Unit,
    onExportList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var quotes by remember(list.id, list.items.size) {
        mutableStateOf<List<SourcedQuote>?>(null)
    }
    LaunchedEffect(list.id, list.items) {
        quotes = withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                manager.quoteListQuotes(list.items)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
    val loaded = quotes
    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    val q = loaded
                    if (!q.isNullOrEmpty()) onPlay(q)
                },
                enabled = !loaded.isNullOrEmpty()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Play")
            }
            TextButton(
                onClick = onExportList,
                enabled = list.items.isNotEmpty()
            ) {
                Icon(Icons.Filled.IosShare, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Export")
            }
        }
        when {
            loaded == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            loaded.isEmpty() -> {
                EmptyState(
                    if (list.items.isEmpty()) "This list is empty."
                    else "None of these quotes are available right now."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(loaded, key = { _, sq -> "${sq.dbId}/${sq.quote.number}" }) { index, sq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQuoteClick(sq.dbId, sq.quote.number) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuoteNumberBadge(number = sq.quote.number)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = sq.quote.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    listStore.moveItem(list.id, index, index - 1)
                                    onChanged()
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = {
                                    listStore.moveItem(list.id, index, index + 1)
                                    onChanged()
                                },
                                enabled = index < loaded.size - 1
                            ) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
                            }
                            IconButton(onClick = {
                                listStore.removeItem(list.id, sq.dbId, sq.quote.number)
                                onChanged()
                            }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove from list",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

/**
 * v1.66 "Add to list…" picker: every list with a tap-to-toggle checkmark,
 * plus an inline new-list field. Shared by the detail screen and the
 * long-press action sheet.
 */
@Composable
fun AddToListDialog(
    listStore: QuoteListStore,
    dbId: String,
    number: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var refreshTick by remember { mutableStateOf(0) }
    val lists = remember(refreshTick) { listStore.lists() }
    val containing = remember(refreshTick) { listStore.containingLists(dbId, number).toSet() }
    var newName by remember { mutableStateOf("") }
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Add to list") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (lists.isEmpty()) {
                    Text(
                        "No lists yet — create one below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(lists) { list ->
                            val inList = list.id in containing
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (inList) {
                                            listStore.removeItem(list.id, dbId, number)
                                        } else {
                                            listStore.addItem(list.id, dbId, number)
                                        }
                                        refreshTick++
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (inList) Icons.Filled.Check else Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = if (inList) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        list.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (inList) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        "${list.items.size} quote" +
                                            if (list.items.size == 1) "" else "s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New list name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = {
                    val created = listStore.create(newName)
                    if (created == null) {
                        toast("Enter a name")
                    } else {
                        listStore.addItem(created.id, dbId, number)
                        newName = ""
                        refreshTick++
                    }
                }) {
                    Text("Create and add")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
