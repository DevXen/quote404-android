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
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ENTRANTS = 8

/**
 * Tournament: 8 quotes enter a single-elimination bracket —
 * quarterfinals, semifinals, final. Each matchup shows two quotes and you
 * pick the funnier/better one; the winner advances until a champion is
 * crowned. Completed tournaments are counted persistently.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentGameScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePrefs.of(context) }
    val scope = rememberCoroutineScope()
    var matchups by remember { mutableStateOf<List<Pair<SourcedQuote, SourcedQuote>>>(emptyList()) }
    var matchIndex by remember { mutableStateOf(0) }
    var winners by remember { mutableStateOf<List<SourcedQuote>>(emptyList()) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var champion by remember { mutableStateOf<SourcedQuote?>(null) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(prefs.getBestScore(GamePrefs.TOURNAMENT)) }

    fun roundName(): String = when (matchups.size) {
        4 -> "Quarterfinal"
        2 -> "Semifinal"
        1 -> "Final"
        else -> "Match"
    }

    fun draw() {
        loading = true
        champion = null
        results = emptyList()
        winners = emptyList()
        matchIndex = 0
        scope.launch {
            val entrants = withContext(Dispatchers.IO) {
                manager.gameQuotes(ENTRANTS * 3).take(ENTRANTS)
            }
            if (entrants.size < ENTRANTS) {
                failed = true
            } else {
                matchups = entrants.shuffled().chunked(2).map { it[0] to it[1] }
            }
            loading = false
        }
    }

    fun pick(winner: SourcedQuote) {
        val m = matchups.getOrNull(matchIndex) ?: return
        if (champion != null) return
        val loser = if (winner == m.first) m.second else m.first
        results = results + "${roundName()}: #${winner.quote.number} beat #${loser.quote.number}"
        val newWinners = winners + winner
        if (matchIndex + 1 < matchups.size) {
            matchIndex++
            winners = newWinners
        } else if (newWinners.size == 1) {
            champion = newWinners[0]
            completed++
            prefs.updateBestScore(GamePrefs.TOURNAMENT, completed)
        } else {
            matchups = newWinners.shuffled().chunked(2).map { it[0] to it[1] }
            matchIndex = 0
            winners = emptyList()
        }
    }

    LaunchedEffect(Unit) { draw() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Tournament") },
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
                val champ = champion
                Text(
                    when {
                        champ != null -> "Champion crowned"
                        matchups.isNotEmpty() ->
                            "${roundName()} ${matchIndex + 1}/${matchups.size}"
                        else -> "Tournament"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Tournaments: $completed",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            when {
                loading && matchups.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                failed -> {
                    EmptyState("Couldn't draw 8 quotes for the bracket.\nTry again.")
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            failed = false
                            draw()
                        }) { Text("Retry") }
                    }
                }
                champion != null -> {
                    val c = champion!!
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            "Champion!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TournamentCard(
                            sq = c,
                            sourceLabel = manager.shortLabel(c.dbId),
                            onPick = {}
                        )
                        if (results.isNotEmpty()) {
                            Text(
                                "Bracket results",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            results.forEach { line ->
                                Text(
                                    line,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { draw() }) { Text("New tournament") }
                        Spacer(Modifier.height(16.dp))
                    }
                }
                else -> {
                    val m = matchups.getOrNull(matchIndex)
                    if (m == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap the funnier quote — winner advances",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            TournamentCard(
                                sq = m.first,
                                sourceLabel = manager.shortLabel(m.first.dbId),
                                onPick = { pick(m.first) }
                            )
                            Text(
                                "VS",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            TournamentCard(
                                sq = m.second,
                                sourceLabel = manager.shortLabel(m.second.dbId),
                                onPick = { pick(m.second) }
                            )
                            if (results.isNotEmpty()) {
                                Text(
                                    "Decided so far",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                results.takeLast(4).forEach { line ->
                                    Text(
                                        line,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(
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
                // Null ratings render as "—" inside RatingBadge.
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
