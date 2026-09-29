package com.quotebrowser.app.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quotebrowser.app.widget.QuizWidgetProvider

/**
 * Refreshes every placed quiz widget once a day so the date-seeded
 * question rolls over at midnight. Follows the existing worker pattern
 * (see QotdWorker); scheduled once by [QuizWidgetProvider].
 */
class QuizWidgetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, QuizWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val refresh = Intent(context, QuizWidgetProvider::class.java).apply {
                    action = QuizWidgetProvider.ACTION_REFRESH
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
