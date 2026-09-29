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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.quotebrowser.app.ui.RatingGuessViewModel
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.theme.scaledTextStyle

/**
 * "Guess the rating": a rated quote is shown, pick which bucket its rating
 * falls into. Correct guesses build a streak; the best streak persists.
 */
@Composable
fun RatingGuessScreen(vm: RatingGuessViewModel) {
    val round by vm.round.collectAsStateWithLifecycle()
    val picked by vm.picked.collectAsStateWithLifecycle()
    val score by vm.score.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val best by vm.bestStreak.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Guess the Rating")
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
                EmptyState("Couldn't find a rated quote.\nTry changing your random sources.")
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
                val correct = answered && picked == r.answerIndex
                Column(
                    Modifier
                        .fillMaxSize()
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
                                SourceChip(label = vm.labelFor(r.quote.dbId))
                                Spacer(Modifier.weight(1f))
                                AnimatedVisibility(visible = answered) {
                                    RatingBadge(r.quote.quote.rating)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            val bodyStyle =
                                scaledTextStyle(MaterialTheme.typography.bodyMedium)
                            Text(
                                text = rememberNickAnnotatedText(r.quote.quote.text, bodyStyle),
                                style = bodyStyle,
                                maxLines = if (answered) Int.MAX_VALUE else 8,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        "What bucket is its rating in?",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    RatingGuessViewModel.buckets.forEachIndexed { index, label ->
                        val isAnswer = answered && index == r.answerIndex
                        val isWrongPick = answered && picked == index && !isAnswer
                        when {
                            isAnswer -> Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(label) }
                            isWrongPick -> Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(label) }
                            else -> OutlinedButton(
                                onClick = { if (!answered) vm.pick(index) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(label) }
                        }
                    }
                    AnimatedVisibility(
                        visible = answered,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = if (correct) "Correct! 🎉" else "Wrong — streak reset.",
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
