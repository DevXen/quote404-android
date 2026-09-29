package com.quotebrowser.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quotebrowser.app.data.DatabaseManager

/**
 * Scheduled quote pings: posts a random-quote notification on the user's
 * chosen interval. Silent no-op when pings are disabled or nothing
 * visible can be picked.
 */
class QuotePingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = DatabaseManager.getInstance(applicationContext)
        if (!manager.quotePingEnabled.value) return Result.success()
        // v1.74: the shared ping picker — identical filtering to the
        // notification's Next action (see worker/QuotePingFilter.kt).
        val picked = pickPingQuote(manager) ?: return Result.success()
        postQuoteNotification(
            applicationContext,
            "Random quote — #${picked.second.number}",
            picked.first,
            picked.second
        )
        manager.recordPingNotified(picked.first, picked.second.number)
        return Result.success()
    }
}
