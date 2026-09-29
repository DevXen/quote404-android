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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.ui.BulkCategorizeViewModel
import com.quotebrowser.app.ui.BulkResult
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.rememberNickAnnotatedText

/**
 * Search quotes across any combination of databases, checkbox-select the
 * hits (with select-all), and add them to one or more categories — or a
 * brand-new category typed on the spot.
 */
@Composable
fun BulkCategorizeScreen(
    vm: BulkCategorizeViewModel,
    onBack: () -> Unit,
    onOpenQuote: (dbId: String, number: Int) -> Unit
) {
    val databases by vm.databases.collectAsStateWithLifecycle()
    val selectedDbIds by vm.selectedDbIds.collectAsStateWithLifecycle()
    val searching by vm.searching.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    var showCategoryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearStatus()
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Add to categories",
            actions = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = vm.query,
                onValueChange = vm::updateQuery,
                label = { Text("Search all selected databases") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.search() })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { vm.search() }, enabled = vm.query.isNotBlank() && !searching) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item { Spacer(Modifier.width(8.dp)) }
            item {
                val allSelected = selectedDbIds == null
                FilterChip(
                    selected = allSelected,
                    onClick = { vm.selectAllDbs() },
                    label = { Text("All") }
                )
            }
            items(databases, key = { it.id }) { db ->
                val allIds = databases.map { it.id }.toSet()
                val cur = selectedDbIds ?: allIds
                FilterChip(
                    selected = db.id in cur,
                    onClick = { vm.toggleDb(db.id) },
                    label = { Text(db.label) }
                )
            }
            item { Spacer(Modifier.width(8.dp)) }
        }
        Spacer(Modifier.height(4.dp))

        if (searching) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (results.isNotEmpty()) {
            val allKeys = results.map { it.dbId to it.quote.number }.toSet()
            val triState = when {
                selected.isEmpty() -> ToggleableState.Off
                selected.containsAll(allKeys) -> ToggleableState.On
                else -> ToggleableState.Indeterminate
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (triState == ToggleableState.On) vm.clearSelection()
                        else vm.selectAll()
                    }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TriStateCheckbox(
                    state = triState,
                    onClick = {
                        if (triState == ToggleableState.On) vm.clearSelection()
                        else vm.selectAll()
                    }
                )
                Text(
                    text = "${results.size} results · ${selected.size} selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(results, key = { "${it.dbId}:${it.quote.number}" }) { result ->
                    BulkResultRow(
                        result = result,
                        checked = (result.dbId to result.quote.number) in selected,
                        onToggle = { vm.toggleSelect(result) },
                        onOpen = { onOpenQuote(result.dbId, result.quote.number) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
            if (selected.isNotEmpty()) {
                BottomAppBar {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${selected.size} selected",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = { showCategoryDialog = true }) {
                        Text("Add to categories")
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Search for quotes, tick the ones you want, then add\nthem all to one or more categories at once.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }

    if (showCategoryDialog) {
        AddToCategoriesDialog(
            vm = vm,
            onDismiss = { showCategoryDialog = false },
            onApplied = { showCategoryDialog = false }
        )
    }
}

@Composable
private fun BulkResultRow(
    result: BulkResult,
    checked: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpen)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "#${result.quote.number} · ${result.dbLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = rememberNickAnnotatedText(
                        result.quote.text,
                        MaterialTheme.typography.bodyMedium
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Pick existing categories and/or type a new one, then apply to the selection. */
@Composable
private fun AddToCategoriesDialog(
    vm: BulkCategorizeViewModel,
    onDismiss: () -> Unit,
    onApplied: () -> Unit
) {
    val names by vm.allCategoryNames.collectAsStateWithLifecycle()
    val selectedCount by vm.selected.collectAsStateWithLifecycle()
    var checked by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${selectedCount.size} to categories") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New category name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (names.isNotEmpty()) {
                    Text(
                        text = "Existing categories",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(names) { name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        checked =
                                            if (name in checked) checked - name else checked + name
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = name in checked,
                                    onCheckedChange = {
                                        checked =
                                            if (name in checked) checked - name else checked + name
                                    }
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No categories yet — type a name above to create the first one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            val canApply = checked.isNotEmpty() || newName.isNotBlank()
            Button(
                onClick = {
                    val apply = checked.toMutableList()
                    if (newName.isNotBlank()) apply.add(newName.trim())
                    vm.applyCategories(apply)
                    onApplied()
                },
                enabled = canApply
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
