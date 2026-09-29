package com.quotebrowser.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quotebrowser.app.MainActivity
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.applyDisplayBreaks
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val QUOTE_CHANNEL_ID = "random_quote"
internal const val QUOTE_NOTIFICATION_ID = 42

fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

fun ensureQuoteChannel(context: Context) {
    val mgr =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (mgr.getNotificationChannel(QUOTE_CHANNEL_ID) == null) {
        mgr.createNotificationChannel(
            NotificationChannel(
                QUOTE_CHANNEL_ID,
                "Random quotes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}

/**
 * A visible random quote: honors the user's random-source setting,
 * "Never show again", and the content filter. Returns null when nothing
 * visible can be found.
 */
suspend fun pickVisibleRandomQuote(manager: DatabaseManager): Pair<String, Quote>? {
    val configured = manager.randomSourceIds.value
    val all = manager.allDbIds()
    val ids = (configured ?: all.toSet()).ifEmpty { all.toSet() }
    return pickVisibleRandomQuoteFrom(manager, ids)
}

/**
 * A visible random quote drawn from exactly [ids]. Honors "Never show
 * again" and the content filter. Returns null when nothing visible can be
 * found.
 */
suspend fun pickVisibleRandomQuoteFrom(
    manager: DatabaseManager,
    ids: Collection<String>
): Pair<String, Quote>? =
    withContext(Dispatchers.IO) {
        try {
            val list = ids.toList()
            if (list.isEmpty()) return@withContext null
            repeat(40) {
                val id = list.random()
                val q = manager.repoFor(id).randomQuote() ?: return@repeat
                if (!manager.isQuoteHiddenOrMuted(id, q.number, q.text) &&
                    !manager.isHiddenByContentFilter(q.text)
                ) {
                    return@withContext id to q
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

/** Posts a random-quote notification that deep-links to the quote. */
fun postQuoteNotification(context: Context, title: String, dbId: String, quote: Quote) {
    ensureQuoteChannel(context)
    postFullQuoteNotification(
        context = context,
        channelId = QUOTE_CHANNEL_ID,
        notificationId = QUOTE_NOTIFICATION_ID,
        title = title,
        titlePrefix = "Random quote",
        statusLine = null,
        dbId = dbId,
        quote = quote
    )
}

/**
 * Full quote notification with Favorite / Next / Share actions. The
 * channel must already exist; callers ensure it before calling.
 *
 * - Favorite / Next go to [QotdActionReceiver] (adds to favorites, or
 *   picks another visible quote and reposts).
 * - Share is a direct share-sheet activity intent (no trampoline).
 */
internal fun postFullQuoteNotification(
    context: Context,
    channelId: String,
    notificationId: Int,
    title: String,
    titlePrefix: String,
    statusLine: String?,
    dbId: String,
    quote: Quote
) {
    if (!hasNotificationPermission(context)) return
    val open = Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_OPEN_QUOTE
        putExtra(MainActivity.EXTRA_QUOTE_NUMBER, quote.number)
        putExtra(MainActivity.EXTRA_DB_ID, dbId)
    }
    val pending = PendingIntent.getActivity(
        context,
        9000 + (quote.number % 100000),
        open,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val manager = DatabaseManager.getInstance(context)
    val displayText = applyDisplayBreaks(
        quote.text, manager.ircLineBreaks.value, manager.dialogueLineBreaks.value
    )
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_stat_quote)
        .setContentTitle(title)
        .setContentText(statusLine ?: displayText.take(160))
        .setStyle(NotificationCompat.BigTextStyle().bigText(displayText))
        .setContentIntent(pending)
        .setAutoCancel(true)
        .addAction(favoriteAction(context, channelId, notificationId, titlePrefix, dbId, quote))
        .addAction(nextAction(context, channelId, notificationId, titlePrefix))
        .addAction(shareAction(context, quote))
        .build()
    try {
        NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    } catch (e: SecurityException) {
        // Permission was revoked between the check and the post.
    }
}

private fun receiverIntent(
    context: Context,
    action: String,
    channelId: String,
    notificationId: Int,
    titlePrefix: String,
    dbId: String,
    number: Int
): Intent = Intent(context, QotdActionReceiver::class.java).apply {
    this.action = action
    putExtra(QotdActionReceiver.EXTRA_CHANNEL_ID, channelId)
    putExtra(QotdActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
    putExtra(QotdActionReceiver.EXTRA_TITLE_PREFIX, titlePrefix)
    putExtra(QotdActionReceiver.EXTRA_DB_ID, dbId)
    putExtra(QotdActionReceiver.EXTRA_NUMBER, number)
}

private fun favoriteAction(
    context: Context,
    channelId: String,
    notificationId: Int,
    titlePrefix: String,
    dbId: String,
    quote: Quote
): NotificationCompat.Action {
    val pending = PendingIntent.getBroadcast(
        context,
        9100 + (quote.number % 100000),
        receiverIntent(
            context, QotdActionReceiver.ACTION_FAVORITE, channelId,
            notificationId, titlePrefix, dbId, quote.number
        ),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Action(
        android.R.drawable.btn_star_big_on, "Favorite", pending
    )
}

private fun nextAction(
    context: Context,
    channelId: String,
    notificationId: Int,
    titlePrefix: String
): NotificationCompat.Action {
    val pending = PendingIntent.getBroadcast(
        context,
        9200 + notificationId,
        receiverIntent(
            context, QotdActionReceiver.ACTION_NEXT, channelId,
            notificationId, titlePrefix, "", -1
        ),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Action(
        android.R.drawable.ic_media_next, "Next", pending
    )
}

private fun shareAction(context: Context, quote: Quote): NotificationCompat.Action {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "#${quote.number}\n${quote.text}")
    }
    val chooser = Intent.createChooser(send, "Share quote")
    val pending = PendingIntent.getActivity(
        context,
        9300 + (quote.number % 100000),
        chooser,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Action(
        android.R.drawable.ic_menu_share, "Share", pending
    )
}
