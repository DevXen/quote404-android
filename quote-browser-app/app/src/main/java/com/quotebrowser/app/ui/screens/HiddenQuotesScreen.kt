package com.quotebrowser.app.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Quotes hidden via "Never show again". Each row can be restored
 * individually; "Clear all" restores everything after confirmation.
 * Tapping a row opens the quote's detail screen.
 */
@Composable
fun HiddenQuotesScreen(
    vm: MainViewModel,
    onQuoteClick: (dbId: String, number: Int) -> Unit
) {
    val keys by vm.hiddenQuoteKeys.collectAsStateWithLifecycle()
    var entries by remember { mutableStateOf<List<SourcedQuote>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(keys) {
        loading = true
        entries = withContext(Dispatchers.IO) { vm.hiddenQuoteDetails() }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Hidden quotes (${entries.size})",
            actions = {
                if (entries.isNotEmpty()) {
                    TextButton(onClick = { confirmClear = true }) {
                        Text("Clear all")
                    }
                }
            }
        )
        if (loading && entries.isEmpty()) {
            EmptyState("Loading…")
        } else if (entries.isEmpty()) {
            EmptyState("No hidden quotes.\nUse \"Never show again\" on a quote to hide it here.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                flingBehavior = rememberCappedFlingBehavior()) {
                items(entries, key = { "${it.dbId}/${it.quote.number}" }) { sq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        onClick = { onQuoteClick(sq.dbId, sq.quote.number) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                QuoteNumberBadge(number = sq.quote.number)
                                Spacer(Modifier.width(8.dp))
                                SourceChip(
                                    label = vm.labelForDb(sq.dbId),
                                    dbId = sq.dbId
                                )
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    onClick = { vm.unhideQuote(sq.dbId, sq.quote.number) }
                                ) {
                                    Text("Restore")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = sq.quote.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Restore all hidden quotes?") },
            text = { Text("This unhides every quote you marked \"Never show again\".") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearHiddenQuotes()
                    confirmClear = false
                }) { Text("Restore all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}
