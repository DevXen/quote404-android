package com.quotebrowser.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.SlideItem
import com.quotebrowser.app.ui.TopBottomMode
import com.quotebrowser.app.ui.TopBottomViewModel
import com.quotebrowser.app.ui.exportSlideshowVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.components.CategoryFilterDropdown
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.HiddenQuotesBanner
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.QuoteActionSheetHost
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.components.QuoteSelectionHost
import com.quotebrowser.app.ui.components.SelectedQuote
import com.quotebrowser.app.ui.components.rememberQuoteSelection
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

@Composable
fun TopBottomScreen(
    vm: TopBottomViewModel,
    catVm: CategoryViewModel,
    manager: DatabaseManager,
    onQuoteClick: (String, Int) -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    onAddToPlaylist: (List<SourcedQuote>) -> Unit = {},
    onReadAllAloud: (List<SourcedQuote>) -> Unit = {},
    /** v1.74.7: the quote sheet's "Read aloud" row reads one quote aloud. */
    onReadAloudQuote: (SourcedQuote) -> Unit = {}
) {
    val quotes by vm.quotes.collectAsStateWithLifecycle()
    val allDbQuotes by vm.allDbQuotes.collectAsStateWithLifecycle()
    val allFavKeys by vm.allFavoriteKeys.collectAsStateWithLifecycle()
    val favNumbers by vm.favoriteNumbers.collectAsStateWithLifecycle()
    // v1.53 report/flag.
    val flaggedQuotes by manager.flaggedQuotes.collectAsStateWithLifecycle()
    val dbId by vm.dbId.collectAsStateWithLifecycle()
    val hasMore by vm.hasMore.collectAsStateWithLifecycle()
    // "All databases" mode merges every database's top/bottom quotes and
    // shows a source chip on each card.
    val isAll = dbId == DatabaseManager.ALL_DBS_ID
    val categories by catVm.categories.collectAsStateWithLifecycle()
    var sheetQuote by remember { mutableStateOf<Quote?>(null) }
    // In "All databases" mode the sheet needs the quote's real database.
    var sheetSourced by remember { mutableStateOf<SourcedQuote?>(null) }
    val sheetDbId = sheetSourced?.dbId ?: dbId
    val selection = rememberQuoteSelection()
    val showFiltered by vm.showFiltered.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenCount.collectAsStateWithLifecycle()

    // The temporary filter bypass is per-screen: drop it when leaving.
    DisposableEffect(Unit) { onDispose { vm.setShowFiltered(false) } }

    LaunchedEffect(Unit) { catVm.refresh() }
    val allCandidates = if (isAll) {
        allDbQuotes.map { SelectedQuote(it.dbId, it.quote.number) }
    } else {
        quotes.map { SelectedQuote(dbId, it.number) }
    }

    val page = vm.page
    val pageSize = TopBottomViewModel.PAGE_SIZE
    val from = page * pageSize + 1
    val to = page * pageSize + quotes.size
    val rangeLabel = if (quotes.isEmpty()) "" else "$from–$to"
    val edgeLabel = if (vm.mode == TopBottomMode.TOP) "Top" else "Bottom"
    val kindLabel = if (vm.mode == TopBottomMode.TOP) "highest rated" else "lowest rated"
    // Shared by "Read all aloud" and the v1.49 slideshow export.
    val readAloudList: List<SourcedQuote> =
        if (isAll) allDbQuotes else quotes.map { SourcedQuote(dbId, it) }
    // v1.49 slideshow export state.
    val context = LocalContext.current
    val renderScope = rememberCoroutineScope()
    val slideshowEnabled by manager.slideshowExportEnabled
        .collectAsStateWithLifecycle()
    var showSlideshowDialog by remember { mutableStateOf(false) }
    var slideshowNarrate by remember { mutableStateOf(false) }
    var renderingSlideshow by remember { mutableStateOf(false) }

    // v1.58: site cards may only paint their authentic page background
    // outside the card in single-database mode; the merged all-databases
    // ranking keeps the global page background outside the cards.
    CompositionLocalProvider(LocalSingleDatabaseMode provides !isAll) {
        Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (rangeLabel.isEmpty()) "No quotes yet"
            else "$edgeLabel $rangeLabel"
        ) {
            OutlinedButton(
                onClick = vm::prevPage,
                enabled = page > 0
            ) {
                Text("‹ Prev", maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = vm::nextPage,
                enabled = hasMore
            ) {
                Text("Next ›", maxLines = 1)
            }
        }
        Text(
            text = kindLabel.replaceFirstChar { it.uppercase() } + " quotes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(4.dp))
        // Categories are per-database, so the filter is unavailable in
        // "All databases" mode.
        if (!isAll) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                CategoryFilterDropdown(
                    categories = categories,
                    selectedIds = vm.categoryIds,
                    onSelect = { vm.updateCategories(it) }
                )
            }
            Spacer(Modifier.height(4.dp))
        }
        // Read-aloud playlist entry point: reads the whole current page
        // (the Top/Bottom 100) from the top. The lists are already
        // content-filtered by the VM.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onReadAllAloud(readAloudList) },
                enabled = readAloudList.isNotEmpty()
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Read all aloud")
            }
            // v1.49: Top/Bottom-100 slideshow video export, optional narration.
            if (slideshowEnabled) {
                TextButton(
                    onClick = { showSlideshowDialog = true },
                    enabled = readAloudList.isNotEmpty() && !renderingSlideshow
                ) {
                    if (renderingSlideshow) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Slideshow")
                }
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
        if (isAll && allDbQuotes.isEmpty() || !isAll && quotes.isEmpty()) {
            EmptyState("No quotes on this page.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                flingBehavior = rememberCappedFlingBehavior()) {
                if (isAll) {
                    items(allDbQuotes, key = { "${it.dbId}:${it.quote.number}" }) { sq ->
                        val selKey = SelectedQuote(sq.dbId, sq.quote.number)
                        QuoteRow(
                            quote = sq.quote,
                            dbId = sq.dbId,
                            isFavorite = (sq.dbId to sq.quote.number) in allFavKeys,
                            onToggleFavorite = { vm.toggleFavorite(sq.dbId, sq.quote) },
                            onClick = { onQuoteClick(sq.dbId, sq.quote.number) },
                            onLongClick = { selection.enter(selKey) },
                            onMoreClick = { sheetSourced = sq },
                            selectionActive = selection.active,
                            isSelected = selection.isSelected(selKey),
                            onSelectionToggle = { selection.toggle(selKey) },
                            showNumber = showNumber,
                            showRating = showRating,
                            sourceLabel = manager.shortLabel(sq.dbId),
                            sourceDbId = sq.dbId,
                            // v1.53 report/flag.
                            isFlagged = "${sq.dbId}/${sq.quote.number}" in flaggedQuotes,
                            flagInfo = flaggedQuotes["${sq.dbId}/${sq.quote.number}"],
                            onFlagQuote = { reason, custom ->
                                vm.flagQuote(sq.dbId, sq.quote.number, reason, custom)
                            }
                        )
                    }
                } else {
                    items(quotes, key = { it.number }) { quote ->
                        val selKey = SelectedQuote(dbId, quote.number)
                        QuoteRow(
                            quote = quote,
                            dbId = dbId,
                            isFavorite = quote.number in favNumbers,
                            onToggleFavorite = { vm.toggleFavorite(quote) },
                            onClick = { onQuoteClick(dbId, quote.number) },
                            onLongClick = { selection.enter(selKey) },
                            onMoreClick = { sheetSourced = SourcedQuote(dbId, quote) },
                            selectionActive = selection.active,
                            isSelected = selection.isSelected(selKey),
                            onSelectionToggle = { selection.toggle(selKey) },
                            showNumber = showNumber,
                            showRating = showRating,
                            // v1.53 report/flag.
                            isFlagged = "$dbId/${quote.number}" in flaggedQuotes,
                            flagInfo = flaggedQuotes["$dbId/${quote.number}"],
                            onFlagQuote = { reason, custom ->
                                vm.flagQuote(dbId, quote.number, reason, custom)
                            }
                        )
                    }
                }
            }
        }
    }

    val sq = sheetSourced?.quote
    QuoteActionSheetHost(
        quote = sq,
        isFavorite = sq?.let { it.number in favNumbers } == true,
        onDismiss = { sheetSourced = null },
        onToggleFavorite = { sq?.let { vm.toggleFavorite(sheetDbId, it) } },
        onOpen = { sq?.let { onQuoteClick(sheetDbId, it.number) } },
        dbId = sheetDbId,
        inBasket = sq?.let { BasketEntry(sheetDbId, it.number) in basket } == true,
        onToggleBasket = { sq?.let { onToggleBasket(sheetDbId, it.number) } },
        onHideQuote = { sq?.let { vm.hideQuote(sheetDbId, it.number) } },
        // v1.74.7: "Read aloud" in the quote menu.
        onReadAloud = { val s = sq; if (s != null) onReadAloudQuote(SourcedQuote(sheetDbId, s)) },
        // v1.53 report/flag.
        isFlagged = sq?.let { "$sheetDbId/${it.number}" in flaggedQuotes } == true,
        onFlagQuote = { sheetDbId, number, reason, custom ->
            vm.flagQuote(sheetDbId, number, reason, custom)
        },
        onUnflagQuote = { sq?.let { vm.unflagQuote(sheetDbId, it.number) } }
    )

    // v1.49 slideshow export dialog: optional narration, then render and
    // share the MP4.
    if (showSlideshowDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!renderingSlideshow) showSlideshowDialog = false
            },
            title = { Text("Export slideshow") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Renders the ${readAloudList.size.coerceAtMost(100)} quotes " +
                            "on this page as an MP4 slideshow video.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = slideshowNarrate,
                            onCheckedChange = { slideshowNarrate = it },
                            enabled = !renderingSlideshow
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Narrate each quote aloud")
                    }
                    if (renderingSlideshow) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Rendering video…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        renderingSlideshow = true
                        val slides = readAloudList.take(100).map { sq2 ->
                            SlideItem(sq2.quote, manager.shortLabel(sq2.dbId))
                        }
                        val narrate = slideshowNarrate
                        renderScope.launch(Dispatchers.IO) {
                            try {
                                val file = exportSlideshowVideo(
                                    context, slides, narrate
                                )
                                withContext(Dispatchers.Main) {
                                    renderingSlideshow = false
                                    showSlideshowDialog = false
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "com.quotebrowser.app.fileprovider",
                                        file
                                    )
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(send, "Share slideshow")
                                    )
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    renderingSlideshow = false
                                    showSlideshowDialog = false
                                    Toast.makeText(
                                        context,
                                        "Slideshow failed: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = !renderingSlideshow
                ) { Text("Export video") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSlideshowDialog = false },
                    enabled = !renderingSlideshow
                ) { Text("Cancel") }
            }
        )
    }
    } // LocalSingleDatabaseMode
}
