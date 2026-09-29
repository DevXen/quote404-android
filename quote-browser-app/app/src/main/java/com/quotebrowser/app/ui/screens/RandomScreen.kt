package com.quotebrowser.app.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DisplayPrefs
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.MOOD_PRESETS
import com.quotebrowser.app.ui.isMoodActive
import com.quotebrowser.app.ui.moodNameToId
import com.quotebrowser.app.ui.toggleMoodSelection
import com.quotebrowser.app.ui.MoodPreset
import com.quotebrowser.app.ui.RandomViewModel
import com.quotebrowser.app.ui.components.CastSizeFilterDropdown
import com.quotebrowser.app.ui.components.DbPageBackground
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.HiddenQuotesBanner
import com.quotebrowser.app.ui.components.MaturityFilterDropdown
import com.quotebrowser.app.ui.components.LocalChromeAccent
import com.quotebrowser.app.ui.components.LocalDbThemeSnapshots
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteActionSheetHost
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.components.QuoteSelectionHost
import com.quotebrowser.app.ui.components.SelectedQuote
import com.quotebrowser.app.ui.components.ShareOptionsDialog
import com.quotebrowser.app.ui.components.dbAccentColor
import com.quotebrowser.app.ui.components.rememberQuoteSelection
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RandomScreen(
    vm: RandomViewModel,
    catVm: CategoryViewModel,
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    shakeEnabled: Boolean = true,
    onDeckClick: () -> Unit = {},
    onReadAloudSet: () -> Unit = {},
    onRatingDeckClick: () -> Unit = {},
    onFlashcardsClick: () -> Unit = {},
    onAddToPlaylist: (List<SourcedQuote>) -> Unit = {},
    onRadioClick: () -> Unit = {},
    /** v1.74.7: the quote sheet's "Read aloud" row reads one quote aloud. */
    onReadAloudQuote: (SourcedQuote) -> Unit = {},
    // v1.67: "Export video" in the header menu — sends the current set to
    // the export queue and opens Export with MP4 preselected.
    onExportVideo: () -> Unit = {},
    onNickClick: (String) -> Unit = {}
) {
    // v1.88.22: "Break between quotes" now controls blank lines between
    // quote-body turns (via paragraphizeBody, read inside cleanDisplayText).
    // Collected as state so the list recomposes when the setting changes;
    // the value itself is read downstream, not here.
    // (Idempotent; first call wins and loads the persisted setting.)
    DisplayPrefs.init(LocalContext.current)
    @Suppress("UNUSED_VARIABLE")
    val breakSetting by DisplayPrefs.breakBetweenQuotes.collectAsState()
    val quotes by vm.quotes.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val favKeys by vm.favoriteKeys.collectAsStateWithLifecycle()
    // v1.53 report/flag.
    val flaggedQuotes by manager.flaggedQuotes.collectAsStateWithLifecycle()
    val databases by vm.databases.collectAsStateWithLifecycle()
    val sourceIds by vm.sourceIds.collectAsStateWithLifecycle()
    // Source chips only make sense when the random pool spans databases.
    val multiDbPool = (sourceIds?.size ?: databases.size) > 1
    val hideSeen by vm.hideSeen.collectAsStateWithLifecycle()
    var sheetQuote by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.61.9: one-tap share from the card header.
    var shareTarget by remember { mutableStateOf<SourcedQuote?>(null) }
    var countText by remember { mutableStateOf(vm.count.toString()) }
    // v1.67: the spelled-out Options section is gone — its actions live in
    // the header ⋮ menu, and categories moved to the top bar next to the
    // database pill.
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // v1.88.19: so Enter/Done in the Count box can dismiss the keyboard.
    val keyboardController = LocalSoftwareKeyboardController.current
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    // v1.71: moods are additive toggle filters in the Options pull-down,
    // same shared behavior as Search — several can be on at once, and each
    // toggle re-rolls the deck. The menu stays open to flip through moods.
    val allCategories by catVm.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val moodNameToId = remember(allCategories) { moodNameToId(allCategories) }
    fun toggleMood(preset: MoodPreset) {
        if (allCategories.isEmpty()) {
            toast("Categories still loading — try again in a moment")
            return
        }
        val next = toggleMoodSelection(vm.categoryIds.toSet(), preset, moodNameToId)
        if (next == vm.categoryIds.toSet()) {
            toast("No matching categories in this database")
            return
        }
        val names = allCategories.filter { it.id in next }.map { it.name }.toSet()
        vm.updateCategories(next, names)
    }
    val haptics = LocalHapticFeedback.current
    val selection = rememberQuoteSelection()
    val showFiltered by vm.showFiltered.collectAsStateWithLifecycle()
    val hiddenCount by vm.hiddenCount.collectAsStateWithLifecycle()
    val localRatings by manager.localRatings.collectAsStateWithLifecycle()

    // The temporary filter bypass is per-screen: drop it when leaving.
    DisposableEffect(Unit) { onDispose { vm.setShowFiltered(false) } }

    // Slide direction for the shuffle animation: 1 = new set slides in
    // from the right, -1 = stepping back slides in from the left.
    var direction by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) { catVm.refresh() }

    // v1.88.19: commits whatever is currently typed in the Count box
    // first (so a new number typed then tapping Shuffle is used), and
    // dismisses the keyboard so Enter/Done leaves the list visible.
    fun shuffle() {
        countText.toIntOrNull()?.let { vm.updateCount(it) }
        keyboardController?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        direction = 1
        vm.newRandom()
    }

    fun goBack() {
        if (vm.goBack()) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            direction = -1
        }
    }

    // Shake the phone to shuffle (toggleable in Settings).
    DisposableEffect(shakeEnabled) {
        if (!shakeEnabled) return@DisposableEffect onDispose {}
        val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                ?: return@DisposableEffect onDispose {}
        var lastShakeAt = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val v = event.values
                val gForce =
                    sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) /
                        SensorManager.GRAVITY_EARTH
                if (gForce > 2.4f) {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastShakeAt > 1200) {
                        lastShakeAt = now
                        shuffle()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(
            listener, accelerometer, SensorManager.SENSOR_DELAY_UI
        )
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // Pull down to shuffle. The stock pull-to-refresh state drives
    // isRefreshing itself when the list is pulled past the threshold; a
    // small custom indicator below is driven by our own flag (reset in a
    // finally block) so it can never get stuck on screen.
    var refreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()
    if (pullState.isRefreshing) {
        LaunchedEffect(Unit) {
            refreshing = true
            try {
                shuffle()
            } finally {
                refreshing = false
                pullState.endRefresh()
            }
        }
    }
    // The header card isn't scrollable, so the stock pull gesture can't
    // start there (it takes up half the screen). Track a manual downward
    // pull on the header too, honored only when the quote list is at
    // the very top.
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val headerPullThresholdPx = with(density) { 96.dp.toPx() }
    var headerPullPx by remember { mutableFloatStateOf(0f) }

    // Screen-level box so the pull indicator can float above everything.
    // v1.56: per-database page background — exactly one selected source
    // database paints that database's own page color (or its custom
    // color/image); multiple/all sources keep the global theme page.
    val singleSourceDbId = sourceIds?.singleOrNull()
    // v1.57: exactly one random source tints the screen chrome (Shuffle,
    // action buttons, dialogs, menus) to that database's own theme
    // accent; multiple/all sources keep the neutral theme chrome.
    val dbThemeSnap = LocalDbThemeSnapshots.current
    val dbAccent = remember(dbThemeSnap, singleSourceDbId) {
        dbAccentColor(dbThemeSnap, singleSourceDbId)
    }
    // v1.57: the screen chrome (Shuffle, action buttons) follows the
    // active app theme; only the database pill carries the database
    // accent. LocalChromeAccent tints dialogs/menus.
    // v1.58: site cards may only paint their authentic page background
    // outside the card when the random pool is exactly one database
    // (mirrors BrowseScreen); mixed pools keep the global page
    // background outside the cards.
    CompositionLocalProvider(
        LocalChromeAccent provides dbAccent,
        LocalSingleDatabaseMode provides (singleSourceDbId != null)
    ) {
    DbPageBackground(
        dbId = singleSourceDbId,
        manager = manager,
        modifier = Modifier.fillMaxSize()
    ) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                // v1.72.4: tucked up right under the search bar — was
                // vertical = 8.dp.
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .pointerInput(listState) {
                    // Manual pull-to-shuffle for drags that start on the
                    // header (it isn't scrollable, so the stock pull
                    // gesture can't start there). Only honored when the
                    // quote list is sitting at the very top.
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (headerPullPx >= headerPullThresholdPx) {
                                shuffle()
                            }
                            headerPullPx = 0f
                        },
                        onDragCancel = { headerPullPx = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            val atTop =
                                listState.firstVisibleItemIndex == 0 &&
                                    listState.firstVisibleItemScrollOffset == 0
                            if (atTop && dragAmount > 0f) {
                                headerPullPx =
                                    (headerPullPx + dragAmount).coerceAtLeast(0f)
                                change.consume()
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                // v1.72.4: tighter so the controls sit just under the
                // search bar — was vertical = 10.dp.
                // v1.81: tighter still (4.dp / 6.dp) so the rows sit
                // closer to the top bar and take up less space.
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // v1.68: two control rows. Row 1 is a full-width line:
                // the Count box, a wide blue "Shuffle" button that
                // fills the space, and the ⋮ menu. Row 2 is the action
                // icons, centered underneath. Categories moved to the top
                // bar next to the database pill, so they're gone from here
                // entirely.
                // v1.80: pill-sized controls — 40.dp like the top bar's
                // database/Categories pills (was 56.dp), centered in the
                // 56.dp row so the row keeps the top bar's height.
                // v1.81: 48.dp row (was 56.dp) — sits closer to the top
                // bar and takes up less space.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Count stays visible next to Shuffle at all times.
                    // v1.84: BasicTextField with a hand-drawn outline — the
                    // M3 OutlinedTextField kept shaving the tops of the
                    // digits no matter the font size, so the field now
                    // centers the number itself with exact padding.
                    val countTextStyle = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BasicTextField(
                        value = countText,
                        onValueChange = { input ->
                            countText = input.filter { it.isDigit() }.take(3)
                            countText.toIntOrNull()?.let { vm.updateCount(it) }
                        },
                        // v1.74.1: placeholder instead of a label — the label
                        // cramped the number in the fixed field height so the
                        // count wasn't visible.
                        // v1.88.19: Enter/Done commits the count and
                        // shuffles with it, then dismisses the keyboard.
                        singleLine = true,
                        textStyle = countTextStyle,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { shuffle() }
                        ),
                        modifier = Modifier
                            .width(78.dp)
                            .height(40.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (countText.isEmpty()) {
                                    Text(
                                        "Count",
                                        maxLines = 1,
                                        style = countTextStyle.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Box(Modifier.fillMaxWidth()) { innerTextField() }
                            }
                        }
                    )
                    Button(
                        onClick = ::shuffle,
                        // v1.80: natural pill height (40.dp) like the top
                        // bar's pills — was pinned to 56.dp.
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Casino, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        // v1.72.3: one-word label, pinned to one line.
                        Text("Shuffle", maxLines = 1)
                    }
                    // v1.80: the Box wraps the natural-height pill (dropdown
                    // anchor) — no fixed height.
                    Box {
                        // v1.68: labeled "Options" pull-down instead of a
                        // bare ⋮ icon, so it's obvious what it is.
                        OutlinedButton(
                            onClick = { menuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Options")
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Endless deck") },
                                leadingIcon = { Icon(Icons.Filled.AllInclusive, contentDescription = null) },
                                onClick = { menuExpanded = false; onDeckClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Flashcards") },
                                leadingIcon = { Icon(Icons.Filled.Style, contentDescription = null) },
                                onClick = { menuExpanded = false; onFlashcardsClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Rate") },
                                leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) },
                                onClick = { menuExpanded = false; onRatingDeckClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("Read aloud") },
                                leadingIcon = { Icon(Icons.Filled.VolumeUp, contentDescription = null) },
                                onClick = { menuExpanded = false; onReadAloudSet() },
                                enabled = quotes.isNotEmpty()
                            )
                            DropdownMenuItem(
                                text = { Text("Export video") },
                                leadingIcon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                                onClick = { menuExpanded = false; onExportVideo() },
                                enabled = quotes.isNotEmpty()
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (hideSeen) "Show read" else "Hide read") },
                                leadingIcon = {
                                    Icon(
                                        if (hideSeen) Icons.Filled.Check
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                },
                                onClick = { vm.setHideSeen(!hideSeen) }
                            )
                            // v1.70: mood filters in the Options pull-down —
                            // each pick re-rolls the deck in that vibe. The
                            // menu stays open so you can flip through moods.
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Mood",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                enabled = false,
                                onClick = {}
                            )
                            MOOD_PRESETS.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset.label) },
                                    leadingIcon = { Text(preset.emoji) },
                                    trailingIcon = {
                                        if (isMoodActive(vm.categoryIds, preset, moodNameToId)) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = "Mood on"
                                            )
                                        }
                                    },
                                    onClick = { toggleMood(preset) }
                                )
                            }
                            // v1.74.1: per-screen maturity + cast filters.
                            // The menu stays open after a pick so you can
                            // flip through options; each pick re-rolls.
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    MaturityFilterDropdown(
                                        selected = vm.maturity,
                                        onSelect = { vm.updateMaturity(it) }
                                    )
                                },
                                onClick = {}
                            )
                            DropdownMenuItem(
                                text = {
                                    CastSizeFilterDropdown(
                                        selected = vm.castSize,
                                        onSelect = { vm.updateCastSize(it) }
                                    )
                                },
                                onClick = {}
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
                ) {
                    OutlinedIconButton(
                        onClick = onDeckClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.AllInclusive, contentDescription = "Endless deck")
                    }
                    OutlinedIconButton(
                        onClick = onFlashcardsClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Style, contentDescription = "Flashcards")
                    }
                    OutlinedIconButton(
                        onClick = onRatingDeckClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Rate quotes")
                    }
                    OutlinedIconButton(
                        onClick = onReadAloudSet,
                        enabled = quotes.isNotEmpty(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "Read aloud")
                    }
                    OutlinedIconButton(
                        onClick = onRadioClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Radio, contentDescription = "Quote radio")
                    }
                }
            }
        }
        // Swipe right for a new random set, swipe left to step back to the
        // previous set. Pull down at the top of the list also shuffles.
        QuoteSelectionHost(
            selection = selection,
            manager = manager,
            allCandidates = quotes.map { SelectedQuote(it.dbId, it.quote.number) },
            onAddToPlaylist = onAddToPlaylist
        )
        HiddenQuotesBanner(
            hiddenCount = hiddenCount,
            showingHidden = showFiltered,
            onShow = { vm.setShowFiltered(true) },
            onHide = { vm.setShowFiltered(false) }
        )
        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(pullState.nestedScrollConnection)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                totalDrag > 120f -> shuffle()
                                totalDrag < -120f -> goBack()
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    ) { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                }
        ) {
            when {
                loading && quotes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                quotes.isEmpty() -> {
                    EmptyState("No quotes found.")
                }
                else -> {
                    AnimatedContent(
                        targetState = quotes,
                        transitionSpec = {
                            // New sets slide in from the right; stepping back
                            // slides in from the left.
                            (fadeIn() + slideInHorizontally { (it / 4) * direction }) togetherWith fadeOut()
                        },
                        label = "randomShuffle"
                    ) { list ->
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(list, key = { "${it.dbId}:${it.quote.number}" }) { sq ->
                                val selKey = SelectedQuote(sq.dbId, sq.quote.number)
                                QuoteRow(
                                    quote = sq.quote,
                                    dbId = sq.dbId,
                                    isFavorite = (sq.dbId to sq.quote.number) in favKeys,
                                    onToggleFavorite = { vm.toggleFavorite(sq.dbId, sq.quote) },
                                    onClick = { onQuoteClick(sq.dbId, sq.quote.number) },
                                    onLongClick = { selection.enter(selKey) },
                                    onMoreClick = { sheetQuote = sq },
                                    // v1.61.9: one-tap share from the header.
                                    onShareClick = { shareTarget = sq },
                                    selectionActive = selection.active,
                                    isSelected = selection.isSelected(selKey),
                                    onSelectionToggle = { selection.toggle(selKey) },
                                    showNumber = showNumber,
                                    showRating = showRating,
                                    sourceLabel = if (multiDbPool) manager.shortLabel(sq.dbId) else null,
                                    sourceDbId = if (multiDbPool) sq.dbId else null,
                                    myRating = localRatings["${sq.dbId}/${sq.quote.number}"] ?: 0,
                                    onRate = { vm.rateQuote(sq.dbId, sq.quote.number, it) },
                                    // v1.53 report/flag.
                                    isFlagged = "${sq.dbId}/${sq.quote.number}" in flaggedQuotes,
                                    flagInfo = flaggedQuotes["${sq.dbId}/${sq.quote.number}"],
                                    onFlagQuote = { reason, custom ->
                                        vm.flagQuote(sq.dbId, sq.quote.number, reason, custom)
                                    },
                                    onNickClick = onNickClick)
                            }
                            item {
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
        } // inner Column
        // Small pull indicator floating above everything. Combines the
        // stock list-pull progress with the manual header pull. The
        // spinner half is driven by our own refreshing flag (reset in a
        // finally block) so it can never get stuck on screen the way the
        // stock container did.
        val pullProgress = maxOf(
            pullState.progress,
            (headerPullPx / headerPullThresholdPx).coerceIn(0f, 1f)
        )
        if (pullProgress > 0.01f || refreshing) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    CircularProgressIndicator(
                        progress = pullProgress.coerceIn(0f, 1f),
                        modifier = Modifier.size(30.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } // screen Box

    val sq = sheetQuote
    QuoteActionSheetHost(
        quote = sq?.quote,
        isFavorite = sq?.let { (it.dbId to it.quote.number) in favKeys } == true,
        onDismiss = { sheetQuote = null },
        onToggleFavorite = { sq?.let { vm.toggleFavorite(it.dbId, it.quote) } },
        onOpen = { sq?.let { onQuoteClick(it.dbId, it.quote.number) } },
        dbId = sq?.dbId ?: "",
        inBasket = sq?.let { BasketEntry(it.dbId, it.quote.number) in basket } == true,
        onToggleBasket = { sq?.let { onToggleBasket(it.dbId, it.quote.number) } },
        onHideQuote = { sq?.let { vm.hideQuote(it.dbId, it.quote.number) } },
        // v1.74.7: "Read aloud" in the quote menu.
        onReadAloud = { sq?.let(onReadAloudQuote) },
        // v1.53 report/flag.
        isFlagged = sq?.let { "${it.dbId}/${it.quote.number}" in flaggedQuotes } == true,
        onFlagQuote = { dbId, number, reason, custom ->
            vm.flagQuote(dbId, number, reason, custom)
        },
        onUnflagQuote = { sq?.let { vm.unflagQuote(it.dbId, it.quote.number) } }
    )
    // v1.61.9: one-tap share from the card header.
    shareTarget?.let { target ->
        ShareOptionsDialog(
            quote = target.quote,
            dbId = target.dbId,
            onDismiss = { shareTarget = null }
        )
    }
    } // DbPageBackground
    } // LocalChromeAccent
}

