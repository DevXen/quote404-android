package com.quotebrowser.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * v1.50 vacation mode (reading streak freeze).
 *
 * A settings row with a summary subtitle plus a dialog for picking the
 * vacation date range. Days are [LocalDate.toEpochDay] values, consistent
 * with the streak computation in DatabaseManager; end == -1 means
 * "until I turn it off".
 */
@Composable
fun VacationModeSetting(
    enabled: Boolean,
    startDay: Long,
    endDay: Long,
    onEnabledChange: (Boolean) -> Unit,
    onRangeChange: (Long, Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Vacation mode", style = MaterialTheme.typography.bodyLarge)
                Text(
                    vacationSummary(enabled, startDay, endDay),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
    }

    if (showDialog) {
        VacationRangeDialog(
            startDay = startDay,
            endDay = endDay,
            onDismiss = { showDialog = false },
            onConfirm = { start, end ->
                onRangeChange(start, end)
                showDialog = false
            }
        )
    }
}

/** One-line state summary: "Off", "On until you turn it off", or the range. */
private fun vacationSummary(enabled: Boolean, startDay: Long, endDay: Long): String {
    if (!enabled) return "Off"
    if (startDay < 0) return "On — pick your vacation dates"
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    val start = fmt.format(LocalDate.ofEpochDay(startDay))
    return if (endDay < 0) {
        "On until you turn it off, starting $start"
    } else {
        "On: $start – ${fmt.format(LocalDate.ofEpochDay(endDay))}"
    }
}

@Composable
private fun VacationRangeDialog(
    startDay: Long,
    endDay: Long,
    onDismiss: () -> Unit,
    onConfirm: (startDay: Long, endDay: Long) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var start by remember(startDay) {
        mutableStateOf(
            if (startDay >= 0) LocalDate.ofEpochDay(startDay) else today
        )
    }
    var end by remember(endDay) {
        mutableStateOf(
            if (endDay >= 0) LocalDate.ofEpochDay(endDay) else today.plusDays(7)
        )
    }
    var openEnded by remember(endDay) { mutableStateOf(endDay < 0) }

    fun pickDate(initial: LocalDate, onPicked: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onPicked(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vacation dates") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Vacation days don't count for or against your " +
                        "reading streak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DateRow(
                    label = "Start",
                    date = start,
                    onClick = { pickDate(start) { start = it } }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = openEnded,
                        onCheckedChange = { openEnded = it }
                    )
                    Text(
                        "Until I turn it off",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (!openEnded) {
                    DateRow(
                        label = "End",
                        date = end,
                        onClick = { pickDate(end) { picked -> end = picked } }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val realEnd = if (end.isBefore(start)) start else end
                    onConfirm(start.toEpochDay(), if (openEnded) -1L else realEnd.toEpochDay())
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DateRow(label: String, date: LocalDate, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = onClick) {
            Text(DateTimeFormatter.ofPattern("MMM d, yyyy").format(date))
        }
    }
}
