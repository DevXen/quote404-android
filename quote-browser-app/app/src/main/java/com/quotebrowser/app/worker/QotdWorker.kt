package com.quotebrowser.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first

private const val CHANNEL_ID = "quote_of_the_day"
private const val NOTIFICATION_ID = 42

/**
 * Posts the Quote of the Day as a notification. Runs daily, first run
 * scheduled for 9:00am. Tapping the notification opens the quote in the app.
 */
class QotdWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val sourced = try {
            val manager = DatabaseManager.getInstance(applicationContext)
            val selected = manager.randomSourceIds.first()?.toList()
            val dbs = try {
                manager.availableDatabases().first()
            } catch (e: Exception) {
                emptyList()
            }
            val all = dbs.map { it.id }
            // Same candidate pool as the Home card: the user's sources (or
            // every database), skipping empty ones so the rotation never
            // lands on an empty pool.
            val ids = manager.qotdNonEmptyDbIds(
                (selected ?: listOf(manager.defaultDbId()))
                    .filter { it in all }.ifEmpty { all }.sorted()
            )
            if (ids.isEmpty()) return Result.success()
            // Deterministic per day: the same database all day, rotating daily.
            val today = java.time.LocalDate.now()
            val id = ids[
                Math.floorMod(today.toEpochDay(), ids.size.toLong()).toInt()
            ]
            id to manager.repoFor(id)
                .visibleQuoteOfTheDay(today) { q ->
                    // v1.65: the global maturity ceiling applies to the
                    // notification Quote of the Day too.
                    manager.isQuoteHiddenFromLists(id, q.number, q.text) ||
                        !manager.isMaturityAllowed(q)
                }
        } catch (e: Exception) {
            null
        } ?: return Result.success()
        val quote = sourced.second ?: return Result.success()
        postQotdNotification(applicationContext, sourced.first, quote)
        return Result.success()
    }
}

private fun ensureChannel(context: Context) {
    val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
        mgr.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Quote of the Day",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}

private fun postQotdNotification(context: Context, dbId: String, quote: Quote) {
    if (!hasNotificationPermission(context)) return
    ensureChannel(context)
    postFullQuoteNotification(
        context = context,
        channelId = CHANNEL_ID,
        notificationId = NOTIFICATION_ID,
        title = "Quote of the Day — #${quote.number}",
        titlePrefix = "Quote of the Day",
        statusLine = null,
        dbId = dbId,
        quote = quote
    )
}
