package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Muted nicks manager (under Content Filter): every quote containing a
 * muted nick is hidden at the same enforcement points as "Never show
 * again". "Show anyway" bypasses only the offensive-word filter — muted
 * nicks always stay hidden.
 */
@Composable
fun MutedNicksScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val muted by vm.mutedNicks.collectAsStateWithLifecycle()
    var newNick by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Unmute all nicks?") },
            text = { Text("Quotes from ${muted.size} muted nicks will show up again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearMutedNicks()
                        confirmClear = false
                        toast("All nicks unmuted")
                    }
                ) { Text("Unmute all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier.fillMaxSize()) {
        ScreenHeader(title = "Muted nicks")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = rememberCappedFlingBehavior()) {
            item {
                Text(
                    "Quotes containing any of these nicks are hidden everywhere " +
                        "\"Never show again\" applies. Matching is case-insensitive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newNick,
                        onValueChange = { newNick = it },
                        label = { Text("Nick to mute") },
                        placeholder = { Text("some_nick") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val added = vm.addMutedNick(newNick)
                            toast(
                                if (added) "Muted <${newNick.trim()}>"
                                else "Already muted or blank"
                            )
                            if (added) newNick = ""
                        },
                        enabled = newNick.isNotBlank()
                    ) {
                        Text("Mute")
                    }
                }
            }
            if (muted.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${muted.size} muted",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { confirmClear = true }) {
                            Text("Unmute all")
                        }
                    }
                }
            }
            if (muted.isEmpty()) {
                item {
                    EmptyState("No muted nicks — every speaker shows up.")
                }
            } else {
                items(muted.sorted(), key = { it }) { nick ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "<$nick>",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    vm.removeMutedNick(nick)
                                    toast("Unmuted <$nick>")
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Unmute <$nick>"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
