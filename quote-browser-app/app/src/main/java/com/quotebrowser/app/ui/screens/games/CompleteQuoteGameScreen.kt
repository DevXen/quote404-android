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
import androidx.compose.material3.OutlinedButton
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
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10

private data class CompleteRound(
    val quote: SourcedQuote,
    val leadIn: String,
    val answer: String,
    val options: List<String>
)

/**
 * Complete the quote: the first half of a quote is shown and you pick
 * the real ending from four options. Distractor endings are the tails
 * of other quotes, trimmed to a similar length. 10 rounds per game;
 * the best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteQuoteGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<CompleteRound?>(null) }
    var picked by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.COMPLETE_QUOTE)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun loadRound() {
        loading = true
        picked = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildCompleteRound(manager) }
            round = r
            failed = r == null
            loading = false
        }
    }

    fun answer(choice: String) {
        if (picked != null) return
        picked = choice
        val r = round ?: return
        if (choice == r.answer) {
            score++
            if (score > best) {
                best = score
                prefs.updateBestScore(GamePrefs.COMPLETE_QUOTE, score)
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
                title = { Text("Complete the Quote") },
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
                    EmptyState("Couldn't build a completion round.\nTry again.")
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
                    val correct = answered && picked == r.answer
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
                                    Spacer(Modifier.padding(4.dp))
                                    SourceChip(label = manager.shortLabel(r.quote.dbId))
                                }
                                Spacer(Modifier.height(8.dp))
                                val bodyStyle =
                                    scaledTextStyle(MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = r.leadIn,
                                    style = bodyStyle,
                                    maxLines = 12,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            "How does it end?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        r.options.forEach { option ->
                            val isAnswer = option == r.answer
                            val isWrongPick = answered && picked == option && !isAnswer
                            when {
                                answered && isAnswer -> Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(option) }
                                isWrongPick -> Button(
                                    onClick = {},
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(option) }
                                else -> OutlinedButton(
                                    onClick = { if (!answered) answer(option) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(option) }
                            }
                        }
                        AnimatedVisibility(
                            visible = answered,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = if (correct) "Correct! 🎉"
                                else "The real ending was:\n“${r.answer}”",
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
 * Picks a quote with >= 12 words, splits it near the midpoint into a
 * shown lead-in and the true ending. Distractor endings are the tails
 * of other quotes, trimmed to a similar word count.
 */
private suspend fun buildCompleteRound(manager: DatabaseManager): CompleteRound? {
    repeat(12) {
        val pool = manager.gameQuotes(120)
        if (pool.isEmpty()) return@repeat
        val target = pool.random()
        val words = target.quote.text.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.size < 12) return@repeat
        val splitAt = words.size / 2
        val leadIn = words.take(splitAt).joinToString(" ") + " …"
        val answer = words.drop(splitAt).joinToString(" ")
        val answerWords = words.size - splitAt
        // Distractor endings: last N words of other quotes, N close to the
        // true ending's word count so length doesn't give it away.
        val seen = linkedSetOf<String>()
        pool.filter { it.dbId != target.dbId || it.quote.number != target.quote.number }
            .shuffled()
            .forEach { sq ->
                if (seen.size >= 3) return@forEach
                val other = sq.quote.text.split("\\s+".toRegex()).filter { it.isNotBlank() }
                if (other.size < 6) return@forEach
                val take = answerWords.coerceIn(3, other.size)
                val ending = other.takeLast(take).joinToString(" ")
                if (!ending.equals(answer, ignoreCase = true) && seen.none { s ->
                        s.equals(ending, ignoreCase = true)
                    }
                ) {
                    seen += ending
                }
            }
        if (seen.size < 3) return@repeat
        return CompleteRound(
            quote = target,
            leadIn = leadIn,
            answer = answer,
            options = (seen.toList() + answer).shuffled()
        )
    }
    return null
}
