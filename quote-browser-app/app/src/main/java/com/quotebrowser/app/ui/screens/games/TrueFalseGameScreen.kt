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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10

data class TrueFalseRound(
    val quote: SourcedQuote,
    val claimedNick: String,
    val actualNick: String,
    val isTrue: Boolean
)

@Composable
private fun androidx.compose.foundation.layout.RowScope.TrueFalseButton(
    label: String,
    value: Boolean,
    picked: Boolean?,
    isCorrectAnswer: Boolean,
    onPick: () -> Unit
) {
    val answered = picked != null
    val colors = when {
        answered && isCorrectAnswer -> ButtonDefaults.buttonColors()
        answered && picked == value -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        answered -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> ButtonDefaults.buttonColors()
    }
    Button(
        onClick = onPick,
        enabled = !answered,
        colors = colors,
        modifier = Modifier.weight(1f)
    ) { Text(label) }
}

/**
 * True or false: "Did <nick> actually say this?" Half the time the
 * claimed nick really appears in the quote, half the time it is a
 * nick lifted from a different quote. 10 quick rounds; the best
 * score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<TrueFalseRound?>(null) }
    var picked by remember { mutableStateOf<Boolean?>(null) }
    var score by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.TRUE_FALSE)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun loadRound() {
        loading = true
        picked = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildTrueFalseRound(manager) }
            round = r
            failed = r == null
            loading = false
        }
    }

    fun answer(saidTrue: Boolean) {
        if (picked != null) return
        picked = saidTrue
        val r = round ?: return
        if (saidTrue == r.isTrue) {
            score++
            if (score > best) {
                best = score
                prefs.updateBestScore(GamePrefs.TRUE_FALSE, score)
            }
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
                title = { Text("True or False") },
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
                                "You scored $score out of $TOTAL_ROUNDS.",
                                style = MaterialTheme.typography.titleLarge
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
                loading && round == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed || round == null -> {
                    EmptyState("Couldn't build a true/false round.\nTry again.")
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
                    val r = round!!
                    val answered = picked != null
                    val correct = answered && picked == r.isTrue
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Did “${r.claimedNick}” actually say this?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                    Spacer(Modifier.padding(4.dp))
                                    SourceChip(label = manager.shortLabel(r.quote.dbId))
                                }
                                Spacer(Modifier.height(8.dp))
                                val bodyStyle =
                                    scaledTextStyle(MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = rememberNickAnnotatedText(
                                        r.quote.quote.text, bodyStyle
                                    ),
                                    style = bodyStyle,
                                    maxLines = 12,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TrueFalseButton(
                                label = "TRUE",
                                value = true,
                                picked = picked,
                                isCorrectAnswer = r.isTrue,
                                onPick = { answer(true) }
                            )
                            TrueFalseButton(
                                label = "FALSE",
                                value = false,
                                picked = picked,
                                isCorrectAnswer = !r.isTrue,
                                onPick = { answer(false) }
                            )
                        }
                        AnimatedVisibility(
                            visible = answered,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = when {
                                    correct -> "Correct! 🎉"
                                    r.isTrue -> "Wrong — “${r.actualNick}” really did say it."
                                    else -> "Wrong — it was actually “${r.actualNick}”."
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (correct) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { next() },
                            enabled = !loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(if (answered) "Next round" else "Skip")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds one round: a quote with at least one nick. Half the time the
 * claimed nick is the real one; otherwise a nick from another quote.
 * Reused by the survivor game.
 */
suspend fun buildTrueFalseRound(manager: DatabaseManager): TrueFalseRound? {
    repeat(12) {
        val pool = manager.gameQuotes(120)
        if (pool.size < 2) return@repeat
        val withNicks = pool.filter { extractNicks(it.quote.text).isNotEmpty() }
        if (withNicks.isEmpty()) return@repeat
        val target = withNicks.random()
        val realNicks = extractNicks(target.quote.text)
        val actualNick = realNicks.random()
        val allNicks = pool
            .flatMap { extractNicks(it.quote.text) }
            .distinct()
        val realLower = realNicks.map { it.lowercase() }.toSet()
        val distractors = allNicks.filter { it.lowercase() !in realLower }
        val makeTrue = (0..1).random() == 0
        if (makeTrue) {
            return TrueFalseRound(target, actualNick, actualNick, true)
        } else {
            if (distractors.isEmpty()) return@repeat
            return TrueFalseRound(target, distractors.random(), actualNick, false)
        }
    }
    return null
}
