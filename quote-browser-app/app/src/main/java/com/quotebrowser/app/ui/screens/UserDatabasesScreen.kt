package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.UserDbInfo
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.repoFor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

private val CreatedDateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatCreated(createdAt: Long): String = try {
    if (createdAt <= 0L) ""
    else Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault())
        .toLocalDate().format(CreatedDateFormat)
} catch (_: Exception) {
    ""
}

/**
 * v1.88.36: "My Databases" — the user's own named quote databases. Each
 * row opens full add/edit/delete quote management (the generalized
 * MyQuotesScreen); the FAB creates a new database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDatabasesScreen(
    manager: DatabaseManager,
    onOpenDb: (id: String, label: String) -> Unit,
    onBack: () -> Unit
) {
    val userDbs by manager.userDbs.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf<UserDbInfo?>(null) }
    var renameName by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<UserDbInfo?>(null) }

    // Quote counts per database, loaded off the main thread and cached.
    val counts = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(userDbs) {
        val fresh = withContext(Dispatchers.IO) {
            userDbs.associate { info ->
                info.id to try {
                    manager.repoFor(info.id).count()
                } catch (_: Exception) {
                    0
                }
            }
        }
        counts.clear()
        counts.putAll(fresh)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Databases") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { createName = ""; showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New database")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (userDbs.isEmpty()) {
                EmptyState(
                    "No databases yet — tap + to create your first one.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    flingBehavior = rememberCappedFlingBehavior()) {
                    items(userDbs, key = { it.id }) { info ->
                        UserDbRow(
                            info = info,
                            count = counts[info.id],
                            onOpen = { onOpenDb(info.id, info.label) },
                            onRename = { renameName = info.label; renaming = info },
                            onDelete = { deleting = info }
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New database") },
            text = {
                OutlinedTextField(
                    value = createName,
                    onValueChange = { createName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    manager.createUserDb(createName)
                    showCreate = false
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            }
        )
    }

    renaming?.let { info ->
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Rename database") },
            text = {
                OutlinedTextField(
                    value = renameName,
                    onValueChange = { renameName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    manager.renameUserDb(info.id, renameName)
                    renaming = null
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renaming = null }) { Text("Cancel") }
            }
        )
    }

    deleting?.let { info ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete database?") },
            text = {
                Text(
                    "Delete \"${info.label}\" and all of its quotes? " +
                        "This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    manager.deleteUserDb(info.id)
                    deleting = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun UserDbRow(
    info: UserDbInfo,
    count: Int?,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onOpen),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(info.label, style = MaterialTheme.typography.titleMedium)
                val created = formatCreated(info.createdAt)
                val sub = buildString {
                    append(
                        when (count) {
                            null -> "…"
                            1 -> "1 quote"
                            else -> "$count quotes"
                        }
                    )
                    if (created.isNotEmpty()) append(" · $created")
                }
                Text(
                    sub,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Database options")
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { menuOpen = false; onRename() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { menuOpen = false; onDelete() }
                )
            }
        }
    }
}
