package com.quotebrowser.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Settings card for the daily quote reminder notification. A switch arms
 * an AlarmManager daily alarm (see QuoteReminderScheduler); when enabled,
 * a time button opens a framework TimePickerDialog to set the alarm time.
 * The notification permission is requested on Android 13+ when turning
 * the reminder on — a denial still saves the setting and just shows a
 * note, it never crashes.
 */
@Composable
fun DailyReminderSetting(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    // v1.59: notification quote source ("random_sources" | "favorites").
    // Defaults keep the existing Settings call site compiling until the
    // settings agent wires the new pref through.
    qotdSource: String = "random_sources",
    onQotdSourceChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var permissionNote by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Save the toggle either way so the schedule follows the
            // switch; without the permission the alarm simply can't post.
            permissionNote = !granted
            onEnabledChange(true)
        }

    fun tryEnable() {
        val needsPermission =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionNote = false
            onEnabledChange(true)
        }
    }

    // Framework dialog (like the quote-ping and QotD time pickers) to avoid
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
                "A quote-of-the-day notification, every day at your chosen time.",
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
                        "Daily quote reminder",
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
                        if (on) {
                            tryEnable()
                        } else {
                            permissionNote = false
                            onEnabledChange(false)
                        }
                    }
                )
            }
            if (enabled && permissionNote) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reminder is on, but notifications are blocked — " +
                        "allow them in system settings to see it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            // v1.59: where the daily notification quote comes from.
            Spacer(Modifier.height(8.dp))
            Text(
                "Quote source",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            QotdSourceOption(
                label = "Random sources",
                selected = qotdSource != "favorites",
                onSelect = { onQotdSourceChange("random_sources") }
            )
            QotdSourceOption(
                label = "From my favorites",
                selected = qotdSource == "favorites",
                onSelect = { onQotdSourceChange("favorites") }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "“From my favorites” picks the daily quote from your " +
                    "favorited quotes; when you have none it falls back " +
                    "to random sources.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QotdSourceOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
