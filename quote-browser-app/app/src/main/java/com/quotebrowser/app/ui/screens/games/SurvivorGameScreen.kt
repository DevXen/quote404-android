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
import androidx.compose.material.icons.filled.Whatshot
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
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.redactNicksForQuiz
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface SurvivorRound {
    /** Shown in the header while this round is active. */
    val title: String
    /** Human-readable correct answer, revealed on a miss. */
    val reveal: String
}

private data class WhoSaidData(
    val quote: SourcedQuote,
    val redacted: String,
    val answer: String,
    val options: List<String>
)

private data class WhoSaidRound(val data: WhoSaidData) : SurvivorRound {
    override val title = "Who said it?"
    override val reveal = "It was “${data.answer}”."
}

private data class HigherLowerRound(
    val a: SourcedQuote,
    val b: SourcedQuote,
    val firstIsHigher: Boolean
) : SurvivorRound {
    override val title = "Which is rated higher?"
    override val reveal = if (firstIsHigher) "#${a.quote.number} was rated higher."
    else "#${b.quote.number} was rated higher."
}

private data class TfSurvivorRound(val data: TrueFalseRound) : SurvivorRound {
    override val title = "Did “${data.claimedNick}” say this?"
    override val reveal = if (data.isTrue) "True — they really said it."
    else "False — it was “${data.actualNick}”."
}

/**
 * Survivor: an endless run mixing who-said-it, higher-or-lower, and
 * true/false questions. One wrong answer ends the run; the best
 * streak persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurvivorGameScreen(
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
    var streak by remember { mutableStateOf(0) }
    var best by remember { mutableStateOf(prefs.getBestStreak(GamePrefs.SURVIVOR)) }
    var rounds by remember { mutableStateOf<List<SurvivorRound>>(emptyList()) }
    var roundIndex by remember { mutableStateOf(0) }
    var refilling by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var lastReveal by remember { mutableStateOf<String?>(null) }

    val round = rounds.getOrNull(roundIndex)

    /** Persists the best streak when the run ends. */
    fun persistBest() {
        if (streak > best) {
            best = streak
            prefs.updateBestStreak(GamePrefs.SURVIVOR, streak)
        }
    }

    fun endRun(reveal: String) {
        lastReveal = reveal
        running = false
        finished = true
        persistBest()
    }

    fun topUp() {
        if (refilling || finished) return
        refilling = true
        scope.launch {
            val exclude = rounds.flatMap {
                when (it) {
                    is WhoSaidRound -> listOf(it.data.quote)
                    is HigherLowerRound -> listOf(it.a, it.b)
                    is TfSurvivorRound -> listOf(it.data.quote)
                }
            }.map { it.dbId to it.quote.number }.toSet()
            val more = withContext(Dispatchers.IO) {
                buildSurvivorRounds(manager, exclude)
            }
            if (more.isEmpty()) {
                if (rounds.isEmpty()) {
                    loadFailed = true
                } else if (roundIndex >= rounds.size) {
                    // No more questions can be built: end the run on a high.
                    endRun("You outlasted every question we could build!")
                }
            } else {
                rounds = rounds + more
                if (!running && !finished && rounds.isNotEmpty()) {
                    running = true
                }
            }
            refilling = false
        }
    }

    fun start() {
        gameId++
        streak = 0
        rounds = emptyList()
        roundIndex = 0
        finished = false
        loadFailed = false
        lastReveal = null
        running = false
        refilling = false
        topUp()
    }

    fun advance() {
        // Moving past the end shows the loading spinner until topUp lands.
        roundIndex++
        if (rounds.size - roundIndex < 6) topUp()
    }

    /** Shared by all three question types: correct moves on, wrong ends the run. */
    fun resolve(correct: Boolean, reveal: String) {
        if (!running) return
        if (correct) {
            streak++
            advance()
        } else {
            endRun(reveal)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Survivor") },
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
                    "Streak: $streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Best: $best",
                    style = MaterialTheme.typography.titleMedium
                )
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
                            Icon(
                                Icons.Filled.Whatshot,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                "Run over!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            lastReveal?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            Text(
                                "You survived $streak question${if (streak == 1) "" else "s"}.",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Best streak: $best",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { start() }) { Text("Run it back") }
                        }
                    }
                }
                loadFailed -> {
                    EmptyState("Couldn't build survivor questions.\nTry again.")
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
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.Whatshot,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Text(
                                    "How long can you survive?",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Who-said-it, higher-or-lower, and true/false " +
                                        "questions in random order. One wrong answer " +
                                        "ends the run.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Best streak: $best",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(onClick = { start() }) { Text("Start run") }
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                else -> {
                    when (val r = round!!) {
                        is WhoSaidRound -> WhoSaidBody(
                            manager = manager,
                            data = r.data,
                            onPick = { nick ->
                                resolve(nick.equals(r.data.answer, ignoreCase = true), r.reveal)
                            }
                        )
                        is HigherLowerRound -> HigherLowerBody(
                            manager = manager,
                            round = r,
                            onPick = { choice ->
                                val pickedIsHigher =
                                    if (choice == r.a) r.firstIsHigher else !r.firstIsHigher
                                resolve(pickedIsHigher, r.reveal)
                            }
                        )
                        is TfSurvivorRound -> TfBody(
                            manager = manager,
                            data = r.data,
                            onAnswer = { saidTrue ->
                                resolve(saidTrue == r.data.isTrue, r.reveal)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhoSaidBody(
    manager: DatabaseManager,
    data: WhoSaidData,
    onPick: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        RoundHeader("Who said it?")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuoteNumberBadge(number = data.quote.quote.number)
                    Spacer(Modifier.width(8.dp))
                    SourceChip(label = manager.shortLabel(data.quote.dbId))
                }
                Spacer(Modifier.height(8.dp))
                // Nicks already replaced with "•••"; plain text avoids color leaks.
                Text(
                    text = data.redacted,
                    style = scaledTextStyle(MaterialTheme.typography.bodyMedium),
                    maxLines = 14,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        data.options.forEach { option ->
            OutlinedButton(
                onClick = { onPick(option) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(option) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RoundHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun HigherLowerBody(
    manager: DatabaseManager,
    round: HigherLowerRound,
    onPick: (SourcedQuote) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        RoundHeader("Which quote is rated higher?")
        SurvivorQuoteCard(
            sq = round.a,
            sourceLabel = manager.shortLabel(round.a.dbId),
            onPick = { onPick(round.a) }
        )
        Text(
            "VS",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        SurvivorQuoteCard(
            sq = round.b,
            sourceLabel = manager.shortLabel(round.b.dbId),
            onPick = { onPick(round.b) }
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TfBody(
    manager: DatabaseManager,
    data: TrueFalseRound,
    onAnswer: (Boolean) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Did “${data.claimedNick}” actually say this?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SurvivorQuoteCard(
            sq = data.quote,
            sourceLabel = manager.shortLabel(data.quote.dbId),
            onPick = {}
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onAnswer(true) },
                modifier = Modifier.weight(1f)
            ) { Text("TRUE") }
            Button(
                onClick = { onAnswer(false) },
                modifier = Modifier.weight(1f)
            ) { Text("FALSE") }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SurvivorQuoteCard(
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
                Spacer(Modifier.weight(1f))
                RatingBadge(sq.quote.rating)
            }
            Spacer(Modifier.height(8.dp))
            val bodyStyle = scaledTextStyle(MaterialTheme.typography.bodyMedium)
            Text(
                text = rememberNickAnnotatedText(sq.quote.text, bodyStyle),
                style = bodyStyle,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Builds a small batch of mixed-type rounds, excluding already-seen quotes. */
private suspend fun buildSurvivorRounds(
    manager: DatabaseManager,
    exclude: Set<Pair<String, Int>>,
    count: Int = 8
): List<SurvivorRound> {
    val rounds = mutableListOf<SurvivorRound>()
    repeat(count) {
        val builders = listOf(
            suspend { buildWhoSaid(manager, exclude) as SurvivorRound? },
            suspend { buildHigherLower(manager, exclude) as SurvivorRound? },
            suspend { buildTfSurvivor(manager) as SurvivorRound? }
        ).shuffled()
        for (build in builders) {
            val r = try {
                build()
            } catch (e: Exception) {
                null
            }
            if (r != null) {
                rounds += r
                break
            }
        }
    }
    return rounds
}

/** One who-said-it round: redacted quote plus 4 nick options. */
private suspend fun buildWhoSaid(
    manager: DatabaseManager,
    exclude: Set<Pair<String, Int>>,
    tries: Int = 6
): WhoSaidRound? {
    repeat(tries) {
        val pool = manager.gameQuotes(60)
        if (pool.isEmpty()) return@repeat
        val withNicks = pool.filter {
            (it.dbId to it.quote.number) !in exclude &&
                extractNicks(it.quote.text).isNotEmpty()
        }
        if (withNicks.isEmpty()) return@repeat
        val target = withNicks.random()
        val answer = extractNicks(target.quote.text).first()
        val redacted = redactNicksForQuiz(target.quote.text, answer).first
        val seen = linkedMapOf<String, String>()
        pool.forEach { sq ->
            extractNicks(sq.quote.text).forEach { nick ->
                if (!nick.equals(answer, ignoreCase = true)) {
                    seen.getOrPut(nick.lowercase()) { nick }
                }
            }
        }
        val distractors = seen.values.shuffled().take(3)
        if (distractors.size < 3) return@repeat
        return WhoSaidRound(
            WhoSaidData(
                quote = target,
                redacted = redacted,
                answer = answer,
                options = (distractors + answer).shuffled()
            )
        )
    }
    return null
}

/** One higher-or-lower round: two distinctly-rated quotes. */
private suspend fun buildHigherLower(
    manager: DatabaseManager,
    exclude: Set<Pair<String, Int>>,
    tries: Int = 6
): HigherLowerRound? {
    repeat(tries) {
        val rated = manager.gameQuotes(60)
            .filter {
                it.quote.rating != null &&
                    (it.dbId to it.quote.number) !in exclude
            }
            .shuffled()
        if (rated.size < 2) return@repeat
        val a = rated[0]
        val b = rated[1]
        val ra = a.quote.rating!!
        val rb = b.quote.rating!!
        if (ra != rb) return HigherLowerRound(a, b, ra > rb)
    }
    return null
}

/** One true/false round, reusing the shared true/false builder. */
private suspend fun buildTfSurvivor(manager: DatabaseManager): TfSurvivorRound? {
    val data = buildTrueFalseRound(manager) ?: return null
    return TfSurvivorRound(data)
}
