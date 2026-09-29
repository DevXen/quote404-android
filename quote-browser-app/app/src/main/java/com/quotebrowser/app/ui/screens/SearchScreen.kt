package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DisplayPrefs
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.QuoteListStore
import com.quotebrowser.app.data.SortOrder
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.MOOD_PRESETS
import com.quotebrowser.app.ui.isMoodActive
import com.quotebrowser.app.ui.moodNameToId
import com.quotebrowser.app.ui.toggleMoodSelection
import com.quotebrowser.app.ui.MoodPreset
import com.quotebrowser.app.ui.SearchHistory
import com.quotebrowser.app.ui.SearchRecord
import com.quotebrowser.app.ui.SearchViewModel
import com.quotebrowser.app.ui.components.CategoryFilterDropdown
import com.quotebrowser.app.ui.components.DatabaseMultiSelectDialog
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.HiddenQuotesBanner
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteActionSheetHost
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.components.QuoteSelectionHost
import com.quotebrowser.app.ui.components.QueryAutocomplete
import com.quotebrowser.app.ui.components.VoiceSearchButton
import com.quotebrowser.app.ui.components.SelectedQuote
import com.quotebrowser.app.ui.components.rememberQuoteSelection

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(vm: SearchViewModel, catVm: CategoryViewModel, manager: DatabaseManager,
    onQuoteClick: (Int) -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    onAddToPlaylist: (List<SourcedQuote>) -> Unit = {},
    /** v1.74.7: the quote sheet's "Read aloud" row reads one quote aloud. */
    onReadAloudQuote: (SourcedQuote) -> Unit = {},
    /** Notes search spans all databases; cross-db taps use this. */
    onSourcedQuoteClick: (dbId: String, number: Int) -> Unit = { _, n -> onQuoteClick(n) },
    onNickClick: (String) -> Unit = {},
    /** v1.66: user quote lists — enables the sheet's "Add to list…" row. */
    quoteListStore: QuoteListStore? = null,
    /** v1.72: hoisted to AppNav so the global search row shares the store. */
    searchHistory: SearchHistory
) {
    val pagingItems = vm.results.collectAsLazyPagingItems()
    val favNumbers by vm.favoriteNumbers.collectAsStateWithLifecycle()
    // v1.53 report/flag.
    val flaggedQuotes by manager.flaggedQuotes.collectAsStateWithLifecycle()
    val dbId by vm.dbId.collectAsStateWithLifecycle()
    // v1.49: Select all / Deselect all in the bulk-selection bar.
    val bulkSelectAll by manager.bulkSelectAll.collectAsStateWithLifecycle()
    val allResults by vm.allDbResults.collectAsStateWithLifecycle()
    val allFavKeys by vm.allFavoriteKeys.collectAsStateWithLifecycle()
    val categories by catVm.categories.collectAsStateWithLifecycle()
    val namedSearches by vm.namedSearches.collectAsStateWithLifecycle()
    var showSaveSearchDialog by remember { mutableStateOf(false) }
    var showManageSearches by remember { mutableStateOf(false) }
    var saveSearchName by remember { mutableStateOf("") }
    // v1.74.7: the ⋯ sheet is source-aware — multi-database and notes
    // results carry their own database id so favorite/flag/read-aloud act
    // on the right database.
    var sheetQuote by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.66: add-to-list target from the long-press sheet (dbId to number).
    var showAddToListFor by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var confirmClearSaved by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val selection = rememberQuoteSelection()
    val showFiltered by vm.showFiltered.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenCount.collectAsStateWithLifecycle()
    val noteResults by vm.noteResults.collectAsStateWithLifecycle()
    val databases by manager.availableDatabases().collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    // v1.88.22: "Break between quotes" now controls blank lines between
    // quote-body turns (via paragraphizeBody, read inside cleanDisplayText).
    // Collected as state so the lists recompose when the setting changes;
    // the value itself is read downstream, not here.
    DisplayPrefs.init(context)
    @Suppress("UNUSED_VARIABLE")
    val breakSetting by DisplayPrefs.breakBetweenQuotes.collectAsStateWithLifecycle()
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    // Recent-search history (hoisted SearchHistory store): recorded on
    // every submitted search. The legacy manager.savedSearches list is
    // migrated in once so existing users keep their history.
    var recentHistory by remember { mutableStateOf(searchHistory.history()) }
    LaunchedEffect(Unit) {
        if (!searchHistory.migrationDone) {
            val legacy = vm.savedSearches.value.filter { it.isNotBlank() }
            if (legacy.isNotEmpty() && searchHistory.history().isEmpty()) {
                legacy.asReversed().forEach {
                    searchHistory.add(SearchRecord.fromLegacyQuery(it))
                }
                recentHistory = searchHistory.history()
            }
            searchHistory.migrationDone = true
        }
    }

    fun submitSearch() {
        // v1.66: records the full search snapshot, not just the query text.
        searchHistory.add(vm.snapshotSearchRecord())
        recentHistory = searchHistory.history()
        focusManager.clearFocus()
    }

    // v1.69: the Search button runs the current filters even with no
    // query typed — blank query flips on filter-browse mode. v1.72: the
    // filter panel below owns its expanded state; running a search
    // collapses it.
    var filtersExpanded by remember { mutableStateOf(false) }
    fun runSearch() {
        if (vm.query.isBlank()) vm.updateFilterBrowse(true)
        filtersExpanded = false
        submitSearch()
    }

    // Database scope: null = this database only; otherwise the chosen set.
    // (The scope chip and its dialog live in SearchFilterPanel now.)
    // v1.77: the effective scope — the virtual "all" database expands to
    // every real id so each hit keeps its true source (and site theme).
    // (Local copy so smart casts work — a delegated property can't be
    // smart-cast.)
    val scopeIds: Set<String>? = vm.effectiveSearchDbIds.collectAsStateWithLifecycle().value

    // The temporary filter bypass is per-screen: drop it when leaving.
    DisposableEffect(Unit) { onDispose { vm.setShowFiltered(false) } }

    // Multi-select candidates: whatever is currently loaded, in whichever
    // scope is active.
    val allCandidates = if (scopeIds != null) {
        allResults.map { SelectedQuote(it.dbId, it.quote.number) }
    } else {
        (0 until pagingItems.itemCount).mapNotNull { i ->
            pagingItems[i]?.let { SelectedQuote(dbId, it.number) }
        }
    }

    LaunchedEffect(Unit) { catVm.refresh() }


    // v1.58: site cards may only paint their authentic page background
    // outside the card when the search scope is exactly one database
    // ("This database" or a single chosen database); mixed scopes keep
    // the global page background outside the cards.
    val searchSingleDb = scopeIds == null || scopeIds.size == 1
    CompositionLocalProvider(LocalSingleDatabaseMode provides searchSingleDb) {
    Column(Modifier.fillMaxSize()) {
        // v1.69: query field + a Search button beside it. The button runs
        // the search with whatever's set — filters alone, no query needed.
        // v1.80: pill-sized controls — the field is 40.dp and the Search
        // button its natural 40.dp pill height, matching the top bar's
        // database/Categories pills (was a 56.dp row with 56.dp controls).
        // v1.81: 48.dp row with tighter vertical padding (was 56.dp /
        // 8.dp) so the row sits closer to the top bar and takes less space.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        // v1.84: BasicTextField with a hand-drawn pill — the M3 TextField
        // kept shaving the tops of the glyphs no matter the font size, so
        // the field now centers the text itself with exact padding instead
        // of relying on M3's internals.
        val pillTextStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        BasicTextField(
            value = vm.query,
            onValueChange = vm::updateQuery,
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            singleLine = true,
            textStyle = pillTextStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { runSearch() }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (vm.query.isEmpty()) {
                            Text(
                                "Search quote text… or a #number",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = pillTextStyle.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        innerTextField()
                    }
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.updateQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    } else {
                        // v1.73: mic in the trailing slot when there's nothing
                        // to clear — dictated text runs the search.
                        VoiceSearchButton(
                            onResult = {
                                vm.updateQuery(it)
                                runSearch()
                            },
                            onUnavailable = { toast("Voice search not available") }
                        )
                    }
                }
            }
        )
        Button(
            onClick = { runSearch() },
            // v1.80: natural pill height (40.dp) like the top bar's pills.
        ) {
            Text("Search")
        }
        }
        // v1.74.5: regex mode with a pattern that won't compile.
        if (vm.regexInvalid) {
            Text(
                "Invalid regular expression — check the pattern.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )
        }
        // v1.73: nick + category autocomplete under the query field.
        // A category tap filters to it and searches; a nick tap opens the
        // person's page (same as the Person quick-jump).
        // v1.74.5: autocomplete is off in regex mode — the query is a
        // pattern, not a prefix.
        if (!vm.useRegex) {
        QueryAutocomplete(
            query = vm.query,
            manager = manager,
            categories = categories,
            onNickClick = onNickClick,
            onCategoryClick = { cat ->
                vm.updateCategories(vm.categoryIds + cat.id)
                vm.updateQuery("") // v1.73: commit the suggestion, dismiss autocomplete
                runSearch()
            }
        )
        }
        // v1.72.3: the Tip row is gone (syntax help lives in Help) — its
        // Save button moved into the Filters header, and the Person
        // quick-jump moved inside the expanded filter section, so quotes
        // are visible without scrolling.
        // v1.71: when a search comes up empty, name the filters that might
        // be hiding everything so there's something actionable to try.
        val blockingFilters = buildList {
            if (vm.minRating != null) add("Min rating")
            vm.maturity?.let { add("Maturity") }
            vm.castSize?.let { add("Cast size") }
            if (vm.excludedCategoryIds.isNotEmpty() ||
                vm.excludedMaturities.isNotEmpty() ||
                vm.excludedCasts.isNotEmpty()
            ) add("exclusions")
            if (scopeIds != null) add("database scope")
            if (vm.notesScope) add("Notes")
            if (vm.voiceOnly) add("Has voice")
            // v1.75: "Has explanation" shows in the active-filter summary.
            if (vm.explanationOnly) add("Has explanation")
            if (vm.useRegex) add("Regex")
            if (vm.categoryIds.isNotEmpty()) add("categories")
            if (vm.lengthMode != com.quotebrowser.app.data.LengthMode.ALL) add("length")
        }
        // v1.74.3 diagnostic: when a cast filter is set, count matching
        // quotes directly in the databases to distinguish "no data" from
        // "search bug" in the empty state.
        var castDiagCount by remember { mutableStateOf<Int?>(null) }
        val diagCast = vm.castSize
        LaunchedEffect(diagCast, scopeIds) {
            castDiagCount = null
            if (diagCast != null) {
                castDiagCount = try {
                    vm.countByCastAcrossScope(diagCast)
                } catch (e: Exception) {
                    -1
                }
            }
        }
        fun emptyMessage(query: String, where: String = ""): String {
            val base = "No quotes match \"$query\"$where."
            val msg = if (blockingFilters.isEmpty()) base
            else "$base\nTry clearing: ${blockingFilters.joinToString(", ")}."
            // Append the diagnostic when a cast filter is active.
            return if (diagCast != null && castDiagCount != null) {
                val diag = if (castDiagCount!! >= 0) {
                    "\n\n[Debug] Database has ${castDiagCount} quotes with ${diagCast.label}."
                } else {
                    "\n\n[Debug] Could not count database."
                }
                msg + diag
            } else msg
        }
        // v1.72: the shared filter panel (moods + collapsible filters).
        // v1.72.3: Person quick-jump and the Save action live inside the
        // panel now so the page top stays compact.
        com.quotebrowser.app.ui.components.SearchFilterPanel(
            vm = vm,
            categories = categories,
            databases = databases,
            filtersExpanded = filtersExpanded,
            onFiltersExpandedChange = { filtersExpanded = it },
            toast = ::toast,
            autoSubmit = true,
            onSubmit = { submitSearch() },
            onVoiceToggled = { pagingItems.refresh() },
            onSaveSearch = {
                saveSearchName = vm.query
                showSaveSearchDialog = true
            },
            extraExpandedContent = {
                // Person quick-jump: navigation, not a filter — pinned at
                // the top of the expanded section.
                com.quotebrowser.app.ui.components.PersonFilterButton(
                    manager = manager,
                    onNickClick = onNickClick,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        )

        QuoteSelectionHost(
            selection = selection,
            manager = manager,
            allCandidates = if (vm.notesScope) {
                noteResults.map { SelectedQuote(it.dbId, it.quote.number) }
            } else {
                allCandidates
            },
            onAddToPlaylist = onAddToPlaylist,
            autoRulePhrase = vm.query.takeIf { it.isNotBlank() },
            showSelectAll = bulkSelectAll
        )
        if (!vm.notesScope) {
            HiddenQuotesBanner(
                hiddenCount = hiddenCount,
                showingHidden = showFiltered,
                onShow = { vm.setShowFiltered(true) },
                onHide = { vm.setShowFiltered(false) }
            )
        }

        // v1.66: filter browsing (mood buttons) shows results with a blank
        // query instead of the recent/saved-search picker.
        if (vm.query.isBlank() && !vm.filterBrowse) {
            if (recentHistory.isEmpty() && namedSearches.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Type to search quote text.\nEnter digits to jump to a quote number.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (recentHistory.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { confirmClearSaved = true }) {
                                Text("Clear")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // v1.66: each entry is a full search snapshot —
                            // tapping re-runs it exactly, filters and all.
                            recentHistory.forEach { recent ->
                                val summary = recent.filterSummary()
                                InputChip(
                                    selected = false,
                                    onClick = {
                                        vm.applySearchRecord(recent)
                                        focusManager.clearFocus()
                                    },
                                    label = {
                                        Text(
                                            buildString {
                                                append(
                                                    recent.query.ifBlank { "(filter browse)" }
                                                )
                                                if (summary.isNotEmpty()) {
                                                    append(" · ")
                                                    append(summary)
                                                }
                                            }
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove recent search",
                                            modifier = Modifier.clickable {
                                                searchHistory.remove(recent)
                                                recentHistory = searchHistory.history()
                                            }
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    if (namedSearches.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Named searches",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { showManageSearches = true }) {
                                Text("Manage")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            namedSearches.forEach { ns ->
                                InputChip(
                                    selected = false,
                                    onClick = { vm.applyNamedSearch(ns) },
                                    label = { Text(ns.name) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    Text(
                        text = "Tip: enter digits to jump to a quote number.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (scopeIds != null) {
            // Multi-database scope: merged hits from the selected databases,
            // each row showing which database it came from.
            if (allResults.isEmpty()) {
                EmptyState(emptyMessage(vm.query, " in any database"))
            } else {
                LazyColumn(
                    Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            "${allResults.size} results",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(allResults.size) { index ->
                        val sq = allResults[index]
                        val selKey = SelectedQuote(sq.dbId, sq.quote.number)
                        QuoteRow(
                            quote = sq.quote,
                            isFavorite = (sq.dbId to sq.quote.number) in allFavKeys,
                            onToggleFavorite = { vm.toggleFavorite(sq.dbId, sq.quote) },
                            onClick = { onSourcedQuoteClick(sq.dbId, sq.quote.number) },
                            onLongClick = { selection.enter(selKey) },
                            selectionActive = selection.active,
                            isSelected = selection.isSelected(selKey),
                            onSelectionToggle = { selection.toggle(selKey) },
                            showNumber = showNumber,
                            showRating = showRating,
                            // v1.66: cross-database dedup — one row, with the
                            // other databases noted on the source label.
                            sourceLabel = buildString {
                                append(manager.shortLabel(sq.dbId))
                                if (sq.alsoIn.isNotEmpty()) {
                                    append(" · also in ")
                                    append(sq.alsoIn.joinToString(", "))
                                }
                            },
                            sourceDbId = sq.dbId,
                            dbId = sq.dbId,
                            // v1.74.7: ⋯ menu on multi-db rows too.
                            onMoreClick = { sheetQuote = sq },
                            onNickClick = onNickClick)
                    }
                }
            }
        } else if (vm.notesScope) {
            // Notes scope: quotes whose personal note matches the query.
            if (noteResults.isEmpty()) {
                EmptyState(emptyMessage(vm.query))
            } else {
                LazyColumn(
                    Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            "${noteResults.size} results",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(noteResults.size) { index ->
                        val sq = noteResults[index]
                        val selKey = SelectedQuote(sq.dbId, sq.quote.number)
                        QuoteRow(
                            quote = sq.quote,
                            dbId = sq.dbId,
                            isFavorite = false,
                            onToggleFavorite = {},
                            showFavoriteAction = false,
                            onClick = { onSourcedQuoteClick(sq.dbId, sq.quote.number) },
                            onLongClick = { selection.enter(selKey) },
                            selectionActive = selection.active,
                            isSelected = selection.isSelected(selKey),
                            onSelectionToggle = { selection.toggle(selKey) },
                            showNumber = showNumber,
                            showRating = showRating,
                            sourceLabel = manager.shortLabel(sq.dbId),
                            sourceDbId = sq.dbId,
                            // v1.74.7: ⋯ menu on notes-scope rows too.
                            onMoreClick = { sheetQuote = sq },
                            onNickClick = onNickClick)
                    }
                }
            }
        } else {
            val refreshState = pagingItems.loadState.refresh
            when {
                refreshState is LoadState.Loading && pagingItems.itemCount == 0 -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                refreshState is LoadState.Error && pagingItems.itemCount == 0 -> {
                    EmptyState("Search failed.\n${refreshState.error.message}")
                }
                refreshState is LoadState.NotLoading && pagingItems.itemCount == 0 -> {
                    EmptyState(emptyMessage(vm.query))
                }
                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "${pagingItems.itemCount} loaded",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(pagingItems.itemCount) { index ->
                            val quote = pagingItems[index]
                            if (quote != null) {
                                val selKey = SelectedQuote(dbId, quote.number)
                                QuoteRow(
                                    quote = quote,
                                    dbId = dbId,
                                    isFavorite = quote.number in favNumbers,
                                    onToggleFavorite = { vm.toggleFavorite(quote) },
                                    onClick = { onQuoteClick(quote.number) },
                                    onLongClick = { selection.enter(selKey) },
                                    onMoreClick = { sheetQuote = SourcedQuote(dbId, quote) },
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
                                    },
                            onNickClick = onNickClick)
                            }
                        }
                        if (pagingItems.loadState.append is LoadState.Loading) {
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
    }

    val sheetItem = sheetQuote
    val sq = sheetItem?.quote
    // v1.74.7: the sheet acts on the quote's own database in
    // multi-database / notes results, not the screen's database.
    val sheetDbId = sheetItem?.dbId ?: dbId
    QuoteActionSheetHost(
        quote = sq,
        isFavorite = sq?.let { (sheetDbId to it.number) in allFavKeys } == true,
        onDismiss = { sheetQuote = null },
        onToggleFavorite = { sq?.let { vm.toggleFavorite(sheetDbId, it) } },
        onOpen = { sq?.let { onSourcedQuoteClick(sheetDbId, it.number) } },
        dbId = sheetDbId,
        inBasket = sq?.let { BasketEntry(sheetDbId, it.number) in basket } == true,
        onToggleBasket = { sq?.let { onToggleBasket(sheetDbId, it.number) } },
        onHideQuote = { sq?.let { vm.hideQuote(sheetDbId, it.number) } },
        // v1.74.7: "Read aloud" in the quote menu.
        onReadAloud = { sheetItem?.let(onReadAloudQuote) },
        // v1.53 report/flag.
        isFlagged = sq?.let { "$sheetDbId/${it.number}" in flaggedQuotes } == true,
        onFlagQuote = { dbId, number, reason, custom ->
            vm.flagQuote(dbId, number, reason, custom)
        },
        onUnflagQuote = { sq?.let { vm.unflagQuote(sheetDbId, it.number) } },
        // v1.66: "Add to list…" (user quote lists).
        onAddToList = if (quoteListStore != null) {
            { showAddToListFor = sq?.let { sheetDbId to it.number } }
        } else {
            null
        }
    )

    // v1.66: the add-to-list picker opened from the long-press sheet.
    val addTarget = showAddToListFor
    val listStore = quoteListStore
    if (addTarget != null && listStore != null) {
        AddToListDialog(
            listStore = listStore,
            dbId = addTarget.first,
            number = addTarget.second,
            onDismiss = { showAddToListFor = null }
        )
    }

    if (confirmClearSaved) {
        AlertDialog(
            onDismissRequest = { confirmClearSaved = false },
            title = { Text("Clear recent searches?") },
            text = { Text("This removes all ${recentHistory.size} recent searches.") },
            confirmButton = {
                TextButton(onClick = {
                    searchHistory.clear()
                    recentHistory = emptyList()
                    confirmClearSaved = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearSaved = false }) { Text("Cancel") }
            }
        )
    }

    if (showSaveSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSearchDialog = false },
            title = { Text("Save search") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Saves the query plus the current databases, " +
                            "categories, exclusions, maturity, cast size, " +
                            "rating, sort, and length filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = saveSearchName,
                        onValueChange = { saveSearchName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (vm.saveNamedSearch(saveSearchName)) {
                            toast("Search saved")
                            showSaveSearchDialog = false
                        } else {
                            toast("Enter a name")
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveSearchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showManageSearches) {
        ManageNamedSearchesDialog(
            searches = namedSearches,
            onRename = { old, new -> vm.renameNamedSearch(old, new) },
            onDelete = { vm.deleteNamedSearch(it) },
            onAlertToggle = { name, alert -> vm.setNamedSearchAlert(name, alert) },
            onDismiss = { showManageSearches = false }
        )
    }
    } // LocalSingleDatabaseMode
}
