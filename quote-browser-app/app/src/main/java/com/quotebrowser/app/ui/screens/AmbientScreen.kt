package com.quotebrowser.app.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.LocalKidSafeEnabled
import com.quotebrowser.app.ui.applyKidSafeMask
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.quoteTextStyle
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.delay

/**
 * v1.53 Ambient mode: edge-to-edge fullscreen rotating quotes. The quote
 * advances every [intervalSec] seconds while playing; tapping pauses, and
 * tapping again advances one quote. The screen stays awake while active
 * and the display follows the active theme.
 */
@Composable
fun AmbientScreen(
    manager: DatabaseManager,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val intervalSec by manager.ambientIntervalSec.collectAsStateWithLifecycle()
    val dbId by manager.currentDbId.collectAsStateWithLifecycle()
    var quotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Keep the screen awake while ambient mode is active.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Load a batch of random quotes; refill when exhausted.
    LaunchedEffect(dbId) {
        quotes = loadAmbientBatch(manager, dbId)
        index = 0
    }
    LaunchedEffect(index, quotes.size, dbId) {
        if (quotes.isNotEmpty() && index >= quotes.size - 3) {
            val more = loadAmbientBatch(manager, dbId)
            if (more.isNotEmpty()) quotes = quotes + more
        }
    }

    // Rotation timer.
    LaunchedEffect(paused, intervalSec, quotes.size) {
        while (!paused && quotes.isNotEmpty()) {
            delay(intervalSec.coerceIn(5, 120) * 1000L)
            if (!paused && quotes.isNotEmpty()) {
                index = (index + 1) % quotes.size
            }
        }
    }

    // Controls fade after a few seconds of playing.
    LaunchedEffect(paused, controlsVisible, index) {
        if (!paused && controlsVisible) {
            delay(4000)
            controlsVisible = false
        }
    }

    val quote = quotes.getOrNull(index)
    val kidSafe = LocalKidSafeEnabled.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Tap: pause while playing; advance one quote while paused.
                    if (paused) {
                        if (quotes.isNotEmpty()) index = (index + 1) % quotes.size
                    } else {
                        paused = true
                    }
                    controlsVisible = true
                }
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (quote != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val bodyStyle = quoteTextStyle(
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 22.sp,
                            lineHeight = 32.sp
                        )
                    )
                    val displayText =
                        if (kidSafe) applyKidSafeMask(quote.text) else quote.text
                    Text(
                        text = rememberNickAnnotatedText(displayText, bodyStyle),
                        style = bodyStyle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "#${quote.number}" +
                            (quote.rating?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Text(
                    text = "Loading quotes…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (controlsVisible) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onClose() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Exit ambient mode",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = {
                        paused = !paused
                        controlsVisible = true
                    }) {
                        Icon(
                            if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (paused) "Resume" else "Pause",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = {
                        if (quotes.isNotEmpty()) index = (index + 1) % quotes.size
                        controlsVisible = true
                    }) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Next quote",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                if (paused) {
                    Text(
                        text = "Paused — tap to advance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

/** One batch of random quotes for the ambient rotation. */
private suspend fun loadAmbientBatch(
    manager: DatabaseManager,
    dbId: String
): List<Quote> = try {
    manager.repoFor(dbId).randomQuotes(25)
} catch (e: Exception) {
    emptyList()
}
