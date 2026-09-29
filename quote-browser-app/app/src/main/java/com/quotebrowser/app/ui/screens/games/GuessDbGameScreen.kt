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
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10

private data class GuessDbRound(
    val quote: SourcedQuote,
    /** Database ids backing the four label options, shuffled. */
    val options: List<String>
)

/**
 * Guess the database: a quote is shown (no source chip!) and you pick
 * which of the bundled archives it comes from, from four options.
 * 10 rounds per game; the best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuessDbGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<GuessDbRound?>(null) }
    var picked by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.GUESS_DB)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun loadRound() {
        loading = true
        picked = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildGuessDbRound(manager) }
            round = r
            failed = r == null
            loading = false
        }
    }

    fun pick(id: String) {
        if (picked != null) return
        picked = id
        if (id == round?.quote?.dbId) {
            score++
            if (score > best) {
                best = score
                prefs.updateBestScore(GamePrefs.GUESS_DB, score)
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
                title = { Text("Guess the Database") },
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
                                "You placed $score out of $TOTAL_ROUNDS quotes.",
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
                loading && round == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed || round == null -> {
                    EmptyState("Couldn't draw a quote from a database.\nTry again.")
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
                    val correct = answered && picked == r.quote.dbId
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
                                // No SourceChip here — it would give away the answer.
                                QuoteNumberBadge(number = r.quote.quote.number)
                                Spacer(Modifier.height(8.dp))
                                val bodyStyle =
                                    scaledTextStyle(MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = rememberNickAnnotatedText(
                                        r.quote.quote.text, bodyStyle
                                    ),
                                    style = bodyStyle,
                                    maxLines = if (answered) Int.MAX_VALUE else 10,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            "Which archive is it from?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        r.options.forEach { id ->
                            val label = manager.shortLabel(id)
                            val isAnswer = id == r.quote.dbId
                            val isWrongPick = answered && picked == id && !isAnswer
                            when {
                                answered && isAnswer -> Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(label) }
                                isWrongPick -> Button(
                                    onClick = {},
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(label) }
                                else -> OutlinedButton(
                                    onClick = { if (!answered) pick(id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(label) }
                            }
                        }
                        AnimatedVisibility(
                            visible = answered,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = if (correct) "Correct! 🎉"
                                else "It was from ${manager.shortLabel(r.quote.dbId)}.",
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

/** Draws a visible quote from a random database with 3 other db options. */
private suspend fun buildGuessDbRound(manager: DatabaseManager): GuessDbRound? {
    val ids = try {
        manager.allDbIds().filter { it != DatabaseManager.ALL_DBS_ID }
    } catch (e: Exception) {
        emptyList()
    }
    if (ids.size < 4) return null
    val filterOn = manager.contentFilterEnabled.value
    repeat(10) {
        val options = ids.shuffled().take(4)
        val answerId = options.random()
        val quotes = try {
            manager.repoFor(answerId).randomQuotes(15)
                .filter { q ->
                    !manager.isQuoteHiddenOrMuted(answerId, q.number, q.text) &&
                        !(filterOn && manager.isHiddenByContentFilter(q.text))
                }
        } catch (e: Exception) {
            emptyList()
        }
        val q = quotes.firstOrNull() ?: return@repeat
        return GuessDbRound(SourcedQuote(answerId, q), options.shuffled())
    }
    return null
}
