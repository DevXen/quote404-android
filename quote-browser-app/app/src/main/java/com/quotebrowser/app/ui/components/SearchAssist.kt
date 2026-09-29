package com.quotebrowser.app.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.data.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * v1.73: mic button for voice search. Uses the system speech recognizer
 * activity, so no RECORD_AUDIO permission is needed in our own manifest
 * (the recognizer app handles its own). Calls [onResult] with the top
 * transcription; [onUnavailable] when no recognizer is installed.
 */
@Composable
fun VoiceSearchButton(
    onResult: (String) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let(onResult)
        }
    }
    IconButton(
        onClick = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your search")
            }
            try {
                launcher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                onUnavailable()
            }
        },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Mic, contentDescription = "Voice search")
    }
}

/**
 * v1.73: autocomplete dropdown under a search field. Shows matching nicks
 * from the leaderboard (tap = open that person's page) and matching
 * categories (tap = filter to it and search). Appears once two characters
 * are typed; hidden when there are no matches.
 */
@Composable
fun QueryAutocomplete(
    query: String,
    manager: DatabaseManager,
    categories: List<Category>,
    onNickClick: (String) -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    var nicks by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    LaunchedEffect(Unit) {
        nicks = withContext(Dispatchers.IO) {
            try {
                manager.topNicks(50)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    val q = query.trim().lowercase()
    val nickHits = remember(q, nicks) {
        if (q.length < 2) emptyList()
        else nicks.filter { it.first.lowercase().contains(q) }.take(3)
    }
    val catHits = remember(q, categories) {
        if (q.length < 2) emptyList()
        else categories.filter { it.name.lowercase().contains(q) }.take(3)
    }
    if (nickHits.isEmpty() && catHits.isEmpty()) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            nickHits.forEach { (nick, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNickClick(nick) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        nick,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$count",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            catHits.forEach { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        cat.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cat.quoteCount > 0) {
                        Text(
                            "${cat.quoteCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
