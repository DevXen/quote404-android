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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10

private data class FillBlankRound(
    val quote: SourcedQuote,
    val blanked: String,
    val answer: String,
    val options: List<String>
)

/**
 * Fill in the blank: a quote with >= 8 words has one mid-length word
 * blanked out (never a nick). Pick the missing word from four options.
 * 10 rounds per game; the best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlankGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<FillBlankRound?>(null) }
    var picked by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var roundNumber by remember { mutableStateOf(1) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.FILL_BLANK)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun loadRound() {
        loading = true
        picked = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { buildFillBlankRound(manager) }
            round = r
            failed = r == null
            loading = false
        }
    }

    fun answer(choice: String) {
        if (picked != null) return
        picked = choice
        val r = round ?: return
        if (choice.equals(r.answer, ignoreCase = true)) {
            score++
            if (score > best) {
                best = score
                prefs.updateBestScore(GamePrefs.FILL_BLANK, score)
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
                title = { Text("Fill in the Blank") },
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
                    EmptyState("Couldn't find a quote with a blankable word.\nTry again.")
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
                    val correct = answered && picked.equals(r.answer, ignoreCase = true)
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
                                    GameSourceLabel(dbId = r.quote.dbId, manager = manager)
                                }
                                Spacer(Modifier.height(8.dp))
                                val bodyStyle =
                                    scaledTextStyle(MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (answered) rememberNickAnnotatedText(
                                        r.quote.quote.text,
                                        bodyStyle
                                    ) else AnnotatedString(r.blanked),
                                    style = bodyStyle,
                                    maxLines = if (answered) Int.MAX_VALUE else 12,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            "Which word is missing?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        r.options.forEach { option ->
                            val isAnswer = option.equals(r.answer, ignoreCase = true)
                            val isWrongPick = answered && picked.equals(
                                option, ignoreCase = true
                            ) && !isAnswer
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
                                else "The word was “${r.answer}”.",
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

/** Picks a quote with >= 8 words, blanks one 4+ letter non-nick word. */
private suspend fun buildFillBlankRound(manager: DatabaseManager): FillBlankRound? {
    repeat(12) {
        val pool = manager.gameQuotes(70)
        if (pool.isEmpty()) return@repeat
        val target = pool.random()
        val tokens = target.quote.text.split("\\s+".toRegex())
        if (tokens.size < 8) return@repeat
        val nicks = extractNicks(target.quote.text).map { it.lowercase() }.toSet()
        val candidates = tokens.indices.filter { i ->
            val w = tokens[i].trim { c -> !c.isLetterOrDigit() }
            w.length >= 4 && w.any { c -> c.isLetter() } && w.lowercase() !in nicks
        }
        if (candidates.isEmpty()) return@repeat
        val idx = candidates.random()
        val answer = tokens[idx].trim { c -> !c.isLetterOrDigit() }
        val blanked = tokens.toMutableList()
            .also { it[idx] = "_____" }
            .joinToString(" ")
        // Distractors: words from other quotes, deduplicated case-insensitively.
        val seen = linkedMapOf<String, String>()
        pool.filter { it.dbId != target.dbId || it.quote.number != target.quote.number }
            .forEach { sq ->
                sq.quote.text.split("\\s+".toRegex()).forEach { token ->
                    val w = token.trim { c -> !c.isLetterOrDigit() }
                    if (w.length >= 4 && w.any { c -> c.isLetter() } &&
                        !w.equals(answer, ignoreCase = true)
                    ) {
                        seen.getOrPut(w.lowercase()) { w }
                    }
                }
            }
        val distractors = seen.values.shuffled().take(3)
        if (distractors.size < 3) return@repeat
        return FillBlankRound(
            quote = target,
            blanked = blanked,
            answer = answer,
            options = (distractors + answer).shuffled()
        )
    }
    return null
}
