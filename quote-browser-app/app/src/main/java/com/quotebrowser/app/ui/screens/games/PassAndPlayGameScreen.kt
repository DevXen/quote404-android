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
import androidx.compose.material.icons.filled.People
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
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10

private data class PassRound(
    val a: SourcedQuote,
    val b: SourcedQuote,
    val firstIsHigher: Boolean
)

private enum class PassPhase { IDLE, LOADING, PASS_PROMPT, PLAYING, REVEAL, GAME_OVER }

/**
 * Pass and play: local 2-player head-to-head. Players alternate rounds;
 * each round shows two quotes and the active player taps the one they
 * believe is rated higher. Correct picks score a point for the active
 * player. A pass-the-phone interstitial separates turns. The best
 * (winning) score across matches persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassAndPlayGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf(PassPhase.IDLE) }
    var rounds by remember { mutableStateOf<List<PassRound>>(emptyList()) }
    var roundIndex by remember { mutableStateOf(0) }
    var p1Score by remember { mutableStateOf(0) }
    var p2Score by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf<SourcedQuote?>(null) }
    var lastCorrect by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.PASS_AND_PLAY)) }

    val activePlayer = (roundIndex % 2) + 1 // Round 1 -> P1, round 2 -> P2, ...

    fun start() {
        phase = PassPhase.LOADING
        loadFailed = false
        roundIndex = 0
        p1Score = 0
        p2Score = 0
        picked = null
        scope.launch {
            val built = withContext(Dispatchers.IO) { buildPassRounds(manager) }
            if (built.size < TOTAL_ROUNDS) {
                rounds = emptyList()
                loadFailed = true
                phase = PassPhase.IDLE
            } else {
                rounds = built
                phase = PassPhase.PASS_PROMPT
            }
        }
    }

    fun pick(choice: SourcedQuote) {
        if (phase != PassPhase.PLAYING) return
        val r = rounds.getOrNull(roundIndex) ?: return
        picked = choice
        val pickedIsHigher = if (choice == r.a) r.firstIsHigher else !r.firstIsHigher
        lastCorrect = pickedIsHigher
        if (pickedIsHigher) {
            if (activePlayer == 1) p1Score++ else p2Score++
        }
        phase = PassPhase.REVEAL
    }

    fun next() {
        picked = null
        if (roundIndex + 1 >= TOTAL_ROUNDS) {
            val winning = maxOf(p1Score, p2Score)
            if (winning > best) {
                best = winning
                prefs.updateBestScore(GamePrefs.PASS_AND_PLAY, winning)
            }
            phase = PassPhase.GAME_OVER
        } else {
            roundIndex++
            phase = PassPhase.PASS_PROMPT
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pass & Play") },
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
                    "P1: $p1Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (activePlayer == 1 && phase != PassPhase.GAME_OVER)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "P2: $p2Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (activePlayer == 2 && phase != PassPhase.GAME_OVER)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when (phase) {
                        PassPhase.GAME_OVER -> "Done"
                        PassPhase.IDLE -> "2 players"
                        else -> "Round ${roundIndex + 1}/$TOTAL_ROUNDS"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
            when (phase) {
                PassPhase.IDLE -> {
                    if (loadFailed) {
                        EmptyState(
                            "Couldn't draw $TOTAL_ROUNDS rated pairs.\n" +
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
                    } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                "Two players, one phone.",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Take turns tapping the higher-rated quote. " +
                                    "Hand the phone over between rounds. " +
                                    "$TOTAL_ROUNDS rounds, highest score wins.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Best winning score: $best",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { start() }) { Text("Start match") }
                        }
                    }
                }
                }
                PassPhase.LOADING -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                PassPhase.PASS_PROMPT -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Hand the phone to",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Player $activePlayer",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Round ${roundIndex + 1} of $TOTAL_ROUNDS — no peeking!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { phase = PassPhase.PLAYING }) {
                                Text("I'm Player $activePlayer")
                            }
                        }
                    }
                }
                PassPhase.PLAYING, PassPhase.REVEAL -> {
                    val r = rounds.getOrNull(roundIndex)
                    if (r == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val revealed = phase == PassPhase.REVEAL
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Player $activePlayer: which quote is rated higher?",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            PassQuoteCard(
                                sq = r.a,
                                sourceLabel = manager.shortLabel(r.a.dbId),
                                picked = picked == r.a,
                                revealed = revealed,
                                onPick = { pick(r.a) }
                            )
                            Text(
                                "VS",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            PassQuoteCard(
                                sq = r.b,
                                sourceLabel = manager.shortLabel(r.b.dbId),
                                picked = picked == r.b,
                                revealed = revealed,
                                onPick = { pick(r.b) }
                            )
                            if (revealed) {
                                Text(
                                    text = if (lastCorrect)
                                        "Correct! Point for Player $activePlayer."
                                    else "Wrong — no point this round.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastCorrect) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.weight(1f))
                                Button(
                                    onClick = { next() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        if (roundIndex + 1 >= TOTAL_ROUNDS) "See results"
                                        else "Pass to Player ${if (activePlayer == 1) 2 else 1}"
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
                PassPhase.GAME_OVER -> {
                    val winner = when {
                        p1Score > p2Score -> "Player 1 wins!"
                        p2Score > p1Score -> "Player 2 wins!"
                        else -> "It's a tie!"
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                winner,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Player 1: $p1Score  •  Player 2: $p2Score",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "Best winning score: $best",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { start() }) { Text("Rematch") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassQuoteCard(
    sq: SourcedQuote,
    sourceLabel: String,
    picked: Boolean,
    revealed: Boolean,
    onPick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (picked) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onPick,
        enabled = !revealed
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuoteNumberBadge(number = sq.quote.number)
                Spacer(Modifier.width(8.dp))
                SourceChip(label = sourceLabel)
                if (revealed) {
                    Spacer(Modifier.weight(1f))
                    RatingBadge(sq.quote.rating)
                }
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

/** Builds [TOTAL_ROUNDS] pairs of distinctly-rated quotes. */
private suspend fun buildPassRounds(manager: DatabaseManager): List<PassRound> {
    val rated = manager.gameQuotes(TOTAL_ROUNDS * 8)
        .filter { it.quote.rating != null }
        .shuffled()
    val pairs = mutableListOf<PassRound>()
    var i = 0
    while (i + 1 < rated.size && pairs.size < TOTAL_ROUNDS) {
        val a = rated[i]
        val b = rated[i + 1]
        val ra = a.quote.rating!!
        val rb = b.quote.rating!!
        if (ra != rb && (a.dbId to a.quote.number) != (b.dbId to b.quote.number)) {
            pairs += PassRound(a, b, ra > rb)
        }
        i += 2
    }
    return pairs
}
