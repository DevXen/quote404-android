package com.quotebrowser.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.ui.SleepTimerControl

/**
 * Mini player shown above the bottom nav while the read-aloud playlist is
 * active: current position, skip, stop, and the sleep timer.
 */
@Composable
fun PlaylistBar(
    position: Int,
    total: Int,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    playing: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Reading aloud ${position + 1} of $total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onSkip) {
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "Skip to next quote",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onStop) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Stop reading aloud",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            // Sleep timer: when it elapses it stops playback (onStop is
            // playlistVm::stopPlayback at the call site). Counts down only
            // while playing; pauses/resumes with playback.
            SleepTimerControl(
                onExpire = onStop,
                playing = playing
            )
        }
    }
}
