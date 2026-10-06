package com.quotebrowser.app.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.mutableStateListOf
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
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

private const val TOTAL_ROUNDS = 10
private const val QUOTES_PER_ROUND = 4

/**
 * Sort by rating: four quotes with distinct non-null ratings are shown
 * shuffled. Reorder them with the up/down arrows, then "Check" — you score
 * one point per quote in the correct highest-to-lowest position.
 * 10 rounds; the best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByRatingGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var quotes by remember { mutableStateOf<List<SourcedQuote>?>(null) }
    var answered by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.SORT_RATING)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    // Current display order (mutable while the round is unanswered).
    val order = remember(quotes) {
        mutableStateListOf<SourcedQuote>().apply {
            quotes?.let { addAll(it.shuffled()) }
        }
    }
    val correctOrder = remember(quotes) {
        quotes?.sortedByDescending { it.quote.rating ?: Int.MIN_VALUE }.orEmpty()
    }

    fun loadRound() {
        loading = true
        answered = false
        correctCount = 0
        scope.launch {
            val q = withContext(Dispatchers.IO) { buildSortRound(manager) }
            quotes = q
            failed = q == null
            loading = false
        }
    }

    fun move(index: Int, delta: Int) {
        if (answered) return
        val to = index + delta
        if (to !in order.indices) return
        val tmp = order[index]
        order[index] = order[to]
        order[to] = tmp
    }

    fun check() {
        if (answered) return
        answered = true
        correctCount = order.indices.count { order[it] == correctOrder[it] }
        score += correctCount
        if (score > best) {
            best = score
            prefs.updateBestScore(GamePrefs.SORT_RATING, score)
        }
    }

    fun next() {
        if (roundNumber >= TOTAL_ROUNDS) {
            gameOver = true
        } else {
            roundNumber++
            loadRound()
        }
    }

    fun restart() {
        score = 0
        roundNumber = 1
        gameOver = false
        loadRound()
    }

    LaunchedEffect(Unit) { loadRound() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Sort by Rating") },
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
                Text("Score: $score", style = MaterialTheme.typography.titleMedium)
                Text("Best: $best", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (gameOver) "Done" else "Round $roundNumber/$TOTAL_ROUNDS",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            when {
                gameOver -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Game over!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "You scored $score out of ${TOTAL_ROUNDS * QUOTES_PER_ROUND}.",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Best: $best",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { restart() }) { Text("Play again") }
                        }
                    }
                }
                loading && quotes == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed || quotes == null -> {
                    EmptyState("Couldn't find four distinctly-rated quotes.\nTry again.")
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { loadRound() }) { Text("Retry") }
                    }
                }
                else -> {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            "Highest rated at the top — tap the arrows to reorder",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            flingBehavior = rememberCappedFlingBehavior()) {
                            itemsIndexed(order, key = { _, sq ->
                                "${sq.dbId}:${sq.quote.number}"
                            }) { index, sq ->
                                val inPlace = answered && order[index] == correctOrder[index]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            answered && inPlace ->
                                                MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                QuoteNumberBadge(number = sq.quote.number)
                                                if (answered) {
                                                    Spacer(Modifier.width(8.dp))
                                                    RatingBadge(sq.quote.rating)
                                                }
                                            }
                                            val bodyStyle = scaledTextStyle(
                                                MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = rememberNickAnnotatedText(
                                                    sq.quote.text, bodyStyle
                                                ),
                                                style = bodyStyle,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            SourceChip(label = manager.shortLabel(sq.dbId))
                                        }
                                        Column {
                                            IconButton(
                                                onClick = { move(index, -1) },
                                                enabled = !answered && index > 0
                                            ) {
                                                Icon(
                                                    Icons.Filled.KeyboardArrowUp,
                                                    contentDescription = "Move up"
                                                )
                                            }
                                            IconButton(
                                                onClick = { move(index, 1) },
                                                enabled = !answered && index < order.size - 1
                                            ) {
                                                Icon(
                                                    Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = "Move down"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(
                            visible = answered,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "$correctCount of $QUOTES_PER_ROUND in the right place",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                        Button(
                            onClick = { if (answered) next() else check() },
                            enabled = !loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Text(if (answered) "Next round" else "Check")
                        }
                    }
                }
            }
        }
    }
}

/** Draws 4 quotes with distinct non-null ratings. */
private suspend fun buildSortRound(manager: DatabaseManager): List<SourcedQuote>? {
    repeat(8) {
        val pool = manager.gameQuotes(40)
            .filter { it.quote.rating != null }
            .shuffled()
        val picked = mutableListOf<SourcedQuote>()
        for (sq in pool) {
            val key = sq.dbId to sq.quote.number
            if (picked.none { (it.dbId to it.quote.number) == key } &&
                picked.none { it.quote.rating == sq.quote.rating }
            ) {
                picked += sq
            }
            if (picked.size == QUOTES_PER_ROUND) break
        }
        if (picked.size == QUOTES_PER_ROUND) return picked
    }
    return null
}
