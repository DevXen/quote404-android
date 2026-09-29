package com.quotebrowser.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.FlashcardViewModel
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.theme.scaledTextStyle

/**
 * Flashcards for memorizing quotes: the front shows only the database and
 * quote number, tap to flip and reveal the text. v1.53 memorize mode adds
 * deck sources (Random / Favorites / Category), Got it / Missed it
 * self-scoring, and per-session statistics.
 */
@Composable
fun FlashcardScreen(
    vm: FlashcardViewModel,
    fetchBatch: suspend () -> List<SourcedQuote>,
    catVm: CategoryViewModel? = null
) {
    val card by vm.current.collectAsStateWithLifecycle()
    val revealed by vm.revealed.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val empty by vm.empty.collectAsStateWithLifecycle()
    val memorizedCount by vm.memorizedCount.collectAsStateWithLifecycle()
    val poolSize by vm.poolSize.collectAsStateWithLifecycle()
    val index by vm.index.collectAsStateWithLifecycle()
    val deckSource by vm.deckSource.collectAsStateWithLifecycle()
    val sessionGot by vm.sessionGot.collectAsStateWithLifecycle()
    val sessionMissed by vm.sessionMissed.collectAsStateWithLifecycle()
    val sessionTotal by vm.sessionTotal.collectAsStateWithLifecycle()
    val categoriesState = catVm?.categories?.collectAsStateWithLifecycle()
    val categories: List<Category> = categoriesState?.value ?: emptyList()
    var confirmReset by remember { mutableStateOf(false) }
    // v1.53 scoring mode: simple Got it / Missed it, or the detailed SM-2
    // grades. Simple is the memorize-mode default.
    var simpleScoring by remember { mutableStateOf(true) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    LaunchedEffect(vm) { vm.loadPool(fetchBatch) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Flashcards") {
            if (poolSize > 0) {
                Text(
                    "${index + 1} / $poolSize",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // v1.53 memorize deck source: Random / Favorites / Category.
        DeckSourceRow(
            selected = deckSource,
            categories = categories,
            onSelectRandom = { vm.loadPool(fetchBatch) },
            onSelectFavorites = { vm.loadFavoritesPool() },
            onSelectCategory = { vm.loadCategoryPool(it) },
            showCategoryPicker = showCategoryPicker,
            onCategoryPickerChange = { showCategoryPicker = it }
        )
        // v1.53 scoring mode toggle.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Scoring:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterChip(
                selected = simpleScoring,
                onClick = { simpleScoring = true },
                label = { Text("Got it / Missed it") }
            )
            FilterChip(
                selected = !simpleScoring,
                onClick = { simpleScoring = false },
                label = { Text("Detailed") }
            )
        }
        // v1.53 session statistics.
        val sessionDone = sessionGot + sessionMissed
        Text(
            text = if (sessionTotal > 0) {
                "Session: $sessionGot got · $sessionMissed missed" +
                    (if (sessionDone >= sessionTotal && sessionTotal > 0)
                        " — done!" else "")
            } else {
                "Learned: $memorizedCount"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        val c = card
        when {
            loading && c == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            c == null || empty -> {
                EmptyState("No cards left.\nMemorize more or reset the set.")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { vm.resetMemorized(fetchBatch) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Reset progress") }
                    OutlinedButton(
                        onClick = { vm.reshuffle() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Reshuffle") }
                }
            }
            else -> {
                val rotation by animateFloatAsState(
                    targetValue = if (revealed) 180f else 0f,
                    label = "cardFlip"
                )
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        onClick = { if (revealed) vm.next() else vm.reveal() }
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = if (revealed) 180f else 0f },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!revealed) {
                                // Front: database + quote number only.
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = vm.labelFor(c.dbId),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    QuoteNumberBadge(number = c.quote.number)
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "Tap to reveal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Back: the quote itself.
                                Column(Modifier.padding(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        QuoteNumberBadge(number = c.quote.number)
                                        Spacer(Modifier.width(4.dp))
                                        RatingBadge(c.quote.rating)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    val bodyStyle = scaledTextStyle(
                                        MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = rememberNickAnnotatedText(
                                            c.quote.text, bodyStyle
                                        ),
                                        style = bodyStyle
                                    )
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = revealed,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        // Spaced-repetition grades (SM-2): Again / Hard / Good / Easy.
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (simpleScoring) {
                                // v1.53 Got it / Missed it self-scoring.
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { vm.gradeSimple(false) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Text(
                                            "Missed it",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    Button(
                                        onClick = { vm.gradeSimple(true) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Text(
                                            "Got it",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val grades = listOf(
                                        "Again" to 0,
                                        "Hard" to 3,
                                        "Good" to 4,
                                        "Easy" to 5
                                    )
                                    grades.forEach { (label, q) ->
                                        OutlinedButton(
                                            onClick = { vm.grade(q) },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Text(label, style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                }
                            }
                            Button(
                                onClick = { vm.next() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Next card")
                            }
                        }
                    }
                    if (!revealed) {
                        Button(
                            onClick = { vm.reveal() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reveal")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.reshuffle() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reshuffle")
                        }
                        TextButton(
                            onClick = { confirmReset = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset progress")
                        }
                    }
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("Clears spaced-repetition progress for the $memorizedCount learned flashcard${if (memorizedCount == 1) "" else "s"}.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetMemorized(fetchBatch)
                    confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * v1.53 memorize deck source picker: Random mix, Favorites, or one
 * category (current database's categories).
 */
@Composable
private fun DeckSourceRow(
    selected: FlashcardViewModel.DeckSource,
    categories: List<Category>,
    onSelectRandom: () -> Unit,
    onSelectFavorites: () -> Unit,
    onSelectCategory: (Long) -> Unit,
    showCategoryPicker: Boolean,
    onCategoryPickerChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Deck:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilterChip(
            selected = selected == FlashcardViewModel.DeckSource.RANDOM,
            onClick = onSelectRandom,
            label = { Text("Random") }
        )
        FilterChip(
            selected = selected == FlashcardViewModel.DeckSource.FAVORITES,
            onClick = onSelectFavorites,
            label = { Text("Favorites") }
        )
        Box {
            FilterChip(
                selected = selected == FlashcardViewModel.DeckSource.CATEGORY,
                onClick = { onCategoryPickerChange(true) },
                label = { Text("Category") }
            )
            DropdownMenu(
                expanded = showCategoryPicker,
                onDismissRequest = { onCategoryPickerChange(false) }
            ) {
                if (categories.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No categories yet") },
                        onClick = { onCategoryPickerChange(false) },
                        enabled = false
                    )
                }
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text("${cat.name} (${cat.quoteCount})") },
                        onClick = {
                            onCategoryPickerChange(false)
                            onSelectCategory(cat.id)
                        }
                    )
                }
            }
        }
    }
}
