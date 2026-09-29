package com.quotebrowser.app.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.extractNicks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PAIRS = 6
/** Best score is stored as SCORE_BASE - moves so GamePrefs' max-wins rule keeps the fewest moves. */
private const val SCORE_BASE = 1000

private enum class CardKind { NICK, SNIPPET }

private data class MemCard(
    val pairId: Int,
    val kind: CardKind,
    val text: String
)

private data class MemCardState(
    val card: MemCard,
    val faceUp: Boolean = false,
    val matched: Boolean = false
)

/**
 * Memory match: 12 face-down cards — 6 nicks and 6 quote snippets,
 * one snippet per nick. Flip two at a time; a nick matches the
 * snippet from its own quote. Fewest moves wins; the best (fewest)
 * move count persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMatchGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var cards by remember { mutableStateOf<List<MemCardState>>(emptyList()) }
    var flipped by remember { mutableStateOf<List<Int>>(emptyList()) }
    var moves by remember { mutableStateOf(0) }
    var matches by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var bestMoves by remember {
        val stored = prefs.getBestScore(GamePrefs.MEMORY_MATCH)
        mutableStateOf(if (stored > 0) SCORE_BASE - stored else 0)
    }

    fun deal() {
        loading = true
        flipped = emptyList()
        moves = 0
        matches = 0
        failed = false
        scope.launch {
            val built = withContext(Dispatchers.IO) { buildMemoryCards(manager) }
            if (built.size < PAIRS * 2) {
                failed = true
                cards = emptyList()
            } else {
                cards = built
            }
            loading = false
        }
    }

    fun flip(index: Int) {
        val state = cards.getOrNull(index) ?: return
        if (state.faceUp || state.matched || flipped.size >= 2) return
        val newFlipped = flipped + index
        cards = cards.mapIndexed { i, c ->
            if (i == index) c.copy(faceUp = true) else c
        }
        if (newFlipped.size == 2) {
            moves++
            val (i, j) = newFlipped
            if (cards[i].card.pairId == cards[j].card.pairId) {
                cards = cards.mapIndexed { k, c ->
                    if (k == i || k == j) c.copy(matched = true) else c
                }
                val newMatches = matches + 1
                matches = newMatches
                if (newMatches >= PAIRS) {
                    val scoreValue = SCORE_BASE - moves
                    if (scoreValue > prefs.getBestScore(GamePrefs.MEMORY_MATCH)) {
                        prefs.updateBestScore(GamePrefs.MEMORY_MATCH, scoreValue)
                        bestMoves = moves
                    }
                }
            }
        }
        flipped = newFlipped
    }

    // Flip non-matching pairs back over after a short look.
    LaunchedEffect(flipped) {
        if (flipped.size == 2) {
            delay(900)
            val (i, j) = flipped
            val a = cards.getOrNull(i)
            val b = cards.getOrNull(j)
            if (a != null && b != null &&
                !a.matched && !b.matched &&
                a.card.pairId != b.card.pairId
            ) {
                cards = cards.mapIndexed { k, c ->
                    if (k == i || k == j) c.copy(faceUp = false) else c
                }
            }
            flipped = emptyList()
        }
    }

    LaunchedEffect(Unit) { deal() }

    val won = matches >= PAIRS && cards.isNotEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Memory Match") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Moves: $moves", style = MaterialTheme.typography.titleMedium)
                Text("Pairs: $matches/$PAIRS", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (bestMoves > 0) "Best: $bestMoves" else "Best: —",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed -> {
                    EmptyState(
                        "Couldn't deal $PAIRS nick/quote pairs.\n" +
                            "Try changing your random sources."
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { deal() }) { Text("Retry") }
                    }
                }
                won -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "All pairs matched!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "You finished in $moves moves.",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                if (bestMoves > 0) "Best: $bestMoves moves"
                                else "No best yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { deal() }) { Text("Play again") }
                        }
                    }
                }
                else -> {
                    Text(
                        "Flip two cards — match each nick with its quote.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(cards) { index, state ->
                            MemoryCard(
                                state = state,
                                onFlip = { flip(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(
    state: MemCardState,
    onFlip: () -> Unit
) {
    val faceUp = state.faceUp || state.matched
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                state.matched -> MaterialTheme.colorScheme.primaryContainer
                faceUp -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = when {
                state.matched -> MaterialTheme.colorScheme.onPrimaryContainer
                faceUp -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onPrimary
            }
        ),
        onClick = onFlip,
        enabled = !state.matched
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (faceUp) {
                Text(
                    text = state.card.text,
                    style = if (state.card.kind == CardKind.NICK)
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    else MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = if (state.card.kind == CardKind.NICK) 3 else 6,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Icon(
                    Icons.Filled.QuestionMark,
                    contentDescription = null
                )
            }
        }
    }
}

/**
 * Picks [PAIRS] quotes with distinct first nicks; each pair is the
 * nick plus a short snippet of that nick's quote.
 */
private suspend fun buildMemoryCards(manager: DatabaseManager): List<MemCardState> {
    val seenNicks = mutableSetOf<String>()
    val pairs = mutableListOf<Pair<String, String>>()
    val pool = manager.gameQuotes(PAIRS * 12).shuffled()
    for (sq in pool) {
        if (pairs.size >= PAIRS) break
        val nicks = extractNicks(sq.quote.text)
        if (nicks.isEmpty()) continue
        val nick = nicks.first()
        if (!seenNicks.add(nick.lowercase())) continue
        val singleLine = sq.quote.text.replace("\\s+".toRegex(), " ").trim()
        val snippet = if (singleLine.length > 90) singleLine.take(87) + "…" else singleLine
        pairs += nick to snippet
    }
    if (pairs.size < PAIRS) return emptyList()
    val cards = pairs.flatMapIndexed { pairId, (nick, snippet) ->
        listOf(
            MemCard(pairId, CardKind.NICK, nick),
            MemCard(pairId, CardKind.SNIPPET, snippet)
        )
    }.shuffled()
    return cards.map { MemCardState(it) }
}
