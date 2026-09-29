package com.quotebrowser.app.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.ui.NotesViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip

/**
 * All personal notes: note text plus a preview of the quote it belongs
 * to. Tapping opens the quote's detail screen.
 */
@Composable
fun NotesScreen(
    vm: NotesViewModel,
    onQuoteClick: (dbId: String, number: Int) -> Unit
) {
    val notes by vm.entries.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Notes (${notes.size})")
        if (notes.isEmpty()) {
            EmptyState("No notes yet.\nOpen a quote's detail page to add one.")
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(notes, key = { "${it.dbId}/${it.number}" }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        onClick = { onQuoteClick(entry.dbId, entry.number) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                QuoteNumberBadge(number = entry.number)
                                Spacer(Modifier.width(8.dp))
                                SourceChip(label = entry.dbLabel)
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { vm.deleteNote(entry.dbId, entry.number) }) {
                                    Text("Delete")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = entry.note,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            val preview = entry.quoteText
                            if (!preview.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = preview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
