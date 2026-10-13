package com.quotebrowser.app.data

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * v1.59 database verification / repair tool (Settings → Verify databases).
 *
 * Runs a set of conservative checks over every known quote database:
 * required tables exist, the FTS index is in sync with `quotes`, no
 * blank-text or non-positive-number rows, category tables exist, and
 * bundled databases are non-empty.
 *
 * Repairs are conservative by design:
 * - an out-of-sync or missing FTS index is rebuilt (FTS5 external-content
 *   `rebuild`, the same population step tools/make_db.py performs);
 * - rows with blank text are deleted (clearly bad, nothing references them);
 * - missing category tables are created via [CategoryStore.ensureTables].
 * Anything destructive beyond that (bad quote numbers, empty bundled
 * databases, unreadable files) is reported as NEEDS_ATTENTION, never
 * auto-fixed.
 *
 * Note: there is no pre-existing app-side FTS rebuild function to reuse —
 * QuoteDao's KDoc states the index is pre-populated and "never rebuilt by
 * the app" — so the rebuild here uses the standard FTS5 `rebuild` command
 * against the external-content table the bundled files ship with.
 */

/** Outcome of one verification check. */
enum class VerifyStatus {
    OK,
    FIXED,
    NEEDS_ATTENTION
}

/** One named check on one database and its outcome. */
data class DbCheckResult(
    val name: String,
    val status: VerifyStatus,
    val message: String
)

/** All check results for a single database. */
data class DbVerifyResult(
    val dbId: String,
    val label: String,
    val checks: List<DbCheckResult>
)

/** Full verification report across every known database. */
data class VerifyReport(
    val results: List<DbVerifyResult>,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long = System.currentTimeMillis()
) {
    val fixedCount: Int
        get() = results.sumOf { r -> r.checks.count { it.status == VerifyStatus.FIXED } }
    val attentionCount: Int
        get() = results.sumOf { r -> r.checks.count { it.status == VerifyStatus.NEEDS_ATTENTION } }
}

private const val FTS_TABLE = "quotes_fts"

private val CATEGORY_TABLES =
    listOf("categories", "quote_categories", "category_settings", "category_auto_rules")

private fun SupportSQLiteDatabase.tableExists(name: String): Boolean =
    query(
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '$name' LIMIT 1"
    ).use { c -> c.moveToFirst() }

private fun SupportSQLiteDatabase.rowCount(table: String): Long =
    query("SELECT COUNT(*) FROM $table").use { c ->
        if (c.moveToFirst()) c.getLong(0) else 0L
    }

private fun SupportSQLiteDatabase.scalarCount(sql: String): Long =
    query(sql).use { c -> if (c.moveToFirst()) c.getLong(0) else 0L }

/** Rebuilds an external-content FTS5 index from its content table. */
private fun SupportSQLiteDatabase.rebuildFts() {
    execSQL("INSERT INTO $FTS_TABLE($FTS_TABLE) VALUES('rebuild')")
}

/**
 * Verifies every known database and applies conservative repairs.
 * Runs on Dispatchers.IO; [onProgress] reports (done, total) per database.
 */
suspend fun verifyDatabases(
    manager: DatabaseManager,
    onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
): VerifyReport = withContext(Dispatchers.IO) {
    val startedAt = System.currentTimeMillis()
    val ids = try {
        manager.allDbIds()
    } catch (e: Exception) {
        emptyList()
    }
    val results = mutableListOf<DbVerifyResult>()
    ids.forEachIndexed { index, id ->
        results.add(verifyOneDatabase(manager, id))
        onProgress(index + 1, ids.size)
    }
    VerifyReport(results, startedAt, System.currentTimeMillis())
}

private suspend fun verifyOneDatabase(
    manager: DatabaseManager,
    id: String
): DbVerifyResult {
    val label = try {
        manager.shortLabel(id)
    } catch (e: Exception) {
        id
    }
    val checks = mutableListOf<DbCheckResult>()
    fun check(name: String, status: VerifyStatus, message: String) {
        checks.add(DbCheckResult(name, status, message))
    }
    try {
        val quoteDb = try {
            manager.getQuoteDatabase(id)
        } catch (e: Exception) {
            check(
                "Open database", VerifyStatus.NEEDS_ATTENTION,
                "Could not open this database: ${e.message}"
            )
            return DbVerifyResult(id, label, checks)
        }
        val sql: SupportSQLiteDatabase = quoteDb.openHelper.writableDatabase

        // --- Required tables ---
        if (!sql.tableExists("quotes")) {
            check(
                "Required tables", VerifyStatus.NEEDS_ATTENTION,
                "The 'quotes' table is missing — this file is not a readable " +
                    "quote database. Re-import it from a valid .db file."
            )
            return DbVerifyResult(id, label, checks)
        }

        // --- Clearly-bad rows: blank text is deleted; bad numbers are flagged ---
        val blankCount =
            sql.scalarCount("SELECT COUNT(*) FROM quotes WHERE text IS NULL OR TRIM(text) = ''")
        if (blankCount > 0) {
            sql.execSQL("DELETE FROM quotes WHERE text IS NULL OR TRIM(text) = ''")
            check(
                "Bad rows", VerifyStatus.FIXED,
                "Deleted $blankCount quote(s) with blank text."
            )
        } else {
            check("Bad rows", VerifyStatus.OK, "No blank-text rows.")
        }
        val badNumberCount =
            sql.scalarCount("SELECT COUNT(*) FROM quotes WHERE number <= 0")
        if (badNumberCount > 0) {
            check(
                "Quote numbers", VerifyStatus.NEEDS_ATTENTION,
                "$badNumberCount quote(s) have a number of 0 or less. Left in " +
                    "place — favorites and history may reference them; " +
                    "review manually before deleting."
            )
        } else {
            check("Quote numbers", VerifyStatus.OK, "All quote numbers are positive.")
        }

        // --- FTS index: must exist and match the quotes row count ---
        val quoteCount = sql.rowCount("quotes")
        if (!sql.tableExists(FTS_TABLE)) {
            // Same schema + sync triggers as tools/make_db.py, then populate.
            sql.execSQL(
                "CREATE VIRTUAL TABLE IF NOT EXISTS $FTS_TABLE " +
                    "USING fts5(text, content='quotes', content_rowid='number')"
            )
            sql.execSQL(
                "CREATE TRIGGER IF NOT EXISTS quotes_fts_ai AFTER INSERT ON quotes BEGIN " +
                    "INSERT INTO $FTS_TABLE(rowid, text) VALUES (new.number, new.text); END"
            )
            sql.execSQL(
                "CREATE TRIGGER IF NOT EXISTS quotes_fts_ad AFTER DELETE ON quotes BEGIN " +
                    "INSERT INTO $FTS_TABLE($FTS_TABLE, rowid, text) " +
                    "VALUES ('delete', old.number, old.text); END"
            )
            sql.execSQL(
                "CREATE TRIGGER IF NOT EXISTS quotes_fts_au AFTER UPDATE ON quotes BEGIN " +
                    "INSERT INTO $FTS_TABLE($FTS_TABLE, rowid, text) " +
                    "VALUES ('delete', old.number, old.text); " +
                    "INSERT INTO $FTS_TABLE(rowid, text) VALUES (new.number, new.text); END"
            )
            sql.rebuildFts()
            val ftsCount = sql.rowCount(FTS_TABLE)
            check(
                "Search index", VerifyStatus.FIXED,
                "The FTS index was missing — recreated and populated " +
                    "($ftsCount rows for $quoteCount quotes)."
            )
        } else {
            val ftsCount = sql.rowCount(FTS_TABLE)
            if (ftsCount == quoteCount) {
                check(
                    "Search index", VerifyStatus.OK,
                    "FTS index in sync ($ftsCount rows)."
                )
            } else {
                sql.rebuildFts()
                val after = sql.rowCount(FTS_TABLE)
                if (after == quoteCount) {
                    check(
                        "Search index", VerifyStatus.FIXED,
                        "FTS index was out of sync ($ftsCount rows vs " +
                            "$quoteCount quotes) — rebuilt."
                    )
                } else {
                    check(
                        "Search index", VerifyStatus.NEEDS_ATTENTION,
                        "FTS index out of sync ($ftsCount rows vs $quoteCount " +
                            "quotes) and the rebuild did not converge " +
                            "($after rows). The database file may be corrupt."
                    )
                }
            }
        }

        // --- Category tables (created on demand by the app; repair = create) ---
        val missingCatTables = CATEGORY_TABLES.filter { !sql.tableExists(it) }
        if (missingCatTables.isEmpty()) {
            check("Category tables", VerifyStatus.OK, "All category tables present.")
        } else {
            CategoryStore(quoteDb).ensureTables()
            val stillMissing = CATEGORY_TABLES.filter { !sql.tableExists(it) }
            if (stillMissing.isEmpty()) {
                check(
                    "Category tables", VerifyStatus.FIXED,
                    "Created missing tables: ${missingCatTables.joinToString(", ")}."
                )
            } else {
                check(
                    "Category tables", VerifyStatus.NEEDS_ATTENTION,
                    "Could not create tables: ${stillMissing.joinToString(", ")}."
                )
            }
        }

        // --- Sanity: catalog databases must be non-empty ---
        if (quoteCount == 0L && id.startsWith("asset:")) {
            check(
                "Quote count", VerifyStatus.NEEDS_ATTENTION,
                "This database contains no quotes — its data may " +
                    "have been lost. Re-download it in Settings → Databases."
            )
        } else {
            check(
                "Quote count", VerifyStatus.OK,
                "$quoteCount quote(s)." +
                    if (quoteCount == 0L) " (Empty is fine here — add quotes under My Quotes.)"
                    else ""
            )
        }
    } catch (e: Exception) {
        check(
            "Verification", VerifyStatus.NEEDS_ATTENTION,
            "Unexpected error while checking: ${e.message}"
        )
    }
    return DbVerifyResult(id, label, checks)
}
