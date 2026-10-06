package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.repoFor
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import com.quotebrowser.app.data.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * v1.88.2 dedicated "On this day" screen, rewritten for honesty: "On this
 * day" means quotes genuinely POSTED on today's month/day in past years
 * (bash.im — the only bundled database with real per-record posting
 * dates). qdb.us Wayback capture dates get their own clearly-labeled
 * section (archived-on, not posted-on), and the old deterministic
 * "deep cuts" randoms are gone — random quotes were never "on this day".
 * The user's own history (favorited/read on this date in earlier years)
 * stays as its own section.
 */
@Composable
fun OnThisDayScreen(
    manager: DatabaseManager,
    showNumber: Boolean,
    showRating: Boolean,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var posted by remember {
        mutableStateOf<List<DatabaseManager.PostedDayQuote>?>(null)
    }
    var captured by remember { mutableStateOf<List<SourcedQuote>?>(null) }
    var memoryLane by remember {
        mutableStateOf<List<DatabaseManager.MemoryLaneEntry>?>(null)
    }

    LaunchedEffect(Unit) {
        posted = try {
            manager.postedOnThisDay(12)
        } catch (e: Exception) {
            emptyList()
        }
        captured = try {
            manager.capturedOnThisDay(12)
        } catch (e: Exception) {
            emptyList()
        }
        memoryLane = try {
            manager.onThisDay(12)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val today = remember {
        try {
            val now = java.time.LocalDate.now()
            now.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d"))
        } catch (e: Exception) {
            "today"
        }
    }

    if (posted == null || captured == null || memoryLane == null) {
        Column(
            modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Digging through the archives…")
        }
        return
    }

    val postedList = posted!!
    val capturedList = captured!!
    val laneList = memoryLane!!

    if (postedList.isEmpty() && capturedList.isEmpty() && laneList.isEmpty()) {
        Column(
            modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Nothing from $today in the archives yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // v1.58: this screen mixes databases, so site cards keep the global
    // page background outside the card (no single-DB page backdrop).
    CompositionLocalProvider(LocalSingleDatabaseMode provides false) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        flingBehavior = rememberCappedFlingBehavior()) {
        item {
            Text(
                text = "On this day — $today",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        if (postedList.isNotEmpty()) {
            item {
                Text(
                    text = "Posted on this day",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Text(
                    text = "Quotes originally posted on $today in past years.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(
                items = postedList,
                key = { (it.dbId + "/" + it.quote.number) }
            ) { entry ->
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = entry.postedDate.substring(0, 4),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp
                        )
                    )
                    DayQuoteRow(
                        manager = manager,
                        dbId = entry.dbId,
                        quote = entry.quote,
                        showNumber = showNumber,
                        showRating = showRating,
                        onQuoteClick = onQuoteClick
                    )
                }
            }
        }
        if (capturedList.isNotEmpty()) {
            item {
                Text(
                    text = "From the Wayback archive",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Text(
                    text = "qdb.us quotes the Wayback Machine captured on $today — capture dates, not necessarily the dates they were posted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(
                items = capturedList,
                key = { (it.dbId + "/" + it.quote.number) }
            ) { sq ->
                DayQuoteRow(
                    manager = manager,
                    dbId = sq.dbId,
                    quote = sq.quote,
                    showNumber = showNumber,
                    showRating = showRating,
                    onQuoteClick = onQuoteClick
                )
            }
        }
        if (laneList.isNotEmpty()) {
            item {
                Text(
                    text = "Your history",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    )
                )
                Text(
                    text = "Quotes you favorited or read on $today in past years.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(
                items = laneList,
                key = { (it.dbId + "/" + it.quote.number + "/" + it.year) }
            ) { entry ->
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = "${entry.year} · ${entry.kind}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp
                        )
                    )
                    DayQuoteRow(
                        manager = manager,
                        dbId = entry.dbId,
                        quote = entry.quote,
                        showNumber = showNumber,
                        showRating = showRating,
                    onQuoteClick = onQuoteClick
                    )
                }
            }
        }
    }
    } // LocalSingleDatabaseMode
}

/**
 * QuoteRow wrapper for the On-this-day lists: wires the required favorite
 * state to the quote's own database so rows match the rest of the app.
 */
@Composable
private fun DayQuoteRow(
    manager: DatabaseManager,
    dbId: String,
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    onQuoteClick: (dbId: String, number: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    // v1.88.12: repoFor() can do disk work on a cold open — resolve off
    // the main thread during composition and show the row once ready.
    val repo by produceState<QuoteRepository?>(initialValue = null, manager, dbId) {
        value = withContext(Dispatchers.IO) { manager.repoFor(dbId) }
    }
    val isFav = repo?.isFavorite(quote.number)?.collectAsState(initial = false)?.value ?: false
    QuoteRow(
        quote = quote,
        dbId = dbId,
        isFavorite = isFav,
        onToggleFavorite = { scope.launch { repo?.toggleFavorite(quote) } },
        onClick = { onQuoteClick(dbId, quote.number) },
        showNumber = showNumber,
        showRating = showRating,
        sourceLabel = null
    )
}
