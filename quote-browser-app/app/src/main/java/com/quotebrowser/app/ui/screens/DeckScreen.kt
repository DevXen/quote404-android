package com.quotebrowser.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.RandomViewModel
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.theme.scaledTextStyle
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
/**
 * Endless full-screen quote deck. Flick up for the next random quote, flick
 * down to go back through the ones already seen. Opened from the Random
 * screen's "Endless deck" button; the X button, the system back button, or
 * the Random bottom tab all return to the Random screen.
 *
 * Each page is intentionally NOT internally scrollable: the pager owns every
 * vertical gesture so flicks always advance to the next card. Very long
 * quotes shrink their type size; extremely long ones are truncated with a
 * "tap to read the full quote" hint (tap opens the detail screen).
 */
@Composable
fun DeckScreen(
    randomVm: RandomViewModel,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onExit: () -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    /**
     * Enables deck resume: when non-null, the deck's quote keys (capped at
     * [DECK_RESUME_CAP]) plus the current index are persisted to
     * SharedPreferences whenever the index changes, and restored on the
     * next open. Null (default) keeps the always-fresh deck behavior.
     * Optional so existing call sites keep compiling; pass the app's
     * DatabaseManager to turn resume on.
     */
    manager: DatabaseManager? = null
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val cards = remember { mutableStateListOf<SourcedQuote>() }
    var loadingMore by remember { mutableStateOf(false) }
    val favKeys by randomVm.favoriteKeys.collectAsStateWithLifecycle()
    val deckPrefs = remember {
        context.getSharedPreferences(DECK_RESUME_PREFS, Context.MODE_PRIVATE)
    }

    fun appendMore() {
        if (loadingMore) return
        loadingMore = true
        scope.launch {
            try {
                val batch = randomVm.fetchRandomBatch(25)
                val known = cards.map { it.dbId to it.quote.number }.toSet()
                cards.addAll(batch.filter { (it.dbId to it.quote.number) !in known })
            } finally {
                loadingMore = false
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { cards.size })

    /**
     * Restores the deck from the last session: the saved quote keys are
     * re-fetched from their databases (in order) and the pager jumps back
     * to the saved index. Returns false when there is nothing usable to
     * restore, or when no [manager] was supplied to load quotes with.
     */
    suspend fun restoreDeck(): Boolean {
        val mgr = manager ?: return false
        val raw = deckPrefs.getString(KEY_DECK_KEYS, null) ?: return false
        val keys = raw.split(",").mapNotNull { part ->
            val dbId = part.substringBeforeLast("/")
            val number = part.substringAfterLast("/").toIntOrNull()
            if (dbId.isNotEmpty() && number != null) dbId to number else null
        }.take(DECK_RESUME_CAP)
        if (keys.isEmpty()) return false
        val restored = withContext(Dispatchers.IO) {
            val byKey = mutableMapOf<Pair<String, Int>, Quote>()
            for ((dbId, numbers) in keys.groupBy({ it.first }, { it.second })) {
                try {
                    val dao = mgr.getQuoteDatabase(mgr.singleDbId(dbId)).quoteDao()
                    for (q in dao.getByNumbers(numbers)) {
                        byKey[dbId to q.number] = q
                    }
                } catch (_: Exception) {
                    // A database that can't be read contributes nothing.
                }
            }
            keys.mapNotNull { k -> byKey[k]?.let { q -> SourcedQuote(k.first, q) } }
        }
        if (restored.isEmpty()) return false
        cards.addAll(restored)
        val index = deckPrefs.getInt(KEY_DECK_INDEX, 0)
            .coerceIn(0, restored.size - 1)
        pagerState.scrollToPage(index)
        return true
    }

    // First composition: pick up where the last deck session left off when
    // saved state exists, otherwise start a fresh deck.
    LaunchedEffect(Unit) {
        if (!restoreDeck()) appendMore()
    }

    // Keep the deck fed: when nearing the end, append another batch.
    LaunchedEffect(pagerState.currentPage, cards.size) {
        if (cards.isNotEmpty() && pagerState.currentPage >= cards.size - 6) {
            appendMore()
        }
    }

    // Reading stats: viewing a card counts as seeing the quote.
    LaunchedEffect(pagerState.currentPage) {
        cards.getOrNull(pagerState.currentPage)?.let { sq ->
            randomVm.markSeen(sq.dbId, sq.quote.number)
        }
        // Deck resume: persist the deck's quote keys (capped) plus the
        // current index whenever the index changes, so reopening the deck
        // continues where the user left off.
        if (cards.isNotEmpty()) {
            deckPrefs.edit()
                .putString(
                    KEY_DECK_KEYS,
                    cards.take(DECK_RESUME_CAP)
                        .joinToString(",") { "${it.dbId}/${it.quote.number}" }
                )
                .putInt(KEY_DECK_INDEX, pagerState.currentPage)
                .apply()
        }
    }

    // Subtle tick as each new card settles into place.
    var lastSettled by remember { mutableStateOf(0) }
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != lastSettled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastSettled = pagerState.settledPage
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Back to Random")
            }
            Text(
                "Endless deck",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.weight(1f))
            if (cards.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        "${pagerState.currentPage + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
        Box(Modifier.weight(1f)) {
            when {
                cards.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondBoundsPageCount = 1
                    ) { page ->
                        val sq = cards.getOrNull(page) ?: return@VerticalPager
                        DeckPage(
                            sq = sq,
                            isFavorite = (sq.dbId to sq.quote.number) in favKeys,
                            onToggleFavorite = { randomVm.toggleFavorite(sq.dbId, sq.quote) },
                            onClick = { onQuoteClick(sq.dbId, sq.quote.number) },
                            showNumber = showNumber,
                            showRating = showRating,
                            sourceLabel = randomVm.labelFor(sq.dbId)
                        )
                    }
                }
            }
        }
        Text(
            "Flick up for the next quote · double-tap to save",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}

/** Maximum characters shown on a deck card before truncating with a hint. */
private const val DECK_TRUNCATE_AT = 2600

/** SharedPreferences file holding the deck resume state (v1.44). */
private const val DECK_RESUME_PREFS = "deck_resume_v144"

/** Maximum deck quote keys persisted for resume. */
private const val DECK_RESUME_CAP = 200

/** Prefs keys: the saved quote keys ("dbId/number,...") and page index. */
private const val KEY_DECK_KEYS = "keys"
private const val KEY_DECK_INDEX = "index"

@Composable
private fun DeckPage(
    sq: SourcedQuote,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    showNumber: Boolean,
    showRating: Boolean,
    sourceLabel: String?
) {
    val haptics = LocalHapticFeedback.current
    val quote = sq.quote
    val truncated = quote.text.length > DECK_TRUNCATE_AT
    val displayText = if (truncated) {
        quote.text.take(DECK_TRUNCATE_AT).trimEnd() + "…"
    } else {
        quote.text
    }

    // Larger type for short quotes, smaller for walls of text.
    val baseStyle = when {
        displayText.length <= 280 -> MaterialTheme.typography.headlineSmall
        displayText.length <= 800 -> MaterialTheme.typography.titleLarge
        displayText.length <= 1600 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.bodyLarge
    }
    val bodyStyle = scaledTextStyle(baseStyle)
    // Short quotes look best centered; IRC-style logs stay left-aligned.
    val textAlign = if (displayText.length <= 280) TextAlign.Center else TextAlign.Start
    // v1.89.7: URL tap support — the card's tap detector works in card
    // coordinates, so the body text's root position is captured and the
    // tap is translated into text coordinates for the hit-test.
    val bodyAnnotated = rememberNickAnnotatedText(displayText, bodyStyle)
    val deckUriHandler = LocalUriHandler.current
    var deckBodyLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var deckBodyPos by remember { mutableStateOf(Offset.Zero) }
    var deckCardPos by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Soft vertical wash so the page feels designed, not blank.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
        )
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .heightIn(max = maxHeight * 0.84f)
                .onGloballyPositioned { deckCardPos = it.positionInRoot() }
                .pointerInput(onClick, onToggleFavorite, bodyAnnotated) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            // v1.89.7: a tap on a URL in the quote opens
                            // it instead of opening the detail screen.
                            var openedLink = false
                            val layout = deckBodyLayout
                            if (layout != null) {
                                val local = tapOffset + deckCardPos - deckBodyPos
                                if (local.x >= 0f && local.y >= 0f &&
                                    local.x < layout.size.width &&
                                    local.y < layout.size.height
                                ) {
                                    val offset = layout.getOffsetForPosition(local)
                                    val url = if (offset in bodyAnnotated.indices) {
                                        bodyAnnotated.getStringAnnotations(
                                            "url", offset,
                                            (offset + 1).coerceAtMost(bodyAnnotated.length)
                                        ).firstOrNull()?.item
                                    } else {
                                        null
                                    }
                                    if (url != null) {
                                        try {
                                            deckUriHandler.openUri(url)
                                        } catch (_: Exception) {
                                        }
                                        openedLink = true
                                    }
                                }
                            }
                            if (!openedLink) onClick()
                        },
                        onDoubleTap = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFavorite()
                        }
                    )
                },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showNumber) {
                        QuoteNumberBadge(
                            number = quote.number,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    if (showRating) {
                        RatingBadge(quote.rating)
                        Spacer(Modifier.width(8.dp))
                    }
                    if (sourceLabel != null) {
                        SourceChip(label = sourceLabel, dbId = sq.dbId)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFavorite()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites"
                            else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "“",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    modifier = Modifier.padding(bottom = 0.dp)
                )
                Text(
                    text = bodyAnnotated,
                    style = bodyStyle,
                    textAlign = textAlign,
                    onTextLayout = { deckBodyLayout = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { deckBodyPos = it.positionInRoot() }
                )
                if (truncated) {
                    Text(
                        "Tap to read the full quote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
