package com.quotebrowser.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/** File the crash handler writes to; shown by CrashActivity on next launch. */
internal fun crashFile(context: Context): File = File(context.filesDir, "crash_report.txt")

/** Installs a process-wide handler that saves the stack trace and opens the viewer. */
internal fun installCrashHandler(app: Context) {
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            crashFile(app).writeText(
                "${java.util.Date()}\nThread: ${thread.name}\n${throwable}\n\n${sw}"
            )
        } catch (_: Exception) {
        }
        try {
            val intent = Intent(app, CrashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            app.startActivity(intent)
        } catch (_: Exception) {
        }
        previous?.uncaughtException(thread, throwable)
        Process.killProcess(Process.myPid())
        exitProcess(2)
    }
}

/**
 * Plain-framework crash viewer (no Compose, no app init) so it can display
 * even when the normal startup path is what's crashing.
 */
class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // v1.88.8: tell the startup watchdog the viewer drew, and never let
        // the viewer itself die silently (a crash here used to leave the
        // launch splash frozen with no clue why).
        StartupWatchdog.viewerShown = true
        try {
            showReport()
        } catch (t: Throwable) {
            try {
                val tv = TextView(this)
                tv.text = "Crash viewer failed: $t"
                tv.setPadding(24, 24, 24, 24)
                setContentView(tv)
            } catch (_: Throwable) {
            }
        }
    }

    private fun showReport() {
        // v1.88.2: the report file may legitimately be gone (already
        // dismissed/cleared) while this viewer is still up. Say so plainly
        // instead of dumping a FileNotFoundException stack on the user.
        val reportFile = crashFile(this)
        val report = if (reportFile.exists()) {
            try {
                reportFile.readText()
            } catch (e: Exception) {
                "Could not read crash report: $e"
            }
        } else {
            "No crash details found \u2014 the previous report was already cleared.\n\n" +
                "Tap \"Dismiss and retry app\" below to open the app fresh."
        }

        val textView = TextView(this).apply {
            text = report
            setTextIsSelectable(true)
            textSize = 12f
            setPadding(24, 24, 24, 24)
        }
        val copyButton = Button(this).apply {
            text = "Copy error details"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("crash", report))
                Toast.makeText(this@CrashActivity, "Copied", Toast.LENGTH_SHORT).show()
            }
        }
        val clearButton = Button(this).apply {
            text = "Dismiss and retry app"
            setOnClickListener {
                try { crashFile(this@CrashActivity).delete() } catch (_: Exception) { }
                try {
                    val launch = packageManager.getLaunchIntentForPackage(packageName)
                    startActivity(launch)
                } catch (_: Exception) { }
                finish()
                // v1.88.2: restart in a FRESH process. Application.onCreate
                // does not re-run for activities started in this process, so
                // without this the stale in-memory startupCrashed flag would
                // bounce MainActivity straight back to this viewer — with the
                // report file already deleted, i.e. the "Could not read crash
                // report: FileNotFoundException" dead end. Killing the process
                // forces a clean Application init on relaunch.
                Process.killProcess(Process.myPid())
                exitProcess(0)
            }
        }
        val hasReport = reportFile.exists()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                TextView(this@CrashActivity).apply {
                    text = if (hasReport) {
                        "Quote Browser crashed on startup. Send these details to Terminator:"
                    } else {
                        "No crash report to show."
                    }
                    textSize = 16f
                    setPadding(24, 24, 24, 12)
                }
            )
            addView(copyButton)
            addView(clearButton)
            addView(ScrollView(this@CrashActivity).apply { addView(textView) },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1f
                ))
        }
        setContentView(layout)
    }
}
