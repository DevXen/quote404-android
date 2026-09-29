package com.quotebrowser.app.ui.screens.games

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 5
private const val SLIDER_MIN = -1000f
private const val SLIDER_MAX = 10000f

/** One rating-guess round: a rated quote and its true rating. */
private data class RatingRound(
    val quote: SourcedQuote,
    val actual: Int
)

/** Points for one round: 100 at an exact hit, -10 per point of miss, floor 0. */
private fun roundPoints(guess: Int, actual: Int): Int =
    (100 - 10 * abs(guess - actual)).coerceAtLeast(0)

/**
 * Rating guess: a rated quote is shown; drag the slider to where you think
 * its true rating is. Points are scored by closeness (100 for exact, minus
 * 10 per rating point of miss, never below 0), summed over 5 rounds. The
 * best score persists. (Deliberately distinct from the older bucket-based
 * "Guess the rating" game.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingGuessGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var rounds by remember { mutableStateOf<List<RatingRound>?>(null) }
    var roundNumber by remember { mutableStateOf(1) }
    var guess by remember { mutableStateOf(0f) }
    var answered by remember { mutableStateOf(false) }
    var points by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.RATING_GUESS)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun loadGame() {
        loading = true
        answered = false
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildRatingRounds(manager) }
            rounds = r
            roundNumber = 1
            guess = 0f
            score = 0
            failed = r == null || r.isEmpty()
            loading = false
        }
    }

    fun restart() {
        gameOver = false
        loadGame()
    }

    fun lockIn() {
        val r = rounds?.getOrNull(roundNumber - 1) ?: return
        if (answered) return
        answered = true
        points = roundPoints(guess.toInt(), r.actual)
        score += points
        if (score > best) {
            best = score
            prefs.updateBestScore(GamePrefs.RATING_GUESS, score)
        }
    }

    fun next() {
        val total = rounds?.size ?: 0
        if (roundNumber >= total) {
            gameOver = true
        } else {
            roundNumber++
            guess = 0f
            answered = false
        }
    }

    LaunchedEffect(Unit) { loadGame() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Rating Guess") },
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
                    if (gameOver) "Done" else "Round $roundNumber/${rounds?.size ?: TOTAL_ROUNDS}",
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
                                "You scored $score of ${(rounds?.size ?: TOTAL_ROUNDS) * 100} points.",
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
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed -> {
                    EmptyState(
                        "Couldn't find rated quotes.\n" +
                            "Try changing your random sources."
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { loadGame() }) { Text("Retry") }
                    }
                }
                else -> {
                    val r = rounds!![roundNumber - 1]
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QuoteNumberBadge(number = r.quote.quote.number)
                                    Spacer(Modifier.width(8.dp))
                                    GameSourceLabel(
                                        dbId = r.quote.dbId,
                                        manager = manager
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                val bodyStyle = scaledTextStyle(
                                    MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = rememberNickAnnotatedText(
                                        r.quote.quote.text, bodyStyle
                                    ),
                                    style = bodyStyle,
                                    maxLines = 8,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            "What is this quote's true rating?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Your guess: ${guess.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Slider(
                            value = guess,
                            onValueChange = { if (!answered) guess = it },
                            valueRange = SLIDER_MIN..SLIDER_MAX,
                            enabled = !answered,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${SLIDER_MIN.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${SLIDER_MAX.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (answered) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "True rating: ",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        RatingBadge(r.actual)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "+$points points " +
                                            "(off by ${abs(guess.toInt() - r.actual)})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (answered) {
                            Button(
                                onClick = { next() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    if (roundNumber >= (rounds?.size ?: 0)) "See results"
                                    else "Next round"
                                )
                            }
                        } else {
                            Button(
                                onClick = { lockIn() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text("Lock in ${guess.toInt()}")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/** Draws [TOTAL_ROUNDS] rated quotes (filter-honoring), deduped by key. */
private suspend fun buildRatingRounds(
    manager: DatabaseManager,
    count: Int = TOTAL_ROUNDS
): List<RatingRound>? {
    val seen = mutableSetOf<Pair<String, Int>>()
    val out = mutableListOf<RatingRound>()
    for (sq in manager.gameQuotes(count * 6)) {
        if (out.size >= count) break
        val key = sq.dbId to sq.quote.number
        if (key in seen) continue
        val rating = sq.quote.rating ?: continue
        seen += key
        out += RatingRound(sq, rating)
    }
    return out
}
