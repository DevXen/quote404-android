package com.quotebrowser.app

import android.app.Application
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.PlaylistPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class QuoteApp : Application(), androidx.work.Configuration.Provider {
    lateinit var dbManager: DatabaseManager
        private set

    /**
     * v1.87.1: androidx.startup's InitializationProvider is stripped from our
     * hand-built manifest, so WorkManager would otherwise have no initializer.
     * Implementing Configuration.Provider guarantees WorkManager can always
     * initialize on demand — even on the crash-safe early-return path in
     * onCreate() below. Without this, a stale crash_report.txt plus any
     * scheduled job (wallpaper/quote-ping workers) caused a permanent
     * SystemJobService crash loop on startup.
     */
    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder().build()

    /** True when a previous run crashed: skip normal init so CrashActivity can show. */
    var startupCrashed: Boolean = false
        private set

    /** Application-wide coroutine scope for app-scoped playback. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * v1.63: the read-aloud/radio engine, shared by the phone UI's
     * PlaylistViewModel and Android Auto's media browser service so both
     * drive the same queue.
     */
    val playlistPlayer: PlaylistPlayer by lazy { PlaylistPlayer(dbManager, appScope) }

    override fun onCreate() {
        super.onCreate()
        // v1.88.22: load the "Break between quotes" display setting early so
        // every path (UI, workers, widgets, exports) sees the persisted value
        // even if no composable has run yet. paragraphizeBody() reads it via
        // DisplayPrefs.breakBetweenQuotes.value; init is idempotent.
        try {
            com.quotebrowser.app.data.DisplayPrefs.init(this)
        } catch (_: Exception) {
        }
        // v1.88.33: arm the startup watchdog FIRST. start() only registers
        // activity-gated callbacks now — the 30s kill countdown begins on
        // the first activity's onActivityCreated, so service-only process
        // starts (SystemJobService, workers) can never arm the killer, and
        // its report goes to watchdog_report.txt instead of poisoning
        // crash_report.txt. Two earlier releases froze on the launch splash
        // without drawing, and the dump it writes on a wedged start is the
        // only way to see the true hang off-device.
        StartupWatchdog.start(this)
        installCrashHandler(this)
        StartupWatchdog.mark("crash handler installed")
        if (crashFile(this).exists()) {
            startupCrashed = true
            StartupWatchdog.startupCrashedPath = true
            StartupWatchdog.mark("early return: crash report present")
            StartupWatchdog.appInitDone = true
            return
        }
        StartupWatchdog.mark("no crash report; full init")
        // Manual WorkManager init (androidx.startup provider is stripped from the manifest).
        try {
            androidx.work.WorkManager.initialize(
                this,
                androidx.work.Configuration.Builder().build()
            )
        } catch (_: Exception) {
        }
        StartupWatchdog.mark("workmanager init done")
        // v1.88.19: single application-scoped instance — workers, widgets,
        // and receivers resolve the same instance via getInstance().
        dbManager = DatabaseManager.getInstance(this)
        StartupWatchdog.mark("DatabaseManager init done")
        // Re-arm the daily wallpaper schedule after a reboot or update.
        try {
            dbManager.ensureWallpaperWorker()
        } catch (_: Exception) {
        }
        // Re-arm scheduled quote pings after a reboot or update.
        try {
            dbManager.ensureQuotePingWorker()
        } catch (_: Exception) {
        }
        StartupWatchdog.mark("workers re-armed")
        // v1.83: scan for plugin packs (quote packs, theme packs) on a
        // background thread. The scan never throws and a malformed pack is
        // skipped with a logged reason — startup can never be blocked by
        // a bad pack.
        try {
            Thread({
                try {
                    com.quotebrowser.app.data.plugins.PluginManager.scan(this)
                } catch (e: Exception) {
                    android.util.Log.w("QuoteApp", "plugin scan failed", e)
                }
            }, "plugin-scan").start()
        } catch (e: Exception) {
            android.util.Log.w("QuoteApp", "could not start plugin scan", e)
        }
        // v1.75: load the saved-explanations manifest at startup so card
        // icons and the "Has explanation" search filter work immediately.
        // v1.88.12: load() reads + parses JSON on disk — do it off the main
        // thread; the cache publishes into a StateFlow when ready.
        try {
            Thread({
                try {
                    com.quotebrowser.app.ui.ExplanationCache.ensure(this)
                } catch (_: Exception) {
                }
            }, "explanation-cache-load").start()
        } catch (_: Exception) {
        }
        StartupWatchdog.mark("QuoteApp.onCreate done")
        StartupWatchdog.appInitDone = true
    }
}
