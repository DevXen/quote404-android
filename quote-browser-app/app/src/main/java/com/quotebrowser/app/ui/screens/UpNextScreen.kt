package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.QueueStore
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * "Up Next" queue: the user's ordered list of quotes to read next. Each
 * row shows its position, a quote snippet, and the source database, with
 * move-up / move-down / remove buttons; tapping a row opens the quote.
 * "Clear" empties the queue after confirmation.
 *
 * Quote text is loaded per row with [DatabaseManager.repoFor] +
 * [QuoteRepository.peekQuote] (read-only, no history side-effects), the
 * same by-number lookup the detail screen's view model uses.
 */
@Composable
fun UpNextScreen(
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember { QueueStore(context) }
    var entries by remember { mutableStateOf(store.queue()) }
    var quotes by remember { mutableStateOf<Map<Pair<String, Int>, Quote>>(emptyMap()) }
    var confirmClear by remember { mutableStateOf(false) }

    fun refresh() {
        entries = store.queue()
    }

    // Load the quote text for each queued key (DB reads stay off main).
    LaunchedEffect(entries) {
        quotes = withContext(Dispatchers.IO) {
            entries.mapNotNull { (dbId, number) ->
                try {
                    manager.repoFor(dbId).peekQuote(number)
                        ?.let { (dbId to number) to it }
                } catch (_: Exception) {
                    // A database that can't be read contributes nothing.
                    null
                }
            }.toMap()
        }
    }

    val currentPos = remember(entries) { store.position() }

    Column(modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Up Next (${entries.size})",
            actions = {
                if (entries.isNotEmpty()) {
                    TextButton(onClick = { confirmClear = true }) {
                        Text("Clear")
                    }
                }
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        if (entries.isEmpty()) {
            EmptyState(
                "Your Up Next queue is empty.\n" +
                    "Use \"Add to Up Next\" on any quote's detail screen " +
                    "to build a reading list."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = entries.size,
                    key = { i -> "${entries[i].first}/${entries[i].second}" }
                ) { i ->
                    val (dbId, number) = entries[i]
                    UpNextRow(
                        position = i + 1,
                        isCurrent = i == currentPos,
                        dbLabel = manager.shortLabel(dbId),
                        number = number,
                        snippet = quotes[dbId to number]
                            ?.text
                            ?.replace("\n", " ")
                            ?.trim()
                            ?.take(160),
                        onOpen = {
                            store.setPosition(i)
                            onQuoteClick(dbId, number)
                        },
                        onMoveUp = {
                            if (i > 0) {
                                store.move(i, i - 1)
                                refresh()
                            }
                        },
                        onMoveDown = {
                            if (i < entries.size - 1) {
                                store.move(i, i + 1)
                                refresh()
                            }
                        },
                        onRemove = {
                            store.removeAt(i)
                            refresh()
                        },
                        canMoveUp = i > 0,
                        canMoveDown = i < entries.size - 1
                    )
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear Up Next?") },
            text = { Text("This removes all ${entries.size} queued quotes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.clear()
                        refresh()
                        confirmClear = false
                    }
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

/** One queued quote: position, snippet, database label, reorder controls. */
@Composable
private fun UpNextRow(
    position: Int,
    isCurrent: Boolean,
    dbLabel: String,
    number: Int,
    snippet: String?,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean
) {
    Card(
        modifier = Modifier.clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snippet ?: "Quote #$number",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$dbLabel · #$number",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(
                    Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Move up"
                )
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Move down"
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove from Up Next"
                )
            }
        }
    }
}
