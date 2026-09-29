package com.quotebrowser.app.ui.screens.games

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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 5
private const val MIN_LINES = 4

/** One line-scramble round: the quote's lines in original and shuffled order. */
private data class ScrambleRound(
    val quote: SourcedQuote,
    val original: List<String>,
    val display: List<String>
)

/**
 * Line scramble: a multi-line quote's lines are shuffled. Tap the lines in
 * their original order — +1 point per solved round. Solving a round with no
 * wrong taps keeps a "clean" streak; the best score and best streak persist.
 * 5 rounds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineScrambleGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<ScrambleRound?>(null) }
    var solved by remember { mutableStateOf(false) }
    var wrongTaps by remember { mutableStateOf(0) }
    var hint by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.LINE_SCRAMBLE)) }
    var bestStreak by remember {
        mutableStateOf(prefs.getBestStreak(GamePrefs.LINE_SCRAMBLE))
    }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    val used = remember { mutableSetOf<Pair<String, Int>>() }

    // Indices of [display] lines the player has placed, in tap order.
    val accepted = remember(round) { mutableStateListOf<Int>() }

    fun loadRound() {
        loading = true
        solved = false
        wrongTaps = 0
        hint = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildScrambleRound(manager, used) }
            if (r != null) used += r.quote.dbId to r.quote.quote.number
            round = r
            failed = r == null
            loading = false
        }
    }

    fun restart() {
        score = 0
        streak = 0
        roundNumber = 1
        gameOver = false
        used.clear()
        loadRound()
    }

    fun solve() {
        solved = true
        score++
        if (score > best) {
            best = score
            prefs.updateBestScore(GamePrefs.LINE_SCRAMBLE, score)
        }
        if (wrongTaps == 0) {
            streak++
            if (streak > bestStreak) {
                bestStreak = streak
                prefs.updateBestStreak(GamePrefs.LINE_SCRAMBLE, streak)
            }
        } else {
            streak = 0
        }
    }

    fun tap(index: Int) {
        if (solved || round == null || index in accepted) return
        val r = round!!
        if (r.display[index] == r.original[accepted.size]) {
            accepted += index
            hint = null
            if (accepted.size == r.display.size) solve()
        } else {
            wrongTaps++
            hint = "Not that one — it comes later. Try again."
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

    LaunchedEffect(Unit) { loadRound() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Line Scramble") },
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
                Text("Streak: $streak", style = MaterialTheme.typography.titleMedium)
                Text("Best: $best", style = MaterialTheme.typography.titleMedium)
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
                                "You solved $score of $TOTAL_ROUNDS rounds.",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Best score: $best • Best streak: $bestStreak",
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
                failed || round == null -> {
                    EmptyState(
                        "Couldn't find a quote with at least $MIN_LINES lines.\n" +
                            "Try changing your random sources."
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { restart() }) { Text("Retry") }
                    }
                }
                else -> {
                    val r = round!!
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (solved) "Solved!"
                            else "Tap the lines in their original order. " +
                                "Placed: ${accepted.size}/${r.display.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (solved) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Round $roundNumber/$TOTAL_ROUNDS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        r.display.forEachIndexed { index, line ->
                            val placedPos = accepted.indexOf(index)
                            if (placedPos >= 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        QuoteNumberBadge(number = placedPos + 1)
                                        Spacer(Modifier.width(8.dp))
                                        val bodyStyle = scaledTextStyle(
                                            MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = rememberNickAnnotatedText(line, bodyStyle),
                                            style = bodyStyle,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { tap(index) },
                                    enabled = !solved,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    val bodyStyle = scaledTextStyle(
                                        MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = rememberNickAnnotatedText(line, bodyStyle),
                                        style = bodyStyle,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                        hint?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (solved) {
                            Button(
                                onClick = { next() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    if (roundNumber >= TOTAL_ROUNDS) "See results"
                                    else "Next round"
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    accepted.clear()
                                    hint = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text("Start this round over")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/** Draws one scramble round: a quote with 4+ lines, excluding seen keys. */
private suspend fun buildScrambleRound(
    manager: DatabaseManager,
    exclude: Set<Pair<String, Int>>
): ScrambleRound? {
    for (sq in manager.gameQuotes(60)) {
        if ((sq.dbId to sq.quote.number) in exclude) continue
        val lines = sq.quote.text.lines()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
        if (lines.size < MIN_LINES) continue
        var display = lines.shuffled()
        var tries = 0
        while (display == lines && tries < 5) {
            display = lines.shuffled()
            tries++
        }
        if (display == lines) continue
        return ScrambleRound(sq, lines, display)
    }
    return null
}
