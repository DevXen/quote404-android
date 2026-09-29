package com.quotebrowser.app.data

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Category ordering modes for [CategoryStore.orderMode].
 */
const val CATEGORY_ORDER_ALPHA = "alpha"
const val CATEGORY_ORDER_CUSTOM = "custom"

/**
 * One user-defined quote category (tag). Categories live inside the quote
 * database file itself (tables `categories` / `quote_categories`) so they
 * travel with the database; the bundled database ships pre-tagged.
 */
data class Category(
    val id: Long,
    val name: String,
    val quoteCount: Int = 0,
    /** RRGGBB chip color, or null = default chip style (off). */
    val colorHex: String? = null,
    /** v1.65: section grouping shown in the Categories screen and the
     * category pull-downs ("" = ungrouped / user-created). */
    val groupName: String = ""
)

/**
 * One auto-categorize phrase attached to a category. A phrase matches when
 * it appears in the quote text (case-insensitive). [wholeWord] phrases
 * only match on word boundaries, so "ass" doesn't tag "classic".
 */
data class AutoRule(
    val phrase: String,
    val wholeWord: Boolean = false
)

/** Name of the built-in system category seeded with offensive-word rules. */
const val OFFENSIVE_CATEGORY_NAME = "Offensive"

/**
 * Category CRUD plus quote <-> category mapping, executed as plain SQL
 * against the Room database's underlying connection. Room's schema
 * validation ignores these extra tables, so no migration is needed.
 *
 * All methods are suspend and run on Dispatchers.IO; never call from the
 * main thread.
 */
class CategoryStore(db: QuoteDatabase) {

    private val helper = db.openHelper

    private fun db(): SupportSQLiteDatabase = helper.writableDatabase

    /** Guards ensureTables migrations: the pragma check-then-ALTER is not
     * atomic, so concurrent first calls (e.g. per-row forQuote) could both
     * run ALTER and crash with "duplicate column name". */
    private val migrateLock = Any()

    suspend fun ensureTables() = withContext(Dispatchers.IO) {
        synchronized(migrateLock) { ensureTablesSync() }
    }

    private fun ensureTablesSync() {
        db().execSQL(
            "CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE)"
        )
        db().execSQL(
            "CREATE TABLE IF NOT EXISTS quote_categories (" +
                "quote_number INTEGER NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "PRIMARY KEY (quote_number, category_id))"
        )
        db().execSQL(
            "CREATE INDEX IF NOT EXISTS idx_qc_category " +
                "ON quote_categories(category_id)"
        )
        db().execSQL(
            "CREATE INDEX IF NOT EXISTS idx_qc_quote " +
                "ON quote_categories(quote_number)"
        )
        // is_auto column (v1.47): provenance of each quote<->category link —
        // 0 = added manually by the user, 1 = added by an auto-rule.
        // Rescans only ever remove auto links; manual links are sacred.
        val hasIsAuto = db().query("PRAGMA table_info(quote_categories)").use { c ->
            var found = false
            while (c.moveToNext()) {
                if (c.getString(1) == "is_auto") { found = true; break }
            }
            found
        }
        if (!hasIsAuto) {
            try {
                db().execSQL(
                    "ALTER TABLE quote_categories " +
                        "ADD COLUMN is_auto INTEGER NOT NULL DEFAULT 0"
                )
            } catch (_: android.database.sqlite.SQLiteException) { }
        }
        // sort_order column (added after the initial schema; guard with pragma).
        val hasSortOrder = db().query("PRAGMA table_info(categories)").use { c ->
            var found = false
            while (c.moveToNext()) {
                if (c.getString(1) == "sort_order") { found = true; break }
            }
            found
        }
        if (!hasSortOrder) {
            // try/catch as a second guard: another process/thread may have
            // added the column between the pragma check and now.
            try {
                db().execSQL(
                    "ALTER TABLE categories ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0"
                )
            } catch (_: android.database.sqlite.SQLiteException) { }
        }
        // color column (added later; guard with pragma like sort_order).
        val hasColor = db().query("PRAGMA table_info(categories)").use { c ->
            var found = false
            while (c.moveToNext()) {
                if (c.getString(1) == "color") { found = true; break }
            }
            found
        }
        if (!hasColor) {
            try {
                db().execSQL("ALTER TABLE categories ADD COLUMN color TEXT")
            } catch (_: android.database.sqlite.SQLiteException) { }
        }
        // group_name column (v1.65): section grouping for the category
        // list and pull-downs; guard with pragma like sort_order/color.
        val hasGroup = db().query("PRAGMA table_info(categories)").use { c ->
            var found = false
            while (c.moveToNext()) {
                if (c.getString(1) == "group_name") { found = true; break }
            }
            found
        }
        if (!hasGroup) {
            try {
                db().execSQL(
                    "ALTER TABLE categories ADD COLUMN group_name TEXT NOT NULL DEFAULT ''"
                )
            } catch (_: android.database.sqlite.SQLiteException) { }
        }
        db().execSQL(
            "CREATE TABLE IF NOT EXISTS category_settings (" +
                "key TEXT PRIMARY KEY, value TEXT NOT NULL)"
        )
        // Auto-categorize phrases: one row per (category, phrase).
        // whole_word = 1 means the phrase only matches on word boundaries.
        db().execSQL(
            "CREATE TABLE IF NOT EXISTS category_auto_rules (" +
                "category_id INTEGER NOT NULL, " +
                "phrase TEXT NOT NULL, " +
                "whole_word INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (category_id, phrase))"
        )
        ensureOffensiveCategorySync()
    }

    /**
     * Seeds the built-in "Offensive" category and its hidden phrase list
     * once per database. The seed flag keeps user edits (renames, removed
     * phrases, deleted category) from being clobbered.
     */
    private fun ensureOffensiveCategorySync() {
        val seeded = db().query(
            "SELECT value FROM category_settings WHERE key = 'offensive_seeded' LIMIT 1"
        ).use { c -> if (c.moveToFirst()) c.getString(0) == "1" else false }
        if (seeded) return
        db().execSQL(
            "INSERT OR IGNORE INTO categories (name) VALUES (?)",
            arrayOf<Any>(OFFENSIVE_CATEGORY_NAME)
        )
        val catId = db().query(
            "SELECT id FROM categories WHERE name = ? LIMIT 1",
            arrayOf<Any>(OFFENSIVE_CATEGORY_NAME)
        ).use { c -> if (c.moveToFirst()) c.getLong(0) else -1L }
        if (catId != -1L) {
            val ruleCount = db().query(
                "SELECT COUNT(*) FROM category_auto_rules WHERE category_id = ?",
                arrayOf<Any>(catId)
            ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
            if (ruleCount == 0) {
                db().beginTransaction()
                try {
                    for (phrase in OFFENSIVE_SEED_PHRASES) {
                        db().execSQL(
                            "INSERT OR IGNORE INTO category_auto_rules " +
                                "(category_id, phrase, whole_word) VALUES (?, ?, 1)",
                            arrayOf<Any>(catId, phrase)
                        )
                    }
                    db().setTransactionSuccessful()
                } finally {
                    db().endTransaction()
                }
            }
            db().execSQL(
                "INSERT OR REPLACE INTO category_settings (key, value) " +
                    "VALUES ('offensive_category_id', ?)",
                arrayOf<Any>(catId.toString())
            )
        }
        db().execSQL(
            "INSERT OR REPLACE INTO category_settings (key, value) " +
                "VALUES ('offensive_seeded', '1')"
        )
    }

    suspend fun list(): List<Category> = withContext(Dispatchers.IO) {
        ensureTables()
        val orderBy = if (orderModeSync() == CATEGORY_ORDER_CUSTOM) {
            "ORDER BY c.sort_order, c.name COLLATE NOCASE"
        } else {
            "ORDER BY c.name COLLATE NOCASE"
        }
        val out = mutableListOf<Category>()
        db().query(
            "SELECT c.id, c.name, c.color, c.group_name, COUNT(qc.quote_number) " +
                "FROM categories c LEFT JOIN quote_categories qc " +
                "ON qc.category_id = c.id " +
                "GROUP BY c.id $orderBy"
        ).use { c -> readCategories(c, out, withCount = true) }
        out
    }

    /** Non-suspending read of the order mode; call only from Dispatchers.IO. */
    private fun orderModeSync(): String {
        db().query(
            "SELECT value FROM category_settings WHERE key = 'order_mode' LIMIT 1"
        ).use { c ->
            if (c.moveToFirst()) return c.getString(0) ?: CATEGORY_ORDER_ALPHA
        }
        return CATEGORY_ORDER_ALPHA
    }

    /** Current ordering mode: [CATEGORY_ORDER_ALPHA] or [CATEGORY_ORDER_CUSTOM]. */
    suspend fun orderMode(): String = withContext(Dispatchers.IO) {
        ensureTables()
        orderModeSync()
    }

    /** Switch the ordering mode without changing any positions. */
    suspend fun setOrderMode(mode: String) = withContext(Dispatchers.IO) {
        ensureTables()
        db().execSQL(
            "INSERT OR REPLACE INTO category_settings (key, value) VALUES ('order_mode', ?)",
            arrayOf<Any>(mode)
        )
    }

    /**
     * Persist a full custom ordering (first id = top of the list) and
     * switch the mode to [CATEGORY_ORDER_CUSTOM].
     */
    suspend fun setOrder(ids: List<Long>) = withContext(Dispatchers.IO) {
        ensureTables()
        val s = db()
        s.beginTransaction()
        try {
            ids.forEachIndexed { index, id ->
                s.execSQL(
                    "UPDATE categories SET sort_order = ? WHERE id = ?",
                    arrayOf<Any>(index, id)
                )
            }
            s.execSQL(
                "INSERT OR REPLACE INTO category_settings (key, value) VALUES ('order_mode', ?)",
                arrayOf<Any>(CATEGORY_ORDER_CUSTOM)
            )
            s.setTransactionSuccessful()
        } finally {
            s.endTransaction()
        }
    }

    suspend fun add(name: String): Long = withContext(Dispatchers.IO) {
        ensureTables()
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Category name must not be blank" }
        db().execSQL("INSERT OR IGNORE INTO categories (name) VALUES (?)", arrayOf<Any>(clean))
        val id = db().query("SELECT id FROM categories WHERE name = ?", arrayOf<Any>(clean)).use { c ->
            if (c.moveToFirst()) c.getLong(0) else -1L
        }
        // In custom mode a new category goes at the end of the list
        // instead of sort_order 0 (which would put it on top).
        if (id != -1L && orderModeSync() == CATEGORY_ORDER_CUSTOM) {
            val maxOrder = db().query("SELECT COALESCE(MAX(sort_order), -1) FROM categories").use { c ->
                if (c.moveToFirst()) c.getInt(0) else -1
            }
            db().execSQL(
                "UPDATE categories SET sort_order = ? WHERE id = ? AND sort_order <= 0",
                arrayOf<Any>(maxOrder + 1, id)
            )
        }
        id
    }

    suspend fun rename(id: Long, name: String) = withContext(Dispatchers.IO) {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Category name must not be blank" }
        db().execSQL(
            "UPDATE OR IGNORE categories SET name = ? WHERE id = ?",
            arrayOf<Any>(clean, id)
        )
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db().execSQL("DELETE FROM quote_categories WHERE category_id = ?", arrayOf<Any>(id))
        db().execSQL("DELETE FROM categories WHERE id = ?", arrayOf<Any>(id))
    }

    /** Set a category's chip color (RRGGBB), or null to use the default chip style. */
    suspend fun setColor(id: Long, hex: String?) = withContext(Dispatchers.IO) {
        ensureTables()
        if (hex == null) {
            db().execSQL("UPDATE categories SET color = NULL WHERE id = ?", arrayOf<Any>(id))
        } else {
            val clean = hex.trim().removePrefix("#").uppercase().take(6)
            db().execSQL(
                "UPDATE categories SET color = ? WHERE id = ?",
                arrayOf<Any>(clean, id)
            )
        }
    }

    /** Category with this exact name, or null (used for cross-database filtering). */
    suspend fun findByName(name: String): Category? = withContext(Dispatchers.IO) {
        ensureTables()
        var found: Category? = null
        db().query(
            "SELECT id, name, color, group_name FROM categories WHERE name = ? LIMIT 1",
            arrayOf<Any>(name)
        ).use { c ->
            if (c.moveToFirst()) found = Category(
                id = c.getLong(0),
                name = c.getString(1),
                colorHex = c.getString(2),
                groupName = c.getString(3) ?: ""
            )
        }
        found
    }

    /** Drop all category mappings for a deleted quote. */
    suspend fun removeQuote(number: Int) = withContext(Dispatchers.IO) {
        ensureTables()
        db().execSQL("DELETE FROM quote_categories WHERE quote_number = ?", arrayOf<Any>(number))
    }

    /** Categories assigned to one quote, alphabetical. */
    suspend fun forQuote(number: Int): List<Category> = withContext(Dispatchers.IO) {
        ensureTables()
        val out = mutableListOf<Category>()
        db().query(
            "SELECT c.id, c.name, c.color, c.group_name FROM categories c " +
                "JOIN quote_categories qc ON qc.category_id = c.id " +
                "WHERE qc.quote_number = ? ORDER BY c.name COLLATE NOCASE",
            arrayOf<Any>(number)
        ).use { c -> readCategories(c, out, withCount = false) }
        out
    }

    /**
     * Replace the category set of one quote.
     * @param auto provenance for newly added links (user actions pass
     * false, the default); links that were already auto and stay selected
     * keep their auto provenance.
     */
    suspend fun setForQuote(number: Int, categoryIds: Set<Long>, auto: Boolean = false) =
        withContext(Dispatchers.IO) {
            ensureTables()
            val s = db()
            val keepAuto = mutableSetOf<Long>()
            s.query(
                "SELECT category_id FROM quote_categories " +
                    "WHERE quote_number = ? AND is_auto = 1",
                arrayOf<Any>(number)
            ).use { c ->
                while (c.moveToNext()) keepAuto.add(c.getLong(0))
            }
            s.beginTransaction()
            try {
                s.execSQL(
                    "DELETE FROM quote_categories WHERE quote_number = ?",
                    arrayOf<Any>(number)
                )
                for (id in categoryIds) {
                    val isAuto = if (id in keepAuto || auto) 1 else 0
                    s.execSQL(
                        "INSERT OR IGNORE INTO quote_categories " +
                            "(quote_number, category_id, is_auto) " +
                            "VALUES (?, ?, ?)",
                        arrayOf<Any>(number, id, isAuto)
                    )
                }
                s.setTransactionSuccessful()
            } finally {
                s.endTransaction()
            }
        }

    /**
     * Add category mappings for one quote without removing its existing
     * ones (bulk tagging). Duplicate mappings are ignored.
     * @param auto true when the links come from an auto-rule (provenance
     * flag); user actions always pass false (the default).
     */
    suspend fun addMappings(number: Int, categoryIds: Collection<Long>, auto: Boolean = false) =
        withContext(Dispatchers.IO) {
            ensureTables()
            if (categoryIds.isEmpty()) return@withContext
            val s = db()
            s.beginTransaction()
            try {
                for (id in categoryIds) {
                    s.execSQL(
                        "INSERT OR IGNORE INTO quote_categories " +
                            "(quote_number, category_id, is_auto) " +
                            "VALUES (?, ?, ?)",
                        arrayOf<Any>(number, id, if (auto) 1 else 0)
                    )
                }
                s.setTransactionSuccessful()
            } finally {
                s.endTransaction()
            }
        }

    suspend fun countInCategory(id: Long): Int = withContext(Dispatchers.IO) {        ensureTables()
        db().query(
            "SELECT COUNT(*) FROM quote_categories WHERE category_id = ?",
            arrayOf<Any>(id)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    suspend fun quoteNumbersInCategory(id: Long): List<Int> =
        withContext(Dispatchers.IO) {
            ensureTables()
            val out = mutableListOf<Int>()
            db().query(
                "SELECT quote_number FROM quote_categories WHERE category_id = ? " +
                    "ORDER BY quote_number",
                arrayOf<Any>(id)
            ).use { c ->
                while (c.moveToNext()) out.add(c.getInt(0))
            }
            out
        }

    private fun readCategories(c: Cursor, out: MutableList<Category>, withCount: Boolean) {
        while (c.moveToNext()) {
            out.add(
                Category(
                    id = c.getLong(0),
                    name = c.getString(1) ?: "",
                    colorHex = c.getString(2),
                    groupName = c.getString(3) ?: "",
                    quoteCount = if (withCount) c.getInt(4) else 0
                )
            )
        }
    }

    /**
     * Version of the prebuilt tag data already imported into this database
     * (0 = never). Backed by a tiny table so it travels with the database.
     */
    suspend fun seedVersion(): Int = withContext(Dispatchers.IO) {
        ensureTables()
        val hasTable = db().query(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' " +
                "AND name = 'category_seed_version'"
        ).use { it.moveToFirst() }
        if (!hasTable) return@withContext 0
        db().query("SELECT version FROM category_seed_version LIMIT 1").use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    /**
     * Auto-categorize phrases for one category, in insertion order. Every
     * category's list — including the built-in "Offensive" one — is
     * user-manageable: phrases are shown in the category editor and can be
     * added or removed there.
     */
    suspend fun autoRules(categoryId: Long): List<AutoRule> =
        withContext(Dispatchers.IO) {
            ensureTables()
            val out = mutableListOf<AutoRule>()
            db().query(
                "SELECT phrase, whole_word FROM category_auto_rules " +
                    "WHERE category_id = ? ORDER BY rowid",
                arrayOf<Any>(categoryId)
            ).use { c ->
                while (c.moveToNext()) {
                    out.add(AutoRule(c.getString(0) ?: "", c.getInt(1) != 0))
                }
            }
            out
        }

    /** All auto-rule phrases of a category (built-in seed included). */
    suspend fun autoRulePhrases(categoryId: Long): List<String> =
        autoRules(categoryId).map { it.phrase }

    /** True when this is the built-in "Offensive" system category. */
    suspend fun isOffensiveCategory(categoryId: Long): Boolean =
        withContext(Dispatchers.IO) {
            ensureTables()
            isOffensiveCategorySync(categoryId)
        }

    private fun isOffensiveCategorySync(categoryId: Long): Boolean {
        val id = db().query(
            "SELECT value FROM category_settings " +
                "WHERE key = 'offensive_category_id' LIMIT 1"
        ).use { c -> if (c.moveToFirst()) c.getString(0)?.toLongOrNull() else null }
        return id == categoryId
    }

    /**
     * Replaces a category's auto-rule phrase list. Match mode follows the
     * category: the built-in "Offensive" list matches whole-word (so
     * "ass" doesn't tag "classic"); every other category matches
     * case-insensitive substrings.
     */
    suspend fun setAutoRulePhrases(categoryId: Long, phrases: List<String>) =
        withContext(Dispatchers.IO) {
            ensureTables()
            val clean = phrases.map { it.trim() }
                .filter { it.isNotEmpty() }.distinct()
            val wholeWord = if (isOffensiveCategorySync(categoryId)) 1 else 0
            val s = db()
            s.beginTransaction()
            try {
                s.execSQL(
                    "DELETE FROM category_auto_rules WHERE category_id = ?",
                    arrayOf<Any>(categoryId)
                )
                for (phrase in clean) {
                    s.execSQL(
                        "INSERT OR IGNORE INTO category_auto_rules " +
                            "(category_id, phrase, whole_word) VALUES (?, ?, ?)",
                        arrayOf<Any>(categoryId, phrase, wholeWord)
                    )
                }
                s.setTransactionSuccessful()
            } finally {
                s.endTransaction()
            }
        }

    /** Adds one phrase to a category (match mode follows the category). */
    suspend fun addAutoRule(categoryId: Long, phrase: String) =
        withContext(Dispatchers.IO) {
            ensureTables()
            val clean = phrase.trim()
            if (clean.isEmpty()) return@withContext
            val wholeWord = if (isOffensiveCategorySync(categoryId)) 1 else 0
            db().execSQL(
                "INSERT OR IGNORE INTO category_auto_rules " +
                    "(category_id, phrase, whole_word) VALUES (?, ?, ?)",
                arrayOf<Any>(categoryId, clean, wholeWord)
            )
        }

    /** Every (category, phrase, whole-word) rule in this database. */
    private fun allRulesSync(): List<Triple<Long, String, Boolean>> {
        val out = mutableListOf<Triple<Long, String, Boolean>>()
        db().query(
            "SELECT category_id, phrase, whole_word FROM category_auto_rules " +
                "ORDER BY category_id, rowid"
        ).use { c ->
            while (c.moveToNext()) {
                val phrase = c.getString(1) ?: continue
                if (phrase.isBlank()) continue
                out.add(Triple(c.getLong(0), phrase, c.getInt(2) != 0))
            }
        }
        return out
    }

    private fun escapeLike(s: String): String {
        val b = StringBuilder(s.length)
        for (ch in s) {
            if (ch == '\\' || ch == '%' || ch == '_') b.append('\\')
            b.append(ch)
        }
        return b.toString()
    }

    private fun wholeWordRegex(phrase: String): Regex =
        Regex(
            "(?<![\\p{L}\\p{Nd}])" + Regex.escape(phrase) +
                "(?![\\p{L}\\p{Nd}])",
            RegexOption.IGNORE_CASE
        )

    /**
     * Result of an auto-rule run: how many quote<->category links were
     * added and how many stale auto links were removed.
     */
    data class AutoRuleResult(val added: Int, val removed: Int) {
        operator fun plus(other: AutoRuleResult) =
            AutoRuleResult(added + other.added, removed + other.removed)
    }

    /**
     * Runs this database's auto-categorize rules and RECONCILES each
     * category that has rules: quotes matching the current phrases are
     * linked (provenance = auto), and auto-linked quotes that no longer
     * match are unlinked. Links the user added manually (provenance =
     * manual) are never touched. When [numbers] is given only those quote
     * numbers are considered (used for newly imported quotes); otherwise
     * every quote is scanned. Reports per-category progress via
     * [onProgress] so callers can show a progress bar.
     */
    suspend fun applyAutoRules(
        numbers: List<Int>? = null,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): AutoRuleResult = withContext(Dispatchers.IO) {
        ensureTables()
        val byCategory = allRulesSync().groupBy({ it.first }, { it })
        if (byCategory.isEmpty()) return@withContext AutoRuleResult(0, 0)
        var added = 0
        var removed = 0
        val s = db()
        val quoteScope =
            if (!numbers.isNullOrEmpty()) " AND number IN (${numbers.joinToString(",")})" else ""
        val linkScope =
            if (!numbers.isNullOrEmpty()) " AND quote_number IN (${numbers.joinToString(",")})" else ""
        byCategory.entries.forEachIndexed { index, (catId, rules) ->
            // 1. Quotes matching any of this category's phrases.
            val matched = mutableSetOf<Int>()
            for ((_, phrase, wholeWord) in rules) {
                val likePattern = "%${escapeLike(phrase.lowercase())}%"
                s.query(
                    "SELECT number, text FROM quotes " +
                        "WHERE LOWER(text) LIKE ? ESCAPE '\\'$quoteScope",
                    arrayOf<Any>(likePattern)
                ).use { c ->
                    if (wholeWord) {
                        val rx = wholeWordRegex(phrase)
                        while (c.moveToNext()) {
                            val text = c.getString(1) ?: continue
                            if (rx.containsMatchIn(text)) matched.add(c.getInt(0))
                        }
                    } else {
                        while (c.moveToNext()) matched.add(c.getInt(0))
                    }
                }
            }
            // 2. Current links (any provenance) and auto links, in scope.
            val linked = mutableSetOf<Int>()
            val autoLinked = mutableSetOf<Int>()
            s.query(
                "SELECT quote_number, is_auto FROM quote_categories " +
                    "WHERE category_id = ?$linkScope",
                arrayOf<Any>(catId)
            ).use { c ->
                while (c.moveToNext()) {
                    linked.add(c.getInt(0))
                    if (c.getInt(1) != 0) autoLinked.add(c.getInt(0))
                }
            }
            val toAdd = matched - linked
            val toRemove = autoLinked - matched
            if (toAdd.isNotEmpty() || toRemove.isNotEmpty()) {
                s.beginTransaction()
                try {
                    for (n in toAdd) {
                        s.execSQL(
                            "INSERT OR IGNORE INTO quote_categories " +
                                "(quote_number, category_id, is_auto) " +
                                "VALUES (?, ?, 1)",
                            arrayOf<Any>(n, catId)
                        )
                    }
                    for (n in toRemove) {
                        s.execSQL(
                            "DELETE FROM quote_categories " +
                                "WHERE category_id = ? AND quote_number = ? " +
                                "AND is_auto = 1",
                            arrayOf<Any>(catId, n)
                        )
                    }
                    s.setTransactionSuccessful()
                } finally {
                    s.endTransaction()
                }
                added += toAdd.size
                removed += toRemove.size
            }
            onProgress(index + 1, byCategory.size)
        }
        AutoRuleResult(added, removed)
    }

/**
 * Built-in seed phrases for the "Offensive" category: severe racial/ethnic
 * slurs plus the strongest profanity. All are whole-word matched. The list
 * is user-manageable from the category editor like any other phrase list;
 * it must never be written to logs.
 */
private val OFFENSIVE_SEED_PHRASES: List<String> = listOf(
    // Severe racial/ethnic slurs.
    "nigger", "niggers", "nigga", "niggas",
    "chink", "chinks",
    "gook", "gooks",
    "spic", "spics",
    "kike", "kikes",
    "wetback", "wetbacks",
    "coon", "coons",
    "darkie", "darkies",
    "jigaboo", "jigaboos",
    "sambo", "sambos",
    "towelhead", "towelheads",
    "raghead", "ragheads",
    "paki", "pakis",
    "wog", "wogs",
    "hebe", "hebes",
    "yid", "yids",
    "kraut", "krauts",
    "camel jockey",
    // Strongest profanity.
    "fuck", "fucking", "fucked", "fucker", "fuckers",
    "motherfucker", "motherfuckers",
    "shit", "shite",
    "bitch", "bitches",
    "bastard", "bastards",
    "asshole", "assholes", "arsehole", "arseholes",
    "dick", "dicks",
    "cunt", "cunts",
    "cock", "cocks", "cocksucker",
    "twat", "twats",
    "wanker", "wankers",
    "bollocks",
    "piss", "pissed",
    "whore", "whores",
    "slut", "sluts"
)

    /**
     * Imports prebuilt category tags from the bundled seed JSON. Safe to
     * call repeatedly: categories are matched by name (INSERT OR IGNORE)
     * and tag rows are idempotent, so user edits are never wiped.
     */
    suspend fun seedFromJson(json: String, version: Int) =
        withContext(Dispatchers.IO) {
            ensureTables()
            val obj = JSONObject(json)
            val names = obj.getJSONArray("categories")
            val tags = obj.getJSONArray("tags")
            // v1.65 (seed v4): entries are {"name":..., "group":...} objects;
            // older seeds are bare name strings.
            fun entryName(i: Int): String {
                val v = names.get(i)
                return if (v is JSONObject) v.getString("name") else names.getString(i)
            }
            fun entryGroup(i: Int): String {
                val v = names.get(i)
                return if (v is JSONObject) v.optString("group", "") else ""
            }
            val s = db()
            s.beginTransaction()
            try {
                val nameToId = mutableMapOf<String, Long>()
                for (i in 0 until names.length()) {
                    val name = entryName(i)
                    val group = entryGroup(i)
                    s.execSQL(
                        "INSERT OR IGNORE INTO categories (name, group_name) VALUES (?, ?)",
                        arrayOf<Any>(name, group)
                    )
                    // Backfill the group on rows the seed created earlier
                    // (or the user renamed into place); never touch a group
                    // the user set themselves on a non-seed row... seed rows
                    // are authoritative for their own group.
                    if (group.isNotEmpty()) {
                        s.execSQL(
                            "UPDATE categories SET group_name = ? WHERE name = ? " +
                                "AND (group_name IS NULL OR group_name = '')",
                            arrayOf<Any>(group, name)
                        )
                    }
                    s.query(
                        "SELECT id FROM categories WHERE name = ?",
                        arrayOf<Any>(name)
                    ).use { c ->
                        if (c.moveToFirst()) nameToId[name] = c.getLong(0)
                    }
                }
                for (i in 0 until tags.length()) {
                    val pair = tags.getJSONArray(i)
                    val number = pair.getInt(0)
                    val catName = entryName(pair.getInt(1))
                    val catId = nameToId[catName] ?: continue
                    s.execSQL(
                        "INSERT OR IGNORE INTO quote_categories " +
                            "(quote_number, category_id) VALUES (?, ?)",
                        arrayOf<Any>(number, catId)
                    )
                }
                s.execSQL(
                    "CREATE TABLE IF NOT EXISTS category_seed_version " +
                        "(version INTEGER NOT NULL)"
                )
                s.execSQL("DELETE FROM category_seed_version")
                s.execSQL(
                    "INSERT INTO category_seed_version (version) VALUES (?)",
                    arrayOf<Any>(version)
                )
                s.setTransactionSuccessful()
            } finally {
                s.endTransaction()
            }
        }
} // class CategoryStore
