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
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val GAME_SECONDS = 60

/**
 * Speed round: 60 seconds of higher-or-lower. Two rated quotes face off —
 * tap the one rated higher (unrated quotes are skipped). Every correct
 * guess scores one point; the best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedRoundGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var gameId by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(GAME_SECONDS) }
    var score by remember { mutableStateOf(0) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.SPEED_ROUND)) }
    var pairs by remember { mutableStateOf<List<Pair<SourcedQuote, SourcedQuote>>>(emptyList()) }
    var pairIndex by remember { mutableStateOf(0) }
    var pair by remember { mutableStateOf<Pair<SourcedQuote, SourcedQuote>?>(null) }
    var refilling by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<Boolean?>(null) }

    fun finish() {
        running = false
        finished = true
        if (score > best) {
            best = score
            prefs.updateBestScore(GamePrefs.SPEED_ROUND, score)
        }
    }

    fun topUp() {
        if (refilling) return
        refilling = true
        scope.launch {
            val exclude = pairs.flatMap { listOf(it.first, it.second) }
                .map { it.dbId to it.quote.number }
                .toSet()
            val more = withContext(Dispatchers.IO) {
                buildSpeedPairs(manager, exclude)
            }
            pairs = pairs + more
            refilling = false
            if (more.isEmpty() && pairs.isEmpty()) {
                loadFailed = true
            } else if (pair == null && pairs.isNotEmpty()) {
                pairIndex = 0
                pair = pairs[0]
                // Start the clock only once the first pair is on screen.
                running = true
            }
        }
    }

    fun start() {
        gameId++
        score = 0
        timeLeft = GAME_SECONDS
        finished = false
        loadFailed = false
        pairs = emptyList()
        pairIndex = 0
        pair = null
        lastResult = null
        running = false
        topUp()
    }

    fun nextPair() {
        if (pairIndex + 1 < pairs.size) {
            pairIndex++
            pair = pairs[pairIndex]
        } else {
            pair = null
        }
        if (pairs.size - pairIndex < 8) topUp()
    }

    fun pick(choice: SourcedQuote) {
        if (!running) return
        val p = pair ?: return
        val other = if (choice == p.first) p.second else p.first
        val correct = (choice.quote.rating ?: 0) >= (other.quote.rating ?: 0)
        lastResult = correct
        if (correct) score++
        nextPair()
    }

    LaunchedEffect(gameId, running) {
        if (!running) return@LaunchedEffect
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        finish()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Speed Round") },
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
                Text(
                    if (running || finished) "${timeLeft}s" else "$GAME_SECONDS s",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (running && timeLeft <= 10) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
                Text("Score: $score", style = MaterialTheme.typography.titleMedium)
                Text("Best: $best", style = MaterialTheme.typography.titleMedium)
            }
            when {
                finished -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Time's up!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "You got $score right in $GAME_SECONDS seconds.",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Best: $best",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { start() }) { Text("Play again") }
                        }
                    }
                }
                loadFailed -> {
                    EmptyState("Couldn't find rated quotes.\nTry changing your random sources.")
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { start() }) { Text("Retry") }
                    }
                }
                pair == null -> {
                    if (gameId == 0) {
                        // Idle: explain and offer to start.
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "60 seconds on the clock.",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Tap the higher-rated quote, as many as you can.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(onClick = { start() }) { Text("Start") }
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                else -> {
                    val p = pair!!
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Which quote is rated higher?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        SpeedCard(
                            sq = p.first,
                            sourceLabel = manager.shortLabel(p.first.dbId),
                            onPick = { pick(p.first) }
                        )
                        Text(
                            "VS",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        SpeedCard(
                            sq = p.second,
                            sourceLabel = manager.shortLabel(p.second.dbId),
                            onPick = { pick(p.second) }
                        )
                        lastResult?.let { wasCorrect ->
                            Text(
                                text = if (wasCorrect) "Correct!" else "Wrong!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (wasCorrect) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedCard(
    sq: SourcedQuote,
    sourceLabel: String,
    onPick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onPick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuoteNumberBadge(number = sq.quote.number)
                Spacer(Modifier.width(8.dp))
                SourceChip(label = sourceLabel)
            }
            Spacer(Modifier.height(8.dp))
            val bodyStyle = scaledTextStyle(MaterialTheme.typography.bodyMedium)
            Text(
                text = rememberNickAnnotatedText(sq.quote.text, bodyStyle),
                style = bodyStyle,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Builds pairs of distinctly-rated quotes, excluding already-seen keys. */
private suspend fun buildSpeedPairs(
    manager: DatabaseManager,
    exclude: Set<Pair<String, Int>>,
    count: Int = 20
): List<Pair<SourcedQuote, SourcedQuote>> {
    val rated = manager.gameQuotes(count * 4)
        .filter { it.quote.rating != null }
        .filter { (it.dbId to it.quote.number) !in exclude }
        .shuffled()
    val pairs = mutableListOf<Pair<SourcedQuote, SourcedQuote>>()
    var i = 0
    while (i + 1 < rated.size && pairs.size < count) {
        val a = rated[i]
        val b = rated[i + 1]
        if (a.quote.rating != b.quote.rating &&
            (a.dbId to a.quote.number) != (b.dbId to b.quote.number)
        ) {
            pairs += a to b
        }
        i += 2
    }
    return pairs
}
