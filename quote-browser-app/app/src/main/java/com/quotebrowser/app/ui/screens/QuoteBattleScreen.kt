package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.quotebrowser.app.data.BattleEntry
import com.quotebrowser.app.data.BattleStore
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.screens.games.GamePrefs
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Quote battle: two random quotes face off — tap the funnier one. The
 * winner climbs an Elo ladder persisted in [BattleStore]; the header
 * shows battles fought, the current champion (tap to open it), the
 * session record, and the all-time best win streak. A Rankings button
 * switches to the top-30 Elo leaderboard.
 */
@Composable
fun QuoteBattleScreen(
    manager: DatabaseManager,
    battleStore: BattleStore,
    onQuoteClick: (String, Int) -> Unit,
    onBack: () -> Unit,
    /** v1.66: opens the 8-quote single-elimination Tournament game. */
    onTournament: () -> Unit = {}
) {
    val context = LocalContext.current
    val gamePrefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var pair by remember { mutableStateOf<Pair<SourcedQuote, SourcedQuote>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var picked by remember { mutableStateOf<SourcedQuote?>(null) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var battles by remember { mutableStateOf(battleStore.battleCount()) }
    var champion by remember { mutableStateOf<BattleEntry?>(battleStore.topRated(1).firstOrNull()) }
    var failed by remember { mutableStateOf(false) }
    var showRankings by remember { mutableStateOf(false) }
    var sessionWins by remember { mutableStateOf(0) }
    var sessionSkips by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var bestStreak by remember { mutableStateOf(gamePrefs.getBestStreak(GamePrefs.QUOTE_BATTLE)) }

    fun nextPair() {
        loading = true
        scope.launch {
            val p = withContext(kotlinx.coroutines.Dispatchers.IO) { drawPair(manager) }
            picked = null
            resultText = null
            pair = p
            failed = p == null
            loading = false
        }
    }

    fun skipPair() {
        sessionSkips++
        streak = 0
        nextPair()
    }

    fun pick(winner: SourcedQuote, loser: SourcedQuote) {
        if (picked != null) return
        picked = winner
        val before = battleStore.eloFor(winner.dbId, winner.quote.number)
        battleStore.recordWin(winner.dbId, winner.quote.number, loser.dbId, loser.quote.number)
        val delta = (battleStore.eloFor(winner.dbId, winner.quote.number) - before).toInt()
        resultText = "#${winner.quote.number} wins! +$delta"
        battles = battleStore.battleCount()
        champion = battleStore.topRated(1).firstOrNull()
        sessionWins++
        streak++
        gamePrefs.updateBestStreak(GamePrefs.QUOTE_BATTLE, streak)
        bestStreak = gamePrefs.getBestStreak(GamePrefs.QUOTE_BATTLE)
        scope.launch {
            delay(1400)
            nextPair()
        }
    }

    LaunchedEffect(Unit) { nextPair() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Quote Battle",
            actions = {
                IconButton(onClick = { if (showRankings) showRankings = false else onBack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Battles: $battles", style = MaterialTheme.typography.titleMedium)
            val c = champion
            if (c == null) {
                Text("No champion yet", style = MaterialTheme.typography.titleMedium)
            } else {
                TextButton(onClick = { onQuoteClick(c.dbId, c.number) }) {
                    Text(
                        "👑 #${c.number} (${c.elo.toInt()})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            TextButton(onClick = { showRankings = true }) {
                Text("Rankings", style = MaterialTheme.typography.titleMedium)
            }
            // v1.66: 8-quote single-elimination tournament game.
            TextButton(onClick = onTournament) {
                Text("Tournament", style = MaterialTheme.typography.titleMedium)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Session: $sessionWins–$sessionSkips",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Best streak: $bestStreak",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showRankings) {
            BattleRankings(
                battleStore = battleStore,
                manager = manager,
                onQuoteClick = onQuoteClick
            )
        } else {
            val p = pair
            when {
                loading && p == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed || p == null -> {
                    EmptyState("Couldn't find two quotes.\nTry changing your random sources.")
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { nextPair() }) { Text("Retry") }
                    }
                }
                else -> {
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
                            "Tap the funnier quote",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        BattleCard(
                            sq = p.first,
                            sourceLabel = manager.shortLabel(p.first.dbId),
                            dimmed = answered && picked != p.first,
                            onPick = { if (!answered) pick(p.first, p.second) },
                            onOpen = { onQuoteClick(p.first.dbId, p.first.quote.number) }
                        )
                        Text(
                            "VS",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        BattleCard(
                            sq = p.second,
                            sourceLabel = manager.shortLabel(p.second.dbId),
                            dimmed = answered && picked != p.second,
                            onPick = { if (!answered) pick(p.second, p.first) },
                            onOpen = { onQuoteClick(p.second.dbId, p.second.quote.number) }
                        )
                        resultText?.let { result ->
                            Text(
                                text = result,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (!answered) {
                            TextButton(
                                onClick = { skipPair() },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Skip this pair")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Top-30 Elo leaderboard. Tapping a row opens the quote.
 */
@Composable
private fun BattleRankings(
    battleStore: BattleStore,
    manager: DatabaseManager,
    onQuoteClick: (String, Int) -> Unit
) {
    val rankings = remember { battleStore.topRated(30) }
    if (rankings.isEmpty()) {
        EmptyState("No battles yet.\nTap the funnier quote to start the ladder.")
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(rankings) { index, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQuoteClick(entry.dbId, entry.number) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QuoteNumberBadge(number = entry.number)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Elo ${entry.elo.toInt()} • ${entry.wins} win" +
                                    if (entry.wins == 1) "" else "s",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = manager.shortLabel(entry.dbId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = when (index) {
                            0 -> "👑"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BattleCard(
    sq: SourcedQuote,
    sourceLabel: String,
    dimmed: Boolean,
    onPick: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dimmed) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onPick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuoteNumberBadge(number = sq.quote.number)
                Spacer(Modifier.width(8.dp))
                SourceChip(label = sourceLabel)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpen) {
                    Icon(
                        Icons.Filled.OpenInNew,
                        contentDescription = "Open quote",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

/**
 * Draws two distinct visible quotes for a battle, honoring the user's
 * random sources and the content filter (same approach as GameViewModel).
 */
private suspend fun drawPair(manager: DatabaseManager): Pair<SourcedQuote, SourcedQuote>? {
    val all = manager.allDbIds()
    val selected = manager.randomSourceIds.value
    val ids = (if (selected == null) all else all.filter { it in selected })
        .ifEmpty { all }
    if (ids.isEmpty()) return null
    // v1.65: battles honor the global maturity ceiling.
    val maturities = listOf(manager.maturityCeiling.value)
    repeat(25) {
        val aId = ids.random()
        val bId = ids.random()
        val a = try {
            manager.repoFor(aId).randomQuotes(12, maturities = maturities)
                .filter { q -> !manager.isQuoteHiddenFromLists(aId, q.number, q.text) }
                .shuffled()
        } catch (e: Exception) {
            emptyList()
        }
        val b = try {
            manager.repoFor(bId).randomQuotes(12, maturities = maturities)
                .filter { q -> !manager.isQuoteHiddenFromLists(bId, q.number, q.text) }
                .shuffled()
        } catch (e: Exception) {
            emptyList()
        }
        val qa = a.firstOrNull() ?: return@repeat
        val qb = b.firstOrNull { q -> q.number != qa.number || bId != aId } ?: return@repeat
        return SourcedQuote(aId, qa) to SourcedQuote(bId, qb)
    }
    return null
}
