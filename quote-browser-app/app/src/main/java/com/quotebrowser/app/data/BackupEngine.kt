package com.quotebrowser.app.data

import android.content.Context
import android.content.Intent
import android.os.Process
import com.quotebrowser.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/**
 * Full backup & restore. Everything lives inside one zip so a backup
 * captures literally all app state:
 * - prefs/<name>      every *.xml from <app>/shared_prefs/ (all settings,
 *                     theme customizations, filters, game scores, ...)
 * - databases/meta.db (+wal/-shm)   the Room meta DB (reading history,
 *                     favorites, categories, notes, ...)
 * - userdb/<id>.db    user-added databases: My Quotes, imports, collections
 * - manifest.json     format marker used to validate a restore
 *
 * Room holds open file handles, so after [restoreFullBackup] the process
 * must die and start fresh — see [restartApp]. Never try to hot-swap.
 */
object BackupEngine {
    const val FORMAT = "qb-full-backup"
    const val FORMAT_VERSION = 1
    const val APP_VERSION = "1.44"

    private const val PREFS_NAME = "backup_v144"
    private const val KEY_FULL_IN_AUTO = "full_in_auto"

    private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

    data class BackupPreview(
        val createdAt: String,
        val appVersion: String,
        val entries: List<String>,
        val sizeBytes: Long
    )

    /** Own SharedPreferences; full backup included in auto runs by default. */
    fun isFullBackupInAuto(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FULL_IN_AUTO, true)

    fun setFullBackupInAuto(context: Context, include: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_FULL_IN_AUTO, include).apply()
    }

    /** Builds the zip in the cache dir and returns it; caller moves/deletes it. */
    suspend fun createFullBackup(context: Context): File = withContext(Dispatchers.IO) {
        val stamp = LocalDateTime.now().format(STAMP)
        val out = File(context.cacheDir, "quotebrowser-full-$stamp.zip")
        val root = context.filesDir.parentFile
            ?: error("App data directory unavailable.")
        ZipOutputStream(out.outputStream()).use { zip ->
            // (a) every *.xml in shared_prefs/
            File(root, "shared_prefs")
                .listFiles { f -> f.isFile && f.extension == "xml" }
                ?.sortedBy { it.name }
                ?.forEach { file ->
                    zip.putNextEntry(ZipEntry("prefs/${file.name}"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            // (b) Room meta DB files (meta.db, -wal, -shm when present), plus
            // v1.61 the translations DB (translations.db) — quote
            // translations are cached there, translated once per language.
            val metaDir = File(root, "databases")
            listOf(
                "meta.db", "meta.db-wal", "meta.db-shm",
                "translations.db", "translations.db-wal", "translations.db-shm"
            ).forEach { name ->
                val file = File(metaDir, name)
                if (file.isFile) {
                    zip.putNextEntry(ZipEntry("databases/$name"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            // (c) user databases (user imports + My Quotes); bundled DBs
            // live in the APK and are intentionally skipped.
            File(context.filesDir, "databases")
                .listFiles { f -> f.isFile && f.name.endsWith(".db") }
                ?.sortedBy { it.name }
                ?.forEach { file ->
                    zip.putNextEntry(ZipEntry("userdb/${file.name}"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            // (d) v1.61.18: read-aloud (Gemini TTS) audio cache — each
            // quote's narration is fetched once and replayed from disk,
            // so the audio comes along in the backup too.
            File(context.filesDir, "gemini_tts")
                .listFiles { f -> f.isFile }
                ?.sortedBy { it.name }
                ?.forEach { file ->
                    zip.putNextEntry(ZipEntry("tts/${file.name}"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            // (e) v1.61.20: the voice-recordings manifest (which quote
            // each saved read-aloud file belongs to, plus alternates).
            File(context.filesDir, "voice_recordings.json")
                .takeIf { it.exists() }
                ?.let { manifest ->
                    zip.putNextEntry(ZipEntry("voice_recordings.json"))
                    manifest.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            // (f) manifest, last so a truncated zip fails validation
            val manifest = JSONObject()
                .put("format", FORMAT)
                .put("formatVersion", FORMAT_VERSION)
                .put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .put("appVersion", APP_VERSION)
                .toString(2)
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        out
    }

    /** Reads the manifest and entry list; throws when not a QB backup. */
    suspend fun previewBackup(file: File): BackupPreview = withContext(Dispatchers.IO) {
        ZipFile(file).use { zip ->
            val manifestEntry = zip.getEntry("manifest.json")
                ?: throw IllegalArgumentException("Not a QuoteBrowser backup")
            val manifest = zip.getInputStream(manifestEntry).use {
                JSONObject(it.readBytes().toString(Charsets.UTF_8))
            }
            if (manifest.optString("format") != FORMAT ||
                manifest.optInt("formatVersion", -1) != FORMAT_VERSION
            ) {
                throw IllegalArgumentException("Not a QuoteBrowser backup")
            }
            BackupPreview(
                createdAt = manifest.optString("createdAt", "unknown"),
                appVersion = manifest.optString("appVersion", "unknown"),
                entries = zip.entries().asSequence().map { it.name }.toList(),
                sizeBytes = file.length()
            )
        }
    }

    /**
     * Validates [file] via [previewBackup], unpacks to a temp dir, then
     * copies everything over the live app data. The app MUST restart
     * afterwards (Room holds open handles) — see [restartApp].
     */
    suspend fun restoreFullBackup(context: Context, file: File) =
        withContext(Dispatchers.IO) {
            previewBackup(file) // throws when invalid
            val tmp = File(context.cacheDir, "restore-${System.currentTimeMillis()}")
            tmp.mkdirs()
            try {
                ZipFile(file).use { zip ->
                    val tmpRoot = tmp.canonicalPath + File.separator
                    zip.entries().asSequence().forEach { entry ->
                        if (entry.isDirectory) return@forEach
                        val dest = File(tmp, entry.name)
                        // Zip-slip guard: no entry may escape the temp dir.
                        if (!dest.canonicalPath.startsWith(tmpRoot)) {
                            throw IllegalArgumentException("Bad backup entry: ${entry.name}")
                        }
                        dest.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { it.copyTo(dest.outputStream()) }
                    }
                }
                val root = context.filesDir.parentFile
                    ?: error("App data directory unavailable.")
                File(tmp, "prefs").listFiles()
                    ?.filter { it.isFile && it.extension == "xml" }
                    ?.forEach { src ->
                        val destDir = File(root, "shared_prefs").apply { mkdirs() }
                        src.copyTo(File(destDir, src.name), overwrite = true)
                    }
                File(tmp, "databases").listFiles()
                    ?.filter { it.isFile }
                    ?.forEach { src ->
                        val destDir = File(root, "databases").apply { mkdirs() }
                        src.copyTo(File(destDir, src.name), overwrite = true)
                    }
                File(tmp, "userdb").listFiles()
                    ?.filter { it.isFile && it.name.endsWith(".db") }
                    ?.forEach { src ->
                        val destDir = File(context.filesDir, "databases").apply { mkdirs() }
                        src.copyTo(File(destDir, src.name), overwrite = true)
                    }
                // v1.61.20: restore the voice-recordings manifest.
                File(tmp, "voice_recordings.json")
                    .takeIf { it.exists() }
                    ?.copyTo(
                        File(context.filesDir, "voice_recordings.json"),
                        overwrite = true
                    )
                // v1.61.18: restore the read-aloud audio cache (absent in
                // older backups — listFiles is null-safe there).
                File(tmp, "tts").listFiles()
                    ?.filter { it.isFile }
                    ?.forEach { src ->
                        val destDir = File(context.filesDir, "gemini_tts").apply { mkdirs() }
                        src.copyTo(File(destDir, src.name), overwrite = true)
                    }
            } finally {
                tmp.deleteRecursively()
            }
        }

    /**
     * Relaunches MainActivity with a cleared task and kills this process.
     * Same shape as CrashHandler's restart path.
     */
    fun restartApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}
