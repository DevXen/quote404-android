package com.quotebrowser.app.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quotebrowser.app.widget.StreakWidgetProvider

/**
 * Refreshes every placed reading-streak widget once a day so the day
 * counter rolls over at midnight. Follows the existing worker pattern
 * (see QuizWidgetWorker); scheduled once by [StreakWidgetProvider].
 */
class StreakWidgetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, StreakWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val refresh = Intent(context, StreakWidgetProvider::class.java).apply {
                    action = StreakWidgetProvider.ACTION_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(refresh)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
