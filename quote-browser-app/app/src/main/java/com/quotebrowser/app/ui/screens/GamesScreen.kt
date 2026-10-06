package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Games hub: every game in one place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onHigherOrLower: () -> Unit,
    onFlashcards: () -> Unit,
    onWhoSaid: () -> Unit,
    onRatingGuess: () -> Unit,
    onQuoteBattle: () -> Unit,
    onDailyChallenge: () -> Unit,
    onFillBlank: () -> Unit,
    onOddOneOut: () -> Unit,
    onSortRating: () -> Unit,
    onGuessDb: () -> Unit,
    onSpeedRound: () -> Unit,
    onTournament: () -> Unit,
    onPassAndPlay: () -> Unit,
    onCompleteQuote: () -> Unit,
    onTrueFalse: () -> Unit,
    onTop100Game: () -> Unit,
    onMemoryMatch: () -> Unit,
    onSurvivor: () -> Unit,
    onLineScramble: () -> Unit,
    onBlitzWhoSaidIt: () -> Unit,
    onRatingGuessSlider: () -> Unit,
    dailySubtitle: String = "",
    quoteBattleEnabled: Boolean = true,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Games") },
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
                .padding(padding),
            flingBehavior = rememberCappedFlingBehavior()) {
            item {
                GameRow(
                    title = "Daily Challenge",
                    subtitle = dailySubtitle.ifEmpty {
                        "Five rounds, one scored attempt per day."
                    },
                    icon = Icons.Filled.Event,
                    onClick = onDailyChallenge
                )
            }
            item {
                GameRow(
                    title = "Higher or lower",
                    subtitle = "Two quotes face off — tap the one rated higher.",
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    onClick = onHigherOrLower
                )
            }
            item {
                GameRow(
                    title = "Flashcards",
                    subtitle = "Tap to reveal each quote; mark the ones you've memorized.",
                    icon = Icons.Filled.Layers,
                    onClick = onFlashcards
                )
            }
            item {
                GameRow(
                    title = "Who said it?",
                    subtitle = "Guess which nick a quote belongs to.",
                    icon = Icons.Filled.Person,
                    onClick = onWhoSaid
                )
            }
            if (quoteBattleEnabled) {
                item {
                    GameRow(
                        title = "Quote battle",
                        subtitle = "Two quotes face off — tap the funnier one. Elo ladder.",
                        icon = Icons.Filled.EmojiEvents,
                        onClick = onQuoteBattle
                    )
                }
            }
            item {
                GameRow(
                    title = "Guess the rating",
                    subtitle = "Pick which bucket a quote's rating falls into.",
                    icon = Icons.Filled.Star,
                    onClick = onRatingGuess
                )
            }
            item {
                GameRow(
                    title = "Fill in the blank",
                    subtitle = "Guess the missing word in each quote.",
                    icon = Icons.Filled.Edit,
                    onClick = onFillBlank
                )
            }
            item {
                GameRow(
                    title = "Odd one out",
                    subtitle = "Three quotes share a speaker — find the odd one.",
                    icon = Icons.Filled.Lightbulb,
                    onClick = onOddOneOut
                )
            }
            item {
                GameRow(
                    title = "Sort by rating",
                    subtitle = "Order four quotes from highest to lowest rated.",
                    icon = Icons.Filled.Sort,
                    onClick = onSortRating
                )
            }
            item {
                GameRow(
                    title = "Guess the database",
                    subtitle = "Which of the archives is this quote from?",
                    icon = Icons.Filled.Storage,
                    onClick = onGuessDb
                )
            }
            item {
                GameRow(
                    title = "Speed round",
                    subtitle = "60 seconds of higher-or-lower. How many can you get?",
                    icon = Icons.Filled.Timer,
                    onClick = onSpeedRound
                )
            }
            item {
                GameRow(
                    title = "Tournament",
                    subtitle = "Eight quotes enter — tap the funnier one until a champion remains.",
                    icon = Icons.Filled.SportsEsports,
                    onClick = onTournament
                )
            }
            item {
                GameRow(
                    title = "Pass & Play",
                    subtitle = "Two players, one phone — take turns picking the higher-rated quote.",
                    icon = Icons.Filled.People,
                    onClick = onPassAndPlay
                )
            }
            item {
                GameRow(
                    title = "Complete the Quote",
                    subtitle = "Read the first half, then pick the real ending.",
                    icon = Icons.Filled.FormatQuote,
                    onClick = onCompleteQuote
                )
            }
            item {
                GameRow(
                    title = "True or False",
                    subtitle = "Did that nick really say this? Ten quick rounds.",
                    icon = Icons.Filled.Help,
                    onClick = onTrueFalse
                )
            }
            item {
                GameRow(
                    title = "Top 100 or Not",
                    subtitle = "Is this quote in a database's Top 100? Yes or no.",
                    icon = Icons.Filled.Star,
                    onClick = onTop100Game
                )
            }
            item {
                GameRow(
                    title = "Memory Match",
                    subtitle = "Flip cards and match six nicks to their quotes.",
                    icon = Icons.Filled.GridOn,
                    onClick = onMemoryMatch
                )
            }
            item {
                GameRow(
                    title = "Survivor",
                    subtitle = "Mixed questions, one wrong answer ends the run.",
                    icon = Icons.Filled.Whatshot,
                    onClick = onSurvivor
                )
            }
            item {
                GameRow(
                    title = "Line scramble",
                    subtitle = "Tap the jumbled lines back into their original order.",
                    icon = Icons.Filled.Shuffle,
                    onClick = onLineScramble
                )
            }
            item {
                GameRow(
                    title = "Blitz who-said-it",
                    subtitle = "60 seconds — name as many redacted speakers as you can.",
                    icon = Icons.Filled.Bolt,
                    onClick = onBlitzWhoSaidIt
                )
            }
            item {
                GameRow(
                    title = "Rating guess",
                    subtitle = "Slide to the quote's true rating — closest wins.",
                    icon = Icons.Filled.Tune,
                    onClick = onRatingGuessSlider
                )
            }
        }
    }
}

@Composable
private fun GameRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Small clickable list row used by hub screens. */
@Composable
fun HubRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
