package com.quotebrowser.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.quotebrowser.app.MainActivity
import com.quotebrowser.app.R

private const val SEARCH_ALERT_CHANNEL_ID = "search_alerts"
private const val SEARCH_ALERT_BASE_ID = 7100

private fun ensureSearchAlertChannel(context: Context) {
    val mgr =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (mgr.getNotificationChannel(SEARCH_ALERT_CHANNEL_ID) == null) {
        mgr.createNotificationChannel(
            NotificationChannel(
                SEARCH_ALERT_CHANNEL_ID,
                "Search alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}

/**
 * Posts a search-alert notification: [matchCount] new quotes in a freshly
 * imported database match the saved search [searchName]. Tapping opens the
 * app's search tab (ACTION_OPEN_SEARCH deep-link). Mirrors the QotD
 * permission pattern: silently skips when POST_NOTIFICATIONS is not
 * granted.
 */
fun postSearchAlertNotification(
    context: Context,
    searchName: String,
    matchCount: Int
) {
    if (!hasNotificationPermission(context)) return
    ensureSearchAlertChannel(context)
    val id = SEARCH_ALERT_BASE_ID + (searchName.hashCode() and 0xFFFF)
    val open = Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_OPEN_SEARCH
    }
    val pending = PendingIntent.getActivity(
        context,
        id,
        open,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val text = if (matchCount == 1) "1 new quote matches \"$searchName\""
    else "$matchCount new quotes match \"$searchName\""
    val notification = NotificationCompat.Builder(context, SEARCH_ALERT_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_quote)
        .setContentTitle(searchName)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(pending)
        .setAutoCancel(true)
        .build()
    try {
        NotificationManagerCompat.from(context).notify(id, notification)
    } catch (e: SecurityException) {
        // Permission was revoked between the check and the post.
    }
}
