package com.quotebrowser.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/**
 * Read-aloud sleep timer state: the chosen duration (minutes, 0 = Off)
 * and the live countdown. The choice is persisted; the countdown itself
 * is session-local.
 */
class SleepTimerState(
    initialMinutes: Int,
    private val onChoiceChanged: (Int) -> Unit
) {
    companion object {
        /** Selectable durations in minutes; 0 = Off. */
        val OPTIONS = listOf(0, 15, 30, 60)
    }

    var minutes by mutableStateOf(initialMinutes)
        private set

    var remainingMs by mutableStateOf(0L)
        private set

    init {
        remainingMs = initialMinutes * 60_000L
    }

    val isSet: Boolean get() = minutes > 0
    val isExpired: Boolean get() = isSet && remainingMs <= 0

    /**
     * Picks a new duration: resets the countdown to the full duration;
     * choosing Off (0) cancels any running countdown. Persists the choice.
     */
    fun choose(min: Int) {
        minutes = min
        remainingMs = min * 60_000L
        onChoiceChanged(min)
    }

    /** Advances the countdown by one second. */
    fun tick() {
        if (remainingMs > 0) remainingMs = (remainingMs - 1_000L).coerceAtLeast(0)
    }
}

private const val SLEEP_TIMER_PREFS = "sleep_timer"
private const val KEY_SLEEP_MINUTES = "sleep_minutes"

/**
 * Sleep timer state backed by SharedPreferences: the chosen duration
 * survives app restarts (default Off); the live countdown does not.
 */
@Composable
fun rememberSleepTimerState(): SleepTimerState {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(SLEEP_TIMER_PREFS, Context.MODE_PRIVATE)
    }
    return remember {
        SleepTimerState(prefs.getInt(KEY_SLEEP_MINUTES, 0)) { min ->
            prefs.edit().putInt(KEY_SLEEP_MINUTES, min).apply()
        }
    }
}

/**
 * Compact sleep-timer picker for the read-aloud mini player. Shows the
 * current setting ("Sleep: Off") or the live countdown ("Sleep: 12:34");
 * tapping opens the Off/15/30/60 choices. The countdown only runs while
 * [playing] is true (pausing playback pauses the timer; resuming resumes
 * it), and [onExpire] is called once when it elapses.
 */
@Composable
fun SleepTimerControl(
    onExpire: () -> Unit,
    playing: Boolean,
    modifier: Modifier = Modifier
) {
    val timer = rememberSleepTimerState()
    var expanded by remember { mutableStateOf(false) }

    // One-second ticks gated on playback. When the timer elapses, stop
    // playback and fall back to Off so it fires only once.
    LaunchedEffect(playing, timer.isSet) {
        while (playing && timer.isSet && !timer.isExpired) {
            delay(1_000L)
            timer.tick()
        }
        if (timer.isExpired) {
            onExpire()
            timer.choose(0)
        }
    }

    val label = if (timer.isSet) "Sleep: ${formatSleepRemaining(timer.remainingMs)}"
    else "Sleep: Off"
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = "Sleep timer",
                tint = contentColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SleepTimerState.OPTIONS.forEach { min ->
                DropdownMenuItem(
                    text = { Text(if (min == 0) "Off" else "$min min") },
                    onClick = { timer.choose(min); expanded = false },
                    leadingIcon = {
                        if (min == timer.minutes) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

/** Formats a millisecond countdown as m:ss (or h:mm:ss when long). */
private fun formatSleepRemaining(ms: Long): String {
    val totalSeconds = (ms / 1_000L).coerceAtLeast(0)
    val h = totalSeconds / 3_600L
    val m = (totalSeconds % 3_600L) / 60L
    val s = totalSeconds % 60L
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%d:%02d".format(m, s)
}
