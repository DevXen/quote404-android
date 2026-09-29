package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader

/**
 * Dedicated blocked-words manager: the filter toggle, bulk paste
 * (comma/newline/semicolon separated), one-tap suggested word sets,
 * individual removal, multi-select bulk removal, and confirmed clear-all.
 */
@Composable
fun BlockedWordsScreen(
    vm: MainViewModel,
    onHiddenQuotesClick: () -> Unit = {},
    onMutedNicksClick: () -> Unit = {}
) {
    val words by vm.offensiveWords.collectAsStateWithLifecycle()
    val filterEnabled by vm.contentFilterEnabled.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenQuoteCount.collectAsStateWithLifecycle()
    val mutedCount = vm.mutedNicks.collectAsStateWithLifecycle().value.size
    var bulkText by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    val context = LocalContext.current

    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun exitSelection() {
        selectionMode = false
        selected.clear()
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Content Filter")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                                "Hide offensive quotes",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Whole-word, case-insensitive matching",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = filterEnabled,
                            onCheckedChange = vm::setContentFilterEnabled
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    onClick = onHiddenQuotesClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Hidden quotes",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                if (hiddenCount == 0) "Nothing hidden via \"Never show again\""
                                else "$hiddenCount quote${if (hiddenCount == 1) "" else "s"} hidden via \"Never show again\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    onClick = onMutedNicksClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Muted nicks",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                if (mutedCount == 0) "No nicks muted"
                                else "$mutedCount nick${if (mutedCount == 1) "" else "s"} muted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    "Add words — paste a list separated by commas, newlines, or semicolons.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    value = bulkText,
                    onValueChange = { bulkText = it },
                    label = { Text("Words to block") },
                    placeholder = { Text("word1, word2\nword3") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val (added, skipped) = vm.addOffensiveWordsBulk(bulkText)
                            toast(
                                when {
                                    added == 0 && skipped == 0 -> "No words entered"
                                    added == 0 -> "All words were already on the list"
                                    skipped == 0 -> "$added word${if (added == 1) "" else "s"} added"
                                    else -> "$added added, $skipped already on the list"
                                }
                            )
                            bulkText = ""
                        },
                        enabled = bulkText.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add words")
                    }
                    TextButton(
                        onClick = { confirmClear = true },
                        enabled = words.isNotEmpty()
                    ) {
                        Text("Clear all")
                    }
                }
            }
            item {
                Text(
                    "Suggested sets",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Text(
                    "Tap Add to block a whole set at once. You can remove individual words afterwards.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(SUGGESTED_WORD_SETS, key = { it.id }) { set ->
                val alreadyAll = set.words.all { it in words }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                                set.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "${set.words.size} words · ${set.words.take(5).joinToString(", ")}…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (alreadyAll) {
                            Text(
                                "Added",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val (added, skipped) = vm.addOffensiveWordList(set.words)
                                    toast(
                                        when {
                                            added == 0 -> "Set already fully added"
                                            skipped == 0 -> "${set.title}: $added words added"
                                            else -> "${set.title}: $added added, $skipped already listed"
                                        }
                                    )
                                }
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${words.size} blocked word${if (words.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectionMode) {
                        val allSelected = words.isNotEmpty() && selected.size == words.size
                        TextButton(onClick = {
                            selected.clear()
                            if (!allSelected) selected.addAll(words)
                        }) {
                            Text(if (allSelected) "Deselect all" else "Select all")
                        }
                        TextButton(onClick = ::exitSelection) {
                            Text("Done")
                        }
                    } else {
                        TextButton(
                            onClick = { selectionMode = true },
                            enabled = words.isNotEmpty()
                        ) {
                            Text("Select")
                        }
                    }
                }
            }
            if (words.isEmpty()) {
                item {
                    EmptyState("No blocked words.\nAdd some above to start filtering.")
                }
            } else {
                items(words.sorted(), key = { it }) { word ->
                    val isSelected = word in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectionMode) {
                                if (isSelected) selected.remove(word) else selected.add(word)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selected.add(word) else selected.remove(word)
                                }
                            )
                        }
                        Text(
                            text = word,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (!selectionMode) {
                            IconButton(onClick = { vm.removeOffensiveWord(word) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Remove $word",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            if (selectionMode && selected.isNotEmpty()) {
                item {
                    Button(
                        onClick = {
                            vm.removeOffensiveWords(selected.toList())
                            toast("${selected.size} word${if (selected.size == 1) "" else "s"} removed")
                            exitSelection()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete selected (${selected.size})")
                    }
                }
            }
            item {
                Spacer(Modifier.padding(4.dp))
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all blocked words?") },
            text = { Text("Removes all ${words.size} blocked words. Quotes they hid will show again.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearOffensiveWords()
                    confirmClear = false
                    exitSelection()
                    toast("Blocked words cleared")
                }) { Text("Clear all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}
