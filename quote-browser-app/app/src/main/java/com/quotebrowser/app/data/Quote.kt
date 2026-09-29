package com.quotebrowser.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One quote from a quote database. The table layout matches the schema
 * documented in README.md so that bundled and imported databases are
 * interchangeable: quotes(number INTEGER PRIMARY KEY, rating INTEGER,
 * text TEXT NOT NULL, url TEXT NOT NULL, status TEXT NOT NULL
 * DEFAULT 'approved'). Rating is nullable — deleted quotes carry no
 * rating data and display as "—". Status is 'approved' for main-archive
 * quotes and 'queued' for submissions that never made the archive
 * (v1.50.1).
 * v1.65 adds content columns, rated offline by a heuristic classifier and
 * merged onto devices by the bundled-data refresh:
 * - `maturity` INTEGER NOT NULL DEFAULT 0: 0 Kid Safe, 1 Clean, 2 PG-13,
 *   3 Rated R, 4 NC-17.
 * - `spicy` INTEGER NOT NULL DEFAULT 0: 1 when the quote carries
 *   FML-/spicy-style sexual content (FML's own /spicy curation overrides
 *   the heuristic for FML stories).
 * - `speakers` INTEGER NOT NULL DEFAULT 0: distinct speaker nicks per
 *   quote, counted with the app's speaker patterns (0 = prose, none
 *   detected).
 */
@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey
    @ColumnInfo(name = "number")
    val number: Int,

    @ColumnInfo(name = "rating")
    val rating: Int?,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "url")
    val url: String,

    // v1.50.2: the DDL default is declared verbatim so Room's schema
    // validation sees the same DEFAULT 'approved' that the bundled asset,
    // the startup refresh, and Migration(1,2) all install via
    // ALTER TABLE ... ADD COLUMN status TEXT NOT NULL DEFAULT 'approved'.
    @ColumnInfo(name = "status", defaultValue = "'approved'")
    val status: String = "approved",

    // v1.65: content columns. DDL defaults declared verbatim to match the
    // bundled assets and the ALTER TABLE statements the startup refresh
    // and Migration(2,3) install.
    @ColumnInfo(name = "maturity", defaultValue = "0")
    val maturity: Int = 0,

    @ColumnInfo(name = "spicy", defaultValue = "0")
    val spicy: Int = 0,

    @ColumnInfo(name = "speakers", defaultValue = "0")
    val speakers: Int = 0
)

/**
 * A quote together with the id of the database it came from.
 * v1.66: [alsoIn] carries the display labels of the other databases that
 * contain the same quote text (filled in by all-databases search dedup);
 * empty for single-database results.
 */
data class SourcedQuote(
    val dbId: String,
    val quote: Quote,
    val alsoIn: List<String> = emptyList()
)
