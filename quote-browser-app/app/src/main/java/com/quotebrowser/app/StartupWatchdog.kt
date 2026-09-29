package com.quotebrowser.app

import android.content.Context
import android.os.Process
import android.os.SystemClock
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.system.exitProcess

/**
 * v1.88.8: startup watchdog. Two consecutive releases (1.88.6, 1.88.7)
 * froze on the system launch splash on the Fold7 without ever drawing a
 * frame, and remote guessing got it wrong once already. This thread is
 * armed first thing in Application.onCreate: if neither MainActivity's
 * first frame nor the crash viewer appears within 30s, it writes the
 * startup markers plus a full thread dump to the crash report file and
 * kills the process. The next launch shows CrashActivity with that dump,
 * so the exact wedged thread/stack can be read off the device instead
 * of guessed at. On a healthy start the thread sees firstFrameDrawn (or
 * viewerShown) and exits quietly.
 *
 * v1.88.9: firstFrameDrawn is now set ONLY by a one-shot pre-draw
 * listener on the activity's decor view. v1.88.8 set it in onResume(),
 * which runs before the first frame draws — the app ANR'd on the splash
 * with the flag already true, so the watchdog stood down and wrote
 * nothing. Never trust onResume as proof of a drawn frame.
 *
 * v1.88.33: the 30s kill countdown is now activity-gated. Tapping a quote
 * notification can start the process for a service only (SystemJobService
 * -> WorkManager init wedged the main thread in WorkerFactory.<clinit>);
 * no activity is ever created, the old countdown fired anyway, and its
 * report overwrote crash_report.txt — poisoning the NEXT launch into the
 * crash early-return path. Now start() only registers lifecycle callbacks;
 * the countdown begins on the FIRST activity's onActivityCreated, so
 * service-only starts can never arm the killer. The watchdog report goes
 * to filesDir/watchdog_report.txt; crash_report.txt stays exclusively for
 * the uncaught-exception handler -> CrashActivity flow.
 */
object StartupWatchdog {
    @Volatile var appInitDone = false
    @Volatile var firstFrameDrawn = false
    @Volatile var viewerShown = false
    @Volatile var startupCrashedPath = false

    private val t0 = SystemClock.elapsedRealtime()
    private val marks = ConcurrentLinkedQueue<String>()

    /** v1.88.16: where marks are persisted (cache dir, shareable). */
    private const val LOG_DIR = "startup_logs"
    private const val LOG_NAME = "startup_log.txt"
    // v1.88.19: ring buffer — the file keeps only the last LOG_MAX_LINES
    // lines; LOG_MAX_BYTES is a backstop for pathological long lines.
    private const val LOG_MAX_LINES = 500
    private const val LOG_MAX_BYTES = 256 * 1024L

    @Volatile private var appCtxForLog: Context? = null
    private val logLock = Any()

    // v1.88.19: single-writer background pipeline. mark() enqueues the line
    // and returns immediately — no disk I/O on the caller's thread. One
    // daemon thread drains the FIFO queue, so mark ordering is preserved
    // exactly as before.
    private val logQueue = java.util.concurrent.LinkedBlockingQueue<String>()
    @Volatile private var logWriterStarted = false

    fun mark(s: String) {
        val line = "+${SystemClock.elapsedRealtime() - t0}ms $s"
        marks.add(line)
        enqueueLogLine(line)
    }

    private fun enqueueLogLine(line: String) {
        try {
            if (appCtxForLog == null) return
            startLogWriter()
            logQueue.offer(line)
        } catch (_: Exception) {
        }
    }

    private fun startLogWriter() {
        if (logWriterStarted) return
        synchronized(logLock) {
            if (logWriterStarted) return
            logWriterStarted = true
            Thread({
                try {
                    while (true) writeLogLine(logQueue.take())
                } catch (_: InterruptedException) {
                    // Writer thread interrupted; queued lines are dropped
                    // silently. Startup logging is best-effort diagnostics.
                } catch (_: Exception) {
                }
            }, "startup-log-writer").apply { isDaemon = true }.start()
        }
    }

    /**
     * v1.88.19: runs ONLY on the log-writer thread. Appends the line, then
     * trims the file back to the last LOG_MAX_LINES lines once the writer
     * has appended LOG_MAX_LINES lines since the previous trim (or the file
     * passes LOG_MAX_BYTES). Batching the trim keeps the common case a cheap
     * append; the file can never grow unboundedly. Never throws.
     */
    private var linesSinceTrim = -1 // -1 = not yet seeded this process

    private fun writeLogLine(line: String) {
        val ctx = appCtxForLog ?: return
        try {
            synchronized(logLock) {
                val dir = java.io.File(ctx.cacheDir, LOG_DIR).apply { mkdirs() }
                val f = java.io.File(dir, LOG_NAME)
                if (linesSinceTrim < 0) {
                    linesSinceTrim = try {
                        if (f.exists()) f.readLines().size else 0
                    } catch (_: Exception) {
                        0
                    }
                }
                if (f.length() == 0L) {
                    f.appendText("startup_log process started at " +
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                            .format(java.util.Date()) + "\n")
                    linesSinceTrim++
                }
                f.appendText(line + "\n")
                linesSinceTrim++
                if (linesSinceTrim >= LOG_MAX_LINES || f.length() > LOG_MAX_BYTES) {
                    val kept = try {
                        f.readLines().takeLast(LOG_MAX_LINES)
                    } catch (_: Exception) {
                        emptyList()
                    }
                    try {
                        f.writeText(
                            if (kept.isEmpty()) "" else kept.joinToString("\n", postfix = "\n")
                        )
                    } catch (_: Exception) {
                    }
                    linesSinceTrim = kept.size
                }
            }
        } catch (_: Exception) {
        }
    }

    /** v1.88.16: path of the persisted startup log, or null. */
    fun logFile(context: Context): java.io.File? {
        return try {
            val f = java.io.File(java.io.File(context.cacheDir, LOG_DIR), LOG_NAME)
            if (f.exists()) f else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * v1.88.33: start() no longer starts the 30s kill countdown. It records
     * the app context for logging and registers ActivityLifecycleCallbacks;
     * the countdown begins on the FIRST activity's onActivityCreated
     * (armKillerOnce). A process started for services/workers only
     * (SystemJobService, wallpaper/quote-ping workers) creates no activity
     * and therefore can never arm the killer — previously the countdown
     * fired on those starts and its report overwrote crash_report.txt,
     * bouncing the next launch into the crash early-return path.
     */
    fun start(context: Context) {
        appCtxForLog = context.applicationContext
        mark("watchdog armed (activity-gated)")
        val app = context.applicationContext as? android.app.Application ?: return
        try {
            app.registerActivityLifecycleCallbacks(
                object : android.app.Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(
                        a: android.app.Activity,
                        b: android.os.Bundle?
                    ) {
                        armKillerOnce(app)
                    }

                    override fun onActivityStarted(a: android.app.Activity) {}
                    override fun onActivityResumed(a: android.app.Activity) {}
                    override fun onActivityPaused(a: android.app.Activity) {}
                    override fun onActivityStopped(a: android.app.Activity) {}
                    override fun onActivitySaveInstanceState(
                        a: android.app.Activity,
                        b: android.os.Bundle
                    ) {
                    }

                    override fun onActivityDestroyed(a: android.app.Activity) {}
                }
            )
        } catch (_: Exception) {
        }
    }

    private val killerArmed = java.util.concurrent.atomic.AtomicBoolean(false)

    /**
     * v1.88.33: starts the 30s no-frame kill countdown, exactly once per
     * process, when the first activity is created. Keeps the v1.88.8+
     * diagnostics: startup markers + full thread dump, then kills the
     * process. The report goes to watchdog_report.txt (filesDir) — never
     * crash_report.txt, which belongs to the uncaught-exception handler.
     */
    private fun armKillerOnce(appCtx: Context) {
        if (!killerArmed.compareAndSet(false, true)) return
        mark("watchdog countdown started (first activity created)")
        val ctx = appCtx.applicationContext
        Thread({
            try {
                // v1.88.9: 30s. A healthy cold start draws in ~1-2s; the
                // v1.88.8 60s window only made a wedged user wait longer.
                Thread.sleep(30_000)
            } catch (_: InterruptedException) {
                return@Thread
            }
            if (firstFrameDrawn || viewerShown) return@Thread
            val sb = StringBuilder()
            sb.append("STARTUP WATCHDOG: no frame drawn 30s after first activity created.\n")
            sb.append("appInitDone=").append(appInitDone)
                .append(" startupCrashedPath=").append(startupCrashedPath).append('\n')
            sb.append("--- startup markers ---\n")
            for (m in marks) sb.append(m).append('\n')
            sb.append("--- thread dump ---\n")
            val traces = Thread.getAllStackTraces()
            for (t in traces.keys.sortedBy { it.name }) {
                sb.append('"').append(t.name).append("\" state=").append(t.state).append('\n')
                val st = traces[t] ?: emptyArray()
                for (i in st.indices) {
                    if (i >= 12) {
                        sb.append("    ... (").append(st.size - 12).append(" more)\n")
                        break
                    }
                    sb.append("    at ").append(st[i]).append('\n')
                }
            }
            try {
                watchdogFile(ctx).writeText(sb.toString())
                mark("watchdog fired; report in watchdog_report.txt")
            } catch (_: Exception) {
            }
            try {
                Process.killProcess(Process.myPid())
            } catch (_: Exception) {
            }
            exitProcess(2)
        }, "startup-watchdog").apply { isDaemon = true }.start()
    }

    /**
     * v1.88.33: the watchdog's own report file. Deliberately separate from
     * crash_report.txt, which is written only by the uncaught-exception
     * handler and consumed by CrashActivity / the crash early-return path.
     */
    internal fun watchdogFile(context: Context): java.io.File =
        java.io.File(context.filesDir, "watchdog_report.txt")
}
