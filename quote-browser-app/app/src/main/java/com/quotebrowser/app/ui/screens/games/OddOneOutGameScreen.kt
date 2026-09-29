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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.redactedSpansForQuiz
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10

private data class OddOneOutRound(
    val quotes: List<SourcedQuote>,
    val oddIndex: Int
)

/**
 * Odd one out: four quotes are shown with ALL nicks redacted. Three share
 * one speaker, one doesn't — tap the odd one out. 10 rounds; best score
 * persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OddOneOutGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<OddOneOutRound?>(null) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.ODD_ONE_OUT)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun loadRound() {
        loading = true
        picked = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildOddOneOutRound(manager) }
            round = r
            failed = r == null
            loading = false
        }
    }

    fun pick(index: Int) {
        if (picked != null) return
        picked = index
        if (index == round?.oddIndex) {
            score++
            if (score > best) {
                best = score
                prefs.updateBestScore(GamePrefs.ODD_ONE_OUT, score)
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
                title = { Text("Odd One Out") },
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
                                "You spotted $score out of $TOTAL_ROUNDS odd ones out.",
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
                    EmptyState("Couldn't find quotes from a shared speaker.\nTry again.")
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
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Three share a speaker — tap the odd one out",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        r.quotes.forEachIndexed { index, sq ->
                            val isOdd = index == r.oddIndex
                            val isPicked = picked == index
                            val containerColor = when {
                                answered && isOdd -> MaterialTheme.colorScheme.primaryContainer
                                answered && isPicked -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                onClick = { if (!answered) pick(index) }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        QuoteNumberBadge(number = sq.quote.number)
                                        Spacer(Modifier.width(8.dp))
                                        GameSourceLabel(dbId = sq.dbId, manager = manager)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    val bodyStyle =
                                        scaledTextStyle(MaterialTheme.typography.bodyMedium)
                                    // All nicks redacted so names can't leak
                                    // the answer; each "•••" is colored by
                                    // speaker so repeat speakers are visible.
                                    val redacted = remember(sq.quote.text) {
                                        val (text, spans, _) =
                                            redactedSpansForQuiz(sq.quote.text, "")
                                        buildAnnotatedString {
                                            append(text)
                                            for (s in spans) {
                                                addStyle(
                                                    SpanStyle(color = s.color),
                                                    s.start, s.endExcl
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = redacted,
                                        style = bodyStyle,
                                        maxLines = 8,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(
                            visible = answered,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val wasCorrect = picked == r.oddIndex
                            Text(
                                text = if (wasCorrect) "Correct! 🎉"
                                else "The odd one was #${r.quotes[r.oddIndex].quote.number}.",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (wasCorrect) MaterialTheme.colorScheme.primary
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

/** Finds 3 quotes sharing a first nick plus 1 quote from another nick. */
private suspend fun buildOddOneOutRound(manager: DatabaseManager): OddOneOutRound? {
    repeat(8) {
        val pool = manager.gameQuotes(80)
        if (pool.size < 4) return@repeat
        val byNick = pool.groupBy { sq ->
            extractNicks(sq.quote.text).firstOrNull()?.lowercase().orEmpty()
        }
        val entry = byNick.entries
            .filter { it.key.isNotEmpty() && it.value.size >= 3 }
            .randomOrNull() ?: return@repeat
        val common = entry.value.shuffled().take(3)
        val commonKeys = common.map { it.dbId to it.quote.number }.toSet()
        val odd = pool.shuffled().firstOrNull { sq ->
            (sq.dbId to sq.quote.number) !in commonKeys &&
                extractNicks(sq.quote.text)
                    .none { nick -> nick.equals(entry.key, ignoreCase = true) }
        } ?: return@repeat
        val quotes = (common + odd).shuffled()
        return OddOneOutRound(quotes = quotes, oddIndex = quotes.indexOf(odd))
    }
    return null
}
