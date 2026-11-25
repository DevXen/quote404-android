package com.quotebrowser.app.worker

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.applyCleanMode
import com.quotebrowser.app.ui.applyKidSafeMask
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * v1.89.10 automation intents (Clint, green-lit 2026-09-19): an exported
 * broadcast API so Tasker, MacroDroid, scripts (adb), and other apps can
 * pull quotes out of Quote404 without opening it.
 *
 * Send one of:
 * - `com.quotebrowser.app.action.RANDOM_QUOTE` — a random quote, picked
 *   with the same content filters as the quote pings (hidden/muted quotes
 *   and the maturity ceiling are honored).
 * - `com.quotebrowser.app.action.QUOTE_OF_THE_DAY` — today's deterministic
 *   Quote of the Day, the same pick the app itself shows.
 *
 * Optional input extras:
 * - `db` (String): restrict the random pick to one database — its id
 *   (`asset:fml.db`, `user_my_db`) or its exact display label ("FML").
 *   Ignored for QUOTE_OF_THE_DAY, which follows the app's own sources.
 * - `reply_package` (String) + `reply_action` (String, default
 *   [ACTION_QUOTE_RESULT]): the result is broadcast back to that package
 *   with that action.
 * - `clipboard` (Boolean): also copy the quote text to the clipboard.
 * - `notify` (Boolean): also post the quote as a notification.
 * With no reply/clipboard/notify requested, the quote is posted as a
 * notification so a bare request still does something visible.
 *
 * Result broadcast extras: `kind` ("random" / "qotd"), `db_id`, `db_name`,
 * `number` (Int), `rating` (String, empty when unrated), `text`. The text
 * honors the Clean mode / Kid-safe settings, so automations that repost
 * quotes publicly get the same text the user sees in the app.
 *
 * Exported on purpose: pulling a quote is the entire point, and quotes are
 * the app's public content. No user data (favorites, notes, history) is
 * reachable through this receiver.
 */
class AutomationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_RANDOM_QUOTE = "com.quotebrowser.app.action.RANDOM_QUOTE"
        const val ACTION_QOTD = "com.quotebrowser.app.action.QUOTE_OF_THE_DAY"
        const val ACTION_QUOTE_RESULT = "com.quotebrowser.app.action.QUOTE_RESULT"

        const val EXTRA_DB = "db"
        const val EXTRA_REPLY_PACKAGE = "reply_package"
        const val EXTRA_REPLY_ACTION = "reply_action"
        const val EXTRA_CLIPBOARD = "clipboard"
        const val EXTRA_NOTIFY = "notify"

        const val RESULT_KIND = "kind"
        const val RESULT_DB_ID = "db_id"
        const val RESULT_DB_NAME = "db_name"
        const val RESULT_NUMBER = "number"
        const val RESULT_RATING = "rating"
        const val RESULT_TEXT = "text"

        private const val NOTIFICATION_ID = 4242
    }

    override fun onReceive(context: Context, intent: Intent) {
        val kind = when (intent.action) {
            ACTION_RANDOM_QUOTE -> "random"
            ACTION_QOTD -> "qotd"
            else -> return
        }
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val manager = DatabaseManager.getInstance(context.applicationContext)
                val picked = if (kind == "qotd") pickQotd(manager) else pickRandom(manager, intent)
                if (picked != null) {
                    deliver(context, manager, kind, picked.first, picked.second, intent)
                }
            } catch (e: Exception) {
                // Automation must never crash the sender or the app; a
                // failed request simply produces no result.
            } finally {
                pending.finish()
            }
        }
    }

    /** Random pick: the shared ping picker, or one database when `db` is set. */
    private suspend fun pickRandom(
        manager: DatabaseManager,
        intent: Intent
    ): Pair<String, Quote>? {
        val wanted = intent.getStringExtra(EXTRA_DB)?.takeIf { it.isNotBlank() }
        if (wanted == null) return pickPingQuote(manager)
        val dbs = manager.availableDatabases().first()
        val match = dbs.firstOrNull { it.id == wanted }
            ?: dbs.firstOrNull { it.label.equals(wanted, ignoreCase = true) }
            ?: return null
        val repo = manager.repoFor(match.id)
        repeat(12) {
            val q = repo.randomQuote() ?: return null
            if (manager.isMaturityAllowed(q) &&
                !manager.isQuoteHiddenFromLists(match.id, q.number, q.text)
            ) return match.id to q
        }
        return null
    }

    /** The same deterministic Quote of the Day the app shows (see QotdViewModel). */
    private suspend fun pickQotd(manager: DatabaseManager): Pair<String, Quote>? {
        val dbs = manager.availableDatabases().first()
        val all = dbs.map { it.id }
        if (all.isEmpty()) return null
        val selected = manager.randomSourceIds.value
        val ids = (selected?.toList() ?: listOf(manager.defaultDbId()))
            .filter { it in all }.ifEmpty { all }.sorted()
        val nonEmpty = manager.qotdNonEmptyDbIds(ids)
        if (nonEmpty.isEmpty()) return null
        val today = LocalDate.now()
        val id = nonEmpty[Math.floorMod(today.toEpochDay(), nonEmpty.size.toLong()).toInt()]
        val quote = manager.repoFor(id).visibleQuoteOfTheDay(today) { q ->
            !manager.isMaturityAllowed(q) ||
                manager.isQuoteHiddenFromLists(id, q.number, q.text)
        } ?: return null
        return id to quote
    }

    private suspend fun deliver(
        context: Context,
        manager: DatabaseManager,
        kind: String,
        dbId: String,
        quote: Quote,
        request: Intent
    ) {
        val dbs = manager.availableDatabases().first()
        val dbName = dbs.firstOrNull { it.id == dbId }?.label ?: dbId
        var text = quote.text
        if (manager.cleanModeEnabled.value) text = applyCleanMode(text)
        if (manager.kidSafeEnabled.value) text = applyKidSafeMask(text)

        val replyPackage = request.getStringExtra(EXTRA_REPLY_PACKAGE)?.takeIf { it.isNotBlank() }
        val wantClipboard = request.getBooleanExtra(EXTRA_CLIPBOARD, false)
        val wantNotify = request.getBooleanExtra(EXTRA_NOTIFY, false)

        if (replyPackage != null) {
            val result = Intent(
                request.getStringExtra(EXTRA_REPLY_ACTION)?.takeIf { it.isNotBlank() }
                    ?: ACTION_QUOTE_RESULT
            ).apply {
                setPackage(replyPackage)
                putExtra(RESULT_KIND, kind)
                putExtra(RESULT_DB_ID, dbId)
                putExtra(RESULT_DB_NAME, dbName)
                putExtra(RESULT_NUMBER, quote.number)
                putExtra(RESULT_RATING, quote.rating?.toString() ?: "")
                putExtra(RESULT_TEXT, text)
            }
            context.sendBroadcast(result)
        }
        if (wantClipboard) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("Quote404 #${quote.number}", text))
        }
        if (wantNotify || (replyPackage == null && !wantClipboard)) {
            ensureQuoteChannel(context)
            postFullQuoteNotification(
                context = context,
                channelId = QUOTE_CHANNEL_ID,
                notificationId = NOTIFICATION_ID,
                title = (if (kind == "qotd") "Quote of the Day" else "Random quote") +
                    " — $dbName #${quote.number}",
                titlePrefix = if (kind == "qotd") "Quote of the Day" else "Random quote",
                statusLine = null,
                dbId = dbId,
                quote = quote
            )
        }
    }
}
