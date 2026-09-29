package com.quotebrowser.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The Export Tray: quotes staged from anywhere in the app for combined
 * exports. Like Favorites, but temporary — stage quotes here, reorder
 * them into the order you want, then export (text, JSON, HTML page,
 * PNGs, slideshow MP4…) or share them all as text. The tray keeps its
 * contents across restarts until it is cleared here.
 */
@Composable
fun BasketScreen(
    vm: MainViewModel,
    onExportClick: () -> Unit,
    onQuoteClick: (dbId: String, number: Int) -> Unit
) {
    val context = LocalContext.current
    val basket by vm.basket.collectAsStateWithLifecycle()
    var quotes by remember { mutableStateOf<List<SourcedQuote>?>(null) }

    LaunchedEffect(basket) {
        quotes = withContext(Dispatchers.IO) { vm.basketQuotes() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ScreenHeader(title = "Export tray")
        Text(
            text = when {
                basket.isEmpty() -> "Nothing staged yet."
                else -> "${basket.size} quote${if (basket.size == 1) "" else "s"} staged. " +
                    "Use the arrows to set the export order — the tray keeps " +
                    "your quotes until you clear it."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        val resolved = quotes
        when {
            resolved == null -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            resolved.isEmpty() -> EmptyState(
                "Your export tray is empty.\n\n" +
                    "Long-press a quote anywhere and choose “Add to export tray”."
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        resolved,
                        key = { _, sq -> "${sq.dbId}:${sq.quote.number}" }
                    ) { index, sq ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQuoteClick(sq.dbId, sq.quote.number) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QuoteNumberBadge(number = sq.quote.number)
                                    Spacer(Modifier.width(8.dp))
                                    RatingBadge(sq.quote.rating)
                                    Spacer(Modifier.width(8.dp))
                                    SourceChip(label = vm.labelForDb(sq.dbId), dbId = sq.dbId)
                                    Spacer(Modifier.weight(1f))
                                    // Reorder: move this quote earlier/later in
                                    // the tray. Exports follow tray order.
                                    IconButton(
                                        onClick = { vm.moveBasketEntry(index, index - 1) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.ArrowUpward,
                                            contentDescription = "Move up"
                                        )
                                    }
                                    IconButton(
                                        onClick = { vm.moveBasketEntry(index, index + 1) },
                                        enabled = index < resolved.size - 1,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.ArrowDownward,
                                            contentDescription = "Move down"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            vm.removeFromBasket(sq.dbId, sq.quote.number)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Remove from tray"
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = sq.quote.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onExportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Export ${resolved.size} quote${if (resolved.size == 1) "" else "s"}")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val text = formatSourcedQuotesForExport(
                            resolved, "Export Tray", vm::labelForDb
                        )
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(
                            Intent.createChooser(send, "Share export tray")
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        Icons.Filled.IosShare,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Share all as text")
                }
                TextButton(
                    onClick = { vm.clearBasket() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Clear tray")
                }
            }
        }
    }
}
