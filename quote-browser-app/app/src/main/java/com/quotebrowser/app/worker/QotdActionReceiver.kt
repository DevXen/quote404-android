package com.quotebrowser.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Favorite
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles the Favorite / Next actions on quote notifications (the random
 * quote pings and the Quote of the Day). Explicit intents only; not
 * exported.
 *
 * - Favorite: adds the quoted item to favorites (no-op when already
 *   saved) and confirms on the notification itself.
 * - Next: picks another visible random quote and reposts the
 *   notification with it.
 * - Share needs no receiver: the notification posts a direct share-sheet
 *   activity intent instead (notification trampolines are blocked on
 *   Android 12+).
 */
class QotdActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_FAVORITE = "com.quotebrowser.app.worker.QOTD_ACTION_FAVORITE"
        const val ACTION_NEXT = "com.quotebrowser.app.worker.QOTD_ACTION_NEXT"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE_PREFIX = "title_prefix"
        const val EXTRA_DB_ID = "db_id"
        const val EXTRA_NUMBER = "number"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val manager = DatabaseManager.getInstance(context.applicationContext)
                when (intent.action) {
                    ACTION_FAVORITE -> onFavorite(context, manager, intent)
                    ACTION_NEXT -> onNext(context, manager, intent)
                }
            } catch (e: Exception) {
                // Leave the notification as-is on failure.
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun onFavorite(
        context: Context,
        manager: DatabaseManager,
        intent: Intent
    ) {
        val dbId = intent.getStringExtra(EXTRA_DB_ID) ?: return
        val number = intent.getIntExtra(EXTRA_NUMBER, -1)
        if (dbId.isEmpty() || number < 0) return
        val quote = try {
            manager.repoFor(dbId).getQuote(number)
        } catch (e: Exception) {
            null
        } ?: return
        try {
            val dao = manager.metaDb.metaDao()
            if (dao.favoriteCount(dbId, number) == 0) {
                dao.addFavorite(Favorite(dbId = dbId, number = number))
            }
        } catch (e: Exception) {
            return
        }
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: QUOTE_CHANNEL_ID
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, QUOTE_NOTIFICATION_ID)
        val prefix = intent.getStringExtra(EXTRA_TITLE_PREFIX) ?: "Quote"
        postFullQuoteNotification(
            context = context,
            channelId = channelId,
            notificationId = notificationId,
            title = "$prefix — #$number",
            titlePrefix = prefix,
            statusLine = "★ Saved to favorites",
            dbId = dbId,
            quote = quote
        )
    }

    private suspend fun onNext(
        context: Context,
        manager: DatabaseManager,
        intent: Intent
    ) {
        // v1.74: the shared ping picker — byte-identical filtering to the
        // initial ping notification (see worker/QuotePingFilter.kt). This
        // now honors the Quote Ping settings (maturity ceiling, work
        // hours, moods/categories, min rating, repeat avoidance) on both
        // the ping and Quote of the Day notifications.
        val picked = pickPingQuote(manager) ?: return
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: QUOTE_CHANNEL_ID
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, QUOTE_NOTIFICATION_ID)
        val prefix = intent.getStringExtra(EXTRA_TITLE_PREFIX) ?: "Random quote"
        postFullQuoteNotification(
            context = context,
            channelId = channelId,
            notificationId = notificationId,
            title = "$prefix — #${picked.second.number}",
            titlePrefix = prefix,
            statusLine = null,
            dbId = picked.first,
            quote = picked.second
        )
        manager.recordPingNotified(picked.first, picked.second.number)
    }
}
