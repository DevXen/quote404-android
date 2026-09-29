package com.quotebrowser.app.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Daily quote reminder notification, scheduled with AlarmManager as an
 * inexact repeating daily alarm. Armed/canceled from
 * [DatabaseManager.setDailyReminderEnabled] and
 * [DatabaseManager.setDailyReminderTime]; re-armed on boot by
 * [BootReceiver].
 */
private const val DAILY_QUOTE_CHANNEL_ID = "daily_quote"
private const val DAILY_QUOTE_NOTIFICATION_ID = 43
private const val DAILY_QUOTE_REQUEST_CODE = 7401
private const val ACTION_DAILY_QUOTE = "com.quotebrowser.app.worker.DAILY_QUOTE"

private fun dailyQuoteAlarmIntent(context: Context): Intent =
    Intent(context, QuoteReminderReceiver::class.java).setAction(ACTION_DAILY_QUOTE)

private fun dailyQuoteAlarmPendingIntent(context: Context, flags: Int): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        DAILY_QUOTE_REQUEST_CODE,
        dailyQuoteAlarmIntent(context),
        flags
    )

object QuoteReminderScheduler {

    /** Arms (or re-arms) the daily alarm at the stored reminder time. */
    fun schedule(context: Context) {
        val manager = DatabaseManager.getInstance(context.applicationContext ?: context)
        val hour = manager.dailyReminderHour.value
        val minute = manager.dailyReminderMinute.value
        val triggerAt = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            // Today's slot already passed: start tomorrow.
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
        val pending = dailyQuoteAlarmPendingIntent(
            context,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    /** Cancels the daily alarm, if any. */
    fun cancel(context: Context) {
        val pending = dailyQuoteAlarmPendingIntent(
            context,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pending)
    }
}

/**
 * Posts the daily reminder: the Quote of the Day drawn from the same
 * candidate pool and filter-honoring selection as the Home QotD card
 * (see QotdWorker). Tapping the notification opens the quote in the app
 * via the shared full-quote notification builder.
 */
class QuoteReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY_QUOTE) return
        val finished = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = context.applicationContext ?: context
                val manager = DatabaseManager.getInstance(app)
                val picked = pickQuoteOfTheDay(manager) ?: return@launch
                ensureDailyQuoteChannel(app)
                val shortText = picked.second.text
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(120)
                postFullQuoteNotification(
                    context = app,
                    channelId = DAILY_QUOTE_CHANNEL_ID,
                    notificationId = DAILY_QUOTE_NOTIFICATION_ID,
                    title = "Daily quote — #${picked.second.number}",
                    titlePrefix = "Daily quote",
                    statusLine = shortText,
                    dbId = picked.first,
                    quote = picked.second
                )
            } catch (e: Exception) {
                // Never crash an alarm broadcast.
            } finally {
                finished.finish()
            }
        }
    }
}

/**
 * Picks the Quote of the Day the same way QotdWorker does: the user's
 * random sources (or every database), skipping empty databases, a
 * date-seeded database rotation, and [DatabaseManager.isQuoteHiddenFromLists]
 * so hidden/filtered/muted quotes never leak into the notification.
 */
private suspend fun pickQuoteOfTheDay(manager: DatabaseManager): Pair<String, Quote>? {
    // v1.59: optional "from my favorites" notification source (Settings →
    // Daily reminder). Empty favorites fall through to the normal
    // random-sources path.
    if (manager.qotdSource.value == "favorites") {
        pickFavoriteOfTheDay(manager)?.let { return it }
    }
    return try {
        val selected = manager.randomSourceIds.value?.toList()
        val dbs = try {
            manager.availableDatabases().first()
        } catch (e: Exception) {
            emptyList()
        }
        val all = dbs.map { it.id }
        val ids = manager.qotdNonEmptyDbIds(
            (selected ?: listOf(manager.defaultDbId()))
                .filter { it in all }.ifEmpty { all }.sorted()
        )
        if (ids.isEmpty()) return null
        val today = java.time.LocalDate.now()
        val id = ids[
            Math.floorMod(today.toEpochDay(), ids.size.toLong()).toInt()
        ]
        val quote = manager.repoFor(id)
            .visibleQuoteOfTheDay(today) { q ->
                // v1.65: the global maturity ceiling applies to the
                // notification Quote of the Day too.
                manager.isQuoteHiddenFromLists(id, q.number, q.text) ||
                    !manager.isMaturityAllowed(q)
            } ?: return null
        id to quote
    } catch (e: Exception) {
        null
    }
}

/**
 * v1.59: picks the daily notification from the user's favorites. A
 * date-seeded random pick over every visible (not hidden/filtered/muted)
 * favorite across all databases; returns null when there are no visible
 * favorites so the caller falls back to the random-sources path.
 */
private suspend fun pickFavoriteOfTheDay(manager: DatabaseManager): Pair<String, Quote>? {
    return try {
        val dbs = try {
            manager.availableDatabases().first()
        } catch (e: Exception) {
            emptyList()
        }
        val candidates = mutableListOf<Pair<String, Quote>>()
        for (db in dbs) {
            val favs = try {
                manager.repoFor(db.id).favorites().first()
            } catch (e: Exception) {
                continue
            }
            for (q in favs) {
                if (!manager.isQuoteHiddenFromLists(db.id, q.number, q.text)) {
                    candidates.add(db.id to q)
                }
            }
        }
        if (candidates.isEmpty()) return null
        val today = java.time.LocalDate.now()
        candidates[
            Math.floorMod(today.toEpochDay(), candidates.size.toLong()).toInt()
        ]
    } catch (e: Exception) {
        null
    }
}

private fun ensureDailyQuoteChannel(context: Context) {
    val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (mgr.getNotificationChannel(DAILY_QUOTE_CHANNEL_ID) == null) {
        mgr.createNotificationChannel(
            NotificationChannel(
                DAILY_QUOTE_CHANNEL_ID,
                "Daily quote",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}

/**
 * Re-arms the daily reminder alarm after a reboot (RECEIVE_BOOT_COMPLETED)
 * when the reminder is enabled.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        try {
            val app = context.applicationContext ?: context
            val manager = DatabaseManager.getInstance(app)
            if (manager.dailyReminderEnabled.value) {
                QuoteReminderScheduler.schedule(app)
            }
            // v1.78: re-arm the quote alarm too (it is one-shot exact, so it
            // must be re-scheduled after every reboot when enabled).
            if (manager.quoteAlarmEnabled.value) {
                QuoteAlarmScheduler.schedule(app)
            }
        } catch (e: Exception) {
            // Never crash on boot.
        }
    }
}
