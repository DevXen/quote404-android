package com.quotebrowser.app.ui.components

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.worker.QuoteAlarmScheduler

/**
 * v1.78 Settings card for the quote alarm. A switch arms a one-shot
 * exact AlarmManager alarm (see QuoteAlarmScheduler); when it fires, a
 * foreground service reads a random favorite quote aloud — even with
 * the app closed. When enabled, a time button opens a framework
 * TimePickerDialog to set the alarm time.
 *
 * On Android 12+, exact alarms need the user's "Alarms & reminders"
 * special-app-access grant; without it the alarm still works but is
 * inexact, and the card says so plainly instead of failing silently.
 */
@Composable
fun QuoteAlarmSetting(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    var exactDeniedNote by remember { mutableStateOf(false) }

    // Re-check whenever the toggle changes (and once on entry) so the
    // note appears as soon as the user returns from system settings.
    LaunchedEffect(enabled) {
        exactDeniedNote =
            enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !QuoteAlarmScheduler.canScheduleExactAlarms(context)
    }

    // Framework dialog (like the daily reminder time picker) to avoid
    // Material3 picker complexity.
    val timeLabel = remember(hour, minute, context) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
        }
        android.text.format.DateFormat.getTimeFormat(context).format(cal.time)
    }
    val timeDialog = remember(hour, minute, context) {
        android.app.TimePickerDialog(
            context,
            { _, h, m -> onTimeChange(h, m) },
            hour,
            minute,
            android.text.format.DateFormat.is24HourFormat(context)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "At your chosen time each day, your phone reads a random " +
                    "favorite quote aloud — even with the app closed. " +
                    "No favorites yet? It reads a random quote instead.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Quote alarm",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (enabled) {
                        Text(
                            "Every day at $timeLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (enabled) {
                    TextButton(onClick = { timeDialog.show() }) {
                        Text(timeLabel)
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        onEnabledChange(on)
                    }
                )
            }
            if (enabled && exactDeniedNote) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Quote404 isn't allowed exact alarms on this " +
                        "device, so the alarm may sound a few minutes " +
                        "off. Allow it in system Settings → Apps → " +
                        "Quote404 → Alarms & reminders for exact timing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
