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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.applyDisplayBreaks
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.redactedSpansForQuiz
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val GAME_SECONDS = 60

/** One blitz round: a quote plus four nick options, answer redacted. */
private data class BlitzRound(
    val quote: SourcedQuote,
    val options: List<String>,
    val answer: String
)

/**
 * Blitz who-said-it: 60 seconds on the clock, name as many speakers as you
 * can. The speaker's nick is redacted from the quote (same redaction as the
 * regular Who Said It game); every correct pick scores one point and the
 * round advances immediately. The best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlitzWhoSaidItGameScreen(
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
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.BLITZ_WHO_SAID_IT)) }
    var rounds by remember { mutableStateOf<List<BlitzRound>>(emptyList()) }
    var roundIndex by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf<BlitzRound?>(null) }
    var refilling by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<Boolean?>(null) }
    var lastAnswer by remember { mutableStateOf<String?>(null) }

    fun finish() {
        running = false
        finished = true
        if (score > best) {
            best = score
            prefs.updateBestScore(GamePrefs.BLITZ_WHO_SAID_IT, score)
        }
    }

    fun topUp() {
        if (refilling) return
        refilling = true
        scope.launch {
            val exclude = rounds
                .map { it.quote.dbId to it.quote.quote.number }
                .toSet()
            val more = withContext(Dispatchers.IO) {
                buildBlitzRounds(manager, exclude)
            }
            rounds = rounds + more
            refilling = false
            if (more.isEmpty() && rounds.isEmpty()) {
                loadFailed = true
            } else if (round == null && rounds.isNotEmpty()) {
                roundIndex = 0
                round = rounds[0]
                // Start the clock only once the first round is on screen.
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
        rounds = emptyList()
        roundIndex = 0
        round = null
        lastResult = null
        lastAnswer = null
        running = false
        topUp()
    }

    fun nextRound() {
        if (roundIndex + 1 < rounds.size) {
            roundIndex++
            round = rounds[roundIndex]
        } else {
            round = null
        }
        if (rounds.size - roundIndex < 8) topUp()
    }

    fun pick(nick: String) {
        if (!running) return
        val r = round ?: return
        val correct = nick.equals(r.answer, ignoreCase = true)
        lastResult = correct
        lastAnswer = r.answer
        if (correct) score++
        nextRound()
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
                title = { Text("Blitz Who-Said-It") },
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
                                "You named $score speaker" +
                                    (if (score == 1) "" else "s") +
                                    " in $GAME_SECONDS seconds.",
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
                    EmptyState(
                        "Couldn't find quotes with known speakers.\n" +
                            "Try changing your random sources."
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { start() }) { Text("Retry") }
                    }
                }
                round == null -> {
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
                                    "Name the redacted speaker in each quote — " +
                                        "as many as you can.",
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
                    val r = round!!
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                val bodyStyle = scaledTextStyle(
                                    MaterialTheme.typography.bodyMedium
                                )
                                val palette = LocalNickPalette.current
                                val lineHighlight = MaterialTheme.colorScheme.primaryContainer
                                val annotated = remember(
                                    r.quote.quote.text, r.answer, palette, lineHighlight
                                ) {
                                    val display = applyDisplayBreaks(
                                        r.quote.quote.text,
                                        palette.ircLineBreaks,
                                        palette.dialogueLineBreaks
                                    )
                                    val (redacted, spans, line) =
                                        redactedSpansForQuiz(display, r.answer, palette)
                                    buildAnnotatedString {
                                        append(redacted)
                                        for (s in spans) {
                                            addStyle(
                                                SpanStyle(color = s.color),
                                                s.start, s.endExcl
                                            )
                                        }
                                        if (line != null) {
                                            addStyle(
                                                SpanStyle(background = lineHighlight),
                                                line.first, line.second
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = annotated,
                                    style = bodyStyle,
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            "Who said it?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        r.options.forEach { nick ->
                            OutlinedButton(
                                onClick = { pick(nick) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("<$nick>")
                            }
                        }
                        lastResult?.let { wasCorrect ->
                            Text(
                                text = if (wasCorrect) "Correct!"
                                else "Wrong — it was <${lastAnswer}>.",
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

/**
 * Builds who-said-it rounds from filter-honoring random quotes, excluding
 * already-seen keys. Muted nicks never appear as the answer or as options.
 */
private suspend fun buildBlitzRounds(
    manager: DatabaseManager,
    exclude: Set<Pair<String, Int>>,
    count: Int = 20
): List<BlitzRound> {
    val muted = manager.mutedNicks.value
    val tops = manager.topNicks(60)
        .map { it.first }
        .filter { it.lowercase() !in muted }
    if (tops.size < 4) return emptyList()
    val rounds = mutableListOf<BlitzRound>()
    for (sq in manager.gameQuotes(count * 3)) {
        if (rounds.size >= count) break
        if ((sq.dbId to sq.quote.number) in exclude) continue
        val answer = extractNicks(sq.quote.text)
            .firstOrNull { it.lowercase() !in muted }
            ?: continue
        val distractors = tops
            .filter { t -> !t.equals(answer, ignoreCase = true) }
            .shuffled()
            .take(3)
        if (distractors.size < 3) continue
        rounds += BlitzRound(
            quote = sq,
            options = (distractors + answer).shuffled(),
            answer = answer
        )
    }
    return rounds
}
