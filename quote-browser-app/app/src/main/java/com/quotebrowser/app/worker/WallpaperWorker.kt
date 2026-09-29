package com.quotebrowser.app.worker

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.QuoteCardStyle
import com.quotebrowser.app.ui.renderQuoteCard
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.flow.first

/**
 * Sets a quote as the device wallpaper once a day. The quote card is
 * rendered with the existing share-card renderer.
 */
class WallpaperWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val manager = DatabaseManager.getInstance(applicationContext)
        return try {
            val ok = WallpaperHelper.applyWallpaper(applicationContext, manager)
            if (ok) Result.success() else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

object WallpaperHelper {

    /**
     * Picks a quote per the wallpaper source setting and sets it as the
     * wallpaper. Returns false when there was nothing usable to set.
     */
    suspend fun applyWallpaper(context: Context, manager: DatabaseManager): Boolean {
        val sourced = pickWallpaperQuote(manager) ?: return false
        val bitmap = renderQuoteCard(
            quote = sourced.quote,
            style = QuoteCardStyle.MIDNIGHT,
            includeNumber = true,
            includeRating = true,
            includeUrl = false,
            ircLineBreaks = manager.ircLineBreaks.value,
            dialogueLineBreaks = manager.dialogueLineBreaks.value
        )
        WallpaperManager.getInstance(context).setBitmap(bitmap)
        if (!bitmap.isRecycled) bitmap.recycle()
        return true
    }

    private suspend fun pickWallpaperQuote(manager: DatabaseManager): SourcedQuote? {
        return when (manager.wallpaperSource.value) {
            "random" -> randomQuote(manager)
            else -> favoriteQuote(manager) ?: randomQuote(manager)
        }
    }

    /** A random favorite from any database, or null when there are none. */
    private suspend fun favoriteQuote(manager: DatabaseManager): SourcedQuote? {
        val all = mutableListOf<SourcedQuote>()
        for (id in manager.allDbIds()) {
            val repo = manager.repoFor(id)
            val favs = try {
                repo.favorites().first()
            } catch (e: Exception) {
                emptyList()
            }
            favs.forEach { q -> all += SourcedQuote(id, q) }
        }
        val visible = all.filter { sq ->
            !manager.isQuoteHiddenFromLists(sq.dbId, sq.quote.number, sq.quote.text)
        }
        return visible.randomOrNull()
    }

    /** A random quote honoring the random-source setting and content filter. */
    private suspend fun randomQuote(manager: DatabaseManager): SourcedQuote? {
        val all = manager.allDbIds()
        val selected = manager.randomSourceIds.first()
        val ids = (if (selected == null) all else all.filter { it in selected })
            .ifEmpty { all }
        if (ids.isEmpty()) return null
        repeat(10) {
            val id = ids.random()
            val q: Quote? = try {
                manager.repoFor(id).randomQuote()
            } catch (e: Exception) {
                null
            }
            if (q != null &&
                !manager.isQuoteHiddenFromLists(id, q.number, q.text)
            ) {
                return SourcedQuote(id, q)
            }
        }
        return null
    }
}
