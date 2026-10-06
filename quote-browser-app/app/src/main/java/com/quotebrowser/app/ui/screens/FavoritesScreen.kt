package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import com.quotebrowser.app.ui.QuoteDnaDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.FavoritesViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.HiddenQuotesBanner
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.QuoteActionSheetHost
import com.quotebrowser.app.ui.components.ShareOptionsDialog
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.components.QuoteSelectionHost
import com.quotebrowser.app.ui.components.SelectedQuote
import com.quotebrowser.app.ui.components.rememberQuoteSelection
import com.quotebrowser.app.ui.exportQuotesToUri
import com.quotebrowser.app.ui.CollectionQuote
import com.quotebrowser.app.ui.QuoteCollection
import com.quotebrowser.app.ui.shareCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

@Composable
fun FavoritesScreen(vm: FavoritesViewModel, manager: DatabaseManager, onQuoteClick: (Int) -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    onReadAloud: (List<SourcedQuote>) -> Unit = {},
    onAddToPlaylist: (List<SourcedQuote>) -> Unit = {},
    /** v1.74.7: the quote sheet's "Read aloud" row reads one quote aloud. */
    onReadAloudQuote: (SourcedQuote) -> Unit = {},
    initialFolder: String? = null,
    onInitialFolderConsumed: () -> Unit = {},
    onNickClick: (String) -> Unit = {},
    // v1.77: opening a quote with its true source database (used in the
    // virtual "all" view, where each favorite keeps its own dbId). When
    // null, falls back to [onQuoteClick].
    onSourcedQuoteClick: ((String, Int) -> Unit)? = null) {
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    // v1.77: source-aware favorites for the virtual "all" view — each
    // entry keeps its true dbId so its card resolves the right site theme
    // (the old path collapsed "all" to the first bundled database).
    val allDbFavorites by vm.allDbFavorites.collectAsStateWithLifecycle()
    val allFavKeys by vm.allFavoriteKeys.collectAsStateWithLifecycle()
    // v1.53 report/flag.
    val flaggedQuotes by manager.flaggedQuotes.collectAsStateWithLifecycle()
    // v1.77: local thumbs ratings, so the header +/- vote links reflect
    // and change the user's vote.
    val localRatings by manager.localRatings.collectAsStateWithLifecycle()
    val folders by vm.folders.collectAsStateWithLifecycle()
    val dbId by vm.dbId.collectAsStateWithLifecycle()
    val isAll = dbId == DatabaseManager.ALL_DBS_ID
    // One normalized list: single-db wraps with the current id, all-mode
    // uses each favorite's true source id.
    val sourcedFavorites = remember(favorites, allDbFavorites, isAll, dbId) {
        if (isAll) allDbFavorites
        else favorites.map { SourcedQuote(dbId, it) }
    }
    val openSourced: (SourcedQuote) -> Unit = { sq ->
        if (onSourcedQuoteClick != null) onSourcedQuoteClick(sq.dbId, sq.quote.number)
        else onQuoteClick(sq.quote.number)
    }
    // v1.49: Select all / Deselect all in the bulk-selection bar.
    val bulkSelectAll by manager.bulkSelectAll.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Pinned-shortcut deep link: open straight into this folder.
    LaunchedEffect(initialFolder) {
        if (initialFolder != null) {
            vm.selectFolder(initialFolder)
            onInitialFolderConsumed()
        }
    }
    var sheetSourced by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.88.32: one-tap share from each favorite's card header.
    var shareSourced by remember { mutableStateOf<SourcedQuote?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderMenuFor by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDnaDialog by remember { mutableStateOf(false) }
    val selection = rememberQuoteSelection()
    val showFiltered by vm.showFiltered.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenCount.collectAsStateWithLifecycle()

    // The temporary filter bypass is per-screen: drop it when leaving.
    DisposableEffect(Unit) { onDispose { vm.setShowFiltered(false) } }

    val shownFavorites = remember(sourcedFavorites, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) sourcedFavorites
        else sourcedFavorites.filter {
            it.quote.text.contains(q, ignoreCase = true) || it.quote.number.toString() == q
        }
    }
    // Pinned favorites sort to the top, in the order they were pinned.
    // v1.77: pins are keyed by true source dbId + number.
    var pinnedOrder by remember { mutableStateOf(loadPinnedOrder(context)) }
    fun togglePin(sq: SourcedQuote) {
        val key = pinKey(sq.dbId, sq.quote.number)
        pinnedOrder = if (key in pinnedOrder) pinnedOrder - key else pinnedOrder + key
        savePinnedOrder(context, pinnedOrder)
    }
    val pinnedFirst = remember(shownFavorites, pinnedOrder) {
        val rank = pinnedOrder.withIndex().associate { it.value to it.index }
        shownFavorites.sortedWith(
            compareBy(
                { rank[pinKey(it.dbId, it.quote.number)] ?: Int.MAX_VALUE },
                { it.quote.number }
            )
        )
    }
    val allCandidates = shownFavorites.map { SelectedQuote(it.dbId, it.quote.number) }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                scope.launch {
                    val ok = exportQuotesToUri(
                        context, uri, sourcedFavorites.map { it.quote }, "Quote Browser — Favorites"
                    )
                    Toast.makeText(
                        context,
                        if (ok) "Favorites exported" else "Export failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // v1.77: single-database mode paints the authentic page background
        // outside the cards; the mixed "all" view keeps the global page
        // background (each card still themes itself from its true source).
    CompositionLocalProvider(LocalSingleDatabaseMode provides !isAll) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Favorites") {
            FilledTonalIconButton(
                // v1.77: the playlist keeps each quote's true source dbId.
                onClick = { onReadAloud(sourcedFavorites) },
                enabled = sourcedFavorites.isNotEmpty()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Read favorites aloud")
            }
            FilledTonalIconButton(
                onClick = { exportLauncher.launch("quote-browser-favorites.txt") },
                enabled = sourcedFavorites.isNotEmpty()
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = "Export favorites to .txt")
            }
            // v1.78: "My Quote DNA" — a fun taste profile from favorites + votes.
            FilledTonalIconButton(
                onClick = { showDnaDialog = true }
            ) {
                Icon(Icons.Filled.Science, contentDescription = "My Quote DNA")
            }
        }
        // Folder filter chips + new-folder button.
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = vm.selectedFolder == null,
                    onClick = { vm.selectFolder(null) },
                    label = { Text("All") },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            items(folders) { folder ->
                @OptIn(ExperimentalFoundationApi::class)
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .combinedClickable(
                            onClick = { vm.selectFolder(folder) },
                            onLongClick = { folderMenuFor = folder }
                        )
                ) {
                    // Clicks are handled by the Box above so long-press works.
                    FilterChip(
                        selected = vm.selectedFolder == folder,
                        onClick = {},
                        label = { Text(folder) }
                    )
                    DropdownMenu(
                        expanded = folderMenuFor == folder,
                        onDismissRequest = { folderMenuFor = null }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                folderMenuFor = null
                                renameTarget = folder
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share as collection…") },
                            onClick = {
                                folderMenuFor = null
                                scope.launch {
                                    val quotes = withContext(Dispatchers.IO) {
                                        vm.quotesForCollection(folder)
                                    }
                                    shareCollection(
                                        context,
                                        QuoteCollection(
                                            name = folder,
                                            source = "favorites-folder",
                                            quotes = quotes.map {
                                                CollectionQuote(
                                                    it.number,
                                                    it.text,
                                                    it.rating
                                                )
                                            }
                                        )
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pin to home screen") },
                            onClick = {
                                folderMenuFor = null
                                if (pinFolderShortcut(context, folder)) {
                                    Toast.makeText(
                                        context,
                                        "Pinned \"$folder\" — confirm on your launcher",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Pinning not supported by this launcher",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                folderMenuFor = null
                                deleteTarget = folder
                            }
                        )
                    }
                }
            }
            item {
                TextButton(onClick = { showNewFolderDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("New folder")
                }
            }
        }
        if (sourcedFavorites.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search favorites") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(onClick = { searchQuery = "" }) {
                            Text("Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        QuoteSelectionHost(
            selection = selection,
            manager = manager,
            allCandidates = allCandidates,
            onAddToPlaylist = onAddToPlaylist,
            showSelectAll = bulkSelectAll
        )
        HiddenQuotesBanner(
            hiddenCount = hiddenCount,
            showingHidden = showFiltered,
            onShow = { vm.setShowFiltered(true) },
            onHide = { vm.setShowFiltered(false) }
        )
        if (shownFavorites.isEmpty()) {
            EmptyState(
                if (searchQuery.isNotBlank())
                    "No favorites match \"${searchQuery.trim()}\" here."
                else if (vm.selectedFolder != null)
                    "No favorites in \"${vm.selectedFolder}\" yet.\nLong-press a favorite to move it here."
                else "No favorites yet.\nTap the heart on any quote to save it here."
            )
        } else {
            LazyColumn(Modifier.fillMaxSize(),
                flingBehavior = rememberCappedFlingBehavior()) {
                // v1.77: keyed by source db + number (numbers collide across
                // databases); each card gets its true source dbId.
                items(pinnedFirst, key = { "${it.dbId}:${it.quote.number}" }) { sq ->
                    val quote = sq.quote
                    val selKey = SelectedQuote(sq.dbId, quote.number)
                    val pinned = pinKey(sq.dbId, quote.number) in pinnedOrder
                    // v1.77: the pin lives in the card header now (next to
                    // the flag / in the flag's spot on bash.org), so the
                    // card renders full-width and centered.
                    QuoteRow(
                        quote = quote,
                        dbId = sq.dbId,
                        isFavorite = true,
                        onToggleFavorite = { vm.toggleFavorite(sq.dbId, quote) },
                        onClick = { openSourced(sq) },
                        onLongClick = { selection.enter(selKey) },
                        onMoreClick = { sheetSourced = sq },
                        // v1.88.32: share button on every Favorites card.
                        onShareClick = { shareSourced = sq },
                        selectionActive = selection.active,
                        isSelected = selection.isSelected(selKey),
                        onSelectionToggle = { selection.toggle(selKey) },
                        showNumber = showNumber,
                        showRating = showRating,
                        myRating = localRatings["${sq.dbId}/${quote.number}"] ?: 0,
                        onRate = { vm.rateQuote(sq.dbId, quote.number, it) },
                        isPinned = pinned,
                        onTogglePin = { togglePin(sq) },
                        // v1.53 report/flag.
                        isFlagged = "${sq.dbId}/${quote.number}" in flaggedQuotes,
                        flagInfo = flaggedQuotes["${sq.dbId}/${quote.number}"],
                        onFlagQuote = { reason, custom ->
                            vm.flagQuote(sq.dbId, quote.number, reason, custom)
                        },
                        onNickClick = onNickClick)
                }
            }
        }
    }

    val renameFor = renameTarget
    if (renameFor != null) {
        RenameFolderDialog(
            current = renameFor,
            onDismiss = { renameTarget = null },
            onConfirm = { name ->
                if (!vm.renameFolder(renameFor, name)) {
                    Toast.makeText(
                        context,
                        "That folder name is already used",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                renameTarget = null
            }
        )
    }

    val deleteFor = deleteTarget
    if (deleteFor != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete folder?") },
            text = {
                Text("Favorites in \"$deleteFor\" stay saved, they just leave the folder.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteFolder(deleteFor)
                    deleteTarget = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // v1.78: "My Quote DNA" taste-profile dialog.
    if (showDnaDialog) {
        QuoteDnaDialog(
            manager = manager,
            onDismiss = { showDnaDialog = false }
        )
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { name ->
                if (!vm.addFolder(name)) {
                    Toast.makeText(
                        context,
                        "That folder already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    vm.selectFolder(name)
                }
                showNewFolderDialog = false
            }
        )
    }

    val sq = sheetSourced
    val sheetDbId = sq?.dbId
    QuoteActionSheetHost(
        quote = sq?.quote,
        // v1.77: per-database favorite keys (the sheet can show a quote
        // from any database in the "all" view).
        isFavorite = sq?.let { (it.dbId to it.quote.number) in allFavKeys } == true,
        onDismiss = { sheetSourced = null },
        onToggleFavorite = { sq?.let { vm.toggleFavorite(it.dbId, it.quote) } },
        onOpen = { sq?.let { openSourced(it) } },
        dbId = sheetDbId ?: dbId,
        inBasket = sq?.let { BasketEntry(it.dbId, it.quote.number) in basket } == true,
        onToggleBasket = { sq?.let { onToggleBasket(it.dbId, it.quote.number) } },
        onHideQuote = { sq?.let { vm.hideQuote(it.dbId, it.quote.number) } },
        // v1.74.7: "Read aloud" in the quote menu.
        onReadAloud = { val s = sq; if (s != null) onReadAloudQuote(s) },
        folders = folders,
        currentFolder = sq?.let { manager.folderForFavorite(it.dbId, it.quote.number) },
        onMoveToFolder = { folder ->
            sq?.let { manager.setFavoriteFolder(it.dbId, it.quote.number, folder) }
        },
        // v1.53 report/flag.
        isFlagged = sq?.let { "${it.dbId}/${it.quote.number}" in flaggedQuotes } == true,
        onFlagQuote = { bid, number, reason, custom ->
            vm.flagQuote(bid, number, reason, custom)
        },
        onUnflagQuote = { sq?.let { vm.unflagQuote(it.dbId, it.quote.number) } }
    )

    // v1.88.32: share dialog for the Favorites card share button — uses
    // the quote's real source dbId.
    val shareSq = shareSourced
    if (shareSq != null) {
        ShareOptionsDialog(
            quote = shareSq.quote,
            onDismiss = { shareSourced = null },
            dbId = shareSq.dbId
        )
    }
    } // LocalSingleDatabaseMode
}

/** Renames an existing favorite folder. */
@Composable
private fun RenameFolderDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/** Names a new favorite folder. */
@Composable
private fun NewFolderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.trim().isNotEmpty()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Pins a launcher shortcut that opens Favorites filtered to [folder].
 * Returns false when the launcher doesn't support pinning.
 */private fun pinFolderShortcut(context: android.content.Context, folder: String): Boolean {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return false
    val sm = context.getSystemService(android.content.pm.ShortcutManager::class.java)
        ?: return false
    if (!sm.isRequestPinShortcutSupported) return false
    val intent = android.content.Intent(context, com.quotebrowser.app.MainActivity::class.java).apply {
        action = com.quotebrowser.app.MainActivity.ACTION_OPEN_FOLDER
        putExtra(com.quotebrowser.app.MainActivity.EXTRA_FOLDER, folder)
    }
    val shortcut = android.content.pm.ShortcutInfo.Builder(
        context, "qb-folder-${folder.hashCode()}"
    )
        .setShortLabel(folder.take(25))
        .setLongLabel("QuoteBrowser: $folder")
        .setIcon(
            android.graphics.drawable.Icon.createWithResource(
                context, com.quotebrowser.app.R.drawable.ic_tile_quote
            )
        )
        .setIntent(intent)
        .build()
    return try {
        sm.requestPinShortcut(shortcut, null)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Pinned favorites: a per-install ordered pin list ("dbId:number") kept in
 * SharedPreferences, so pinning needs no database migration. Pinned quotes
 * sort to the top of the favorites list in pin order.
 */
private const val PIN_PREFS = "pinned_favorites_v145"
private const val PIN_KEY = "pinned_order"

private fun pinKey(dbId: String, number: Int) = "$dbId:$number"

private fun loadPinnedOrder(context: android.content.Context): List<String> =
    context.getSharedPreferences(PIN_PREFS, android.content.Context.MODE_PRIVATE)
        .getString(PIN_KEY, "").orEmpty()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

private fun savePinnedOrder(context: android.content.Context, order: List<String>) {
    context.getSharedPreferences(PIN_PREFS, android.content.Context.MODE_PRIVATE)
        .edit()
        .putString(PIN_KEY, order.joinToString(","))
        .apply()
}
