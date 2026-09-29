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
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOTAL_ROUNDS = 10
private const val TRUE_ROUNDS = 5

private data class Top100Round(
    val quote: SourcedQuote,
    val isTop100: Boolean,
    /** 1-based rank inside its database's top 100; 0 when not in it. */
    val rank: Int = 0
)

/**
 * Top 100 or not: half the rounds show a quote from a database's top
 * 100 (same query the Top 100 screen uses), half show quotes outside
 * it. Answer yes/no; the best score persists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Top100GameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var rounds by remember { mutableStateOf<List<Top100Round>>(emptyList()) }
    var roundIndex by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf<Boolean?>(null) }
    var score by remember { mutableStateOf(0) }
    var best by remember { mutableStateOf(prefs.getBestScore(GamePrefs.TOP_100)) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    fun draw() {
        loading = true
        picked = null
        scope.launch {
            val built = withContext(Dispatchers.IO) { buildTop100Rounds(manager) }
            if (built.size < TOTAL_ROUNDS) {
                rounds = emptyList()
                failed = true
            } else {
                rounds = built
                roundIndex = 0
            }
            loading = false
        }
    }

    fun answer(yes: Boolean) {
        if (picked != null) return
        picked = yes
        val r = rounds.getOrNull(roundIndex) ?: return
        if (yes == r.isTop100) {
            score++
            if (score > best) {
                best = score
                prefs.updateBestScore(GamePrefs.TOP_100, score)
            }
        }
    }

    fun next() {
        picked = null
        if (roundIndex + 1 >= rounds.size) {
            gameOver = true
        } else {
            roundIndex++
        }
    }

    fun restart() {
        score = 0
        gameOver = false
        failed = false
        draw()
    }

    LaunchedEffect(Unit) { draw() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Top 100 or Not") },
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
                    when {
                        gameOver -> "Done"
                        rounds.isNotEmpty() -> "Round ${roundIndex + 1}/${rounds.size}"
                        else -> "Top 100"
                    },
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
                                "You scored $score out of ${rounds.size}.",
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
                loading && rounds.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed -> {
                    EmptyState(
                        "Couldn't draw $TOTAL_ROUNDS questions.\n" +
                            "Some databases may be too small — try again."
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
                    val r = rounds.getOrNull(roundIndex)
                    if (r == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val answered = picked != null
                        val correct = answered && picked == r.isTop100
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Is this quote in the Top 100?",
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
                                        Spacer(Modifier.width(8.dp))
                                        SourceChip(label = manager.shortLabel(r.quote.dbId))
                                        Spacer(Modifier.weight(1f))
                                        RatingBadge(r.quote.quote.rating)
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
                                val yesColors = answerButtonColors(
                                    answered, r.isTop100, picked == true
                                )
                                OutlinedButton(
                                    onClick = { answer(true) },
                                    enabled = !answered,
                                    colors = yesColors,
                                    modifier = Modifier.weight(1f)
                                ) { Text("YES") }
                                val noColors = answerButtonColors(
                                    answered, !r.isTop100, picked == false
                                )
                                OutlinedButton(
                                    onClick = { answer(false) },
                                    enabled = !answered,
                                    colors = noColors,
                                    modifier = Modifier.weight(1f)
                                ) { Text("NO") }
                            }
                            AnimatedVisibility(
                                visible = answered,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    text = when {
                                        correct -> "Correct! 🎉"
                                        r.isTop100 -> "Wrong — it's #${r.rank} in " +
                                            "${manager.shortLabel(r.quote.dbId)}."
                                        else -> "Wrong — it's outside the Top 100."
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
}

@Composable
private fun answerButtonColors(
    answered: Boolean,
    isRightAnswer: Boolean,
    wasPicked: Boolean
): androidx.compose.material3.ButtonColors {
    return when {
        answered && isRightAnswer -> androidx.compose.material3.ButtonDefaults.buttonColors()
        answered && wasPicked -> androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        else -> androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
    }
}

/**
 * Draws [TRUE_ROUNDS] quotes from the per-database top 100 (the same
 * `topRated(100, 0)` query the Top 100 screen pages through) and
 * [TOTAL_ROUNDS] - [TRUE_ROUNDS] random quotes outside it. Hidden,
 * muted, and content-filtered quotes are excluded from both groups.
 */
private suspend fun buildTop100Rounds(manager: DatabaseManager): List<Top100Round> {
    val ids = try {
        manager.allDbIds().filter { it != DatabaseManager.ALL_DBS_ID }
    } catch (e: Exception) {
        emptyList()
    }
    if (ids.isEmpty()) return emptyList()
    val filterOn = manager.contentFilterEnabled.value
    val topKeys = mutableSetOf<Pair<String, Int>>()
    val topQuotes = mutableListOf<Top100Round>()
    for (id in ids.shuffled()) {
        try {
            manager.repoFor(id).topRated(100, 0).forEachIndexed { index, q ->
                if (manager.isQuoteHiddenOrMuted(id, q.number, q.text)) return@forEachIndexed
                if (filterOn && manager.isHiddenByContentFilter(q.text)) return@forEachIndexed
                topKeys += id to q.number
                topQuotes += Top100Round(SourcedQuote(id, q), true, index + 1)
            }
        } catch (e: Exception) {
            // Unreadable database — skip it.
        }
    }
    if (topQuotes.size < TRUE_ROUNDS) return emptyList()
    val trues = topQuotes.shuffled().take(TRUE_ROUNDS)
    val falses = manager.gameQuotes(80)
        .filter { (it.dbId to it.quote.number) !in topKeys }
        .shuffled()
        .take(TOTAL_ROUNDS - TRUE_ROUNDS)
        .map { Top100Round(it, false) }
    if (falses.size < TOTAL_ROUNDS - TRUE_ROUNDS) return emptyList()
    return (trues + falses).shuffled()
}
