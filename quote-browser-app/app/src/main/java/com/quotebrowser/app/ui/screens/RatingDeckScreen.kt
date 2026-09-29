package com.quotebrowser.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.RandomViewModel
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import kotlinx.coroutines.launch

private const val RATE_VOTE_THRESHOLD_DP = 110f

/**
 * Tinder-style rating deck: drag a quote right for thumbs-up, left for
 * thumbs-down (or use the buttons). Votes feed the local re-rating system
 * (the same +/-1 behind the detail screen's thumbs), so the rating badge
 * everywhere reflects your taste.
 */
@Composable
fun RatingDeckScreen(
    randomVm: RandomViewModel,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val cards = remember { mutableStateListOf<SourcedQuote>() }
    var loadingMore by remember { mutableStateOf(false) }
    var rated by remember { mutableStateOf(0) }
    var upVotes by remember { mutableStateOf(0) }
    var downVotes by remember { mutableStateOf(0) }
    val offsetX = remember { Animatable(0f) }

    fun appendMore() {
        if (loadingMore) return
        loadingMore = true
        scope.launch {
            try {
                val batch = randomVm.fetchRandomBatch(25)
                val known = cards.map { it.dbId to it.quote.number }.toSet()
                cards.addAll(batch.filter { (it.dbId to it.quote.number) !in known })
            } finally {
                loadingMore = false
            }
        }
    }

    LaunchedEffect(Unit) { appendMore() }

    fun castVote(vote: Int) {
        val sq = cards.firstOrNull() ?: return
        scope.launch {
            randomVm.rateQuote(sq.dbId, sq.quote.number, vote)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            val target = with(density) {
                (if (vote > 0) 1 else -1) * 1200.dp.toPx()
            }
            try {
                offsetX.animateTo(target, tween(220))
            } catch (_: Exception) {
            }
            cards.removeFirstOrNull()
            offsetX.snapTo(0f)
            rated++
            if (vote > 0) upVotes++ else downVotes++
            if (cards.size < 6) appendMore()
        }
    }

    fun skip() {
        scope.launch {
            val target = with(density) { (-600).dp.toPx() }
            try {
                offsetX.animateTo(target, tween(180))
            } catch (_: Exception) {
            }
            cards.removeFirstOrNull()
            offsetX.snapTo(0f)
            if (cards.size < 6) appendMore()
        }
    }

    val thresholdPx = with(density) { RATE_VOTE_THRESHOLD_DP.dp.toPx() }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Back to Random")
            }
            Text("Rate quotes", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Text(
                "$rated rated · $upVotes up · $downVotes down",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val sq = cards.firstOrNull()
            when {
                sq == null -> CircularProgressIndicator()
                else -> {
                    val drag = offsetX.value
                    val upAlpha = (drag / thresholdPx).coerceIn(0f, 1f)
                    val downAlpha = (-drag / thresholdPx).coerceIn(0f, 1f)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .heightIn(max = 560.dp)
                            .graphicsLayer {
                                translationX = drag
                                rotationZ = (drag / 60f).coerceIn(-8f, 8f)
                            }
                            .pointerInput(sq) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                                    },
                                    onDragEnd = {
                                        scope.launch {
                                            when {
                                                offsetX.value > thresholdPx -> castVote(1)
                                                offsetX.value < -thresholdPx -> castVote(-1)
                                                else -> offsetX.animateTo(0f, tween(200))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                                    }
                                )
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 20.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QuoteNumberBadge(
                                        number = sq.quote.number,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.padding(4.dp))
                                    RatingBadge(sq.quote.rating)
                                    Spacer(Modifier.weight(1f))
                                    randomVm.labelFor(sq.dbId)?.let { label ->
                                        SourceChip(label = label, dbId = sq.dbId)
                                    }
                                }
                                val bodyStyle = scaledTextStyle(
                                    when {
                                        sq.quote.text.length <= 280 -> MaterialTheme.typography.headlineSmall
                                        sq.quote.text.length <= 800 -> MaterialTheme.typography.titleLarge
                                        else -> MaterialTheme.typography.titleMedium
                                    }
                                )
                                Text(
                                    text = rememberNickAnnotatedText(sq.quote.text, bodyStyle),
                                    style = bodyStyle
                                )
                            }
                            // Vote overlays while dragging.
                            if (upAlpha > 0f) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color(0x552E7D32))
                                        .alpha(upAlpha),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.ThumbUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(72.dp)
                                    )
                                }
                            }
                            if (downAlpha > 0f) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color(0x55C62828))
                                        .alpha(downAlpha),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.ThumbDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(72.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { castVote(-1) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.ThumbDown,
                    contentDescription = "Thumbs down",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = { skip() }) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Skip",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = { castVote(1) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.ThumbUp,
                    contentDescription = "Thumbs up",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Text(
            "Drag right for yes, left for no · tap a card to open it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}
