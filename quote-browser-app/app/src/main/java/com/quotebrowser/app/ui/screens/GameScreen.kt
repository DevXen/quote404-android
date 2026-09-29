package com.quotebrowser.app.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.GameViewModel
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.theme.scaledTextStyle

/**
 * Higher-or-lower: two quotes face off — tap the one you think is rated
 * higher. Correct guesses build a streak; the best streak persists.
 */
@Composable
fun GameScreen(vm: GameViewModel) {
    val round by vm.round.collectAsStateWithLifecycle()
    val picked by vm.picked.collectAsStateWithLifecycle()
    val score by vm.score.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val best by vm.bestStreak.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Higher or Lower")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Score: $score", style = MaterialTheme.typography.titleMedium)
            Text("Streak: $streak", style = MaterialTheme.typography.titleMedium)
            Text("Best: $best", style = MaterialTheme.typography.titleMedium)
        }
        val r = round
        when {
            loading && r == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            r == null -> {
                EmptyState("Couldn't find two rated quotes.\nTry changing your random sources.")
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = { vm.nextRound() }) { Text("Retry") }
                }
            }
            else -> {
                val answered = picked != null
                val correct = if (answered) {
                    val other = if (picked == r.a) r.b else r.a
                    (picked!!.quote.rating ?: 0) >= (other.quote.rating ?: 0)
                } else null
                Column(
                    Modifier
                        .fillMaxSize()
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
                    GameCard(
                        sq = r.a,
                        sourceLabel = vm.labelFor(r.a.dbId),
                        revealRating = answered,
                        dimmed = answered && picked != r.a,
                        onPick = { if (!answered) vm.pick(r.a) }
                    )
                    GameCard(
                        sq = r.b,
                        sourceLabel = vm.labelFor(r.b.dbId),
                        revealRating = answered,
                        dimmed = answered && picked != r.b,
                        onPick = { if (!answered) vm.pick(r.b) }
                    )
                    AnimatedVisibility(
                        visible = answered,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = if (correct == true) "Correct! 🎉" else "Wrong — streak reset.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (correct == true) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { vm.nextRound() },
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

@Composable
private fun GameCard(
    sq: SourcedQuote,
    sourceLabel: String,
    revealRating: Boolean,
    dimmed: Boolean,
    onPick: () -> Unit
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
                AnimatedVisibility(visible = revealRating) {
                    RatingBadge(sq.quote.rating)
                }
            }
            Spacer(Modifier.height(8.dp))
            val bodyStyle = scaledTextStyle(MaterialTheme.typography.bodyMedium)
            Text(
                text = rememberNickAnnotatedText(sq.quote.text, bodyStyle),
                style = bodyStyle,
                maxLines = if (revealRating) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
