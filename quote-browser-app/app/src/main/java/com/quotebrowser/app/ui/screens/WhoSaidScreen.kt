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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.WhoSaidViewModel
import com.quotebrowser.app.ui.applyDisplayBreaks
import com.quotebrowser.app.ui.redactedSpansForQuiz
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.theme.scaledTextStyle

/**
 * "Who said it?": a quote is shown, guess which nick said it from four
 * options. Correct guesses build a streak; the best streak persists.
 */
@Composable
fun WhoSaidScreen(vm: WhoSaidViewModel) {
    val round by vm.round.collectAsStateWithLifecycle()
    val picked by vm.picked.collectAsStateWithLifecycle()
    val score by vm.score.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val best by vm.bestStreak.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Who Said It?")
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
                EmptyState("Couldn't find a quote with a known speaker.\nTry again.")
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
                val correct = answered && picked.equals(r.answer, ignoreCase = true)
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
                            }
                            Spacer(Modifier.height(8.dp))
                            val bodyStyle =
                                scaledTextStyle(MaterialTheme.typography.bodyMedium)
                            // Question phase: names are redacted (showing them
                            // would give away the answer) and the target
                            // line is highlighted. After answering, the full
                            // colored quote is revealed.
                            val palette = LocalNickPalette.current
                            val lineHighlight = MaterialTheme.colorScheme.primaryContainer
                            val questionText = remember(
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
                                    // Each "•••" takes its speaker's color so
                                    // players can spot repeat speakers; the
                                    // name itself stays unreadable.
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
                                text = if (answered) rememberNickAnnotatedText(
                                    r.quote.quote.text,
                                    bodyStyle
                                ) else questionText,
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
                        val isAnswer = nick.equals(r.answer, ignoreCase = true)
                        val isPicked = answered && picked.equals(nick, ignoreCase = true)
                        val colors = when {
                            answered && isAnswer -> ButtonDefaults.buttonColors()
                            answered && isPicked -> ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                            else -> null
                        }
                        if (colors != null) {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = colors,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("<$nick>")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { if (!answered) vm.pick(nick) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("<$nick>")
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = answered,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = if (correct) "Correct! 🎉"
                            else "Wrong — it was <${r.answer}>.",
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
