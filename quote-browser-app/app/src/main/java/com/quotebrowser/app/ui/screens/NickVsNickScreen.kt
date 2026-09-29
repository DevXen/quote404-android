package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.NickDetail
import com.quotebrowser.app.ui.NickViewModel
import com.quotebrowser.app.ui.components.NickAvatar
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.vmFactory

/**
 * Head-to-head nick comparison: pick two nicks and see quote counts,
 * average ratings, and each nick's best quotes side by side.
 *
 * Wiring (AppNav, not touched here): add route constant
 * `NICK_VS_NICK = "nick_vs_nick"` to Routes and a
 * `composable(Routes.NICK_VS_NICK)` block; hook it from the nicks
 * leaderboard screen (header action) or the overflow menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NickVsNickScreen(
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onBack: () -> Unit,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    modifier: Modifier = Modifier,
    onNickClick: (String) -> Unit = {}
) {
    // v1.58: this screen mixes databases, so site cards keep the global
    // page background outside the card (no single-DB page backdrop).
    CompositionLocalProvider(LocalSingleDatabaseMode provides false) {
        val vm: NickViewModel = viewModel(factory = vmFactory { NickViewModel(manager) })
    val leaderboard by vm.leaderboard.collectAsState()
    val nicks = remember(leaderboard) { leaderboard.map { it.nick } }

    var nickA by remember { mutableStateOf<String?>(null) }
    var nickB by remember { mutableStateOf<String?>(null) }
    var detailA by remember { mutableStateOf<NickDetail?>(null) }
    var detailB by remember { mutableStateOf<NickDetail?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Default to the two most-quoted nicks once the leaderboard loads.
    LaunchedEffect(nicks) {
        if (nicks.size >= 2 && nickA == null && nickB == null) {
            nickA = nicks[0]
            nickB = nicks[1]
        }
    }
    LaunchedEffect(nickA, nickB) {
        val a = nickA
        val b = nickB
        if (a == null || b == null || a.equals(b, ignoreCase = true)) {
            detailA = null
            detailB = null
            return@LaunchedEffect
        }
        loading = true
        try {
            detailA = vm.detailFor(a)
            detailB = vm.detailFor(b)
        } finally {
            loading = false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nick vs nick") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NickPicker(
                        label = "Nick A",
                        selected = nickA,
                        nicks = nicks,
                        exclude = nickB,
                        onPick = { nickA = it },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val tmp = nickA
                            nickA = nickB
                            nickB = tmp
                        },
                        enabled = nickA != null && nickB != null
                    ) {
                        Icon(
                            Icons.Filled.CompareArrows,
                            contentDescription = "Swap nicks"
                        )
                    }
                    NickPicker(
                        label = "Nick B",
                        selected = nickB,
                        nicks = nicks,
                        exclude = nickA,
                        onPick = { nickB = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (loading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            val a = detailA
            val b = detailB
            if (a != null && b != null) {
                item {
                    VersusHeader(a = a, b = b)
                }
                item {
                    HeadToHeadStats(a = a, b = b)
                }
                item {
                    Text(
                        "Best of <${a.nick}>",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(bestQuotes(a), key = { "${it.dbId}:${it.quote.number}" }) { sq ->
                    QuoteRow(
                        quote = sq.quote,
                        dbId = sq.dbId,
                        isFavorite = false,
                        onToggleFavorite = {},
                        showFavoriteAction = false,
                        onClick = { onQuoteClick(sq.dbId, sq.quote.number) },
                        showNumber = showNumber,
                        showRating = showRating,
                        onNickClick = onNickClick)
                }
                item {
                    Text(
                        "Best of <${b.nick}>",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(bestQuotes(b), key = { "${it.dbId}:${it.quote.number}" }) { sq ->
                    QuoteRow(
                        quote = sq.quote,
                        dbId = sq.dbId,
                        isFavorite = false,
                        onToggleFavorite = {},
                        showFavoriteAction = false,
                        onClick = { onQuoteClick(sq.dbId, sq.quote.number) },
                        showNumber = showNumber,
                        showRating = showRating,
                        onNickClick = onNickClick)
                }
            } else if (!loading && nickA != null && nickB != null) {
                item {
                    Text(
                        "Pick two different nicks to compare.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
    } // LocalSingleDatabaseMode
}

/** Top quotes by rating for one nick (highest first, capped at 5). */
private fun bestQuotes(detail: NickDetail): List<SourcedQuote> =
    detail.quotes
        .filter { it.quote.rating != null }
        .sortedByDescending { it.quote.rating }
        .take(5)

@Composable
private fun NickPicker(
    label: String,
    selected: String?,
    nicks: List<String>,
    exclude: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember(selected) { mutableStateOf(selected ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(query, nicks, exclude) {
        val q = query.trim()
        nicks.filter { n ->
            (q.isEmpty() || n.contains(q, ignoreCase = true)) &&
                !n.equals(exclude, ignoreCase = true)
        }.take(8)
    }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { nick ->
                DropdownMenuItem(
                    text = { Text("<$nick>") },
                    onClick = {
                        onPick(nick)
                        query = nick
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun VersusHeader(a: NickDetail, b: NickDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NickAvatar(nick = a.nick, size = 56.dp)
                Spacer(Modifier.height(4.dp))
                Text("<${a.nick}>", fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Text(
                "VS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NickAvatar(nick = b.nick, size = 56.dp)
                Spacer(Modifier.height(4.dp))
                Text("<${b.nick}>", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HeadToHeadStats(a: NickDetail, b: NickDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            StatDuelRow(
                label = "Quotes",
                aValue = a.quoteCount.toString(),
                bValue = b.quoteCount.toString(),
                aWins = a.quoteCount >= b.quoteCount,
                bWins = b.quoteCount >= a.quoteCount
            )
            StatDuelRow(
                label = "Avg rating",
                aValue = a.avgRating?.let { "%.1f".format(it) } ?: "—",
                bValue = b.avgRating?.let { "%.1f".format(it) } ?: "—",
                aWins = (a.avgRating ?: Double.MIN_VALUE) >= (b.avgRating ?: Double.MIN_VALUE),
                bWins = (b.avgRating ?: Double.MIN_VALUE) >= (a.avgRating ?: Double.MIN_VALUE)
            )
            StatDuelRow(
                label = "Rated quotes",
                aValue = a.ratedCount.toString(),
                bValue = b.ratedCount.toString(),
                aWins = a.ratedCount >= b.ratedCount,
                bWins = b.ratedCount >= a.ratedCount
            )
        }
    }
}

@Composable
private fun StatDuelRow(
    label: String,
    aValue: String,
    bValue: String,
    aWins: Boolean,
    bWins: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            aValue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (aWins && !bWins) FontWeight.Bold else FontWeight.Normal,
            color = if (aWins && !bWins) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
            maxLines = 1
        )
        Text(
            bValue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (bWins && !aWins) FontWeight.Bold else FontWeight.Normal,
            color = if (bWins && !aWins) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(64.dp)
        )
    }
}
