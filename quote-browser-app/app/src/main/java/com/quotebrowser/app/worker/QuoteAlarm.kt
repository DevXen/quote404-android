package com.quotebrowser.app.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * v1.78 Quote alarm: at the user's chosen time each day the phone reads
 * a random favorited quote aloud — falling back to a random quote from
 * all databases when there are no favorites — even with the app closed.
 *
 * The alarm is one-shot exact (setExactAndAllowWhileIdle) so it fires at
 * the right minute in Doze; the receiver re-arms it for the next day
 * before doing anything else, so a failure never kills the schedule.
 * Exact alarms can start a foreground service from the background, so
 * the receiver hands the quote to [QuoteAlarmService], which speaks it
 * via the system TextToSpeech engine under a wake lock. A Stop action
 * on the notification ends the speech early.
 *
 * When the user hasn't granted "Alarms & reminders" (Android 12+), the
 * scheduler falls back to an inexact alarm and the Settings UI says so.
 */
private const val QUOTE_ALARM_CHANNEL_ID = "quote_alarm"
private const val QUOTE_ALARM_NOTIFICATION_ID = 44
private const val QUOTE_ALARM_REQUEST_CODE = 7402
private const val QUOTE_ALARM_STOP_REQUEST_CODE = 7403
private const val ACTION_QUOTE_ALARM = "com.quotebrowser.app.worker.QUOTE_ALARM"

private fun quoteAlarmIntent(context: Context): Intent =
    Intent(context, QuoteAlarmReceiver::class.java).setAction(ACTION_QUOTE_ALARM)

private fun quoteAlarmPendingIntent(context: Context, flags: Int): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        QUOTE_ALARM_REQUEST_CODE,
        quoteAlarmIntent(context),
        flags
    )

object QuoteAlarmScheduler {

    /** Arms (or re-arms) the one-shot quote alarm at the stored time. */
    fun schedule(context: Context) {
        val manager = DatabaseManager.getInstance(context.applicationContext ?: context)
        val hour = manager.quoteAlarmHour.value
        val minute = manager.quoteAlarmMinute.value
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
        val pending = quoteAlarmPendingIntent(
            context,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (canScheduleExactAlarms(context)) {
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pending
            )
        } else {
            // Inexact fallback — fires within a few minutes of the time.
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pending
            )
        }
    }

    /** Cancels the quote alarm, if any. */
    fun cancel(context: Context) {
        val pending = quoteAlarmPendingIntent(
            context,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pending)
    }

    /**
     * Whether we may schedule exact alarms. Below Android 12 this is
     * always true; on 12+ it reflects the user's "Alarms & reminders"
     * special-app-access grant.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.canScheduleExactAlarms()
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Fires the quote alarm: re-arms tomorrow's alarm first, picks the quote,
 * then starts [QuoteAlarmService] to speak it. If the foreground service
 * can't start, posts the quote as a plain notification so the alarm
 * still surfaces something.
 */
class QuoteAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_QUOTE_ALARM) return
        val finished = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = context.applicationContext ?: context
                val manager = DatabaseManager.getInstance(app)
                // Defensive: never speak when the alarm is off (e.g. a
                // stale pending intent firing after the toggle was flipped).
                if (!manager.quoteAlarmEnabled.value) return@launch
                // Re-arm tomorrow before anything else can fail.
                try {
                    QuoteAlarmScheduler.schedule(app)
                } catch (e: Exception) {
                    // Keep going — today's alarm still fires.
                }
                val picked = pickAlarmQuote(manager) ?: return@launch
                val serviceIntent = Intent(app, QuoteAlarmService::class.java).apply {
                    action = QuoteAlarmService.ACTION_SPEAK
                    putExtra(
                        QuoteAlarmService.EXTRA_QUOTE_TITLE,
                        "Quote alarm — #${picked.second.number}"
                    )
                    putExtra(
                        QuoteAlarmService.EXTRA_QUOTE_TEXT,
                        picked.second.text.trim()
                    )
                }
                try {
                    ContextCompat.startForegroundService(app, serviceIntent)
                } catch (e: Exception) {
                    // Last resort: at least show the quote.
                    postQuoteAlarmFallbackNotification(app, picked.second)
                }
            } catch (e: Exception) {
                // Never crash an alarm broadcast.
            } finally {
                finished.finish()
            }
        }
    }
}

/**
 * Picks the alarm quote: a uniform random pick over every favorited
 * quote across all databases (resolved through its own repository so
 * the text is current). Falls back to a random quote from a random
 * non-empty database when there are no favorites. Returns null only
 * when no quote could be found at all.
 */
private suspend fun pickAlarmQuote(manager: DatabaseManager): Pair<String, Quote>? {
    try {
        val entries = manager.metaDb.metaDao().allFavoriteEntries().first()
        val resolved = entries.mapNotNull { e ->
            try {
                manager.repoFor(e.dbId).getQuote(e.number)?.let { e.dbId to it }
            } catch (e2: Exception) {
                null
            }
        }
        if (resolved.isNotEmpty()) return resolved.random()
    } catch (e: Exception) {
        // Fall through to the random-quote path.
    }
    return try {
        val ids = manager.availableDatabases().first().map { it.id }.shuffled()
        for (id in ids) {
            try {
                manager.repoFor(id).randomQuote()?.let { return id to it }
            } catch (e: Exception) {
                continue
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}

private fun ensureQuoteAlarmChannel(context: Context) {
    val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (mgr.getNotificationChannel(QUOTE_ALARM_CHANNEL_ID) == null) {
        val channel = NotificationChannel(
            QUOTE_ALARM_CHANNEL_ID,
            "Quote alarm",
            NotificationManager.IMPORTANCE_HIGH
        )
        // The speech itself is the alert; no extra notification sound.
        channel.setSound(null, null)
        mgr.createNotificationChannel(channel)
    }
}

/** Last-resort path when the speech service can't start: show the quote. */
private fun postQuoteAlarmFallbackNotification(context: Context, quote: Quote) {
    try {
        ensureQuoteAlarmChannel(context)
        val shortText = quote.text
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val notification = NotificationCompat.Builder(context, QUOTE_ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Quote alarm — #${quote.number}")
            .setContentText(shortText.take(200))
            .setStyle(NotificationCompat.BigTextStyle().bigText(shortText))
            .setAutoCancel(true)
            .build()
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(QUOTE_ALARM_NOTIFICATION_ID, notification)
    } catch (e: Exception) {
        // Nothing more we can do.
    }
}

/**
 * Foreground service that speaks the alarm quote with the system
 * TextToSpeech engine. Holds a partial wake lock while speaking so the
 * device doesn't sleep mid-sentence, and exposes a Stop action on its
 * notification. Stops itself when the utterance finishes, errors, or a
 * watchdog fires — it never lingers.
 */
class QuoteAlarmService : Service() {

    companion object {
        const val ACTION_SPEAK = "com.quotebrowser.app.worker.QUOTE_ALARM_SPEAK"
        const val ACTION_STOP = "com.quotebrowser.app.worker.QUOTE_ALARM_STOP"
        const val EXTRA_QUOTE_TITLE = "quote_title"
        const val EXTRA_QUOTE_TEXT = "quote_text"
        private const val UTTERANCE_ID = "quote_alarm_utterance"
        private const val MAX_SPEAK_MS = 5 * 60 * 1000L
    }

    private var tts: TextToSpeech? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var watchdog: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "QuoteBrowser:QuoteAlarm"
            ).apply { acquire(MAX_SPEAK_MS) }
        } catch (e: Exception) {
            // Speech still works; the device just might sleep on very
            // long quotes.
        }
        ensureQuoteAlarmChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }
        val text = intent?.getStringExtra(EXTRA_QUOTE_TEXT)?.trim().orEmpty()
        val title = intent?.getStringExtra(EXTRA_QUOTE_TITLE) ?: "Quote alarm"
        // startForeground must happen immediately — before any async work.
        startForeground(QUOTE_ALARM_NOTIFICATION_ID, buildNotification(title, text))
        if (text.isBlank()) {
            stopAlarm()
            return START_NOT_STICKY
        }
        // Watchdog: never let a stuck TTS engine keep the service alive.
        val wd = Runnable { stopAlarm() }
        watchdog = wd
        handler.postDelayed(wd, MAX_SPEAK_MS)
        try {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val engine = tts ?: run {
                            stopAlarm()
                            return@TextToSpeech
                        }
                        engine.setOnUtteranceProgressListener(
                            object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {}
                                override fun onDone(utteranceId: String?) {
                                    stopAlarm()
                                }

                                @Deprecated(
                                    "Deprecated in Java",
                                    ReplaceWith("onError(utteranceId, TextToSpeech.ERROR)")
                                )
                                @Suppress("DEPRECATION")
                                override fun onError(utteranceId: String?) {
                                    stopAlarm()
                                }

                                override fun onError(utteranceId: String?, errorCode: Int) {
                                    stopAlarm()
                                }
                            }
                        )
                        val result = engine.speak(
                            text,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            UTTERANCE_ID
                        )
                        if (result != TextToSpeech.SUCCESS) stopAlarm()
                    } catch (e: Exception) {
                        stopAlarm()
                    }
                } else {
                    stopAlarm()
                }
            }
        } catch (e: Exception) {
            stopAlarm()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(
        title: String,
        text: String
    ): android.app.Notification {
        val stopIntent = Intent(this, QuoteAlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this,
            QUOTE_ALARM_STOP_REQUEST_CODE,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val shortText = text
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return NotificationCompat.Builder(this, QUOTE_ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(shortText.take(200))
            .setStyle(NotificationCompat.BigTextStyle().bigText(shortText))
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPending
            )
            .build()
    }

    private fun stopAlarm() {
        try {
            watchdog?.let { handler.removeCallbacks(it) }
        } catch (e: Exception) {
        }
        watchdog = null
        try {
            tts?.stop()
        } catch (e: Exception) {
        }
        try {
            tts?.shutdown()
        } catch (e: Exception) {
        }
        tts = null
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
        }
        stopSelf()
    }

    override fun onDestroy() {
        try {
            tts?.shutdown()
        } catch (e: Exception) {
        }
        tts = null
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (e: Exception) {
        }
        wakeLock = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
