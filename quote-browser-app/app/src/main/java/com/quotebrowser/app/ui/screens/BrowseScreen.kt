package com.quotebrowser.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.quotebrowser.app.ui.components.AccentDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.contrastAdjusted
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.repoFor
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DisplayPrefs
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.LengthMode
import com.quotebrowser.app.data.SortOrder
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.BrowseViewModel
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.QotdViewModel
import com.quotebrowser.app.ui.components.DbPageBackground
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.LocalChromeAccent
import com.quotebrowser.app.ui.components.LocalDbThemeSnapshots
import com.quotebrowser.app.ui.components.dbAccentColor
import com.quotebrowser.app.ui.components.AccentDropdownMenu
import com.quotebrowser.app.ui.components.SiteThemeQuoteCard
import com.quotebrowser.app.ui.components.translatedBodyText
import com.quotebrowser.app.ui.components.HiddenQuotesBanner
import com.quotebrowser.app.ui.components.QuoteActionSheetHost
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.components.ShareOptionsDialog
import com.quotebrowser.app.ui.components.QuoteSelectionHost
import com.quotebrowser.app.ui.components.SelectedQuote
import com.quotebrowser.app.ui.components.rememberQuoteSelection
import com.quotebrowser.app.ui.theme.scaledTextStyle

@Composable
fun BrowseScreen(
    vm: BrowseViewModel,
    qotdVm: QotdViewModel,
    catVm: CategoryViewModel,
    manager: DatabaseManager,
    onQuoteClick: (String, Int) -> Unit,
    onQotdClick: (dbId: String, number: Int) -> Unit,
    onQotdHistoryClick: () -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    onAddToPlaylist: (List<SourcedQuote>) -> Unit = {},
    /** v1.74.7: the quote sheet's "Read aloud" row reads one quote aloud. */
    onReadAloudQuote: (SourcedQuote) -> Unit = {},
    onNickClick: (String) -> Unit = {}
) {
    // v1.57: Browse/home database multi-select (empty selection = all
    // databases). Exactly one selected → the classic single-database view;
    // otherwise the merged view over the subset.
    val browseIds by vm.browseDbIds.collectAsStateWithLifecycle()
    val singleDbId by vm.browseSingleDbId.collectAsStateWithLifecycle()
    val isSingle = singleDbId != null
    val isMixed = !isSingle
    val pagingItems = if (isSingle) vm.quotes.collectAsLazyPagingItems() else null
    val mergedPagingItems = if (isMixed) vm.mergedQuotes.collectAsLazyPagingItems() else null
    val favKeys by vm.allFavoriteKeys.collectAsStateWithLifecycle()
    // v1.53 report/flag: flagged quotes map ("$dbId/$number" -> info).
    val flaggedQuotes by manager.flaggedQuotes.collectAsStateWithLifecycle()
    var sortExpanded by remember { mutableStateOf(false) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    val dbHasRatings by vm.dbHasRatings.collectAsStateWithLifecycle()
    var sheetItem by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.61.9: one-tap share from the card header.
    var shareItem by remember { mutableStateOf<SourcedQuote?>(null) }
    val selection = rememberQuoteSelection()
    val showFiltered by vm.showFiltered.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenCount.collectAsStateWithLifecycle()
    val localRatings by manager.localRatings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // v1.88.22: "Break between quotes" now controls blank lines between
    // quote-body turns (via paragraphizeBody, read inside cleanDisplayText).
    // Collected as state so the list recomposes when the setting changes;
    // the value itself is read downstream, not here.
    DisplayPrefs.init(context)
    @Suppress("UNUSED_VARIABLE")
    val breakSetting by DisplayPrefs.breakBetweenQuotes.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // v1.59: jump-to-number dialog state.
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpBusy by remember { mutableStateOf(false) }

    /**
     * v1.59: jump-to-number lookup. Single-database mode searches that
     * database; mixed mode searches the selected databases in order (all
     * databases when the selection is empty). Returns the owning database
     * id + number of the first match, or null. Uses peekQuote (not
     * getQuote) so a lookup never records a history view.
     */
    suspend fun findJumpTarget(n: Int): Pair<String, Int>? {
        val ids: List<String> = when {
            isSingle -> listOfNotNull(singleDbId)
            browseIds.isNotEmpty() -> browseIds.sorted()
            else -> manager.allDbIds()
        }
        for (id in ids) {
            val quote = manager.repoFor(id).peekQuote(n) ?: continue
            // v1.65: an exact-number jump bypasses per-screen filters (it's
            // an explicit direct request), but never the global maturity
            // ceiling — that's the safety gate.
            if (!manager.isMaturityAllowed(quote)) continue
            return id to n
        }
        return null
    }

    /** v1.59: submits a jump-to-number. Unknown numbers toast — they never
     * land on the full-screen "doesn't exist" error. */
    fun submitJump(n: Int) {
        if (jumpBusy) return
        jumpBusy = true
        scope.launch(Dispatchers.IO) {
            val target = findJumpTarget(n)
            withContext(Dispatchers.Main) {
                jumpBusy = false
                if (target != null) {
                    showJumpDialog = false
                    onQuoteClick(target.first, target.second)
                } else {
                    Toast.makeText(
                        context, "Quote #$n not found", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // The temporary filter bypass is per-screen: drop it when leaving.
    DisposableEffect(Unit) { onDispose { vm.setShowFiltered(false) } }

    // Refresh the category list whenever this screen is shown (tags may have
    // been edited elsewhere).
    LaunchedEffect(Unit) { catVm.refresh() }

    // (Quote, dbId) for the row at an index, in either browse mode.
    fun itemAt(index: Int): Pair<Quote, String>? {
        return if (isMixed) {
            mergedPagingItems?.get(index)?.let { it.quote to it.dbId }
        } else {
            val sid = singleDbId ?: return null
            pagingItems?.get(index)?.let { it to sid }
        }
    }
    val itemCount =
        if (isMixed) mergedPagingItems?.itemCount ?: 0 else pagingItems?.itemCount ?: 0

    // --- "Pick up where you left off" (v1.51): per-database Browse scroll
    // position memory. Restore happens once per database id, silently (no
    // UI affordance); saves are debounced while scrolling, plus an
    // immediate save when leaving the database or the screen. Everything
    // is gated on the Settings → Features toggle.
    val listState = rememberLazyListState()
    val positionMemoryOn by vm.browsePositionMemory.collectAsStateWithLifecycle()
    // v1.57: scroll position is per browse selection, not per single db.
    val browseKey = remember(browseIds, singleDbId) {
        singleDbId ?: ("mixed:" + browseIds.sorted().joinToString(","))
    }
    var restoredBrowseKey by remember { mutableStateOf<String?>(null) }

    // Restore: once per db id, after the list has loaded items (paging
    // loads asynchronously); the saved index is clamped to the loaded
    // size. Databases with no saved position go to the top, so switching
    // databases never leaves a stale offset from the previous one.
    LaunchedEffect(browseKey, itemCount) {
        if (!positionMemoryOn || restoredBrowseKey == browseKey || itemCount <= 0) return@LaunchedEffect
        restoredBrowseKey = browseKey
        val target = manager.browsePositionStore.positionFor(browseKey)
            .coerceIn(0, itemCount - 1)
        if (target != listState.firstVisibleItemIndex) listState.scrollToItem(target)
    }

    // Save while browsing: debounced so rapid scrolling doesn't churn disk.
    LaunchedEffect(browseKey, positionMemoryOn) {
        if (!positionMemoryOn) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .debounce(2000)
            .collect { index -> manager.browsePositionStore.savePosition(browseKey, index) }
    }

    // Save immediately when leaving this database (db switch or screen
    // exit): covers positions the debounce hadn't flushed yet.
    DisposableEffect(browseKey, positionMemoryOn) {
        onDispose {
            if (positionMemoryOn) {
                manager.browsePositionStore.savePosition(browseKey, listState.firstVisibleItemIndex)
            }
        }
    }

    // Multi-select candidates: whatever is currently loaded.
    val allCandidates = (0 until itemCount).mapNotNull { i ->
        itemAt(i)?.let { (q, id) -> SelectedQuote(id, q.number) }
    }

    // v1.56: force the list to reload on every database switch (the pager
    // normally follows the selection itself, but a stale list must never
    // survive a switch), and drop per-quote UI state from the old selection.
    var lastBrowseKey2 by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(browseKey) {
        if (lastBrowseKey2 != null && lastBrowseKey2 != browseKey) {
            if (isMixed) mergedPagingItems?.refresh() else pagingItems?.refresh()
            sheetItem = null
            selection.clear()
        }
        lastBrowseKey2 = browseKey
    }

    // v1.56: per-database page background — the database's own page color
    // (authentic site default, e.g. CrapShack black) or a custom color/image
    // from Settings → database themes. v1.57: mixed multi-database mode
    // keeps the global theme page (ALL_DBS_ID sentinel → no per-db page).
    DbPageBackground(
        dbId = singleDbId ?: DatabaseManager.ALL_DBS_ID,
        manager = manager,
        modifier = Modifier.fillMaxSize()
    ) {
    // v1.57: site cards may only paint their authentic page background
    // outside the card in single-database mode (qdb.us white page); in
    // mixed lists the global page background shows through instead.
    // v1.57: exactly one selected database tints the screen's dialogs,
    // menus, and empty states to that database's accent; mixed/all
    // selections keep the neutral theme chrome.
    val dbThemeSnap = LocalDbThemeSnapshots.current
    val browseChromeAccent = remember(dbThemeSnap, singleDbId) {
        dbAccentColor(dbThemeSnap, singleDbId)
    }
    CompositionLocalProvider(
        LocalSingleDatabaseMode provides isSingle,
        LocalChromeAccent provides browseChromeAccent
    ) {
    Column(Modifier.fillMaxSize()) {
        QotdCard(
            vm = qotdVm,
            onQuoteClick = onQotdClick,
            onHistoryClick = onQotdHistoryClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            onNickClick = onNickClick
        )

        OnThisDayCard(
            manager = manager,
            onQuoteClick = onQotdClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Collapsible filter header: the full sort/filter/length UI
                // used to eat half the screen, so it now hides behind this.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filters",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val summary = buildList {
                        if (vm.sortOrder != SortOrder.NUMBER_DESC) add(vm.sortOrder.label)
                        vm.minRating?.let { add(">$it") }
                        if (vm.categoryIds.isNotEmpty()) add("${vm.categoryIds.size} cat.")
                        // v1.65 content filters.
                        vm.maturity?.let { add(it.label) }
                        vm.castSize?.let { add(it.label) }
                        if (vm.lengthMode != LengthMode.ALL) add("length")
                    }.joinToString(" · ")
                    if (!filtersExpanded && summary.isNotEmpty()) {
                        Text(
                            summary,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    // v1.59: jump to a quote number (dialog + lookup; never
                    // the full-screen "doesn't exist" error).
                    IconButton(
                        onClick = { showJumpDialog = true },
                        modifier = Modifier.semantics {
                            contentDescription = "Go to quote number"
                        }
                    ) {
                        Text(
                            "#",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = if (filtersExpanded) Icons.Filled.ExpandLess
                        else Icons.Filled.ExpandMore,
                        contentDescription = if (filtersExpanded) "Collapse filters"
                        else "Expand filters",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (filtersExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sort",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Box {
                        TextButton(onClick = { sortExpanded = true }) {
                            Text(vm.sortOrder.label)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change sort order")
                        }
                        // v1.57: rounded, accent-tinted menu.
                        AccentDropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            SortOrder.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text(sort.label) },
                                    onClick = { sortExpanded = false; vm.updateSortOrder(sort) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }

                if (dbHasRatings) {
                    com.quotebrowser.app.ui.components.RatingFilterRow(
                        selected = vm.minRating,
                        onSelect = { vm.updateMinRating(it) }
                    )
                } else {
                    // Databases without ratings (the 2011 archives): a
                    // min-rating filter could only ever show an empty list.
                    Text(
                        "No ratings available for this database",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                // v1.67: the categories dropdown moved to the top bar next
                // to the database pill (single-database views only).
                // v1.65 content filters: the maturity pick ANDs with the
                // global ceiling from Settings; cast size is per-screen.
                // v1.74: the pills are self-labeled ("Maturity: …",
                // "Cast size: …"), so the old separate Text labels are gone.
                com.quotebrowser.app.ui.components.MaturityFilterDropdown(
                    selected = vm.maturity,
                    onSelect = { vm.updateMaturity(it) }
                )
                com.quotebrowser.app.ui.components.CastSizeFilterDropdown(
                    selected = vm.castSize,
                    onSelect = { vm.updateCastSize(it) }
                )
                com.quotebrowser.app.ui.components.PersonFilterButton(
                    manager = manager,
                    onNickClick = onNickClick
                )
                com.quotebrowser.app.ui.components.LengthModeDropdown(
                    mode = vm.lengthMode,
                    onModeChange = { vm.updateLengthMode(it) },
                    minLength = vm.minLength,
                    maxLength = vm.maxLength,
                    onMinChange = { vm.updateMinLength(it) },
                    onMaxChange = { vm.updateMaxLength(it) }
                )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        // v1.59: jump-to-number dialog.
        if (showJumpDialog) {
            JumpToQuoteDialog(
                busy = jumpBusy,
                onDismiss = { showJumpDialog = false },
                onJump = ::submitJump
            )
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

        val refreshState =
            if (isMixed) mergedPagingItems?.loadState?.refresh else pagingItems?.loadState?.refresh
        val appendState =
            if (isMixed) mergedPagingItems?.loadState?.append else pagingItems?.loadState?.append
        when {
            refreshState is LoadState.Loading && itemCount == 0 -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            refreshState is LoadState.Error && itemCount == 0 -> {
                EmptyState("Couldn't load quotes.\n${refreshState.error.message}")
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(itemCount) { index ->
                        val (quote, rowDbId) = itemAt(index) ?: return@items
                        val selKey = SelectedQuote(rowDbId, quote.number)
                        QuoteRow(
                            quote = quote,
                            dbId = rowDbId,
                            isFavorite = (rowDbId to quote.number) in favKeys,
                            onToggleFavorite = { vm.toggleFavorite(rowDbId, quote) },
                            onClick = { onQuoteClick(rowDbId, quote.number) },
                            onLongClick = { selection.enter(selKey) },
                            onMoreClick = { sheetItem = SourcedQuote(rowDbId, quote) },
                            // v1.61.9: one-tap share from the header.
                            onShareClick = { shareItem = SourcedQuote(rowDbId, quote) },
                            selectionActive = selection.active,
                            isSelected = selection.isSelected(selKey),
                            onSelectionToggle = { selection.toggle(selKey) },
                            showNumber = showNumber,
                            showRating = showRating,
                            // All-databases mode: every card carries its source
                            // DB chip (short name to save space, e.g.
                            // "Bash.org"); single-DB mode needs no chip.
                            sourceLabel = if (isMixed) manager.shortLabel(rowDbId) else null,
                            sourceDbId = if (isMixed) rowDbId else null,
                            myRating = localRatings["$rowDbId/${quote.number}"] ?: 0,
                            onRate = { vm.rateQuote(rowDbId, quote.number, it) },
                            // v1.53 report/flag.
                            isFlagged = "$rowDbId/${quote.number}" in flaggedQuotes,
                            flagInfo = flaggedQuotes["$rowDbId/${quote.number}"],
                            onFlagQuote = { reason, custom ->
                                vm.flagQuote(rowDbId, quote.number, reason, custom)
                            },
                            // v1.51: the WQDB site style alternates row shades.
                            listIndex = index,
                            onNickClick = onNickClick)
                    }
                    if (appendState is LoadState.Loading) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    val sheet = sheetItem
    val sheetQuote = sheet?.quote
    val sheetDbId = sheet?.dbId ?: singleDbId ?: DatabaseManager.ALL_DBS_ID
    QuoteActionSheetHost(
        quote = sheetQuote,
        isFavorite = sheet?.let { (it.dbId to it.quote.number) in favKeys } == true,
        onDismiss = { sheetItem = null },
        onToggleFavorite = { sheet?.let { vm.toggleFavorite(it.dbId, it.quote) } },
        onOpen = { sheet?.let { onQuoteClick(it.dbId, it.quote.number) } },
        dbId = sheetDbId,
        inBasket = sheet?.let { BasketEntry(it.dbId, it.quote.number) in basket } == true,
        onToggleBasket = { sheet?.let { onToggleBasket(it.dbId, it.quote.number) } },
        onHideQuote = { sheet?.let { vm.hideQuote(it.dbId, it.quote.number) } },
        // v1.74.7: "Read aloud" in the quote menu.
        onReadAloud = { sheet?.let(onReadAloudQuote) },
        // v1.53 report/flag.
        isFlagged = sheet?.let { "${it.dbId}/${it.quote.number}" in flaggedQuotes } == true,
        onFlagQuote = { dbId, number, reason, custom ->
            vm.flagQuote(dbId, number, reason, custom)
        },
        onUnflagQuote = { sheet?.let { vm.unflagQuote(it.dbId, it.quote.number) } }
    )
    // v1.61.9: one-tap share from the card header.
    shareItem?.let { target ->
        ShareOptionsDialog(
            quote = target.quote,
            dbId = target.dbId,
            onDismiss = { shareItem = null }
        )
    }
    } // LocalSingleDatabaseMode
    } // DbPageBackground
}

/**
 * Quote of the Day card: the same quote all day (deterministic by date).
 * The bell toggles an optional daily 9am notification (asks for the
 * Android 13+ notification permission when enabling).
 *//**
 * "On this day": quotes favorited or read on this month/day in previous
 * years. Hidden entirely when there is nothing to show yet.
 */
@Composable
private fun OnThisDayCard(
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var entries by remember {
        mutableStateOf<List<DatabaseManager.MemoryLaneEntry>>(emptyList())
    }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entries = manager.onThisDay()
        loaded = true
    }
    if (!loaded || entries.isEmpty()) return
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(vertical = 10.dp)) {
            Text(
                "On this day",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.dbId + "/" + it.quote.number }) { e ->
                    Card(
                        onClick = { onQuoteClick(e.dbId, e.quote.number) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier.width(240.dp)
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "${e.kind.replaceFirstChar { it.uppercase() }} in ${e.year} · #${e.quote.number}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                e.quote.text,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QotdCard(
    vm: QotdViewModel,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNickClick: (String) -> Unit = {}
) {
    val sq by vm.quote.collectAsStateWithLifecycle()
    val notificationsEnabled by vm.notificationsEnabled.collectAsStateWithLifecycle()
    val minimized by vm.cardMinimized.collectAsStateWithLifecycle()
    val hour by vm.qotdHour.collectAsStateWithLifecycle()
    val minute by vm.qotdMinute.collectAsStateWithLifecycle()
    val filterOn by vm.contentFilterEnabled.collectAsStateWithLifecycle()
    val bypassed by vm.bypassWordFilter.collectAsStateWithLifecycle()
    val context = LocalContext.current

    /** Current delivery time, formatted the way the phone does. */
    val timeLabel = remember(hour, minute, context) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
        }
        android.text.format.DateFormat.getTimeFormat(context).format(cal.time)
    }

    // Framework time picker; recreated when the stored time changes so it
    // always opens on the current delivery time.
    val timeDialog = remember(hour, minute, context) {
        android.app.TimePickerDialog(
            context,
            { _, h, m -> vm.setQotdTime(h, m) },
            hour,
            minute,
            android.text.format.DateFormat.is24HourFormat(context)
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                vm.setNotificationsEnabled(true)
            } else {
                Toast.makeText(
                    context,
                    "Notification permission denied — daily reminder stays off",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun onBellClick() {
        if (notificationsEnabled) {
            vm.setNotificationsEnabled(false)
        } else {
            val needsPermission =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                vm.setNotificationsEnabled(true)
                Toast.makeText(context, "Daily quote reminder on — $timeLabel", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    val q = sq?.quote
    val dbId = sq?.dbId
    // A null pick no longer means "the filter hid everything": empty
    // databases are skipped by the rotation, so this means every candidate
    // was excluded. Only blame the content filter when it is actually on —
    // and offer a way to see a quote anyway.
    if (q == null || dbId == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "QUOTE OF THE DAY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                if (filterOn && !bypassed) {
                    Text(
                        text = "All quotes are hidden by the content filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { vm.showDespiteFilter() }) {
                        Text("Show one anyway")
                    }
                } else {
                    Text(
                        text = "Couldn't pick a quote of the day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        return
    }
    // Minimized: a slim row that expands back on tap.
    if (minimized) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { vm.setCardMinimized(false) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUOTE OF THE DAY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.setCardMinimized(false) }) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = "Expand quote of the day",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        return
    }
    // v1.57: the quote renders as its database's own site-theme card —
    // tappable to open the full quote — instead of the large blue button.
    // The notification controls live in the slim header row above it.
    // v1.61.9: one-tap share from the card header.
    var showQotdShare by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "QUOTE OF THE DAY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = ::onBellClick) {
                Icon(
                    imageVector = if (notificationsEnabled) Icons.Filled.Notifications
                    else Icons.Outlined.NotificationsOff,
                    contentDescription = if (notificationsEnabled)
                        "Turn off daily quote reminder"
                    else "Turn on daily quote reminder at $timeLabel",
                    tint = if (notificationsEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { timeDialog.show() }) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = "Daily quote time (currently $timeLabel)",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "Past quotes of the day",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { vm.setCardMinimized(true) }) {
                Icon(
                    imageVector = Icons.Filled.ExpandLess,
                    contentDescription = "Minimize quote of the day",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // v1.64: nick-tap hit-testing (same pattern as QuoteRow): the
        // body Text records tap-down positions without consuming; this
        // click handler hit-tests them against nick annotations so a
        // nick tap opens the nick page instead of the quote.
        var qotdTapSeq by remember { mutableIntStateOf(0) }
        var qotdBodyTap by remember { mutableStateOf<Pair<Int, Offset>?>(null) }
        var qotdBodyLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
        var qotdBodyAnnotated by remember { mutableStateOf<AnnotatedString?>(null) }
        Box(
            Modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        qotdTapSeq++
                    }
                }
                .clickable {
                    val tap = qotdBodyTap
                    val layout = qotdBodyLayout
                    val annotated = qotdBodyAnnotated
                    qotdBodyTap = null
                    if (tap != null && tap.first == qotdTapSeq &&
                        layout != null && annotated != null
                    ) {
                        val offset = layout.getOffsetForPosition(tap.second)
                        if (offset in annotated.indices) {
                            val nick = annotated
                                .getStringAnnotations(
                                    "nick", offset,
                                    (offset + 1).coerceAtMost(annotated.length)
                                )
                                .firstOrNull()?.item
                            if (nick != null) {
                                onNickClick(nick)
                                return@clickable
                            }
                        }
                    }
                    onQuoteClick(dbId, q.number)
                }
        ) {
            SiteThemeQuoteCard(
                quote = q,
                dbId = dbId,
                showNumber = true,
                showRating = true,
                showQueuedTag = q.status == "queued",
                showFavoriteAction = false,
                // v1.61.9: one-tap share in the header.
                onShareClick = { showQotdShare = true },
                sourceLabel = vm.labelFor(dbId),
                sourceDbId = dbId,
                body = {
                    val bodyStyle = scaledTextStyle(MaterialTheme.typography.bodyLarge)
                    val qotdAnnotated = rememberNickAnnotatedText(translatedBodyText(q), bodyStyle)
                    Text(
                        // v1.61: translated body text when a translation shows.
                        text = qotdAnnotated,
                        style = bodyStyle,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = {
                            qotdBodyLayout = it
                            qotdBodyAnnotated = qotdAnnotated
                        },
                        // v1.64: records tap-downs for nick hit-testing;
                        // never consumes, so taps behave as before.
                        modifier = Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                qotdBodyTap = qotdTapSeq to down.position
                            }
                        }
                    )
                }
            )
        }
        if (showQotdShare) {
            ShareOptionsDialog(
                quote = q,
                dbId = dbId,
                onDismiss = { showQotdShare = false }
            )
        }
    }
}

/**
 * v1.59: "Go to quote number" dialog: type the digits and hit Go (or the
 * keyboard's Go action). The number is looked up before navigating, so
 * unknown numbers toast "Quote #N not found" instead of landing on the
 * full-screen "doesn't exist" error. In mixed-database mode the first
 * database holding that number wins.
 */
@Composable
private fun JumpToQuoteDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val number = text.toIntOrNull()?.takeIf { it > 0 }
    fun go() {
        number?.let { onJump(it) }
    }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Go to quote number") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter(Char::isDigit).take(6) },
                singleLine = true,
                label = { Text("Quote #") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(onGo = { go() }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy
            )
        },
        confirmButton = {
            TextButton(onClick = ::go, enabled = number != null && !busy) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        }
    )
}

