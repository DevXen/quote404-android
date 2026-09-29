package com.quotebrowser.app.worker

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quotebrowser.app.data.BackupEngine
import com.quotebrowser.app.data.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scheduled database backup: copies every quote database into the
 * user-chosen folder (persisted SAF tree URI). Runs daily or weekly per
 * the auto-backup setting. When "Include full backup in automatic backups"
 * is on (the default), it also writes one full zip (settings, themes,
 * favorites, history, My Quotes, imported databases) and prunes old ones.
 * The outcome is recorded in prefs so Settings can show the last result;
 * failures never retry-loop.
 */
class BackupWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val manager = DatabaseManager.getInstance(applicationContext)
        val treeString = manager.autoBackupTreeUri.value
            ?: inputData.getString(KEY_TREE_URI)
        if (treeString.isNullOrBlank()) {
            manager.setLastAutoBackup("No backup folder chosen.")
            return Result.success()
        }
        val treeUri = Uri.parse(treeString)
        val (count, error) = try {
            manager.backupDatabasesToTree(treeUri)
        } catch (e: Exception) {
            0 to (e.message ?: "Backup failed.")
        }
        val includeFull = BackupEngine.isFullBackupInAuto(applicationContext)
        val fullError = if (includeFull) writeFullBackupToTree(treeUri) else null
        val when_ = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("MMM d, HH:mm")
        )
        manager.setLastAutoBackup(
            when {
                error != null -> "Backup failed: $error"
                fullError != null ->
                    "$count database${if (count == 1) "" else "s"} backed up · " +
                        "full backup failed: $fullError · $when_"
                includeFull ->
                    "$count database${if (count == 1) "" else "s"} + full backup · $when_"
                else -> "$count database${if (count == 1) "" else "s"} backed up · $when_"
            }
        )
        return Result.success()
    }

    /**
     * Writes one full zip into the tree folder ("full-<timestamp>.zip"),
     * keeping the newest 5. Returns an error string, or null on success.
     * Mirrors the DocumentsContract file-writing pattern used by
     * DatabaseManager.backupDatabasesToTree.
     */
    private suspend fun writeFullBackupToTree(treeUri: Uri): String? =
        withContext(Dispatchers.IO) {
            val zip = try {
                BackupEngine.createFullBackup(applicationContext)
            } catch (e: Exception) {
                return@withContext e.message ?: "Couldn't create the backup."
            }
            try {
                val stamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                )
                val name = "full-$stamp.zip"
                val docUri = DocumentsContract.createDocument(
                    applicationContext.contentResolver,
                    treeUri,
                    "application/zip",
                    name
                ) ?: return@withContext "Couldn't create the backup file."
                applicationContext.contentResolver.openOutputStream(docUri)?.use { out ->
                    zip.inputStream().use { input -> input.copyTo(out) }
                } ?: return@withContext "Couldn't write the backup."
                pruneFullBackups(treeUri)
                null
            } catch (e: SecurityException) {
                "Lost access to the backup folder — please choose it again."
            } catch (e: Exception) {
                e.message ?: "Full backup failed."
            } finally {
                zip.delete()
            }
        }

    /** Deletes full-*.zip files beyond the newest 5 in the tree folder. */
    private fun pruneFullBackups(treeUri: Uri) {
        val cr = applicationContext.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri, DocumentsContract.getTreeDocumentId(treeUri)
        )
        val docs = mutableListOf<Pair<String, String>>()
        cr.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null, null, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameCol) ?: continue
                if (name.startsWith("full-") && name.endsWith(".zip")) {
                    docs.add(cursor.getString(idCol) to name)
                }
            }
        }
        // Names are "full-yyyyMMdd-HHmmss.zip", so name order is time order.
        docs.sortedByDescending { it.second }.drop(KEEP_FULL_BACKUPS)
            .forEach { (docId, _) ->
                try {
                    DocumentsContract.deleteDocument(
                        cr,
                        DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    )
                } catch (_: Exception) {
                    // Best effort: a stale listing is not worth failing the run.
                }
            }
    }

    companion object {
        const val KEY_TREE_URI = "tree_uri"
        private const val KEEP_FULL_BACKUPS = 5
    }
}
