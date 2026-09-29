package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.ui.QotdViewModel
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import java.time.format.DateTimeFormatter

/**
 * Dedicated Quote of the Day history: every past daily pick (newest
 * first), computed with the same deterministic rotation as today's quote
 * and the notification worker. Tapping an entry opens the quote.
 */
@Composable
fun QotdHistoryScreen(
    vm: QotdViewModel,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onBack: () -> Unit
) {
    val history by vm.history.collectAsStateWithLifecycle()
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, MMM d") }

    LaunchedEffect(Unit) { vm.refreshHistory(days = 60) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Past quotes of the day",
            actions = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        if (history.isEmpty()) {
            Text(
                text = "Gathering past picks…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.date.toString() + it.dbId }) { e ->
                    val annotated = rememberNickAnnotatedText(
                        e.quote.text,
                        MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onQuoteClick(e.dbId, e.quote.number) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateFmt.format(e.date),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                SourceChip(e.dbLabel, dbId = e.dbId)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(text = annotated)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "#${e.quote.number}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
