package com.quotebrowser.app.tile

import android.service.quicksettings.TileService
import com.quotebrowser.app.QuoteApp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.worker.pickVisibleRandomQuote
import com.quotebrowser.app.worker.postQuoteNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Quick Settings tile: tap for an instant random-quote notification.
 * Add it from the notification shade's tile editor.
 */
class QuoteTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onClick() {
        super.onClick()
        scope.launch {
            val app = application as? QuoteApp ?: return@launch
            val manager: DatabaseManager = try {
                app.dbManager
            } catch (e: Exception) {
                return@launch
            }
            val picked = pickVisibleRandomQuote(manager) ?: return@launch
            postQuoteNotification(
                this@QuoteTileService,
                "Random quote — #${picked.second.number}",
                picked.first,
                picked.second
            )
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
