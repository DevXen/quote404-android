package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.HistoryItem
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.HistoryViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.HiddenQuotesBanner
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.QuoteActionSheetHost
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.components.QuoteSelectionHost
import com.quotebrowser.app.ui.components.SelectedQuote
import com.quotebrowser.app.ui.components.rememberQuoteSelection
import com.quotebrowser.app.ui.exportQuotesToUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

@Composable
fun HistoryScreen(vm: HistoryViewModel, manager: DatabaseManager, onQuoteClick: (Int) -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    onAddToPlaylist: (List<SourcedQuote>) -> Unit = {},
    /** v1.74.7: the quote sheet's "Read aloud" row reads one quote aloud. */
    onReadAloudQuote: (SourcedQuote) -> Unit = {},
    onNickClick: (String) -> Unit = {},
    // v1.77: opening a quote with its true source database (used in the
    // virtual "all" view, where each entry keeps its own dbId). When null,
    // falls back to [onQuoteClick].
    onSourcedQuoteClick: ((String, Int) -> Unit)? = null) {
    val historyItems by vm.historyItems.collectAsStateWithLifecycle()
    // Plain quotes for the export and multi-select flows.
    val history = remember(historyItems) { historyItems.map { it.quote } }
    val allFavKeys by vm.allFavoriteKeys.collectAsStateWithLifecycle()
    // v1.53 report/flag.
    val flaggedQuotes by manager.flaggedQuotes.collectAsStateWithLifecycle()
    val dbId by vm.dbId.collectAsStateWithLifecycle()
    // v1.77: the virtual "all" view mixes databases — each entry keeps its
    // true source dbId (the old path collapsed to the first bundled DB).
    val isAll = dbId == DatabaseManager.ALL_DBS_ID
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sheetItem by remember { mutableStateOf<HistoryItem?>(null) }
    val openSourced: (String, Int) -> Unit = { itemDbId, number ->
        if (onSourcedQuoteClick != null) onSourcedQuoteClick(itemDbId, number)
        else onQuoteClick(number)
    }
    // v1.53: "Clear history" asks for confirmation first.
    var confirmClear by remember { mutableStateOf(false) }
    // v1.53: which entry's full view-datetime list is expanded ("dbId/number").
    var expandedKey by remember { mutableStateOf<String?>(null) }
    val selection = rememberQuoteSelection()
    val showFiltered by vm.showFiltered.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenCount.collectAsStateWithLifecycle()

    // The temporary filter bypass is per-screen: drop it when leaving.
    DisposableEffect(Unit) { onDispose { vm.setShowFiltered(false) } }

    val allCandidates = historyItems.map {
        SelectedQuote(it.dbId.ifEmpty { dbId }, it.quote.number)
    }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                scope.launch {
                    val ok = exportQuotesToUri(
                        context, uri, history, "Quote Browser — History"
                    )
                    Toast.makeText(
                        context,
                        if (ok) "History exported" else "Export failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // v1.77: the mixed "all" view keeps the global page background
        // outside the cards (each card still themes itself from its true
        // source); single-database mode paints the authentic page bg.
    CompositionLocalProvider(LocalSingleDatabaseMode provides !isAll) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "History") {
            FilledTonalIconButton(
                onClick = { exportLauncher.launch("quote-browser-history.txt") },
                enabled = history.isNotEmpty()
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = "Export history to .txt")
            }
            TextButton(
                onClick = { confirmClear = true },
                enabled = history.isNotEmpty()
            ) {
                Text("Clear")
            }
        }
        QuoteSelectionHost(
            selection = selection,
            manager = manager,
            allCandidates = allCandidates,
            onAddToPlaylist = onAddToPlaylist
        )
        HiddenQuotesBanner(
            hiddenCount = hiddenCount,
            showingHidden = showFiltered,
            onShow = { vm.setShowFiltered(true) },
            onHide = { vm.setShowFiltered(false) }
        )
        if (historyItems.isEmpty()) {
            EmptyState("Nothing here yet.\nQuotes you open will appear in this list.")
        } else {
            LazyColumn(Modifier.fillMaxSize(),
                flingBehavior = rememberCappedFlingBehavior()) {
                // v1.77: keyed by source db + number (numbers collide across
                // databases); each card gets its true source dbId.
                items(historyItems, key = { "${it.dbId.ifEmpty { dbId }}:${it.quote.number}" }) { item ->
                    val quote = item.quote
                    // v1.77: per-entry source db (falls back to the current
                    // id for legacy single-DB items).
                    val itemDbId = item.dbId.ifEmpty { dbId }
                    val selKey = SelectedQuote(itemDbId, quote.number)
                    // v1.53: each entry shows the most recent viewed date/time
                    // plus a view count; tapping the caption expands the full
                    // recorded view-datetime list.
                    val expandKey = "$itemDbId/${quote.number}"
                    val expanded = expandedKey == expandKey
                    Text(
                        text = formatHistoryViewedAt(item.viewedAt, item.viewCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp
                            )
                            .clickable {
                                expandedKey = if (expanded) null else expandKey
                            }
                    )
                    if (expanded) {
                        HistoryViewTimesList(
                            dbId = itemDbId,
                            number = quote.number,
                            vm = vm
                        )
                    }
                    QuoteRow(
                        quote = quote,
                        dbId = itemDbId,
                        isFavorite = (itemDbId to quote.number) in allFavKeys,
                        onToggleFavorite = { vm.toggleFavorite(itemDbId, quote) },
                        onClick = { openSourced(itemDbId, quote.number) },
                        onLongClick = { selection.enter(selKey) },
                        onMoreClick = { sheetItem = item },
                        selectionActive = selection.active,
                        isSelected = selection.isSelected(selKey),
                        onSelectionToggle = { selection.toggle(selKey) },
                        showNumber = showNumber,
                        showRating = showRating,
                        // v1.53 report/flag.
                        isFlagged = "$itemDbId/${quote.number}" in flaggedQuotes,
                        flagInfo = flaggedQuotes["$itemDbId/${quote.number}"],
                        onFlagQuote = { reason, custom ->
                            vm.flagQuote(itemDbId, quote.number, reason, custom)
                        },
                        onNickClick = onNickClick)
                }
            }
        }
    }

    // v1.53: clearing history requires confirmation.
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear history?") },
            text = {
                Text(
                    "This removes all ${history.size} viewed " +
                        (if (history.size == 1) "quote" else "quotes") +
                        " from History on this device. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        vm.clearHistory()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val sheetEntry = sheetItem
    val sheetDbId = sheetEntry?.dbId?.ifEmpty { dbId } ?: dbId
    val sq = sheetEntry?.quote
    QuoteActionSheetHost(
        quote = sq,
        isFavorite = sq?.let { (sheetDbId to it.number) in allFavKeys } == true,
        onDismiss = { sheetItem = null },
        onToggleFavorite = { sq?.let { vm.toggleFavorite(sheetDbId, it) } },
        onOpen = { sq?.let { openSourced(sheetDbId, it.number) } },
        dbId = sheetDbId,
        inBasket = sq?.let { BasketEntry(sheetDbId, it.number) in basket } == true,
        onToggleBasket = { sq?.let { onToggleBasket(sheetDbId, it.number) } },
        onHideQuote = { sq?.let { vm.hideQuote(sheetDbId, it.number) } },
        // v1.74.7: "Read aloud" in the quote menu.
        onReadAloud = { val s = sq; if (s != null) onReadAloudQuote(SourcedQuote(sheetDbId, s)) },
        // v1.53 report/flag.
        isFlagged = sq?.let { "$sheetDbId/${it.number}" in flaggedQuotes } == true,
        onFlagQuote = { bid, number, reason, custom ->
            vm.flagQuote(bid, number, reason, custom)
        },
        onUnflagQuote = { sq?.let { vm.unflagQuote(sheetDbId, it.number) } }
    )
    } // LocalSingleDatabaseMode
}

/**
 * v1.53: formats a history entry's caption — the most recent viewed
 * timestamp plus the total view count ("Viewed Sep 15, 2026, 9:37 PM ·
 * 3 views", device locale). Entries that predate timestamp recording
 * (viewedAt <= 0) show "Unknown date" — never crash.
 */
private fun formatHistoryViewedAt(viewedAt: Long, viewCount: Int): String {
    if (viewedAt <= 0) return "Unknown date"
    val countPart = if (viewCount == 1) "1 view" else "$viewCount views"
    return try {
        val formatted = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.MEDIUM,
            java.text.DateFormat.SHORT
        ).format(java.util.Date(viewedAt))
        "Viewed $formatted · $countPart"
    } catch (e: Exception) {
        "Unknown date"
    }
}

/**
 * v1.53: the expandable per-entry list of every recorded view datetime,
 * newest first (seconds included so rapid re-views stay distinct).
 */
@Composable
private fun HistoryViewTimesList(dbId: String, number: Int, vm: HistoryViewModel) {
    var times by remember(dbId, number) { mutableStateOf<List<Long>?>(null) }
    LaunchedEffect(dbId, number) {
        times = withContext(Dispatchers.IO) { vm.viewTimesFor(dbId, number) }
    }
    val loaded = times
    Column(
        modifier = Modifier.padding(start = 32.dp, end = 16.dp, top = 2.dp)
    ) {
        if (loaded == null) {
            Text(
                text = "Loading…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (loaded.isEmpty()) {
            Text(
                text = "No recorded views",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            loaded.forEach { t ->
                Text(
                    text = formatViewDatetime(t),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

/** One view datetime for the expanded list, device locale. */
private fun formatViewDatetime(viewedAt: Long): String {
    return try {
        java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.MEDIUM,
            java.text.DateFormat.MEDIUM
        ).format(java.util.Date(viewedAt))
    } catch (e: Exception) {
        "Unknown date"
    }
}
