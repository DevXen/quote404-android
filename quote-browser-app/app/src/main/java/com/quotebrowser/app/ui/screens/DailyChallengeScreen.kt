package com.quotebrowser.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.DAILY_CHALLENGE_ROUNDS
import com.quotebrowser.app.ui.DailyChallengeViewModel
import com.quotebrowser.app.ui.DailyRound
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.applyDisplayBreaks
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.redactedSpansForQuiz
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle

/**
 * Daily Challenge: five date-seeded rounds (a mix of "Who said it?" and
 * "Higher or lower"). One scored attempt per day; replays after completion
 * are unscored practice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    vm: DailyChallengeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completed by vm.completedToday.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val rounds by vm.rounds.collectAsStateWithLifecycle()
    val index by vm.index.collectAsStateWithLifecycle()
    val score by vm.score.collectAsStateWithLifecycle()
    val finished by vm.finished.collectAsStateWithLifecycle()
    val scored by vm.scored.collectAsStateWithLifecycle()
    val lastScore by vm.lastScore.collectAsStateWithLifecycle()
    val bestScore by vm.bestScore.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenge") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> {
                    Spacer(Modifier.height(64.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                rounds == null -> {
                    DailyStartCard(
                        completed = completed,
                        lastScore = lastScore,
                        bestScore = bestScore,
                        streak = streak,
                        onStart = { vm.start(scored = true) },
                        onPractice = { vm.start(scored = false) }
                    )
                }
                finished -> {
                    DailyResultCard(
                        score = score,
                        total = rounds?.size ?: DAILY_CHALLENGE_ROUNDS,
                        scored = scored,
                        bestScore = bestScore,
                        streak = streak,
                        onPractice = { vm.start(scored = false) },
                        onBack = onBack
                    )
                }
                else -> {
                    val round = rounds!![index]
                    LinearProgressIndicator(
                        progress = { (index + 1).toFloat() / rounds!!.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Round ${index + 1} of ${rounds!!.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Score: $score",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!scored) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "practice",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    when (round) {
                        is DailyRound.WhoSaid -> DailyWhoSaidRound(
                            vm = vm,
                            round = round,
                            isLast = index == rounds!!.size - 1
                        )
                        is DailyRound.HigherLower -> DailyHigherLowerRound(
                            vm = vm,
                            round = round,
                            isLast = index == rounds!!.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyStartCard(
    completed: Boolean,
    lastScore: Int,
    bestScore: Int,
    streak: Int,
    onStart: () -> Unit,
    onPractice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            if (completed) "Today's challenge: done ✅"
            else "Today's challenge",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "$DAILY_CHALLENGE_ROUNDS rounds — a mix of \"Who said it?\" and " +
                "\"Higher or lower\", same for everyone today.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (completed) {
            Text(
                "You scored $lastScore/$DAILY_CHALLENGE_ROUNDS. " +
                    "Come back tomorrow for a new one — or practice " +
                    "today's rounds again, unscored.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        if (!completed) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start today's challenge")
            }
        } else {
            OutlinedButton(
                onClick = onPractice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Practice today's rounds (unscored)")
            }
        }
        if (bestScore > 0 || streak > 0) {
            Text(
                "Best score: $bestScore/$DAILY_CHALLENGE_ROUNDS • " +
                    "Day streak: $streak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyResultCard(
    score: Int,
    total: Int,
    scored: Boolean,
    bestScore: Int,
    streak: Int,
    onPractice: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "$score/$total",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            when {
                !scored -> "Practice round — not recorded."
                score == total -> "Perfect! Come back tomorrow. 🎉"
                else -> "Recorded for today. Come back tomorrow for a new challenge."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (scored) {
            Text(
                "Best score: $bestScore/$total • Day streak: $streak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onPractice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Practice today's rounds (unscored)")
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun DailyWhoSaidRound(
    vm: DailyChallengeViewModel,
    round: DailyRound.WhoSaid,
    isLast: Boolean
) {
    val picked by vm.pickedNick.collectAsStateWithLifecycle()
    val answered = picked != null
    val correct = answered && picked.equals(round.answer, ignoreCase = true)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val bodyStyle = scaledTextStyle(MaterialTheme.typography.bodyMedium)
        val palette = LocalNickPalette.current
        val lineHighlight = MaterialTheme.colorScheme.primaryContainer
        val questionText = remember(
            round.quote.quote.text, round.answer, palette, lineHighlight
        ) {
            val display = applyDisplayBreaks(
                round.quote.quote.text,
                palette.ircLineBreaks,
                palette.dialogueLineBreaks
            )
            val (redacted, spans, line) =
                redactedSpansForQuiz(display, round.answer, palette)
            buildAnnotatedString {
                append(redacted)
                // Each "•••" takes its speaker's color so players can
                // spot repeat speakers; the name itself stays unreadable.
                for (s in spans) {
                    addStyle(
                        SpanStyle(color = s.color),
                        s.start, s.endExcl
                    )
                }
                if (line != null) {
                    addStyle(
                        androidx.compose.ui.text.SpanStyle(
                            background = lineHighlight
                        ),
                        line.first, line.second
                    )
                }
            }
        }
        Text(
            text = if (answered) rememberNickAnnotatedText(
                round.quote.quote.text, bodyStyle
            ) else questionText,
            style = bodyStyle,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Who said it?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        round.options.forEach { nick ->
            val isAnswer = nick.equals(round.answer, ignoreCase = true)
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
                    onClick = { if (!answered) vm.answerWhoSaid(nick) },
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
                else "Wrong — it was <${round.answer}>.",
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
            Text(
                when {
                    !answered -> "Skip"
                    isLast -> "Finish"
                    else -> "Next round"
                }
            )
        }
    }
}

@Composable
private fun DailyHigherLowerRound(
    vm: DailyChallengeViewModel,
    round: DailyRound.HigherLower,
    isLast: Boolean
) {
    val picked by vm.pickedQuote.collectAsStateWithLifecycle()
    val answered = picked != null
    val correct = if (answered) {
        val other = if (picked == round.a) round.b else round.a
        (picked!!.quote.rating ?: 0) >= (other.quote.rating ?: 0)
    } else null
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Which quote is rated higher?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        DailyHigherLowerCard(
            sq = round.a,
            sourceLabel = vm.labelFor(round.a.dbId),
            revealRating = answered,
            dimmed = answered && picked != round.a,
            onPick = { if (!answered) vm.answerHigherLower(round.a) }
        )
        DailyHigherLowerCard(
            sq = round.b,
            sourceLabel = vm.labelFor(round.b.dbId),
            revealRating = answered,
            dimmed = answered && picked != round.b,
            onPick = { if (!answered) vm.answerHigherLower(round.b) }
        )
        AnimatedVisibility(
            visible = answered,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = if (correct == true) "Correct! 🎉" else "Wrong!",
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
            Text(
                when {
                    !answered -> "Skip"
                    isLast -> "Finish"
                    else -> "Next round"
                }
            )
        }
    }
}

@Composable
private fun DailyHigherLowerCard(
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
