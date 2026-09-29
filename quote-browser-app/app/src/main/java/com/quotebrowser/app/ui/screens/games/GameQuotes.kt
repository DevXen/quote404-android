package com.quotebrowser.app.ui.screens.games

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.repoFor

/**
 * Draws [n] random quotes across all real databases, honoring the user's
 * hidden/muted quotes, the content filter, and the maturity ceiling.
 * Over-fetches per database (shuffled) so a heavily-filtered library
 * still fills the request.
 */
suspend fun DatabaseManager.gameQuotes(n: Int): List<SourcedQuote> {
    if (n <= 0) return emptyList()
    val ids = try {
        allDbIds().filter { it != DatabaseManager.ALL_DBS_ID }
    } catch (e: Exception) {
        emptyList()
    }
    if (ids.isEmpty()) return emptyList()
    val filterOn = contentFilterEnabled.value
    val perDb = ((n * 4) / ids.size + 25).coerceIn(10, 400)
    val out = mutableListOf<SourcedQuote>()
    for (id in ids.shuffled()) {
        try {
            repoFor(id).randomQuotes(perDb)
                .filter { q ->
                    !isQuoteHiddenOrMuted(id, q.number, q.text) &&
                        !(filterOn && isHiddenByContentFilter(q.text)) &&
                        // v1.66: the global maturity ceiling applies to
                        // games too (tournament entrants included).
                        isMaturityAllowed(q)
                }
                .forEach { q -> out.add(SourcedQuote(id, q)) }
        } catch (e: Exception) {
            // Unreadable database — skip it.
        }
        if (out.size >= n * 2) break
    }
    return out.shuffled().take(n)
}

/**
 * Small source-database label for game question cards ("Bash.org",
 * "qdb.us"). Shown only when the game quote pool spans more than one
 * database; hidden for single-database pools and never used in the
 * Guess-the-database game (it would give away the answer).
 */
@Composable
fun GameSourceLabel(
    dbId: String,
    manager: DatabaseManager,
    modifier: Modifier = Modifier
) {
    val sourceIds by manager.randomSourceIds.collectAsStateWithLifecycle()
    val databases by manager.availableDatabases()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val poolSize = sourceIds?.size ?: databases.size
    if (poolSize <= 1) return
    Text(
        text = manager.shortLabel(dbId),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
