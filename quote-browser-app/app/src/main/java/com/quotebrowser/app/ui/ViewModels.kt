package com.quotebrowser.app.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.quotebrowser.app.data.AllDbPagingSource
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.CastSize
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.data.CategoryStore
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DatabaseThemeConfig
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.FlagInfo
import com.quotebrowser.app.data.MaturityLevel
import com.quotebrowser.app.data.TranslationRepository
import com.quotebrowser.app.data.FlaggedSourcedQuote
import com.quotebrowser.app.data.HistoryItem
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.QuoteRepository
import com.quotebrowser.app.data.SortOrder
import com.quotebrowser.app.data.LengthMode
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.data.StatusMode
import com.quotebrowser.app.ui.theme.CustomTheme
import com.quotebrowser.app.ui.theme.DevXenBgMode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Builds a repository for whichever database is currently selected. */
internal fun DatabaseManager.repoFor(id: String): QuoteRepository {
    // The virtual "all" view is only rendered by Browse; every other screen
    // falls back to the default single database.
    val realId = singleDbId(id)
    return QuoteRepository(getQuoteDatabase(realId), realId, metaDb.metaDao())
}

/** Category store for a database id (shares the cached Room instance). */
internal fun DatabaseManager.categoryStoreFor(id: String): CategoryStore =
    repoFor(id).categories

/** Activity-scoped: category list + CRUD, shared by every screen. */
class CategoryViewModel(private val manager: DatabaseManager) : ViewModel() {

    private val _tick = kotlinx.coroutines.flow.MutableStateFlow(0)

    val categories: StateFlow<List<Category>> =
        combine(manager.currentDbId, _tick) { id, _ -> id }
            .flatMapLatest { id ->
                kotlinx.coroutines.flow.flow {
                    emit(manager.categoryStoreFor(id).list())
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        _tick.value += 1
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value).add(name)
            refresh()
        }
    }

    fun renameCategory(id: Long, name: String) {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value).rename(id, name)
            refresh()
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value).delete(id)
            refresh()
        }
    }

    /** Set a category's chip color (RRGGBB), or null for the default chip style. */
    fun setCategoryColor(id: Long, hex: String?) {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value).setColor(id, hex)
            refresh()
        }
    }

    /** Category ordering mode for the current database (alpha or custom). */
    val orderMode: StateFlow<String> =
        combine(manager.currentDbId, _tick) { id, _ -> id }
            .flatMapLatest { id ->
                kotlinx.coroutines.flow.flow {
                    emit(manager.categoryStoreFor(id).orderMode())
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                com.quotebrowser.app.data.CATEGORY_ORDER_ALPHA
            )

    /** Persist a drag-and-drop ordering (first id = top of the list). */
    fun setOrder(ids: List<Long>) {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value).setOrder(ids)
            refresh()
        }
    }

    /** Go back to alphabetical ordering for the current database. */
    fun resetToAlphabetical() {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value)
                .setOrderMode(com.quotebrowser.app.data.CATEGORY_ORDER_ALPHA)
            refresh()
        }
    }

    // ---- v1.47 auto-categorize rules ----

    /**
     * Auto-rule phrases of one category. Every category's list — including
     * the built-in "Offensive" one — is user-manageable.
     */
    suspend fun getAutoRulePhrases(categoryId: Long): List<String> =
        manager.categoryStoreFor(manager.currentDbId.value)
            .autoRulePhrases(categoryId)

    /** Replaces a category's auto-rule phrases. */
    fun saveAutoRulePhrases(categoryId: Long, phrases: List<String>) {
        viewModelScope.launch {
            manager.categoryStoreFor(manager.currentDbId.value)
                .setAutoRulePhrases(categoryId, phrases)
        }
    }

    /**
     * Read-aloud playlist entry point: every quote in one category of the
     * current database as [SourcedQuote]s, honoring the content filter,
     * "Never show again", and muted nicks. Runs off the UI thread.
     */
    suspend fun readAloudQuotes(categoryId: Long): List<SourcedQuote> =
        withContext(Dispatchers.IO) {
            val dbId = manager.currentDbId.value
            val repo = manager.repoFor(dbId)
            val numbers =
                manager.categoryStoreFor(dbId).quoteNumbersInCategory(categoryId)
            numbers.mapNotNull { number ->
                try {
                    val quote = repo.getQuote(number) ?: return@mapNotNull null
                    if (manager.isQuoteHiddenFromLists(dbId, number, quote.text)) {
                        null
                    } else {
                        SourcedQuote(dbId, quote)
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }

    /** "Apply auto-rules now" sweep state (one tap scans every database). */
    sealed interface AutoRuleSweep {
        data object Idle : AutoRuleSweep
        data class Running(val label: String, val done: Int, val total: Int) :
            AutoRuleSweep
        data class Done(val added: Int, val removed: Int) : AutoRuleSweep
    }

    private val _autoRuleSweep =
        kotlinx.coroutines.flow.MutableStateFlow<AutoRuleSweep>(AutoRuleSweep.Idle)
    val autoRuleSweep: StateFlow<AutoRuleSweep> = _autoRuleSweep

    init {
        // Surface the one-time v1.47 upgrade sweep (first automatic
        // tagging pass) in the same progress UI as the manual run, so the
        // user sees it working and can't start a second sweep on top of it.
        viewModelScope.launch {
            manager.upgradeAutoRules.collect { u ->
                when (u) {
                    is DatabaseManager.UpgradeAutoRules.Running -> {
                        if (_autoRuleSweep.value is AutoRuleSweep.Idle) {
                            _autoRuleSweep.value = AutoRuleSweep.Running(
                                label = if (u.label.isNotBlank())
                                    "First-time tagging: ${u.label}"
                                else "First-time tagging…",
                                done = u.done,
                                total = u.total
                            )
                        }
                    }
                    is DatabaseManager.UpgradeAutoRules.Done -> {
                        if (_autoRuleSweep.value is AutoRuleSweep.Running) {
                            _autoRuleSweep.value =
                                AutoRuleSweep.Done(u.added, u.removed)
                        }
                    }
                    is DatabaseManager.UpgradeAutoRules.Idle -> {}
                }
            }
        }
    }

    /**
     * Runs every database's auto-categorize rules over all quotes (user
     * phrases + the built-in "Offensive" list), off the UI thread, with
     * per-database progress. Reconciles: adds new matches and removes
     * stale auto links (manual links are never removed). Reports the
     * added/removed counts when done.
     */
    fun applyAllAutoRulesNow() {
        if (_autoRuleSweep.value is AutoRuleSweep.Running) return
        viewModelScope.launch {
            _autoRuleSweep.value = AutoRuleSweep.Running("", 0, 1)
            val result = manager.applyAllAutoRules { label, done, total ->
                _autoRuleSweep.value = AutoRuleSweep.Running(label, done, total)
            }
            _autoRuleSweep.value = AutoRuleSweep.Done(result.added, result.removed)
            refresh()
        }
    }

    fun dismissAutoRuleSweep() {
        _autoRuleSweep.value = AutoRuleSweep.Idle
    }
}

/** Shared favorites helpers for the quote-list screens. */
open class QuoteListViewModel(protected val manager: DatabaseManager) : ViewModel() {

    /** The database id these lists are currently showing. */
    val dbId: StateFlow<String> = manager.currentDbId

    /**
     * Whether the current database has any rated quotes. The 2011 archives
     * carry no ratings, so a min-rating filter on them can only ever show an
     * empty list; screens use this to show "No ratings available" instead.
     * The merged "All databases" view always counts as rated (Bash.org and
     * #CrapShack have ratings).
     */
    open val dbHasRatings: StateFlow<Boolean> =
        manager.currentDbId.flatMapLatest { id ->
            kotlinx.coroutines.flow.flow {
                emit(
                    if (id == DatabaseManager.ALL_DBS_ID) true
                    else manager.repoFor(id).hasRatings()
                )
                // v1.88.11: repoFor opens the database; keep that I/O off
                // the main thread.
            }.flowOn(kotlinx.coroutines.Dispatchers.IO)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)

    /** Local thumbs-up/down vote (+1 / -1 / 0 to clear). */
    fun rateQuote(dbId: String, number: Int, delta: Int) {
        manager.setLocalRating(dbId, number, delta)
    }

    val favoriteNumbers: StateFlow<Set<Int>> =
        manager.currentDbId.flatMapLatest { id ->
            manager.metaDb.metaDao().favoriteEntries(id).map { entries ->
                entries.map { it.number }.toSet()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * Favorite (dbId, number) keys across every database. Used by the merged
     * "All databases" home view, where rows come from many databases.
     */
    val allFavoriteKeys: StateFlow<Set<Pair<String, Int>>> =
        manager.availableDatabases().flatMapLatest { dbs ->
            combine(
                dbs.map { db ->
                    manager.metaDb.metaDao().favoriteEntries(db.id)
                        .map { entries -> entries.map { db.id to it.number }.toSet() }
                }
            ) { sets -> sets.flatMap { it }.toSet() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleFavorite(quote: Quote) {
        toggleFavorite(manager.currentDbId.value, quote)
    }

    fun toggleFavorite(dbId: String, quote: Quote) {
        viewModelScope.launch {
            manager.repoFor(dbId).toggleFavorite(quote)
        }
    }

    /**
     * Quote-length filter in words: null = no bound. Applies to the
     * visible list after hidden-quote filtering. Only used when
     * [lengthMode] is CUSTOM; the presets use Settings thresholds.
     */
    var minLength by mutableStateOf<Int?>(null)
        private set
    var maxLength by mutableStateOf<Int?>(null)
        private set

    /** Length preset: All (default), Longer, Shorter, 1 line, Custom. */
    var lengthMode by mutableStateOf(LengthMode.ALL)
        private set

    fun updateLengthMode(mode: LengthMode) {
        lengthMode = mode
    }

    fun updateMinLength(len: Int?) {
        minLength = len
    }

    fun updateMaxLength(len: Int?) {
        maxLength = len
    }

    /** "Never show again": hides this quote from every list. */
    fun hideQuote(dbId: String, number: Int) {
        manager.setQuoteHidden(dbId, number, true)
    }

    /** v1.53 report/flag: flag a quote with a reason (stored on-device only). */
    fun flagQuote(dbId: String, number: Int, reason: String, customText: String) {
        manager.flagQuote(dbId, number, reason, customText)
    }

    /** v1.53 report/flag: remove the flag from a quote. */
    fun unflagQuote(dbId: String, number: Int) {
        manager.unflagQuote(dbId, number)
    }

    /** v1.53 report/flag: all flagged quotes ("$dbId/$number" -> info). */
    val flaggedQuotes: StateFlow<Map<String, FlagInfo>> = manager.flaggedQuotes

    /** v1.53 report/flag: flagged quotes with details, for the review screen. */
    suspend fun flaggedQuoteDetails(): List<FlaggedSourcedQuote> =
        manager.flaggedQuoteDetails()

    fun clearFlaggedQuotes() = manager.clearFlaggedQuotes()

    /** v1.53 report/flag feature toggle (default ON). */
    val flagQuotesEnabled: StateFlow<Boolean> = manager.flagQuotesEnabled
    fun setFlagQuotesEnabled(enabled: Boolean) = manager.setFlagQuotesEnabled(enabled)

    /** v1.53 "hide flagged quotes" toggle (default OFF). */
    val hideFlaggedQuotes: StateFlow<Boolean> = manager.hideFlaggedQuotes
    fun setHideFlaggedQuotes(enabled: Boolean) = manager.setHideFlaggedQuotes(enabled)

    /**
     * "Pick up where you left off": remember the Browse scroll position
     * per database (Settings → Features toggle, default ON).
     */
    val browsePositionMemory: StateFlow<Boolean> = manager.browsePositionMemory
    fun setBrowsePositionMemory(enabled: Boolean) = manager.setBrowsePositionMemory(enabled)
}

class MainViewModel(private val manager: DatabaseManager) : ViewModel() {

    init {
        // Import the prebuilt category tags once per database, in the
        // background, so filters are populated early.
        viewModelScope.launch {
            manager.currentDbId.collect { manager.seedCategories(it) }
        }
    }

    val currentDbId: StateFlow<String> = manager.currentDbId

    /** Databases with their quote counts (shown in the switcher). */
    val databases: StateFlow<List<DbInfo>> = manager.browseDatabases()
        .mapLatest { list ->
            coroutineScope {
                list.map { db ->
                    async {
                        val count = try {
                            manager.quoteCount(db.id)
                        } catch (e: Exception) {
                            null
                        }
                        db.copy(quoteCount = count)
                    }
                }.awaitAll()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * v1.60: download-based database updates (Settings → Your data →
     * Database updates). Runs on IO; never throws.
     */
    fun getDbUpdateUrl(): String = manager.getDbUpdateUrl()
    fun setDbUpdateUrl(url: String) = manager.setDbUpdateUrl(url)
    suspend fun checkForDatabaseUpdates(): List<DatabaseManager.DbUpdateOutcome> =
        withContext(Dispatchers.IO) { manager.checkForDatabaseUpdates() }

    /**
     * v1.61: quote translations (own Room DB, cached once per language).
     * Provided to the whole tree as [LocalTranslations] in AppNav.
     */
    val translationRepo: TranslationRepository get() = manager.translations

    val currentLabel: StateFlow<String> =
        manager.currentDbId.map { id ->
            manager.currentLabel(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val showQuoteNumber: StateFlow<Boolean> = manager.showQuoteNumber
    val showRating: StateFlow<Boolean> = manager.showRating
    val showUrl: StateFlow<Boolean> = manager.showUrl
    val themeId: StateFlow<String> = manager.themeId

    fun setShowQuoteNumber(show: Boolean) = manager.setShowQuoteNumber(show)
    fun setShowRating(show: Boolean) = manager.setShowRating(show)
    fun setShowUrl(show: Boolean) = manager.setShowUrl(show)
    fun setThemeId(id: String) = manager.setThemeId(id)

    /** DevXen theme background-art placement (Settings → Themes). */
    val devXenBgMode: StateFlow<DevXenBgMode> = manager.devXenBgMode

    /**
     * v1.61.3: the user's own DevXen background picture. Copies a
     * SAF-picked image into app-private storage and stores the file name;
     * deletes the previous image when it is replaced.
     */
    val devXenCustomBg: StateFlow<String> = manager.devXenCustomBg

    fun setDevXenCustomBgImage(uri: android.net.Uri) {
        viewModelScope.launch {
            manager.saveDbBackgroundImage(uri).onSuccess { name ->
                val old = manager.devXenCustomBg.value
                if (old.isNotBlank()) manager.deleteDbBackgroundImage(old)
                manager.setDevXenCustomBg(name)
            }
        }
    }

    /** v1.61.3: removes the custom DevXen background (file + pref). */
    fun clearDevXenCustomBgImage() {
        val old = manager.devXenCustomBg.value
        if (old.isNotBlank()) {
            manager.deleteDbBackgroundImage(old)
            manager.setDevXenCustomBg("")
        }
    }
    fun setDevXenBgMode(mode: DevXenBgMode) = manager.setDevXenBgMode(mode)

    /** v1.55: "Show collection dates" removed — see the per-database info panel. */

    /** Quote-detail swipe: stay in the current database or walk all of them. */
    val detailSwipeAllDbs: StateFlow<Boolean> = manager.detailSwipeAllDbs
    fun setDetailSwipeAllDbs(all: Boolean) = manager.setDetailSwipeAllDbs(all)

    /** Quote-detail swipe order: "random" (default) or "ordered". */
    val detailSwipeOrder: StateFlow<String> = manager.detailSwipeOrder
    fun setDetailSwipeOrder(order: String) = manager.setDetailSwipeOrder(order)

    /**
     * Moves a database within the user-arranged order. [fromIndex]/[toIndex]
     * are positions in the switcher list excluding the pinned "All
     * databases" entry.
     */
    fun moveDatabase(fromIndex: Int, toIndex: Int) {
        val ids = databases.value
            .filter { it.id != DatabaseManager.ALL_DBS_ID }
            .map { it.id }
            .toMutableList()
        if (fromIndex !in ids.indices || toIndex !in ids.indices) return
        val moved = ids.removeAt(fromIndex)
        ids.add(toIndex, moved)
        manager.setDbOrder(ids)
    }

    val customThemes: StateFlow<List<CustomTheme>> = manager.customThemes
    fun addCustomTheme(name: String, colors: String) = manager.addCustomTheme(name, colors)

    // v1.51: built-in theme overrides (presetId -> colors CSV) and the
    // per-database theme configuration.
    val builtInThemeOverrides: StateFlow<Map<String, String>> = manager.builtInThemeOverrides
    fun setBuiltInThemeOverride(presetId: String, colors: String) =
        manager.setBuiltInThemeOverride(presetId, colors)
    fun clearBuiltInThemeOverride(presetId: String) =
        manager.clearBuiltInThemeOverride(presetId)

    val perDbThemesEnabled: StateFlow<Boolean> = manager.perDbThemesEnabled
    fun setPerDbThemesEnabled(enabled: Boolean) = manager.setPerDbThemesEnabled(enabled)

    val masterThemeOverride: StateFlow<Boolean> = manager.masterThemeOverride
    fun setMasterThemeOverride(enabled: Boolean) = manager.setMasterThemeOverride(enabled)

    // v1.59: database themes only on quote cards (default OFF).
    val dbThemesCardsOnly: StateFlow<Boolean> = manager.dbThemesCardsOnly
    fun setDbThemesCardsOnly(enabled: Boolean) = manager.setDbThemesCardsOnly(enabled)

    // v1.59: Quote-of-the-Day notification source ("random_sources" | "favorites").
    val qotdSource: StateFlow<String> = manager.qotdSource
    fun setQotdSource(source: String) = manager.setQotdSource(source)

    val dbThemeConfigs: StateFlow<Map<String, DatabaseThemeConfig>> = manager.dbThemeConfigs
    fun dbThemeConfig(dbId: String): DatabaseThemeConfig = manager.dbThemeConfig(dbId)
    fun setDbThemeConfig(dbId: String, config: DatabaseThemeConfig) =
        manager.setDbThemeConfig(dbId, config)

    /**
     * v1.56: copies a SAF-picked image into app-private storage and stores
     * the file name on the database's theme config; deletes the previous
     * image when it is replaced.
     */
    fun setDbPageBackgroundImage(dbId: String, uri: android.net.Uri) {
        viewModelScope.launch {
            manager.saveDbBackgroundImage(uri).onSuccess { name ->
                val old = manager.dbThemeConfig(dbId)
                if (old.pageBackgroundImage.isNotBlank()) {
                    manager.deleteDbBackgroundImage(old.pageBackgroundImage)
                }
                manager.setDbThemeConfig(dbId, old.copy(pageBackgroundImage = name))
            }
        }
    }

    /** v1.56: removes the database's page background image (file + config). */
    fun clearDbPageBackgroundImage(dbId: String) {
        val old = manager.dbThemeConfig(dbId)
        if (old.pageBackgroundImage.isNotBlank()) {
            manager.deleteDbBackgroundImage(old.pageBackgroundImage)
            manager.setDbThemeConfig(dbId, old.copy(pageBackgroundImage = ""))
        }
    }
    fun statusModeFor(dbId: String): StatusMode? = manager.statusModeFor(dbId)

    // v1.51 feature toggles.
    val classicLayoutOption: StateFlow<Boolean> = manager.classicLayoutOption
    fun setClassicLayoutOption(enabled: Boolean) = manager.setClassicLayoutOption(enabled)
    val plusMinusRatingsEnabled: StateFlow<Boolean> = manager.plusMinusRatingsEnabled
    fun setPlusMinusRatingsEnabled(enabled: Boolean) = manager.setPlusMinusRatingsEnabled(enabled)
    val queuedTagsEnabled: StateFlow<Boolean> = manager.queuedTagsEnabled
    fun setQueuedTagsEnabled(enabled: Boolean) = manager.setQueuedTagsEnabled(enabled)
    val statusFilterEnabled: StateFlow<Boolean> = manager.statusFilterEnabled
    fun setStatusFilterEnabled(enabled: Boolean) = manager.setStatusFilterEnabled(enabled)
    val transformMode: StateFlow<String> = manager.transformMode
    fun setTransformMode(mode: TransformMode) = manager.setTransformMode(mode)

    // v1.53 feature toggles (all default ON except Kid-safe, default OFF).
    val myQuotesEnabled: StateFlow<Boolean> = manager.myQuotesEnabled
    fun setMyQuotesEnabled(enabled: Boolean) = manager.setMyQuotesEnabled(enabled)
    val perDbWidgetEnabled: StateFlow<Boolean> = manager.perDbWidgetEnabled
    fun setPerDbWidgetEnabled(enabled: Boolean) = manager.setPerDbWidgetEnabled(enabled)
    val memorizeModeEnabled: StateFlow<Boolean> = manager.memorizeModeEnabled
    fun setMemorizeModeEnabled(enabled: Boolean) = manager.setMemorizeModeEnabled(enabled)
    val onThisDayEnabled: StateFlow<Boolean> = manager.onThisDayEnabled
    fun setOnThisDayEnabled(enabled: Boolean) = manager.setOnThisDayEnabled(enabled)
    val kidSafeEnabled: StateFlow<Boolean> = manager.kidSafeEnabled
    fun setKidSafeEnabled(enabled: Boolean) = manager.setKidSafeEnabled(enabled)
    val siteShareCardsEnabled: StateFlow<Boolean> = manager.siteShareCardsEnabled
    fun setSiteShareCardsEnabled(enabled: Boolean) = manager.setSiteShareCardsEnabled(enabled)
    val explainQuoteEnabled: StateFlow<Boolean> = manager.explainQuoteEnabled
    fun setExplainQuoteEnabled(enabled: Boolean) = manager.setExplainQuoteEnabled(enabled)
    val ambientModeEnabled: StateFlow<Boolean> = manager.ambientModeEnabled
    fun setAmbientModeEnabled(enabled: Boolean) = manager.setAmbientModeEnabled(enabled)
    val ambientIntervalSec: StateFlow<Int> = manager.ambientIntervalSec
    fun setAmbientIntervalSec(seconds: Int) = manager.setAmbientIntervalSec(seconds)
    // v1.53 main-theme option: black background outside the quote cards.
    val blackOutsideQuotes: StateFlow<Boolean> = manager.blackOutsideQuotes
    fun setBlackOutsideQuotes(enabled: Boolean) = manager.setBlackOutsideQuotes(enabled)

    /** v1.53 report/flag feature toggle (default ON). */
    val flagQuotesEnabled: StateFlow<Boolean> = manager.flagQuotesEnabled
    fun setFlagQuotesEnabled(enabled: Boolean) = manager.setFlagQuotesEnabled(enabled)

    /** v1.53 "hide flagged quotes" toggle (default OFF). */
    val hideFlaggedQuotes: StateFlow<Boolean> = manager.hideFlaggedQuotes
    fun setHideFlaggedQuotes(enabled: Boolean) = manager.setHideFlaggedQuotes(enabled)

    /** v1.53 report/flag: flagged quotes ("$dbId/$number" -> info). */
    val flaggedQuotes: StateFlow<Map<String, FlagInfo>> = manager.flaggedQuotes

    /** v1.53 report/flag: flagged quotes with details, for the review UI. */
    suspend fun flaggedQuoteDetails(): List<FlaggedSourcedQuote> =
        manager.flaggedQuoteDetails()

    /** v1.53 report/flag: remove the flag from one quote. */
    fun unflagQuote(dbId: String, number: Int) = manager.unflagQuote(dbId, number)

    /** v1.53 report/flag: remove all flags (with confirmation in the UI). */
    fun clearFlaggedQuotes() = manager.clearFlaggedQuotes()

    // Database source-tag chip colors (dbId -> RRGGBB; missing = default style).
    val dbTagColors: StateFlow<Map<String, String>> = manager.dbTagColors
    fun setDbTagColor(dbId: String, hex: String?) = manager.setDbTagColor(dbId, hex)

    // Nick colors: per-person <nick> overrides + master toggle.
    val nickRules: StateFlow<List<NickColorRule>> = manager.nickRules
    val nickColoringEnabled: StateFlow<Boolean> = manager.nickColoringEnabled
    fun setNickColoringEnabled(enabled: Boolean) = manager.setNickColoringEnabled(enabled)
    fun addNickRule(nick: String, bracketsHex: String?, nameHex: String?): Boolean =
        manager.addNickRule(nick, bracketsHex, nameHex)
    fun updateNickRule(index: Int, nick: String, bracketsHex: String?, nameHex: String?): Boolean =
        manager.updateNickRule(index, nick, bracketsHex, nameHex)
    fun deleteNickRule(index: Int) = manager.deleteNickRule(index)

    // v1.63 nick color themes: presets (Quote404 logo default, Rainbow,
    // ...) plus user-created themes; each theme paints a nick's name,
    // brackets, and optionally what they say in that nick's own color.
    val nickColorThemeId: StateFlow<String> = manager.nickColorThemeId
    fun setNickColorThemeId(id: String) = manager.setNickColorThemeId(id)
    val customNickColorThemes: StateFlow<List<NickColorTheme>> =
        manager.customNickColorThemes
    fun addCustomNickColorTheme(name: String, colors: List<Int>): NickColorTheme? =
        manager.addCustomNickColorTheme(name, colors)
    fun updateCustomNickColorTheme(id: String, name: String, colors: List<Int>): Boolean =
        manager.updateCustomNickColorTheme(id, name, colors)
    fun deleteCustomNickColorTheme(id: String) = manager.deleteCustomNickColorTheme(id)
    // "Color what they say with the speaker's color" (off by default).
    val nickColorMessage: StateFlow<Boolean> = manager.nickColorMessage
    fun setNickColorMessage(enabled: Boolean) = manager.setNickColorMessage(enabled)

    // IRC " \" line separators -> real line breaks (default on).
    val ircLineBreaks: StateFlow<Boolean> = manager.ircLineBreaks
    fun setIrcLineBreaks(enabled: Boolean) = manager.setIrcLineBreaks(enabled)
    // Dialogue turns ("..." <nick>) -> separate lines (default on).
    val dialogueLineBreaks: StateFlow<Boolean> = manager.dialogueLineBreaks
    fun setDialogueLineBreaks(enabled: Boolean) = manager.setDialogueLineBreaks(enabled)

    /** v1.65 global content maturity ceiling. */
    val maturityCeiling: StateFlow<MaturityLevel> = manager.maturityCeiling
    fun setMaturityCeiling(level: MaturityLevel) = manager.setMaturityCeiling(level)
    // QuoteClean toggles (v1.47): name/action coloring, "/" normalization,
    // timestamp stripping. Wired to Settings -> Quote text.
    val qcColorNames: StateFlow<Boolean> = manager.qcColorNames
    fun setQcColorNames(enabled: Boolean) = manager.setQcColorNames(enabled)
    val qcColorActions: StateFlow<Boolean> = manager.qcColorActions
    fun setQcColorActions(enabled: Boolean) = manager.setQcColorActions(enabled)
    val qcNormalizeBreaks: StateFlow<Boolean> = manager.qcNormalizeBreaks
    fun setQcNormalizeBreaks(enabled: Boolean) = manager.setQcNormalizeBreaks(enabled)
    val qcShowTimestamps: StateFlow<Boolean> = manager.qcShowTimestamps
    fun setQcShowTimestamps(enabled: Boolean) = manager.setQcShowTimestamps(enabled)
    // v1.86: per-category QuoteClean color controls.
    val qcColorMessage: StateFlow<Boolean> = manager.qcColorMessage
    fun setQcColorMessage(enabled: Boolean) = manager.setQcColorMessage(enabled)
    val qcNameColor: StateFlow<String?> = manager.qcNameColor
    fun setQcNameColor(hex: String?) = manager.setQcNameColor(hex)
    val qcMessageColor: StateFlow<String?> = manager.qcMessageColor
    fun setQcMessageColor(hex: String?) = manager.setQcMessageColor(hex)
    val qcActionColor: StateFlow<String?> = manager.qcActionColor
    fun setQcActionColor(hex: String?) = manager.setQcActionColor(hex)
    val qcTimestampColor: StateFlow<String?> = manager.qcTimestampColor
    fun setQcTimestampColor(hex: String?) = manager.setQcTimestampColor(hex)
    val qcColorOther: StateFlow<Boolean> = manager.qcColorOther
    fun setQcColorOther(enabled: Boolean) = manager.setQcColorOther(enabled)
    val qcOtherColor: StateFlow<String?> = manager.qcOtherColor
    fun setQcOtherColor(hex: String?) = manager.setQcOtherColor(hex)
    fun updateCustomTheme(index: Int, name: String, colors: String) =
        manager.updateCustomTheme(index, name, colors)
    fun deleteCustomTheme(index: Int) = manager.deleteCustomTheme(index)

    val textScale: StateFlow<Float> = manager.textScale
    fun setTextScale(scale: Float) = manager.setTextScale(scale)

    val shakeToShuffle: StateFlow<Boolean> = manager.shakeToShuffle
    fun setShakeToShuffle(enabled: Boolean) = manager.setShakeToShuffle(enabled)

    /** Content filter: hide quotes containing words on the user's list. */
    val contentFilterEnabled: StateFlow<Boolean> = manager.contentFilterEnabled
    fun setContentFilterEnabled(enabled: Boolean) = manager.setContentFilterEnabled(enabled)

    /** The user's offensive-words list. */
    val offensiveWords: StateFlow<List<String>> = manager.offensiveWords

    /**
     * Adds a word to the offensive-words list. Returns false when the word
     * is blank or already listed.
     */
    fun addOffensiveWord(word: String): Boolean = manager.addOffensiveWord(word)

    /** Removes a word from the offensive-words list. */
    fun removeOffensiveWord(word: String) = manager.removeOffensiveWord(word)

    /** Nicks whose quotes are hidden everywhere "Never show again" applies. */
    val mutedNicks: StateFlow<Set<String>> = manager.mutedNicks
    fun addMutedNick(nick: String): Boolean = manager.addMutedNick(nick)
    fun removeMutedNick(nick: String) = manager.removeMutedNick(nick)
    fun clearMutedNicks() = manager.clearMutedNicks()

    /** Quote-body font choice: default, serif, monospace, or a font URI. */
    val quoteFont: StateFlow<String> = manager.quoteFont
    fun setQuoteFont(value: String) = manager.setQuoteFont(value)

    /**
     * Adds many words at once from pasted text (split on commas, newlines,
     * semicolons). Returns (added, skipped) where skipped counts blanks and
     * words already on the list.
     */
    fun addOffensiveWordsBulk(text: String): Pair<Int, Int> {
        val submitted = text.split(',', '\n', ';')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
        if (submitted.isEmpty()) return 0 to 0
        val added = manager.addOffensiveWords(submitted)
        return added to (submitted.size - added)
    }

    /** Removes every word from the offensive-words list. */
    fun clearOffensiveWords() = manager.clearOffensiveWords()

    /**
     * Adds a list of words (e.g. a suggested set). Returns (added, skipped)
     * where skipped counts words already on the list.
     */
    fun addOffensiveWordList(words: List<String>): Pair<Int, Int> {
        val distinct = words.map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
        if (distinct.isEmpty()) return 0 to 0
        val added = manager.addOffensiveWords(distinct)
        return added to (distinct.size - added)
    }

    /** Removes several words from the offensive-words list at once. */
    fun removeOffensiveWords(words: Collection<String>) {
        words.forEach { manager.removeOffensiveWord(it) }
    }

    /** Daily quote wallpaper: on/off (WorkManager) and quote source. */
    val wallpaperDailyEnabled: StateFlow<Boolean> = manager.wallpaperDailyEnabled
    val wallpaperSource: StateFlow<String> = manager.wallpaperSource
    fun setWallpaperDailyEnabled(enabled: Boolean) = manager.setWallpaperDailyEnabled(enabled)
    fun setWallpaperSource(source: String) = manager.setWallpaperSource(source)

    /** Sets the wallpaper right now; [onDone] gets a status message. */
    fun applyWallpaperNow(onDone: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val ok = try {
                com.quotebrowser.app.worker.WallpaperHelper.applyWallpaper(
                    manager.context,
                    manager
                )
            } catch (e: Exception) {
                false
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onDone(if (ok) "Wallpaper updated." else "Couldn't set the wallpaper.")
            }
        }
    }

    /** Daily reading goal (0 = off). */
    val dailyGoal: StateFlow<Int> = manager.dailyGoal

    /** Length-filter preset thresholds (words), configurable in Settings. */
    val lengthLongerWords: StateFlow<Int> = manager.lengthLongerWords
    val lengthShorterWords: StateFlow<Int> = manager.lengthShorterWords
    fun setLengthLongerWords(n: Int) = manager.setLengthLongerWords(n)
    fun setLengthShorterWords(n: Int) = manager.setLengthShorterWords(n)

    /** Auto-backup: "off", "daily", or "weekly". */
    fun setDailyGoal(n: Int) = manager.setDailyGoal(n)

    /** Read-aloud: a different TTS voice per speaker nick (default off). */
    val ttsPerNickVoices: StateFlow<Boolean> = manager.ttsPerNickVoices
    fun setTtsPerNickVoices(enabled: Boolean) = manager.setTtsPerNickVoices(enabled)

    /** Number of quotes hidden via "Never show again". */
    val hiddenQuoteCount: StateFlow<Int> =
        manager.hiddenQuoteKeys.map { it.size }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Raw "Never show again" keys, to refresh the management list. */
    val hiddenQuoteKeys: StateFlow<Set<String>> = manager.hiddenQuoteKeys

    /** Hidden quotes with details, for the Content Filter management screen. */
    suspend fun hiddenQuoteDetails() = manager.hiddenQuoteDetails()
    fun unhideQuote(dbId: String, number: Int) = manager.setQuoteHidden(dbId, number, false)
    fun clearHiddenQuotes() = manager.clearHiddenQuotes()

    /** Auto-backup: "off", "daily", or "weekly". */
    val autoBackupFreq: StateFlow<String> = manager.autoBackupFreq
    fun setAutoBackupFreq(freq: String) = manager.setAutoBackupFreq(freq)

    val quotePingEnabled: StateFlow<Boolean> = manager.quotePingEnabled
    val quotePingMinutes: StateFlow<Long> = manager.quotePingMinutes
    val quotePingChoices: List<Long> = manager.quotePingChoices
    fun quotePingLabel(minutes: Long): String = manager.quotePingLabel(minutes)
    fun setQuotePingEnabled(enabled: Boolean) = manager.setQuotePingEnabled(enabled)
    fun setQuotePingMinutes(minutes: Long) = manager.setQuotePingMinutes(minutes)
    val quotePingHour: StateFlow<Int> = manager.quotePingHour
    val quotePingMinute: StateFlow<Int> = manager.quotePingMinute
    fun setQuotePingTime(hour: Int, minute: Int) = manager.setQuotePingTime(hour, minute)
    val quotePingDay: StateFlow<Int> = manager.quotePingDay
    fun setQuotePingDay(day: Int) = manager.setQuotePingDay(day)
    val quotePingDbIds: StateFlow<Set<String>?> = manager.quotePingDbIds
    fun setQuotePingDbIds(ids: Set<String>?) = manager.setQuotePingDbIds(ids)

    // v1.74 notification-quote filters.
    val quotePingUseGlobalCeiling: StateFlow<Boolean> = manager.quotePingUseGlobalCeiling
    fun setQuotePingUseGlobalCeiling(useGlobal: Boolean) =
        manager.setQuotePingUseGlobalCeiling(useGlobal)
    val quotePingCustomCeiling: StateFlow<com.quotebrowser.app.data.MaturityLevel> =
        manager.quotePingCustomCeiling
    fun setQuotePingCustomCeiling(level: com.quotebrowser.app.data.MaturityLevel) =
        manager.setQuotePingCustomCeiling(level)
    val quotePingWorkHoursEnabled: StateFlow<Boolean> = manager.quotePingWorkHoursEnabled
    fun setQuotePingWorkHoursEnabled(enabled: Boolean) =
        manager.setQuotePingWorkHoursEnabled(enabled)
    val quotePingWorkDays: StateFlow<Set<Int>> = manager.quotePingWorkDays
    fun toggleQuotePingWorkDay(day: Int) = manager.toggleQuotePingWorkDay(day)
    val quotePingWorkStartMin: StateFlow<Int> = manager.quotePingWorkStartMin
    fun setQuotePingWorkStart(hour: Int, minute: Int) =
        manager.setQuotePingWorkStart(hour, minute)
    val quotePingWorkEndMin: StateFlow<Int> = manager.quotePingWorkEndMin
    fun setQuotePingWorkEnd(hour: Int, minute: Int) = manager.setQuotePingWorkEnd(hour, minute)
    val quotePingWorkCeiling: StateFlow<com.quotebrowser.app.data.MaturityLevel> =
        manager.quotePingWorkCeiling
    fun setQuotePingWorkCeiling(level: com.quotebrowser.app.data.MaturityLevel) =
        manager.setQuotePingWorkCeiling(level)
    val quotePingMinRating: StateFlow<Int?> = manager.quotePingMinRating
    fun setQuotePingMinRating(minRating: Int?) = manager.setQuotePingMinRating(minRating)
    val quotePingCategoryNames: StateFlow<Set<String>> = manager.quotePingCategoryNames
    fun setQuotePingCategoryNames(names: Set<String>) = manager.setQuotePingCategoryNames(names)
    val quotePingRecencyDays: StateFlow<Int> = manager.quotePingRecencyDays
    val quotePingRecencyChoices: List<Int> = manager.quotePingRecencyChoices
    fun quotePingRecencyLabel(days: Int): String = manager.quotePingRecencyLabel(days)
    fun setQuotePingRecencyDays(days: Int) = manager.setQuotePingRecencyDays(days)
    suspend fun pingCategoryUniverse(dbIds: Collection<String>): List<String> =
        manager.pingCategoryUniverse(dbIds)
    suspend fun allPingDbIds(): List<String> = manager.allDbIds()

    val swipeLeft: StateFlow<com.quotebrowser.app.ui.components.SwipeAction> = manager.swipeLeft
    val swipeRight: StateFlow<com.quotebrowser.app.ui.components.SwipeAction> = manager.swipeRight
    fun setSwipeAction(left: Boolean, action: com.quotebrowser.app.ui.components.SwipeAction) =
        manager.setSwipeAction(left, action)
    val swipeHomeEnabled: StateFlow<Boolean> = manager.swipeHomeEnabled
    val swipeRandomEnabled: StateFlow<Boolean> = manager.swipeRandomEnabled
    fun setSwipeHomeEnabled(enabled: Boolean) = manager.setSwipeHomeEnabled(enabled)
    fun setSwipeRandomEnabled(enabled: Boolean) = manager.setSwipeRandomEnabled(enabled)

    // v1.50 vacation mode (reading streak freeze).
    val vacationMode: StateFlow<Boolean> = manager.vacationMode
    fun setVacationMode(enabled: Boolean) = manager.setVacationMode(enabled)
    val vacationEnabled: StateFlow<Boolean> = manager.vacationEnabled
    fun setVacationEnabled(enabled: Boolean) = manager.setVacationEnabled(enabled)
    val vacationStartDay: StateFlow<Long> = manager.vacationStartDay
    val vacationEndDay: StateFlow<Long> = manager.vacationEndDay
    fun setVacationRange(startDay: Long, endDay: Long) =
        manager.setVacationRange(startDay, endDay)

    // v1.49 feature toggles (all default ON).
    val randomNoRepeat: StateFlow<Boolean> = manager.randomNoRepeat
    fun setRandomNoRepeat(enabled: Boolean) = manager.setRandomNoRepeat(enabled)
    /** Startup screen choice key (default "home"); see StartupDestinations. */
    val startupScreen: StateFlow<String> = manager.startupScreen
    fun setStartupScreen(key: String) = manager.setStartupScreen(key)
    val quoteBattleEnabled: StateFlow<Boolean> = manager.quoteBattleEnabled
    fun setQuoteBattleEnabled(enabled: Boolean) = manager.setQuoteBattleEnabled(enabled)
    val statsCardShare: StateFlow<Boolean> = manager.statsCardShare
    fun setStatsCardShare(enabled: Boolean) = manager.setStatsCardShare(enabled)
    val extraWidgets: StateFlow<Boolean> = manager.extraWidgets
    fun setExtraWidgets(enabled: Boolean) = manager.setExtraWidgets(enabled)
    val weeklyGoal: StateFlow<Int> = manager.weeklyGoal
    fun setWeeklyGoal(n: Int) = manager.setWeeklyGoal(n)
    val monthlyGoal: StateFlow<Int> = manager.monthlyGoal
    fun setMonthlyGoal(n: Int) = manager.setMonthlyGoal(n)
    val gesturesEnabled: StateFlow<Boolean> = manager.gesturesEnabled
    fun setGesturesEnabled(enabled: Boolean) = manager.setGesturesEnabled(enabled)
    val dupMergeEnabled: StateFlow<Boolean> = manager.dupMergeEnabled
    fun setDupMergeEnabled(enabled: Boolean) = manager.setDupMergeEnabled(enabled)
    val bulkSelectAll: StateFlow<Boolean> = manager.bulkSelectAll
    fun setBulkSelectAll(enabled: Boolean) = manager.setBulkSelectAll(enabled)
    val notesEnabled: StateFlow<Boolean> = manager.notesEnabled
    fun setNotesEnabled(enabled: Boolean) = manager.setNotesEnabled(enabled)
    val slideshowExportEnabled: StateFlow<Boolean> = manager.slideshowExportEnabled
    fun setSlideshowExportEnabled(enabled: Boolean) =
        manager.setSlideshowExportEnabled(enabled)
    val readingGoalsEnabled: StateFlow<Boolean> = manager.readingGoalsEnabled
    fun setReadingGoalsEnabled(enabled: Boolean) = manager.setReadingGoalsEnabled(enabled)
    val nickTimelineEnabled: StateFlow<Boolean> = manager.nickTimelineEnabled
    fun setNickTimelineEnabled(enabled: Boolean) = manager.setNickTimelineEnabled(enabled)
    // v1.51 "Pick up where you left off": remember the Browse scroll
    // position per database (default ON). Also exposed on
    // QuoteListViewModel for BrowseScreen's own BrowseViewModel.
    val browsePositionMemory: StateFlow<Boolean> = manager.browsePositionMemory
    fun setBrowsePositionMemory(enabled: Boolean) = manager.setBrowsePositionMemory(enabled)
    // v1.50 rate-as-you-read: personal 1-5 star ratings (default ON).
    val personalRatingsEnabled: StateFlow<Boolean> = manager.personalRatings
    fun setPersonalRatingsEnabled(enabled: Boolean) = manager.setPersonalRatings(enabled)

    // v1.50 feature toggle (default ON): pinch-to-zoom on the quote body.
    val pinchZoomEnabled: StateFlow<Boolean> = manager.pinchZoomEnabled
    fun setPinchZoomEnabled(enabled: Boolean) = manager.setPinchZoomEnabled(enabled)

    // Daily quote reminder notification (defaults OFF; the setting is the toggle).
    val dailyReminderEnabled: StateFlow<Boolean> = manager.dailyReminderEnabled
    fun setDailyReminderEnabled(enabled: Boolean) = manager.setDailyReminderEnabled(enabled)
    val dailyReminderHour: StateFlow<Int> = manager.dailyReminderHour
    val dailyReminderMinute: StateFlow<Int> = manager.dailyReminderMinute
    fun setDailyReminderTime(hour: Int, minute: Int) = manager.setDailyReminderTime(hour, minute)

    // Quote alarm (v1.78, defaults OFF; the setting is the toggle).
    val quoteAlarmEnabled: StateFlow<Boolean> = manager.quoteAlarmEnabled
    fun setQuoteAlarmEnabled(enabled: Boolean) = manager.setQuoteAlarmEnabled(enabled)
    val quoteAlarmHour: StateFlow<Int> = manager.quoteAlarmHour
    val quoteAlarmMinute: StateFlow<Int> = manager.quoteAlarmMinute
    fun setQuoteAlarmTime(hour: Int, minute: Int) = manager.setQuoteAlarmTime(hour, minute)

    /** Auto-backup destination folder (persisted SAF tree URI), or null. */
    val autoBackupTreeUri: StateFlow<String?> = manager.autoBackupTreeUri
    fun setAutoBackupTreeUri(uri: String?) = manager.setAutoBackupTreeUri(uri)

    /** Human-readable outcome of the last backup run. */
    val lastAutoBackup: StateFlow<String?> = manager.lastAutoBackup

    fun autoBackupFolderName(): String? =
        manager.autoBackupTreeUri.value?.let { manager.queryDisplayName(android.net.Uri.parse(it)) }

    /** Runs a backup right now; [onDone] gets the status message. */
    fun runBackupNow(onDone: (String) -> Unit) {
        viewModelScope.launch {
            val tree = manager.autoBackupTreeUri.value
            if (tree.isNullOrBlank()) {
                onDone("Choose a backup folder first.")
                return@launch
            }
            val (count, error) = manager.backupDatabasesToTree(android.net.Uri.parse(tree))
            val msg = if (error == null) {
                val when_ = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("MMM d, HH:mm")
                )
                "$count database${if (count == 1) "" else "s"} backed up · $when_"
            } else {
                "Backup failed: $error"
            }
            manager.setLastAutoBackup(msg)
            onDone(msg)
        }
    }

    /** Widget category filter (null = all categories); names of the current database. */
    val widgetCategory: StateFlow<String?> = manager.widgetCategory
    fun setWidgetCategory(name: String?) = manager.setWidgetCategory(name)
    val widgetCategoryNames: StateFlow<List<String>> = manager.currentDbId
        .flatMapLatest { id ->
            kotlinx.coroutines.flow.flow {
                emit(manager.categoryStoreFor(id).list().map { it.name })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val density: StateFlow<String> = manager.density
    fun setDensity(d: String) = manager.setDensity(d)

    val savedSearches: StateFlow<List<String>> = manager.savedSearches
    fun saveSearch(query: String) = manager.saveSearch(query)
    fun removeSavedSearch(query: String) = manager.removeSavedSearch(query)

    val favoriteFolders: StateFlow<List<String>> = manager.favoriteFolders
    fun addFavoriteFolder(name: String): Boolean = manager.addFavoriteFolder(name)
    fun renameFavoriteFolder(old: String, new: String): Boolean =
        manager.renameFavoriteFolder(old, new)
    fun deleteFavoriteFolder(name: String) = manager.deleteFavoriteFolder(name)
    fun setFavoriteFolder(dbId: String, number: Int, folder: String?) =
        manager.setFavoriteFolder(dbId, number, folder)

    /** Export queue: quotes staged for a multi-quote export. */
    val basket: StateFlow<List<BasketEntry>> = manager.basket
    val basketCount: StateFlow<Int> = manager.basket
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleBasket(dbId: String, number: Int) = manager.toggleBasket(dbId, number)

    /**
     * v1.66: adds to the export basket only when not already there (used
     * when exporting a user quote list).
     */
    fun toggleBasketIfAbsent(dbId: String, number: Int) {
        if (BasketEntry(dbId, number) !in manager.basket.value) {
            toggleBasket(dbId, number)
        }
    }
    fun removeFromBasket(dbId: String, number: Int) = manager.removeFromBasket(dbId, number)
    fun clearBasket() = manager.clearBasket()
    /** v1.86: reorder the export tray; the tray order is the export order. */
    fun moveBasketEntry(fromIndex: Int, toIndex: Int) = manager.moveBasketEntry(fromIndex, toIndex)
    fun labelForDb(id: String): String = manager.shortLabel(id)

    /**
     * v1.86: the [NickPalette] + [QuoteCleanOptions] the standalone HTML
     * export renders quote text with (classified per-category spans).
     * Null when nick coloring is off — the export falls back to plain
     * text.
     */
    fun htmlExportTextStyle() =
        com.quotebrowser.app.ui.layouts.quoteTextExportStyle(manager)

    /** v1.85: category names for one quote (HTML export meta block). */
    suspend fun categoryNamesForQuote(dbId: String, number: Int): List<String> =
        try {
            manager.categoryStoreFor(dbId).forQuote(number).map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    suspend fun basketQuotes(): List<SourcedQuote> =
        manager.basketQuotes().filter { sq ->
            !manager.isQuoteHiddenFromLists(sq.dbId, sq.quote.number, sq.quote.text)
        }

    /** Fetches quotes for export. Scope: "all", "favorites", or "history". */
    suspend fun getQuotesForExport(scope: String, folder: String? = null): List<Quote> {
        val dbId = manager.currentDbId.value
        val repo = manager.repoFor(dbId)
        val quotes = when (scope) {
            "favorites" -> repo.favorites().first()
            "history" -> repo.history().first()
            else -> repo.getAllQuotes()
        }
        val inFolder = if (scope == "favorites" && folder != null) {
            quotes.filter { manager.folderForFavorite(dbId, it.number) == folder }
        } else quotes
        return inFolder.filter { q -> !manager.isQuoteHiddenFromLists(dbId, q.number, q.text) }
    }

    /** Fetches quotes for one category's export. */
    suspend fun getQuotesForCategory(categoryId: Long): List<Quote> {
        val dbId = manager.currentDbId.value
        return manager.repoFor(dbId).getAllQuotes(setOf(categoryId))
            .filter { q -> !manager.isQuoteHiddenFromLists(dbId, q.number, q.text) }
    }

    /** Categories in the current database. */
    suspend fun listCategories(): List<Category> =
        manager.repoFor(manager.currentDbId.value).listCategories()

    private val _importMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val importMessage: StateFlow<String?> = _importMessage

    fun switchDatabase(id: String) = manager.switchDatabase(id)

    /** Quote count for one database (Databases settings section). */
    suspend fun databaseQuoteCount(id: String): Int =
        manager.repoFor(id).quoteCount()

    fun exportFileName(id: String): String = manager.exportFileName(id)

    /** Copies a database to the user-picked location; null = success. */
    suspend fun exportDatabase(id: String, uri: Uri): String? =
        manager.exportDatabaseToUri(id, uri)

    /** Stages a database copy in cache for sharing; null = failure. */
    suspend fun stagedShareFile(id: String): java.io.File? =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            manager.stagedShareFile(id)
        }

    fun consumeImportMessage() {
        _importMessage.value = null
    }

    fun onImportPicked(uri: Uri) {
        viewModelScope.launch {
            val name = manager.queryDisplayName(uri)
            val result = manager.importDatabase(name, uri)
            result.onSuccess { info ->
                manager.switchDatabase(info.id)
                _importMessage.value = "Imported \"${info.label}\" and switched to it."
            }.onFailure { e ->
                _importMessage.value = "Import failed: ${e.message}"
            }
        }
    }
}

class BrowseViewModel(manager: DatabaseManager) : QuoteListViewModel(manager) {

    var sortOrder by mutableStateOf(SortOrder.NUMBER_DESC)
        private set

    /** Minimum rating chip filter: null = All. */
    var minRating by mutableStateOf<Int?>(null)
        private set

    /** Category filter: empty = all quotes. */
    var categoryIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    /**
     * v1.65 per-screen maturity pick: null/EVERYTHING = follow the global
     * ceiling (Settings). A stricter pick narrows further; the global
     * ceiling always applies on top.
     */
    var maturity by mutableStateOf<MaturityLevel?>(null)
        private set

    /** v1.65 per-screen cast-size pick: null = all cast sizes. */
    var castSize by mutableStateOf<CastSize?>(null)
        private set

    fun updateMaturity(level: MaturityLevel?) {
        maturity = level
    }

    fun updateCastSize(cast: CastSize?) {
        castSize = cast
    }

    /**
     * v1.65: the effective maturity predicates — the global ceiling ANDed
     * with the per-screen pick. EVERYTHING on either side is a no-op.
     */
    private fun effectiveMaturities(
        screen: MaturityLevel?,
        ceiling: MaturityLevel
    ): List<MaturityLevel> = listOfNotNull(
        ceiling.takeIf { it != MaturityLevel.EVERYTHING },
        screen?.takeIf { it != MaturityLevel.EVERYTHING }
    )

    /**
     * Per-screen temporary content-filter bypass ("Show" on the hidden
     * banner). In-memory only; the screen resets it when leaving.
     */
    private val _showFiltered = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showFiltered: StateFlow<Boolean> = _showFiltered.asStateFlow()

    /**
     * How many loaded quotes the content filter removed. Updated as pages
     * load; 0 when the filter is off or bypassed.
     */
    private val _hiddenCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    fun setShowFiltered(show: Boolean) {
        if (_showFiltered.value == show) return
        _showFiltered.value = show
    }

    /**
     * v1.57: Browse/home database multi-select. Empty set = all databases.
     * Persists across launches (DatabaseManager).
     */
    val browseDbIds: StateFlow<Set<String>> = manager.browseDbIds

    /**
     * v1.57: the selection resolved to concrete database ids against the
     * available databases. An empty selection — or one that resolves to
     * nothing (e.g. a removed import) — means every database.
     */
    private val resolvedBrowseIds: Flow<List<String>> = combine(
        manager.browseDbIds,
        manager.availableDatabases()
    ) { sel, dbs ->
        val allIds = dbs.map { it.id }
        if (sel.isEmpty()) allIds else sel.filter { it in allIds }.ifEmpty { allIds }
    }

    /**
     * v1.57: the single selected database id, or null when the selection
     * is mixed (several) or all. Single-id mode keeps the classic
     * single-database Browse behavior (page background, categories,
     * jump-to-number, no source chips).
     */
    val browseSingleDbId: StateFlow<String?> =
        resolvedBrowseIds.map { it.singleOrNull() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** v1.57: Browse follows the multi-select for its ratings affordance. */
    override val dbHasRatings: StateFlow<Boolean> =
        browseSingleDbId.flatMapLatest { id ->
            kotlinx.coroutines.flow.flow {
                emit(if (id == null) true else manager.repoFor(id).hasRatings())
            }
        }.flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** v1.57: replace the Browse/home database selection (empty = all). */
    fun setBrowseDbIds(ids: Set<String>) = manager.setBrowseDbIds(ids)

    val quotes: Flow<PagingData<Quote>> = combine(
        browseSingleDbId,
        snapshotFlow { sortOrder },
        snapshotFlow { minRating },
        snapshotFlow { categoryIds },
        manager.contentFilterTick
    ) { dbId: String?, sort, min, cats, _ ->
        // v1.57: the single-database pager only runs when exactly one
        // database is selected; mixed selections use mergedQuotes below.
        // v1.88.12: resolve the repo on IO — repoFor() can do disk work on
        // a cold open (pre-heal + Room validation) and this combine runs
        // on the main dispatcher.
        if (dbId == null) Triple(null, sort, min) to cats
        else withContext(Dispatchers.IO) { Triple(manager.repoFor(dbId), sort, min) to cats }
    }
        .combine(snapshotFlow { lengthMode }) { main, mode -> main to mode }
        .combine(snapshotFlow { minLength }) { main, minLen -> main to minLen }
        .combine(snapshotFlow { maxLength }) { main, maxLen -> main to maxLen }
        .combine(manager.lengthLongerWords) { main, longer -> main to longer }
        .combine(manager.lengthShorterWords) { main, shorter -> main to shorter }
        .combine(_showFiltered) { main, show -> main to show }
        // v1.51: re-run Browse when the per-database status filter changes.
        .combine(manager.dbThemeConfigs) { main, _ -> main }
        .combine(manager.statusFilterEnabled) { main, _ -> main }
        // v1.65: re-run Browse when the content filters change (per-screen
        // picks plus the global maturity ceiling).
        .combine(snapshotFlow { maturity }) { main, mat -> main to mat }
        .combine(snapshotFlow { castSize }) { main, cast -> main to cast }
        .combine(manager.maturityCeiling) { main, ceiling -> main to ceiling }
        .flatMapLatest { (allContent, ceiling) ->
            val (allCast, cast) = allContent
            val (allMat, mat) = allCast
            val (allLens, show) = allMat
            val (noShorter, shorter) = allLens
            val (noLonger, longer) = noShorter
            val (noMax, maxLen) = noLonger
            val (noMin, minLen) = noMax
            val (tripleCats, mode) = noMin
            val (triple, cats) = tripleCats
            val (repo, sort, min) = triple
            _hiddenCount.value = 0
            // v1.57: no single database selected → this pager stays empty;
            // the merged pager below serves mixed selections.
            if (repo == null) return@flatMapLatest flowOf(
                PagingData.empty<Quote>()
            )
            // v1.51 per-database status filter for this database.
            val statusMode = manager.statusModeFor(repo.dbId)
            // v1.65 content filters: global ceiling ANDed with the
            // per-screen picks.
            val maturities = effectiveMaturities(mat, ceiling)
            repo.browse(sort, min, cats, statusMode, maturities, cast).map { paging ->
                paging
                    .filter { q ->
                        lengthFilterOk(q.text, mode, minLen, maxLen, longer, shorter, manager.qcNormalizeBreaks.value)
                    }
                    .let { sized ->
                        // "Never show again" always hides; "Show anyway"
                        // bypasses only the offensive-word filter.
                        sized.filter { q ->
                            val neverShow = manager.isQuoteHiddenOrMuted(repo.dbId, q.number, q.text)
                            val wordHidden =
                                !show && manager.isHiddenByContentFilter(q.text)
                            if (wordHidden) _hiddenCount.value = _hiddenCount.value + 1
                            !neverShow && !wordHidden
                        }
                    }
            }
        }
        .cachedIn(viewModelScope)

    /**
     * Merged home view: quotes from the selected databases interleaved
     * round-robin (each database in [sort] order, [minRating] applied per
     * database). v1.57: serves any multi-database selection (or all), not
     * just "All databases". Category filtering is unavailable here —
     * categories are per-database — so [categoryIds] is ignored.
     */
    val mergedQuotes: Flow<PagingData<SourcedQuote>> = combine(
        resolvedBrowseIds,
        snapshotFlow { sortOrder },
        snapshotFlow { minRating },
        manager.contentFilterTick
    ) { dbIds: List<String>, sort: SortOrder, min: Int?, _ ->
        Triple(dbIds, sort, min)
    }
        .combine(snapshotFlow { lengthMode }) { main, mode -> main to mode }
        .combine(snapshotFlow { minLength }) { main, minLen -> main to minLen }
        .combine(snapshotFlow { maxLength }) { main, maxLen -> main to maxLen }
        .combine(manager.lengthLongerWords) { main, longer -> main to longer }
        .combine(manager.lengthShorterWords) { main, shorter -> main to shorter }
        .combine(_showFiltered) { main, show -> main to show }
        // v1.51: re-run the merged Browse when a per-database status filter changes.
        .combine(manager.dbThemeConfigs) { main, _ -> main }
        .combine(manager.statusFilterEnabled) { main, _ -> main }
        // v1.65: re-run the merged Browse when the content filters change.
        .combine(snapshotFlow { maturity }) { main, mat -> main to mat }
        .combine(snapshotFlow { castSize }) { main, cast -> main to cast }
        .combine(manager.maturityCeiling) { main, ceiling -> main to ceiling }
        .flatMapLatest { (allContent, ceiling) ->
            val (allCast, cast) = allContent
            val (allMat, mat) = allCast
            val (allLens, show) = allMat
            val (noShorter, shorter) = allLens
            val (noLonger, longer) = noShorter
            val (noMax, maxLen) = noLonger
            val (noMin, minLen) = noMax
            val (tripleMin, mode) = noMin
            val (dbIds, sort, min) = tripleMin
            _hiddenCount.value = 0
            // v1.57: the merged pager serves mixed selections (2+); a
            // single-database selection uses the single pager above.
            if (dbIds.size < 2) {
                flowOf(PagingData.empty<SourcedQuote>())
            } else {
                // v1.88.12: resolve repos on IO — a cold repoFor() does disk
                // work (pre-heal + Room validation) and this flatMapLatest
                // runs on the main dispatcher. Default Browse opens all 12
                // bundled DBs, which was freezing the home screen.
                // v1.88.15: open them in PARALLEL. The sequential map above
                // made first paint wait for 12 back-to-back cold opens —
                // including the 59MB bash_im.db, bigger than all other
                // bundled DBs combined. Each database has its own JVM lock
                // so parallel opens don't contend; flash handles the
                // concurrent reads far better than one-at-a-time.
                val repos = withContext(Dispatchers.IO) {
                    dbIds.map { id -> async { manager.repoFor(id) } }.awaitAll()
                }
                // v1.88.16 diagnostics: when the home screen's databases
                // finished opening.
                com.quotebrowser.app.StartupWatchdog.mark(
                    "browse repos resolved: ${repos.size} dbs"
                )
                // v1.51: mixed lists use each source database's own status mode.
                val statusModes = dbIds.associateWith { manager.statusModeFor(it) }
                // v1.65 content filters: global ceiling ANDed with the
                // per-screen picks.
                val maturities = effectiveMaturities(mat, ceiling)
                Pager(PagingConfig(pageSize = 25, enablePlaceholders = false)) {
                    AllDbPagingSource(repos, sort, min, statusModes, maturities, cast)
                }.flow.map { paging ->
                    paging
                        .filter { sq ->
                            lengthFilterOk(sq.quote.text, mode, minLen, maxLen, longer, shorter, manager.qcNormalizeBreaks.value)
                        }
                        .let { sized ->
                            // "Never show again" always hides; "Show anyway"
                            // bypasses only the offensive-word filter.
                            sized.filter { sq ->
                                val neverShow = manager.isQuoteHiddenOrMuted(sq.dbId, sq.quote.number, sq.quote.text)
                                val wordHidden =
                                    !show && manager.isHiddenByContentFilter(sq.quote.text)
                                if (wordHidden) _hiddenCount.value = _hiddenCount.value + 1
                                !neverShow && !wordHidden
                            }
                        }
                }
            }
        }
        .cachedIn(viewModelScope)

    fun updateSortOrder(sort: SortOrder) {
        sortOrder = sort
    }

    fun updateMinRating(rating: Int?) {
        minRating = rating
    }

    init {
        // A min-rating filter on a database with no ratings can only show an
        // empty list, so drop it when the database changes to one without.
        viewModelScope.launch {
            dbHasRatings.collect { has -> if (!has) minRating = null }
        }
    }

    fun updateCategories(ids: Set<Long>) {
        categoryIds = ids
    }
}

class SearchViewModel(manager: DatabaseManager) : QuoteListViewModel(manager) {

    var query by mutableStateOf("")
        private set

    /**
     * v1.74.5: regex search — when on, the query is treated as a regular
     * expression matched against quote text (case-insensitive) instead of
     * the normal fuzzy text search. SQL applies the other filters; the
     * pattern itself is applied in Kotlin since Android's SQLite has no
     * REGEXP.
     */
    var useRegex by mutableStateOf(false)
        private set

    fun updateUseRegex(on: Boolean) {
        useRegex = on
    }

    /** v1.74.5: null = regex off or blank query; non-null = compiled pattern. */
    fun compiledRegex(): Regex? {
        if (!useRegex || query.isBlank()) return null
        return try {
            Regex(query, RegexOption.IGNORE_CASE)
        } catch (_: Exception) {
            null
        }
    }

    /** v1.74.5: true when regex mode is on but the pattern won't compile. */
    val regexInvalid: Boolean
        get() = useRegex && query.isNotBlank() && compiledRegex() == null

    /** Category filter: empty = all quotes. */
    var categoryIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    /** Minimum rating filter: null = All. */
    var minRating by mutableStateOf<Int?>(null)
        private set

    var sortOrder by mutableStateOf(SortOrder.NUMBER_DESC)
        private set

    /**
     * Notes scope: when on, the results list shows quotes whose personal
     * note matches the query instead of the full-text search.
     */
    var notesScope by mutableStateOf(false)
        private set

    fun updateNotesScope(on: Boolean) {
        notesScope = on
        if (on) searchDbIds = null
    }

    /**
     * v1.61.20: "Has voice" filter — only quotes with a saved read-aloud
     * recording. Works with text search and sorting; with no text query it
     * lists every voiced quote in scope.
     */
    var voiceOnly by mutableStateOf(false)
        private set

    fun updateVoiceOnly(on: Boolean) {
        voiceOnly = on
    }

    /**
     * v1.75: "Has explanation" filter — only quotes with a saved Gemini
     * explanation. Mirrors voiceOnly: works with text search and sorting;
     * with no text query it lists every explained quote in scope.
     */
    var explanationOnly by mutableStateOf(false)
        private set

    fun updateExplanationOnly(on: Boolean) {
        explanationOnly = on
    }

    /**
     * v1.65 per-screen content filters. Maturity works like Browse: the
     * pick ANDs with the global ceiling. Cast size: null = all.
     */
    var maturity by mutableStateOf<MaturityLevel?>(null)
        private set

    var castSize by mutableStateOf<CastSize?>(null)
        private set

    fun updateMaturity(level: MaturityLevel?) {
        maturity = level
    }

    fun updateCastSize(cast: CastSize?) {
        castSize = cast
    }

    /**
     * v1.65 search exclusions: these stay out of the results even when
     * they'd otherwise match.
     */
    var excludedCategoryIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    var excludedMaturities by mutableStateOf<Set<Int>>(emptySet())
        private set

    var excludedCasts by mutableStateOf<Set<CastSize>>(emptySet())
        private set

    /**
     * v1.66 filter browsing: when true and the query is blank, the search
     * screens browse by the active filters (categories, maturity, cast)
     * instead of waiting for text. Set by the mood buttons.
     */
    var filterBrowse by mutableStateOf(false)
        private set

    fun updateFilterBrowse(on: Boolean) {
        filterBrowse = on
    }

    /**
     * v1.74.3 diagnostic: count quotes matching a cast size across the
     * databases in the current search scope. Used in the empty state to
     * distinguish "no data" from "search bug".
     */
    suspend fun countByCastAcrossScope(cast: CastSize): Int {
        val wanted = searchDbIds
        val valid = try {
            manager.availableDatabases().first().map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
        val ids = if (wanted == null) valid.toList() else wanted.filter { it in valid }
        // v1.88.15: count in parallel — a cold repoFor() per database was
        // running sequentially here too.
        return coroutineScope {
            ids.map { id ->
                async {
                    try {
                        manager.repoFor(id).countByCast(cast)
                    } catch (e: Exception) {
                        // skip unreadable databases
                        0
                    }
                }
            }.awaitAll().sum()
        }
    }

    /**
     * v1.73: number of currently active search filters, shown as a badge
     * on the global search row's expand arrow. Reads the state properties
     * so Compose recomposes the badge when filters change.
     */
    fun activeFilterCount(): Int {
        var n = 0
        if (categoryIds.isNotEmpty()) n++
        if (minRating != null) n++
        if (sortOrder != SortOrder.NUMBER_DESC) n++
        if (notesScope) n++
        if (voiceOnly) n++
        // v1.75: "Has explanation" filter counts too.
        if (explanationOnly) n++
        if (maturity != null) n++
        if (castSize != null) n++
        if (excludedCategoryIds.isNotEmpty() ||
            excludedMaturities.isNotEmpty() ||
            excludedCasts.isNotEmpty()
        ) n++
        if (searchDbIds != null) n++
        if (lengthMode != com.quotebrowser.app.data.LengthMode.ALL ||
            minLength != null || maxLength != null
        ) n++
        if (useRegex) n++
        return n
    }

    fun updateExcludedCategories(ids: Set<Long>) {
        excludedCategoryIds = ids
    }

    fun updateExcludedMaturities(levels: Set<Int>) {
        excludedMaturities = levels
    }

    fun updateExcludedCasts(casts: Set<CastSize>) {
        excludedCasts = casts
    }

    /**
     * v1.65: the effective maturity predicates — the global ceiling ANDed
     * with the per-screen pick. EVERYTHING on either side is a no-op.
     */
    private fun effectiveMaturities(
        screen: MaturityLevel?,
        ceiling: MaturityLevel
    ): List<MaturityLevel> = listOfNotNull(
        ceiling.takeIf { it != MaturityLevel.EVERYTHING },
        screen?.takeIf { it != MaturityLevel.EVERYTHING }
    )

    /**
     * Search database scope: null = the current database only (default);
     * otherwise exactly these database ids ("all databases" is stored as
     * the full id set). Mutually exclusive with [notesScope].
     */
    var searchDbIds by mutableStateOf<Set<String>?>(null)
        private set

    fun updateSearchDbIds(ids: Set<String>?) {
        searchDbIds = ids
        if (ids != null) notesScope = false
    }

    /**
     * v1.77: the database scope actually searched. The filter panel's
     * explicit selection wins; otherwise the virtual "all" database expands
     * to every real database id, so each hit keeps its true source id and
     * its card resolves the right site theme. Null = exactly one real
     * database (the paging path).
     */
    val effectiveSearchDbIds: StateFlow<Set<String>?> =
        combine(
            manager.currentDbId,
            snapshotFlow { searchDbIds },
            manager.availableDatabases()
        ) { current, explicit, all ->
            explicit
                ?: if (current == DatabaseManager.ALL_DBS_ID) all.map { it.id }.toSet()
                else null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Quotes whose personal note contains the query (used when the notes
     * scope is on). Notes are few, so this is a plain list, not paging.
     */
    val noteResults: StateFlow<List<SourcedQuote>> = combine(
        snapshotFlow { query },
        snapshotFlow { notesScope },
        manager.notes
    ) { q, scopeOn, notes -> Triple(q, scopeOn, notes) }
        .mapLatest { (q, scopeOn, notes) ->
            if (!scopeOn || q.isBlank()) {
                emptyList()
            } else {
                notes.mapNotNull { (key, note) ->
                    if (!note.contains(q, ignoreCase = true)) return@mapNotNull null
                    val dbId = key.substringBeforeLast("/")
                    val number = key.substringAfterLast("/").toIntOrNull()
                        ?: return@mapNotNull null
                    val quote = try {
                        manager.repoFor(dbId).getQuote(number)
                    } catch (e: Exception) {
                        null
                    } ?: return@mapNotNull null
                    SourcedQuote(dbId, quote)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Per-screen temporary content-filter bypass ("Show" on the hidden
     * banner). In-memory only; the screen resets it when leaving.
     */
    private val _showFiltered = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showFiltered: StateFlow<Boolean> = _showFiltered.asStateFlow()

    /**
     * How many loaded quotes the content filter removed. Updated as pages
     * load; 0 when the filter is off or bypassed.
     */
    private val _hiddenCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    fun setShowFiltered(show: Boolean) {
        if (_showFiltered.value == show) return
        _showFiltered.value = show
    }

    @OptIn(FlowPreview::class)
    val results: Flow<PagingData<Quote>> = combine(
        manager.currentDbId,
        snapshotFlow { query }.debounce(300),
        snapshotFlow { categoryIds },
        snapshotFlow { minRating },
        snapshotFlow { sortOrder }
    // v1.88.12: repoFor() can do disk work on a cold open; resolve on IO
    // (this combine transform runs on the main dispatcher).
    ) { dbId, q, cats, min, sort -> withContext(Dispatchers.IO) { Triple(manager.repoFor(dbId), q, cats) to (min to sort) } }
        // Re-run the search when the content filter or word list changes.
        .combine(manager.contentFilterTick) { main, _ -> main }
        .combine(snapshotFlow { lengthMode }) { main, mode -> main to mode }
        .combine(snapshotFlow { minLength }) { main, minLen -> main to minLen }
        .combine(snapshotFlow { maxLength }) { main, maxLen -> main to maxLen }
        .combine(manager.lengthLongerWords) { main, longer -> main to longer }
        .combine(manager.lengthShorterWords) { main, shorter -> main to shorter }
        .combine(_showFiltered) { main, show -> main to show }
        // In multi-database scope the dedicated allDbResults flow serves the
        // screen; keep this one idle (and out of _hiddenCount's way).
        // v1.77: the effective scope expands the virtual "all" database to
        // every real id, so "all" no longer falls through to the paging
        // path with a fake single id.
        .combine(effectiveSearchDbIds) { prev, ids -> prev to (ids != null) }
        // v1.61.20: re-run when the "Has voice" filter changes.
        .combine(snapshotFlow { voiceOnly }) { prev, vo -> prev to vo }
        // v1.51: re-run the search when a per-database status filter changes.
        .combine(manager.dbThemeConfigs) { main, _ -> main }
        .combine(manager.statusFilterEnabled) { main, _ -> main }
        // v1.65: re-run the search when the content filters change
        // (per-screen picks, exclusions, and the global maturity ceiling).
        .combine(snapshotFlow { maturity }) { main, mat -> main to mat }
        .combine(snapshotFlow { castSize }) { main, cast -> main to cast }
        .combine(snapshotFlow { excludedCategoryIds }) { main, xc -> main to xc }
        .combine(snapshotFlow { excludedMaturities }) { main, xm -> main to xm }
        .combine(snapshotFlow { excludedCasts }) { main, xcast -> main to xcast }
        .combine(manager.maturityCeiling) { main, ceiling -> main to ceiling }
        // v1.66: re-run the search when filter browsing toggles.
        .combine(snapshotFlow { filterBrowse }) { main, fb -> main to fb }
        // v1.74.5: re-run when regex mode toggles.
        .combine(snapshotFlow { useRegex }) { main, rx -> main to rx }
        .flatMapLatest { (b7, useRegex) ->
            val (b6, filterBrowse) = b7
            val (b5, ceiling) = b6
            val (b4, xcast) = b5
            val (b3, xm) = b4
            val (b2, xc) = b3
            val (b1, cast) = b2
            val (a, mat) = b1
            val (prevAll, voiceOnly) = a
            val (prev, all) = prevAll
            val (allLens, show) = prev
            val (noShorter, shorter) = allLens
            val (noLonger, longer) = noShorter
            val (noMax, maxLen) = noLonger
            val (noMin, minLen) = noMax
            val (main, mode) = noMin
            val (triple, minSort) = main
            val (repo, q, cats) = triple
            val (min, sort) = minSort
            if (all) flowOf<PagingData<Quote>>(PagingData.empty())
            else {
                _hiddenCount.value = 0
                // v1.51: this database's own status mode applies to the search.
                // v1.61.20: "Has voice" with no text query lists every voiced
                // quote in the database (paged browse, then voice-filtered).
                // v1.66: filter browsing (mood buttons) browses by the
                // active filters with no text query.
                // v1.65: the global maturity ceiling ANDed with the
                // per-screen picks; exclusions leave results out.
                val maturities = effectiveMaturities(mat, ceiling)
                val statusMode = manager.statusModeFor(repo.dbId)
                // v1.74.5: regex mode — compile once per search run. A null
                // pattern with a non-blank query means it wouldn't compile;
                // the UI shows the error and we return nothing.
                val regex = if (useRegex && q.isNotBlank()) {
                    try {
                        Regex(q, RegexOption.IGNORE_CASE)
                    } catch (_: Exception) {
                        null
                    }
                } else null
                val regexBad = useRegex && q.isNotBlank() && regex == null
                val base: Flow<PagingData<Quote>> = if (q.isBlank()) {
                    if (!voiceOnly && !filterBrowse) flowOf(PagingData.empty())
                    else repo.browse(
                        sort, min, cats, statusMode, maturities, cast, xc
                    )
                } else if (regexBad) {
                    flowOf(PagingData.empty())
                } else if (regex != null) {
                    // SQL applies every filter except the text constraint;
                    // the pattern itself filters the pages below.
                    repo.search(
                        q, cats, min, sort, statusMode,
                        maturities, cast, xc, xm, xcast,
                        useRegex = true
                    )
                } else {
                    repo.search(
                        q, cats, min, sort, statusMode,
                        maturities, cast, xc, xm, xcast
                    )
                }
                base.map { paging ->
                    paging
                        .filter { quote ->
                            regex?.containsMatchIn(quote.text) ?: true
                        }
                        .filter { quote ->
                            lengthFilterOk(quote.text, mode, minLen, maxLen, longer, shorter, manager.qcNormalizeBreaks.value)
                        }
                        // v1.61.20: "Has voice" filter.
                        .filter { quote ->
                            !voiceOnly || VoiceRecordings.hasRecording(repo.dbId, quote.number)
                        }
                        // v1.75: "Has explanation" filter.
                        .filter { quote ->
                            !explanationOnly || ExplanationCache.hasExplanation(repo.dbId, quote.number)
                        }
                        .let { sized ->
                            // "Never show again" always hides; "Show anyway"
                            // bypasses only the offensive-word filter.
                            sized.filter { quote ->
                                val neverShow =
                                    manager.isQuoteHiddenOrMuted(repo.dbId, quote.number, quote.text)
                                val wordHidden =
                                    !show && manager.isHiddenByContentFilter(quote.text)
                                if (wordHidden) _hiddenCount.value = _hiddenCount.value + 1
                                !neverShow && !wordHidden
                            }
                        }
                }
            }
        }
        .cachedIn(viewModelScope)

    companion object {
        /** Cap on merged all-databases search results. */
        private const val ALL_DB_SEARCH_CAP = 200
    }

    /**
     * Everything that can change an all-databases search, captured as one
     * snapshot so the flow re-runs when any of it changes.
     */
    private data class AllDbSearchParams(
        val query: String,
        val scopeIds: Set<String>?,
        val sort: SortOrder,
        val minRating: Int?,
        val categoryIds: Set<Long>,
        val mode: LengthMode,
        val minLen: Int?,
        val maxLen: Int?,
        /** v1.61.20: "Has voice" filter. */
        val voiceOnly: Boolean,
        /** v1.75: "Has explanation" filter. */
        val explanationOnly: Boolean,
        /** v1.65: per-screen content picks (the global ceiling is ANDed in
         * at query time). */
        val maturity: MaturityLevel?,
        val cast: CastSize?,
        val excludedCategoryIds: Set<Long>,
        val excludedMaturities: Set<Int>,
        val excludedCasts: Set<CastSize>,
        /** v1.66: blank query + filter browsing (mood buttons). */
        val filterBrowse: Boolean = false,
        /** v1.74.5: regex mode — the query is a pattern, applied in Kotlin. */
        val useRegex: Boolean = false
    )

    /**
     * All-databases search results: [QuoteRepository.searchList] run against
     * every database concurrently, merged round-robin (like Browse's All
     * mode) and capped at [ALL_DB_SEARCH_CAP]. A plain list, not paging —
     * the merged set is small.
     */
    @OptIn(FlowPreview::class)
    val allDbResults: StateFlow<List<SourcedQuote>> =
        snapshotFlow {
            AllDbSearchParams(
                query = query,
                scopeIds = searchDbIds,
                sort = sortOrder,
                minRating = minRating,
                categoryIds = categoryIds,
                mode = lengthMode,
                minLen = minLength,
                maxLen = maxLength,
                voiceOnly = voiceOnly,
                // v1.75: "Has explanation" rides along too.
                explanationOnly = explanationOnly,
                // v1.65: per-screen content picks ride along so the
                // all-DB search re-runs when any of them change.
                maturity = maturity,
                cast = castSize,
                excludedCategoryIds = excludedCategoryIds,
                excludedMaturities = excludedMaturities,
                excludedCasts = excludedCasts,
                filterBrowse = filterBrowse,
                useRegex = useRegex
            )
        }
            .debounce(300)
            // v1.77: resolve the effective scope — the virtual "all"
            // database expands to every real id so each hit keeps its true
            // source (and site theme). The explicit panel selection wins.
            .combine(effectiveSearchDbIds) { params, effective ->
                params.copy(scopeIds = effective)
            }
            .combine(_showFiltered) { params, show -> params to show }
            // Re-run when the content filter, word list, or hidden set changes.
            .combine(manager.contentFilterTick) { pair, _ -> pair }
            // v1.51: re-run when a per-database status filter changes.
            .combine(manager.dbThemeConfigs) { pair, _ -> pair }
            .combine(manager.statusFilterEnabled) { pair, _ -> pair }
            // v1.65: re-run when the global maturity ceiling changes.
            .combine(manager.maturityCeiling) { pair, _ -> pair }
            .combine(manager.lengthLongerWords) { pair, longer ->
                Triple(pair.first, pair.second, longer)
            }
            .combine(manager.lengthShorterWords) { triple, shorter ->
                triple to shorter
            }
            .mapLatest { (triple, shorter) ->
                val (params, show, longer) = triple
                runAllDbSearch(params, show, longer, shorter)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Runs [params.query] against the selected databases concurrently and
     * merges the per-database hits round-robin. Honors the same filters
     * single-DB search applies: the content filter and "never show again"
     * (per database), categories (matched by name across databases), the
     * minimum rating, the length filter, and the sort order (per database,
     * before the round-robin merge).
     */
    /**
     * v1.61.20: multi-database "Has voice" listing with no text query —
     * every recorded quote in the chosen databases, newest first, capped
     * like a normal all-databases search. The shared hidden/content filters
     * still apply downstream.
     */
    private suspend fun voiceOnlyAllDb(
        params: AllDbSearchParams,
        wanted: Set<String>,
        excludedCatNames: Set<String>
    ): List<SourcedQuote> {
        val ids = try {
            val valid = manager.availableDatabases().first().map { it.id }.toSet()
            wanted.filter { it in valid }
        } catch (e: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) return emptyList()
        val keys = VoiceRecordings.recordings.value.keys
        val hits = mutableListOf<SourcedQuote>()
        for (id in ids) {
            val numbers = keys.mapNotNull { k ->
                val bar = k.indexOf('|')
                if (bar > 0 && k.substring(0, bar) == id) {
                    k.substring(bar + 1).toIntOrNull()
                } else null
            }
            for (n in numbers) {
                try {
                    val quote = manager.repoFor(id).getQuote(n)
                    if (quote != null) hits += SourcedQuote(id, quote)
                } catch (e: Exception) {
                }
                if (hits.size >= ALL_DB_SEARCH_CAP) break
            }
            if (hits.size >= ALL_DB_SEARCH_CAP) break
        }
        val showFiltered = _showFiltered.value
        _hiddenCount.value = 0
        // v1.65: the global maturity ceiling and cast filter apply here too
        // (quotes resolved from the voice manifest, so post-filtered).
        // Excluded categories are matched by name into each database's own
        // category ids, like the text-search path does.
        val maturities = effectiveMaturities(params.maturity, manager.maturityCeiling.value)
        val excludedNumbersByDb: Map<String, Set<Int>> = if (excludedCatNames.isEmpty()) {
            emptyMap()
        } else {
            ids.associateWith { id ->
                try {
                    val store = manager.repoFor(id).categories
                    excludedCatNames.mapNotNull { name -> store.findByName(name)?.id }
                        .flatMap { cid -> store.quoteNumbersInCategory(cid) }
                        .toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            }
        }
        fun contentOk(sq: SourcedQuote): Boolean {
            val quote = sq.quote
            if (maturities.any { !it.matches(quote.maturity, quote.spicy) }) return false
            if (params.cast != null && !params.cast.matches(quote.speakers)) return false
            if (quote.maturity in params.excludedMaturities) return false
            if (params.excludedCasts.any { it.matches(quote.speakers) }) return false
            val excluded = excludedNumbersByDb[sq.dbId]
            if (excluded != null && quote.number in excluded) return false
            return true
        }
        return sortAllDbHits(hits, params.sort).filter { sq ->
            val neverShow =
                manager.isQuoteHiddenOrMuted(sq.dbId, sq.quote.number, sq.quote.text)
            val wordHidden =
                !showFiltered && manager.isHiddenByContentFilter(sq.quote.text)
            if (wordHidden) _hiddenCount.value = _hiddenCount.value + 1
            !neverShow && !wordHidden && contentOk(sq)
        }
    }

    /**
     * v1.75: multi-database "Has explanation" listing with no text query —
     * every explained quote in the chosen databases, resolved straight from
     * the explanation manifest (cheap — explanations are few). Mirrors
     * [voiceOnlyAllDb]; when "Has voice" is also on, it applies per quote.
     */
    private suspend fun explanationOnlyAllDb(
        params: AllDbSearchParams,
        wanted: Set<String>,
        excludedCatNames: Set<String>
    ): List<SourcedQuote> {
        val ids = try {
            val valid = manager.availableDatabases().first().map { it.id }.toSet()
            wanted.filter { it in valid }
        } catch (e: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) return emptyList()
        val keys = ExplanationCache.explanations.value.keys
        val hits = mutableListOf<SourcedQuote>()
        for (id in ids) {
            val numbers = keys.mapNotNull { k ->
                val bar = k.indexOf('|')
                if (bar > 0 && k.substring(0, bar) == id) {
                    k.substring(bar + 1).toIntOrNull()
                } else null
            }
            for (n in numbers) {
                // v1.75: both filters on = quote must have both.
                if (params.voiceOnly && !VoiceRecordings.hasRecording(id, n)) continue
                try {
                    val quote = manager.repoFor(id).getQuote(n)
                    if (quote != null) hits += SourcedQuote(id, quote)
                } catch (e: Exception) {
                }
                if (hits.size >= ALL_DB_SEARCH_CAP) break
            }
            if (hits.size >= ALL_DB_SEARCH_CAP) break
        }
        val showFiltered = _showFiltered.value
        _hiddenCount.value = 0
        val maturities = effectiveMaturities(params.maturity, manager.maturityCeiling.value)
        val excludedNumbersByDb: Map<String, Set<Int>> = if (excludedCatNames.isEmpty()) {
            emptyMap()
        } else {
            ids.associateWith { id ->
                try {
                    val store = manager.repoFor(id).categories
                    excludedCatNames.mapNotNull { name -> store.findByName(name)?.id }
                        .flatMap { cid -> store.quoteNumbersInCategory(cid) }
                        .toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            }
        }
        fun contentOk(sq: SourcedQuote): Boolean {
            val quote = sq.quote
            if (maturities.any { !it.matches(quote.maturity, quote.spicy) }) return false
            if (params.cast != null && !params.cast.matches(quote.speakers)) return false
            if (quote.maturity in params.excludedMaturities) return false
            if (params.excludedCasts.any { it.matches(quote.speakers) }) return false
            val excluded = excludedNumbersByDb[sq.dbId]
            if (excluded != null && quote.number in excluded) return false
            return true
        }
        return sortAllDbHits(hits, params.sort).filter { sq ->
            val neverShow =
                manager.isQuoteHiddenOrMuted(sq.dbId, sq.quote.number, sq.quote.text)
            val wordHidden =
                !showFiltered && manager.isHiddenByContentFilter(sq.quote.text)
            if (wordHidden) _hiddenCount.value = _hiddenCount.value + 1
            !neverShow && !wordHidden && contentOk(sq)
        }
    }

    private suspend fun runAllDbSearch(
        params: AllDbSearchParams,
        showFiltered: Boolean,
        longerWords: Int,
        shorterWords: Int
    ): List<SourcedQuote> {
        val q = params.query.trim()
        // The single-DB paging flow owns _hiddenCount when this scope is off.
        val wanted = params.scopeIds
        if (wanted == null) return emptyList()
        // v1.65: excluded-category names (matched by name across databases,
        // like Random does) — needed by both the voice-only and text paths.
        val excludedCatNames: Set<String> = if (params.excludedCategoryIds.isEmpty()) {
            emptySet()
        } else {
            try {
                manager.categoryStoreFor(manager.currentDbId.value).list()
                    .filter { it.id in params.excludedCategoryIds }
                    .map { it.name }
                    .toSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
        if (q.isEmpty()) {
            // v1.61.20: blank query + "Has voice" in multi-database scope:
            // resolve straight from the manifest (cheap — recordings are few).
            // v1.75: blank query + "Has explanation" resolves from the
            // explanation manifest the same way; both filters on = the
            // explanation path with the voice filter applied per quote.
            // v1.66: blank query + filter browsing (mood buttons) falls
            // through to the per-database browse path below.
            if (!params.voiceOnly && !params.explanationOnly && !params.filterBrowse) return emptyList()
            if (!params.filterBrowse) {
                return if (params.explanationOnly) {
                    explanationOnlyAllDb(params, wanted, excludedCatNames)
                } else {
                    voiceOnlyAllDb(params, wanted, excludedCatNames)
                }
            }
        }
        val ids = try {
            val valid = manager.availableDatabases().first().map { it.id }.toSet()
            wanted.filter { it in valid }
        } catch (e: Exception) {
            emptyList()
        }
        if (ids.isEmpty()) return emptyList()

        // Categories are per-database: match the selected ones by name in
        // each database, like Random does.
        val catNames: Set<String> = if (params.categoryIds.isEmpty()) {
            emptySet()
        } else {
            try {
                manager.categoryStoreFor(manager.currentDbId.value).list()
                    .filter { it.id in params.categoryIds }
                    .map { it.name }
                    .toSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
        // v1.65: same name-matching for excluded categories (computed
        // earlier so the voice-only path shares it).
        // v1.65: global ceiling ANDed with the per-screen maturity pick.
        val maturities = effectiveMaturities(params.maturity, manager.maturityCeiling.value)
        val excludedMats = params.excludedMaturities
        val excludedCasts = params.excludedCasts
        val filtering = catNames.isNotEmpty() || params.minRating != null ||
            params.mode != LengthMode.ALL || params.voiceOnly ||
            excludedCatNames.isNotEmpty() || maturities.isNotEmpty() ||
            params.cast != null || excludedMats.isNotEmpty() || excludedCasts.isNotEmpty() ||
            params.filterBrowse || params.useRegex
        // v1.74.5: regex mode — the query is a pattern applied in Kotlin
        // (Android's SQLite has no REGEXP). Null with a non-blank query
        // means it wouldn't compile: match nothing.
        val regex: Regex? = if (params.useRegex && q.isNotBlank()) {
            try {
                Regex(q, RegexOption.IGNORE_CASE)
            } catch (_: Exception) {
                null
            }
        } else null
        val regexBad = params.useRegex && q.isNotBlank() && regex == null
        // Over-fetch per database when post-filters will trim the raw hits.
        val perDb = if (filtering) ALL_DB_SEARCH_CAP
        else ((ALL_DB_SEARCH_CAP + ids.size - 1) / ids.size)

        val perDbHits: List<List<SourcedQuote>> = coroutineScope {
            ids.map { id ->
                async(Dispatchers.IO) {
                    try {
                        val repo = manager.repoFor(id)
                        val store = repo.categories
                        // v1.66: local category ids for the name-matched
                        // include/exclude sets (used by both paths below).
                        val mappedCatIds: Set<Long> = if (catNames.isEmpty()) {
                            emptySet()
                        } else {
                            catNames.mapNotNull { name ->
                                store.findByName(name)?.id
                            }.toSet()
                        }
                        val mappedExcludedIds: Set<Long> =
                            if (excludedCatNames.isEmpty()) {
                                emptySet()
                            } else {
                                excludedCatNames.mapNotNull { name ->
                                    store.findByName(name)?.id
                                }.toSet()
                            }
                        val allowedNumbers: Set<Int>? = if (catNames.isEmpty()) {
                            null
                        } else {
                            // This database has none of the selected
                            // categories: it contributes nothing.
                            if (mappedCatIds.isEmpty()) {
                                return@async emptyList<SourcedQuote>()
                            }
                            mappedCatIds.flatMap { cid ->
                                store.quoteNumbersInCategory(cid)
                            }.toSet()
                        }
                        val min = params.minRating
                        // v1.51: each source database's own status mode.
                        val statusMode = manager.statusModeFor(id)
                        // v1.65: excluded-category names mapped into this
                        // database's own category ids.
                        val excludedNumbers: Set<Int>? = if (excludedCatNames.isEmpty()) {
                            null
                        } else {
                            mappedExcludedIds.flatMap { cid ->
                                store.quoteNumbersInCategory(cid)
                            }.toSet()
                        }
                        // v1.66 filter browsing (mood buttons): no text
                        // query — browse this database with the SQL filters
                        // instead of running a full-text search.
                        // v1.74.5 regex: same browse path (SQL filters, no
                        // text constraint); the pattern applies below.
                        val baseQuotes = if (regexBad) {
                            emptyList()
                        } else if ((q.isEmpty() && params.filterBrowse) || regex != null) {
                            repo.browseList(
                                sort = params.sort,
                                minRating = min,
                                categoryIds = mappedCatIds,
                                statusMode = statusMode,
                                maturities = maturities,
                                cast = params.cast,
                                excludedCategoryIds = mappedExcludedIds,
                                limit = perDb
                            )
                        } else {
                            repo.searchList(q, perDb, statusMode, maturities, params.cast)
                        }
                        sortAllDbHits(
                            baseQuotes
                                .asSequence()
                                .filter { quote ->
                                    (regex?.containsMatchIn(quote.text) ?: true) &&
                                    (allowedNumbers == null || quote.number in allowedNumbers) &&
                                        (min == null || (quote.rating != null && quote.rating >= min)) &&
                                        // v1.65 exclusions.
                                        (excludedNumbers == null || quote.number !in excludedNumbers) &&
                                        (excludedMats.isEmpty() || quote.maturity !in excludedMats) &&
                                        (excludedCasts.isEmpty() || excludedCasts.none { it.matches(quote.speakers) }) &&
                                        lengthFilterOk(
                                            quote.text, params.mode, params.minLen,
                                            params.maxLen, longerWords, shorterWords,
                                            manager.qcNormalizeBreaks.value
                                        ) &&
                                        // v1.61.20: "Has voice" filter.
                                        (!params.voiceOnly || VoiceRecordings.hasRecording(id, quote.number)) &&
                                        // v1.75: "Has explanation" filter.
                                        (!params.explanationOnly || ExplanationCache.hasExplanation(id, quote.number))
                                }
                                .map { SourcedQuote(id, it) }
                                .toList(),
                            params.sort
                        )
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll()
        }

        // "Never show again" always hides; "Show anyway" bypasses only the
        // offensive-word filter — same as single-DB search.
        _hiddenCount.value = 0
        val visible = perDbHits.map { hits ->
            hits.filter { sq ->
                val neverShow = manager.isQuoteHiddenOrMuted(sq.dbId, sq.quote.number, sq.quote.text)
                val wordHidden =
                    !showFiltered && manager.isHiddenByContentFilter(sq.quote.text)
                if (wordHidden) _hiddenCount.value = _hiddenCount.value + 1
                !neverShow && !wordHidden
            }
        }

        // Round-robin merge across databases, like Browse's All mode.
        val merged = ArrayList<SourcedQuote>(ALL_DB_SEARCH_CAP)
        var round = 0
        while (merged.size < ALL_DB_SEARCH_CAP) {
            var took = false
            for (hits in visible) {
                if (round < hits.size) {
                    merged.add(hits[round])
                    took = true
                    if (merged.size >= ALL_DB_SEARCH_CAP) break
                }
            }
            if (!took) break
            round++
        }
        // v1.66 cross-database dedup: the same classic quote often lives in
        // several databases. Collapse identical texts into one row and note
        // the other databases on the survivor's "also in" chip.
        return dedupeSourcedQuotes(merged)
    }

    /**
     * v1.66: collapses [SourcedQuote]s with identical normalized text into
     * the first occurrence, recording the other databases' labels in its
     * [SourcedQuote.alsoIn]. Order is preserved.
     */
    private fun dedupeSourcedQuotes(
        quotes: List<SourcedQuote>
    ): List<SourcedQuote> {
        if (quotes.size < 2) return quotes
        val deduped = ArrayList<SourcedQuote>(quotes.size)
        val seen = HashMap<String, Int>()
        for (sq in quotes) {
            val key = sq.quote.text.lowercase().filter { it.isLetterOrDigit() }
            // Texts with no letters/digits never collapse into each other.
            val first = if (key.isEmpty()) null else seen[key]
            if (first == null) {
                if (key.isNotEmpty()) seen[key] = deduped.size
                deduped.add(sq)
            } else {
                val prev = deduped[first]
                val ownLabel = try {
                    manager.shortLabel(prev.dbId)
                } catch (_: Exception) {
                    prev.dbId
                }
                val label = try {
                    manager.shortLabel(sq.dbId)
                } catch (_: Exception) {
                    sq.dbId
                }
                if (label != ownLabel && label !in prev.alsoIn) {
                    deduped[first] = prev.copy(alsoIn = prev.alsoIn + label)
                }
            }
        }
        return deduped
    }

    /**
     * Per-database ordering for all-databases search, mirroring the SQL
     * ordering [QuoteRepository.search] uses (NULL ratings sort last).
     */
    private fun sortAllDbHits(
        hits: List<SourcedQuote>,
        sort: SortOrder
    ): List<SourcedQuote> = when (sort) {
        SortOrder.NUMBER_ASC -> hits.sortedBy { it.quote.number }
        SortOrder.NUMBER_DESC -> hits.sortedByDescending { it.quote.number }
        SortOrder.RATING_ASC, SortOrder.RATING_DESC -> hits.sortedWith { a, b ->
            val desc = sort == SortOrder.RATING_DESC
            val ra = a.quote.rating
            val rb = b.quote.rating
            val byRating = when {
                ra == null && rb == null -> 0
                ra == null -> 1
                rb == null -> -1
                desc -> rb.compareTo(ra)
                else -> ra.compareTo(rb)
            }
            if (byRating != 0) byRating
            else if (desc) b.quote.number.compareTo(a.quote.number)
            else a.quote.number.compareTo(b.quote.number)
        }
    }

    fun updateQuery(q: String) {
        query = q
    }

    fun updateCategories(ids: Set<Long>) {
        categoryIds = ids
    }

    fun updateMinRating(rating: Int?) {
        minRating = rating
    }

    fun updateSortOrder(sort: SortOrder) {
        sortOrder = sort
    }

    /**
     * Whether a minimum-rating filter makes sense for the current search
     * scope: the current database has ratings, or we're searching several
     * databases (Bash.org and #CrapShack carry ratings).
     */
    val searchHasRatings: StateFlow<Boolean> =
        combine(dbHasRatings, snapshotFlow { searchDbIds }) { has, ids ->
            has || ids?.isNotEmpty() == true
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        // A min-rating filter on a scope with no ratings can only show an
        // empty list, so drop it when the scope changes to one without.
        viewModelScope.launch {
            searchHasRatings.collect { has -> if (!has) minRating = null }
        }
    }

    val savedSearches: StateFlow<List<String>> = manager.savedSearches

    /** Persists the current query as a saved search (no-op when blank). */
    fun persistSearch() = manager.saveSearch(query)

    fun removeSavedSearch(q: String) = manager.removeSavedSearch(q)

    fun clearSavedSearches() = manager.clearSavedSearches()

    /** Named saved searches: full search configurations, one tap to re-run. */
    val namedSearches: StateFlow<List<com.quotebrowser.app.data.NamedSearch>> =
        manager.namedSearches

    /** Snapshots the current search configuration under [name]. */
    fun saveNamedSearch(name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        manager.saveNamedSearch(
            com.quotebrowser.app.data.NamedSearch(
                name = clean,
                query = query,
                dbIds = searchDbIds,
                categoryIds = categoryIds,
                minRating = minRating,
                notesScope = notesScope,
                sortOrder = sortOrder,
                lengthMode = lengthMode,
                minLength = minLength ?: 0,
                maxLength = maxLength ?: 0,
                // v1.66: the full v1.65 filter set.
                excludedCategoryIds = excludedCategoryIds,
                maturity = maturity?.name,
                castSize = castSize?.name,
                excludedMaturities = excludedMaturities,
                excludedCasts = excludedCasts.map { it.name }.toSet(),
                voiceOnly = voiceOnly,
                // v1.75: "Has explanation" persists with the named search.
                explanationOnly = explanationOnly,
                useRegex = useRegex
            )
        )
        return true
    }

    /** Applies a named search; the results re-run automatically. */
    fun applyNamedSearch(ns: com.quotebrowser.app.data.NamedSearch) {
        updateSearchDbIds(ns.dbIds)
        updateCategories(ns.categoryIds)
        updateMinRating(ns.minRating)
        updateSortOrder(ns.sortOrder)
        updateNotesScope(ns.notesScope)
        updateLengthMode(ns.lengthMode)
        updateMinLength(ns.minLength.takeIf { it > 0 })
        updateMaxLength(ns.maxLength.takeIf { it > 0 })
        // v1.66: restore the v1.65 filter set (unknown enum names → no-op).
        updateExcludedCategories(ns.excludedCategoryIds)
        updateMaturity(ns.maturity?.let {
            try {
                MaturityLevel.valueOf(it)
            } catch (_: Exception) {
                null
            }
        })
        updateCastSize(ns.castSize?.let {
            try {
                CastSize.valueOf(it)
            } catch (_: Exception) {
                null
            }
        })
        updateExcludedMaturities(ns.excludedMaturities)
        updateExcludedCasts(ns.excludedCasts.mapNotNull {
            try {
                CastSize.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }.toSet())
        if (voiceOnly != ns.voiceOnly) updateVoiceOnly(ns.voiceOnly)
        // v1.75: "Has explanation" restores with the named search too.
        if (explanationOnly != ns.explanationOnly) updateExplanationOnly(ns.explanationOnly)
        // v1.66: a blank-query saved search with filters re-enters filter
        // browsing instead of landing on the empty picker.
        updateFilterBrowse(
            ns.query.isBlank() && (
                ns.categoryIds.isNotEmpty() || ns.excludedCategoryIds.isNotEmpty() ||
                    ns.maturity != null || ns.castSize != null ||
                    ns.excludedMaturities.isNotEmpty() || ns.excludedCasts.isNotEmpty()
                )
        )
        updateQuery(ns.query)
        updateUseRegex(ns.useRegex)
    }

    /**
     * v1.66: snapshots the current search configuration for the recent-
     * searches history.
     */
    fun snapshotSearchRecord(): com.quotebrowser.app.ui.SearchRecord =
        com.quotebrowser.app.ui.SearchRecord(
            query = query,
            dbIds = searchDbIds,
            categoryIds = categoryIds,
            excludedCategoryIds = excludedCategoryIds,
            maturity = maturity?.name,
            castSize = castSize?.name,
            excludedMaturities = excludedMaturities,
            excludedCasts = excludedCasts.map { it.name }.toSet(),
            minRating = minRating,
            voiceOnly = voiceOnly,
            lengthMode = lengthMode.name,
            useRegex = useRegex
        )

    /** v1.66: re-runs a history snapshot exactly. */
    fun applySearchRecord(r: com.quotebrowser.app.ui.SearchRecord) {
        updateSearchDbIds(r.dbIds)
        updateCategories(r.categoryIds)
        updateExcludedCategories(r.excludedCategoryIds)
        updateMaturity(r.maturity?.let {
            try {
                MaturityLevel.valueOf(it)
            } catch (_: Exception) {
                null
            }
        })
        updateCastSize(r.castSize?.let {
            try {
                CastSize.valueOf(it)
            } catch (_: Exception) {
                null
            }
        })
        updateExcludedMaturities(r.excludedMaturities)
        updateExcludedCasts(r.excludedCasts.mapNotNull {
            try {
                CastSize.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }.toSet())
        updateMinRating(r.minRating)
        if (voiceOnly != r.voiceOnly) updateVoiceOnly(r.voiceOnly)
        try {
            updateLengthMode(
                com.quotebrowser.app.data.LengthMode.valueOf(r.lengthMode)
            )
        } catch (_: Exception) {
            updateLengthMode(com.quotebrowser.app.data.LengthMode.ALL)
        }
        // v1.66: a blank-query history entry with filters re-enters filter
        // browsing instead of landing on the empty picker.
        updateFilterBrowse(
            r.query.isBlank() && (
                r.categoryIds.isNotEmpty() || r.excludedCategoryIds.isNotEmpty() ||
                    r.maturity != null || r.castSize != null ||
                    r.excludedMaturities.isNotEmpty() || r.excludedCasts.isNotEmpty()
                )
        )
        updateQuery(r.query)
        updateUseRegex(r.useRegex)
    }

    fun renameNamedSearch(oldName: String, newName: String): Boolean =
        manager.renameNamedSearch(oldName, newName)

    fun deleteNamedSearch(name: String) = manager.deleteNamedSearch(name)

    /** v1.48: toggles the new-match alert for a named saved search. */
    fun setNamedSearchAlert(name: String, alert: Boolean) =
        manager.setNamedSearchAlert(name, alert)
}

class RandomViewModel(private val manager: DatabaseManager) : ViewModel() {

    private val _quotes = kotlinx.coroutines.flow.MutableStateFlow<List<SourcedQuote>>(emptyList())
    val quotes: StateFlow<List<SourcedQuote>> = _quotes

    private val _loading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    /** How many random quotes to load. Defaults to 50. */
    var count by mutableStateOf(50)
        private set

    /** Category filter: empty = all quotes. */
    var categoryIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    /** Category names for cross-database matching (categories are per-database). */
    private var categoryNames: Set<String> = emptySet()

    /** v1.74.1: per-screen maturity filter; null = use the global ceiling. */
    var maturity by mutableStateOf<MaturityLevel?>(null)
        private set

    /** v1.74.1: per-screen cast-size filter; null = all cast sizes. */
    var castSize by mutableStateOf<CastSize?>(null)
        private set

    fun updateMaturity(level: MaturityLevel?) {
        maturity = level
        newRandom()
    }

    fun updateCastSize(cast: CastSize?) {
        castSize = cast
        newRandom()
    }

    /**
     * v1.74.1: same AND logic as QuoteListViewModel.effectiveMaturities —
     * the global ceiling ANDed with the per-screen pick. EVERYTHING on
     * either side is a no-op.
     */
    private fun effectiveRandomMaturities(
        screen: MaturityLevel?,
        ceiling: MaturityLevel
    ): List<MaturityLevel> = listOfNotNull(
        ceiling.takeIf { it != MaturityLevel.EVERYTHING },
        screen?.takeIf { it != MaturityLevel.EVERYTHING }
    )

    val databases: StateFlow<List<DbInfo>> = manager.availableDatabases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Null = pull from all databases. */
    val sourceIds: StateFlow<Set<String>?> = manager.randomSourceIds

    /** When on, newly shuffled quotes skip ones already marked seen. */
    val hideSeen: StateFlow<Boolean> = manager.hideSeen

    /**
     * Per-screen temporary content-filter bypass ("Show" on the hidden
     * banner). In-memory only; the screen resets it when leaving.
     */
    private val _showFiltered = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showFiltered: StateFlow<Boolean> = _showFiltered.asStateFlow()

    /**
     * How many quotes the content filter removed from the last fetched
     * batch; 0 when the filter is off or bypassed.
     */
    private val _hiddenCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    /** Temporarily shows (or re-hides) filter-hidden quotes on this screen. */
    fun setShowFiltered(show: Boolean) {
        if (_showFiltered.value == show) return
        _showFiltered.value = show
        newRandom()
    }

    fun setHideSeen(enabled: Boolean) {
        manager.setHideSeen(enabled)
        newRandom()
    }

    /** Effective source ids, dropping any that no longer exist. */
    private suspend fun effectiveSourceIds(): List<String> {
        val all = manager.allDbIds()
        val selected = sourceIds.value
        val ids = if (selected == null) all else all.filter { it in selected }
        return ids.ifEmpty { all }
    }

    val favoriteKeys: StateFlow<Set<Pair<String, Int>>> =
        combine(sourceIds, databases) { selected, dbs ->
            selected?.toList() ?: dbs.map { it.id }
        }.flatMapLatest { ids ->
            if (ids.isEmpty()) kotlinx.coroutines.flow.flowOf(emptySet())
            else combine(
                ids.map { id ->
                    manager.metaDb.metaDao().favoriteEntries(id)
                        .map { entries -> entries.map { entry -> id to entry.number }.toSet() }
                }
            ) { sets -> sets.flatMap { it }.toSet() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Back-stack of previously shown random sets; swipe left walks back. */
    private val backStack = ArrayDeque<List<SourcedQuote>>()

    init {
        newRandom()
    }

    fun newRandom() {
        // Remember what's on screen before replacing it (cap the history).
        val current = _quotes.value
        if (current.isNotEmpty()) {
            backStack.addLast(current)
            while (backStack.size > 50) backStack.removeFirst()
        }
        // v1.88.11: the batch load opens quote databases (first open can
        // rebuild a stale copy — heavy disk I/O), so run it off the main
        // thread. The spinner (_loading) already covers the wait.
        // v1.88.13: snapshot Compose state on the calling thread (usually
        // Main) BEFORE the IO launch — reading mutableStateOf from the IO
        // coroutine throws a snapshot-state error and crashes on launch.
        val batchCount = count
        val filterParams = captureFilterParams()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _loading.value = true
            try {
                _quotes.value = fetchRandomBatch(batchCount, filterParams)
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Fetches [limit] random quotes honoring the current source and category
     * filters. Also used by the endless deck to keep appending cards.
     *
     * v1.49 "random without repeats": when enabled, quotes already shown
     * by Random this cycle are excluded at the SQL level, so the batch is
     * actually fresh. Only when the query returns nothing fresh — i.e. the
     * whole eligible pool is exhausted, not just one sampled batch — does
     * the cycle reset and start over. Shown quotes are recorded in the
     * cycle (kept separate from reading-stats "seen", which is never
     * cleared by a reset).
     */
    // v1.88.13: Compose snapshot state (mutableStateOf) may only be read on
    // the thread that owns the snapshot — reading it from a Dispatchers.IO
    // coroutine throws "Reading a state that was created after the snapshot
    // was taken". Capture the filter values on the calling thread and pass
    // them down explicitly so the IO work never touches snapshot state.
    private data class RandomFilterParams(
        val catNames: Set<String>,
        val maturity: MaturityLevel?,
        val castSize: CastSize?
    )

    private fun captureFilterParams(): RandomFilterParams =
        RandomFilterParams(categoryNames, maturity, castSize)

    suspend fun fetchRandomBatch(limit: Int): List<SourcedQuote> =
        fetchRandomBatch(limit, captureFilterParams())

    private suspend fun fetchRandomBatch(limit: Int, params: RandomFilterParams): List<SourcedQuote> {
        val noRepeat = manager.randomNoRepeat.value
        val cycle = if (noRepeat) manager.randomCycle.value else emptySet()
        // Cycle keys are "dbId/number"; group the numbers per database for
        // SQL-level exclusion.
        val excludeByDb: Map<String, List<Int>> =
            if (cycle.isEmpty()) emptyMap()
            else cycle
                .groupBy(
                    { it.substringBefore("/") },
                    { it.substringAfter("/").toIntOrNull() ?: -1 }
                )
                .mapValues { (_, nums) -> nums.filter { it >= 0 } }
                .filterValues { it.isNotEmpty() }
        var visible = fetchBatch(limit, excludeByDb, params)
        if (noRepeat) {
            if (visible.isEmpty() && excludeByDb.isNotEmpty()) {
                // The full eligible pool is exhausted: reset the cycle and
                // start over with a fresh pool.
                manager.clearRandomCycle()
                visible = fetchBatch(limit, emptyMap(), params)
            }
            manager.addToRandomCycle(visible.map { "${it.dbId}/${it.quote.number}" })
        }
        return visible
    }

    /**
     * One sampling pass: [limit] random quotes with per-database SQL
     * exclusion lists, then the usual seen/content-filter/hidden filtering.
     */
    private suspend fun fetchBatch(
        limit: Int,
        excludeByDb: Map<String, List<Int>>,
        params: RandomFilterParams
    ): List<SourcedQuote> {
        val hide = hideSeen.value
        val filterWords = manager.contentFilterEnabled.value && !_showFiltered.value
        // "Never show again" quotes are always excluded from Random, even
        // when the offensive-word filter is off.
        val hasUserHidden = manager.hiddenQuoteKeys.value.isNotEmpty()
        // Over-fetch when hiding seen/filtered quotes so the set stays full.
        val filtering = hide || filterWords || hasUserHidden
        val target = if (filtering) (limit * 3).coerceIn(limit, 500) else limit
        val ids = effectiveSourceIds()
        val perDb = if (ids.isEmpty()) 0
        else ((target + ids.size - 1) / ids.size).coerceIn(1, 500)
        val catNames = params.catNames
        val out = mutableListOf<SourcedQuote>()
        for (id in ids) {
            val repo = manager.repoFor(id)
            val catIds = if (catNames.isEmpty()) {
                emptySet()
            } else {
                catNames.mapNotNull { name ->
                    repo.categories.findByName(name)?.id
                }.toSet()
            }
            // A name filter that this database doesn't have contributes nothing.
            if (catNames.isNotEmpty() && catIds.isEmpty()) continue
            // v1.51: this database's own status mode applies to Random.
            // v1.65: the global maturity ceiling applies to Random too.
            // v1.74.1: per-screen maturity ANDed with the ceiling, plus
            // the per-screen cast-size filter.
            out += repo.randomQuotes(
                perDb, catIds, excludeByDb[id].orEmpty(),
                manager.statusModeFor(id),
                maturities = effectiveRandomMaturities(params.maturity, manager.maturityCeiling.value),
                cast = params.castSize
            ).map { SourcedQuote(dbId = id, quote = it) }
        }
        val batch = out.shuffled().take(target)
        if (!filtering) {
            _hiddenCount.value = 0
            return batch.take(limit)
        }
        val seenByDb = mutableMapOf<String, Set<Int>>()
        var hiddenByFilter = 0
        val visible = batch.filter { sq ->
            val seenOk = if (!hide) true else {
                val seen = seenByDb.getOrPut(sq.dbId) { manager.seenNumbers(sq.dbId) }
                sq.quote.number !in seen
            }
            // "Never show again" always applies; "Show anyway" bypasses
            // only the offensive-word filter.
            val neverShow = manager.isQuoteHiddenOrMuted(sq.dbId, sq.quote.number, sq.quote.text)
            val passesWords =
                !filterWords || !manager.isHiddenByContentFilter(sq.quote.text)
            if (filterWords && !passesWords) hiddenByFilter++
            seenOk && !neverShow && passesWords
        }.take(limit)
        _hiddenCount.value = hiddenByFilter
        return visible
    }

    /** Records a quote as seen (reading stats); safe to call repeatedly. */
    fun markSeen(dbId: String, number: Int) {
        viewModelScope.launch { manager.markSeen(dbId, number) }
    }

    /** Local thumbs-up/down vote (+1 / -1 / 0 to clear). */
    fun rateQuote(dbId: String, number: Int, delta: Int) {
        manager.setLocalRating(dbId, number, delta)
    }

    fun updateCount(n: Int) {
        count = n.coerceIn(1, 500)
    }

    /**
     * Steps back to the previously shown random set. Returns false when
     * there's nothing earlier (already at the oldest set).
     */
    fun goBack(): Boolean {
        val previous = backStack.removeLastOrNull() ?: return false
        _quotes.value = previous
        return true
    }

    fun updateCategories(ids: Set<Long>, names: Set<String>) {
        categoryIds = ids
        categoryNames = names
        newRandom()
    }

    /** v1.70: category names driving the current filter (per-database matching). */
    fun currentCategoryNames(): Set<String> = categoryNames

    fun updateSources(ids: Set<String>?) {
        manager.setRandomSourceIds(ids)
        newRandom()
    }

    fun toggleFavorite(dbId: String, quote: Quote) {
        viewModelScope.launch {
            manager.repoFor(dbId).toggleFavorite(quote)
        }
    }

    /** "Never show again": hides this quote from every list. */
    fun hideQuote(dbId: String, number: Int) {
        manager.setQuoteHidden(dbId, number, true)
    }

    /** v1.53 report/flag. */
    fun flagQuote(dbId: String, number: Int, reason: String, customText: String) {
        manager.flagQuote(dbId, number, reason, customText)
    }

    fun unflagQuote(dbId: String, number: Int) {
        manager.unflagQuote(dbId, number)
    }

    val flaggedQuotes: StateFlow<Map<String, FlagInfo>> = manager.flaggedQuotes

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)
}

class DetailViewModel(
    private val manager: DatabaseManager,
    initialNumber: Int,
    /**
     * v1.56: explicit source database for this detail (set by [openQuote]
     * from the card's real source db). When null the detail seeds from the
     * current global selection, as before. Seeding explicitly means
     * opening a quote from mixed All-databases mode no longer switches
     * the global selection — the detail keeps its real source database
     * independently.
     */
    initialDbId: String? = null
) : ViewModel() {

    /** v1.49: the personal-notes feature toggle gates the note section. */
    val notesEnabled: StateFlow<Boolean> = manager.notesEnabled

    /** v1.50: pinch-to-zoom on the quote body (Settings → Features toggle). */
    val pinchZoomEnabled: StateFlow<Boolean> = manager.pinchZoomEnabled
    fun setPinchZoomEnabled(enabled: Boolean) = manager.setPinchZoomEnabled(enabled)

    /** v1.50: persisted pinch-zoom scale for the quote body (1.0 = base). */
    val detailTextScale: StateFlow<Float> = manager.detailTextScale
    fun setDetailTextScale(scale: Float) = manager.setDetailTextScale(scale)

    /** v1.53 report/flag. */
    val flaggedQuotes: StateFlow<Map<String, FlagInfo>> = manager.flaggedQuotes
    fun flagQuote(dbId: String, number: Int, reason: String, customText: String) =
        manager.flagQuote(dbId, number, reason, customText)
    fun unflagQuote(dbId: String, number: Int) = manager.unflagQuote(dbId, number)

    /**
     * The quote on screen, as a (database id, quote number) pair. This is the
     * single source of truth for quote loading: swiping can move it across
     * databases, and [DatabaseManager.switchDatabase] is kept in sync as a
     * side effect so the rest of the app (switcher pill, favorites, QotD,
     * etc.) agrees on which database is showing.
     */
    private val positionFlow =
        kotlinx.coroutines.flow.MutableStateFlow(
            manager.singleDbId(initialDbId ?: manager.currentDbId.value) to initialNumber
        )

    private val _quote = kotlinx.coroutines.flow.MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote

    /**
     * Real database id of the quote on screen. The virtual "all" id is
     * resolved to the database the quote was actually loaded from, so the
     * detail header always shows the true source database title — never
     * "All databases".
     */
    val positionDbId: StateFlow<String> =
        positionFlow.map { (id, _) -> manager.singleDbId(id) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                manager.singleDbId(positionFlow.value.first)
            )

    private val _loaded = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    /**
     * v1.56: one-shot event fired when following an external database
     * switch lands on a quote number that doesn't exist in the newly
     * selected database. The UI bounces back to the quote list with a
     * snackbar instead of dead-ending on the full-screen "doesn't exist"
     * error (Bug A: switching databases must never replace content with
     * that error).
     */
    private val _followMissed =
        kotlinx.coroutines.flow.MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 1)
    val followMissed: kotlinx.coroutines.flow.SharedFlow<Pair<String, Int>> = _followMissed

    /** Whether IRC " \" separators render (and read) as line breaks. */
    val ircLineBreaks: StateFlow<Boolean> = manager.ircLineBreaks

    /** Whether dialogue turns ("..." <nick>) render as separate lines. */
    val dialogueLineBreaks: StateFlow<Boolean> = manager.dialogueLineBreaks

    /** Whether read-aloud gives each speaker nick its own TTS voice. */
    val ttsPerNickVoices: StateFlow<Boolean> = manager.ttsPerNickVoices

    /**
     * Swipe history: every quote visited on this screen, so swiping back
     * returns to the previous quote even in random mode. [historyIndex] is
     * the current position within it.
     */
    private val history = mutableListOf<Pair<String, Int>>()
    private var historyIndex = -1

    val isFavorite: Flow<Boolean> =
        positionFlow.flatMapLatest { (id, n) -> manager.metaDb.metaDao().isFavorite(id, n) }

    /** This quote's local thumbs rating: -1 (down), 0 (none), 1 (up). */
    val myRating: StateFlow<Int> =
        kotlinx.coroutines.flow.combine(
            positionFlow, manager.localRatings
        ) { (id, n), ratings -> ratings["$id/$n"] ?: 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setMyRating(delta: Int) {
        val (id, n) = positionFlow.value
        manager.setLocalRating(id, n, delta)
    }

    private val _catTick = kotlinx.coroutines.flow.MutableStateFlow(0)

    /** Categories assigned to this quote in its database. */
    val categories: StateFlow<List<Category>> =
        combine(positionFlow, _catTick) { pos, _ -> pos }
            .flatMapLatest { (id, n) ->
                kotlinx.coroutines.flow.flow {
                    emit(manager.categoryStoreFor(id).forQuote(n))
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategories(ids: Set<Long>) {
        val (id, n) = positionFlow.value
        viewModelScope.launch {
            manager.categoryStoreFor(id).setForQuote(n, ids)
            _catTick.value += 1
        }
    }

    init {
        history.add(positionFlow.value)
        historyIndex = 0
        // Load the quote at the current position.
        viewModelScope.launch {
            positionFlow.collect { (id, n) ->
                _loaded.value = false
                // v1.88.12: repoFor() can do disk work on a cold open;
                // keep it off the main dispatcher.
                _quote.value = withContext(Dispatchers.IO) { manager.repoFor(id).getQuote(n) }
                _loaded.value = true
                // Reading stats: opening a quote counts as seeing it.
                manager.markSeen(id, n)
            }
        }
        // Follow database switches made outside this screen (e.g. the
        // top-bar switcher): keep showing the same quote number, now in the
        // newly selected database — but only when the detail was in sync
        // with the previous global selection, and only when that database
        // actually has the quote. v1.56: an independently-seeded detail
        // (opened from mixed All-databases mode) ignores external switches
        // and keeps its own source database; a switch landing on a missing
        // number emits [followMissed] so the UI can return to the list with
        // a snackbar instead of dead-ending on the full-screen "doesn't
        // exist" error. Switches this screen makes itself (swipes) already
        // match [positionFlow], so they are ignored here.
        viewModelScope.launch {
            var lastSeen = manager.currentDbId.value
            manager.currentDbId.collect { id ->
                if (id == lastSeen) return@collect
                val (curId, curN) = positionFlow.value
                val wasSynced =
                    manager.singleDbId(curId) == manager.singleDbId(lastSeen)
                lastSeen = id
                if (!wasSynced) return@collect
                if (manager.singleDbId(id) == manager.singleDbId(curId)) return@collect
                if (manager.repoFor(id).getQuote(curN) == null) {
                    _followMissed.tryEmit(id to curN)
                } else {
                    positionFlow.value = id to curN
                }
            }
        }
    }

    /** This quote's personal note, or null. */
    val note: StateFlow<String?> =
        kotlinx.coroutines.flow.combine(
            positionFlow, manager.notes
        ) { (id, n), notes -> notes["$id/$n"] }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Sets (blank deletes) the personal note on this quote. */
    fun setNote(text: String) {
        val (id, n) = positionFlow.value
        manager.setNote(id, n, text)
    }

    fun toggleFavorite() {
        val (id, _) = positionFlow.value
        val q = _quote.value ?: return
        viewModelScope.launch {
            manager.repoFor(id).toggleFavorite(q)
        }
    }

    /**
     * Moves to [dbId]/[number], recording the visit in swipe history and
     * keeping the app's selected database in sync.
     */
    private fun navigateTo(dbId: String, number: Int, syncSelection: Boolean = true) {
        val pos = dbId to number
        if (historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        if (history.isEmpty() || history.last() != pos) history.add(pos)
        historyIndex = history.size - 1
        positionFlow.value = pos
        if (syncSelection && dbId != manager.currentDbId.value) manager.switchDatabase(dbId)
    }

    /**
     * v1.56: shows a specific quote without touching the global database
     * selection (two-pane layout: the pane keeps its own source database
     * independently, like the seeded detail route).
     */
    fun showQuote(dbId: String, number: Int) = navigateTo(dbId, number, syncSelection = false)

    /** Switches the detail view to another quote (two-pane layout). */
    fun openNumber(number: Int) {
        navigateTo(positionFlow.value.first, number)
    }

    /** Jumps to a quote in a specific database (e.g. from "More like this"). */
    fun openSourcedQuote(dbId: String, number: Int) = navigateTo(dbId, number)

    /** All databases, for the "More like this" database picker. */
    val databases: StateFlow<List<com.quotebrowser.app.data.DbInfo>> =
        manager.availableDatabases()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)

    /**
     * Quotes ranked by token-overlap similarity to [quote]. [dbIds] = null
     * searches just this database; otherwise those databases. Pass the
     * [personalRatings] store to add the rate-as-you-read taste boost
     * (nicks/categories the user rates >= 4.0 stars get a small bonus).
     */
    suspend fun similarQuotesFor(
        quote: Quote,
        dbIds: Set<String>?,
        limit: Int = 10,
        personalRatings: com.quotebrowser.app.data.PersonalRatings? = null
    ): List<com.quotebrowser.app.data.ScoredQuote> {
        val (id, _) = positionFlow.value
        return com.quotebrowser.app.data.findSimilarQuotes(
            manager, id, quote, limit, dbIds ?: setOf(id), personalRatings
        )
    }

    /** Swipe left: the next quote per the swipe source/order settings. */
    fun swipeNext() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val from = positionFlow.value
            val target =
                if (manager.detailSwipeOrder.value == "ordered") nextOrdered(from)
                else randomTarget(from)
            if (target != from) navigateTo(target.first, target.second)
        }
    }

    /**
     * Swipe right: back to the previously viewed quote. History-based, so it
     * behaves the same in random and ordered mode.
     */
    fun swipePrevious() {
        if (historyIndex <= 0) return
        historyIndex--
        val (dbId, number) = history[historyIndex]
        positionFlow.value = dbId to number
        if (dbId != manager.currentDbId.value) manager.switchDatabase(dbId)
    }

    /**
     * Next quote by number. When "swipe through all databases" is on,
     * continues into the next database (in the user's database order, wrapping
     * around) at the end of the current one.
     */
    private suspend fun nextOrdered(from: Pair<String, Int>): Pair<String, Int> {
        val (dbId, n) = from
        manager.repoFor(dbId).nextNumber(n)?.let { return dbId to it }
        if (!manager.detailSwipeAllDbs.value) return from
        val ids = manager.availableDatabases().first().map { it.id }
        if (ids.isEmpty()) return from
        val start = ids.indexOf(dbId).let { if (it < 0) 0 else it }
        for (step in 1..ids.size) {
            val cand = ids[(start + step) % ids.size]
            manager.repoFor(cand).firstNumber()?.let { return cand to it }
        }
        return from
    }

    /**
     * A random quote: from the current database, or from any database
     * (weighted by quote count) when "swipe through all databases" is on.
     * Redraws a few times to avoid landing on the quote you're already on.
     */
    private suspend fun randomTarget(from: Pair<String, Int>): Pair<String, Int> {
        if (!manager.detailSwipeAllDbs.value) {
            val dbId = from.first
            val repo = manager.repoFor(dbId)
            repeat(4) {
                val q = repo.randomQuote() ?: return from
                // v1.65: detail swipe honors the global maturity ceiling.
                if (q.number != from.second && manager.isMaturityAllowed(q)) return dbId to q.number
            }
            return from
        }
        val dbs = manager.browseDatabases().first()
            .filter { it.id != DatabaseManager.ALL_DBS_ID && (it.quoteCount ?: 0) > 0 }
        val total = dbs.sumOf { (it.quoteCount ?: 0).toLong() }
        if (total <= 0) return from
        repeat(4) {
            var r = kotlin.random.Random.nextLong(total)
            var pick = dbs.last()
            for (db in dbs) {
                r -= (db.quoteCount ?: 0)
                if (r < 0) {
                    pick = db
                    break
                }
            }
            val q = manager.repoFor(pick.id).randomQuote() ?: return from
            val target = pick.id to q.number
            // v1.65: detail swipe honors the global maturity ceiling.
            if (target != from && manager.isMaturityAllowed(q)) return target
        }
        return from
    }
}

class FavoritesViewModel(manager: DatabaseManager) : QuoteListViewModel(manager) {

    /** Selected folder filter: null = All favorites. */
    var selectedFolder by mutableStateOf<String?>(null)
        private set

    val folders: StateFlow<List<String>> = manager.favoriteFolders

    /**
     * Per-screen temporary content-filter bypass ("Show" on the hidden
     * banner). In-memory only; the screen resets it when leaving.
     */
    private val _showFiltered = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showFiltered: StateFlow<Boolean> = _showFiltered.asStateFlow()

    /** How many favorites the content filter removed; 0 when off/bypassed. */
    private val _hiddenCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    fun setShowFiltered(show: Boolean) {
        if (_showFiltered.value == show) return
        _showFiltered.value = show
    }

    /**
     * All quotes in one favorites folder (unfiltered), for collection
     * export. v1.77: in the virtual "all" view, collects the folder's
     * favorites across every database (each resolved from its own DB).
     */
    suspend fun quotesForCollection(folder: String): List<Quote> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val dbId = manager.currentDbId.value
            if (dbId != DatabaseManager.ALL_DBS_ID) {
                manager.repoFor(dbId).favorites()
                    .first()
                    .filter { manager.folderForFavorite(dbId, it.number) == folder }
            } else {
                manager.metaDb.metaDao().allFavoriteEntries().first().mapNotNull { e ->
                    if (manager.folderForFavorite(e.dbId, e.number) != folder) null
                    else try {
                        manager.repoFor(e.dbId).getQuote(e.number)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }

    val favorites: StateFlow<List<Quote>> = combine(
        manager.currentDbId,
        snapshotFlow { selectedFolder },
        manager.folderTick,
        manager.contentFilterTick
    ) { id, folder, _, _ -> id to folder }
        .combine(_showFiltered) { main, show -> main to show }
        .flatMapLatest { (idFolder, show) ->
            val (id, folder) = idFolder
            manager.repoFor(id).favorites().map { list ->
                val filtered = if (folder == null) list
                else list.filter { q -> manager.folderForFavorite(id, q.number) == folder }
                // "Never show again" always hides; "Show anyway" bypasses
                // only the offensive-word filter.
                var wordHidden = 0
                val visible = filtered.filter { q ->
                    if (manager.isQuoteHiddenOrMuted(id, q.number, q.text)) return@filter false
                    val hidden = !show && manager.isHiddenByContentFilter(q.text)
                    if (hidden) wordHidden++
                    !hidden
                }
                _hiddenCount.value = wordHidden
                visible
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFolder(folder: String?) {
        selectedFolder = folder
    }

    /**
     * v1.77: source-aware favorites for the virtual "all" view. The old
     * path collapsed "all" to the first bundled database, so every card
     * got the wrong source id (and the wrong site theme — or none). Each
     * entry here keeps its true source dbId, resolved from its own
     * database; missing quotes are dropped. Empty unless "all" is
     * selected. Folder and content filters apply per entry's own dbId,
     * mirroring the single-database [favorites] flow.
     */
    val allDbFavorites: StateFlow<List<SourcedQuote>> =
        combine(
            manager.currentDbId,
            snapshotFlow { selectedFolder },
            manager.folderTick,
            manager.contentFilterTick
        ) { id, folder, _, _ -> id to folder }
            .combine(_showFiltered) { main, show -> main to show }
            .flatMapLatest { (idFolder, show) ->
                val (id, folder) = idFolder
                if (id != DatabaseManager.ALL_DBS_ID) {
                    flowOf(emptyList())
                } else {
                    manager.metaDb.metaDao().allFavoriteEntries().map { entries ->
                        var wordHidden = 0
                        val visible = entries.mapNotNull { e ->
                            val q = try {
                                manager.repoFor(e.dbId).getQuote(e.number)
                            } catch (_: Exception) {
                                null
                            } ?: return@mapNotNull null
                            if (folder != null &&
                                manager.folderForFavorite(e.dbId, e.number) != folder
                            ) return@mapNotNull null
                            if (manager.isQuoteHiddenOrMuted(e.dbId, e.number, q.text)) {
                                return@mapNotNull null
                            }
                            val hidden = !show && manager.isHiddenByContentFilter(q.text)
                            if (hidden) wordHidden++
                            if (hidden) null else SourcedQuote(e.dbId, q)
                        }
                        _hiddenCount.value = wordHidden
                        visible
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun folderFor(number: Int): String? =
        manager.folderForFavorite(manager.currentDbId.value, number)

    fun setFolder(number: Int, folder: String?) =
        manager.setFavoriteFolder(manager.currentDbId.value, number, folder)

    fun addFolder(name: String): Boolean = manager.addFavoriteFolder(name)

    fun renameFolder(old: String, new: String): Boolean {
        val ok = manager.renameFavoriteFolder(old, new)
        if (ok && selectedFolder == old) selectedFolder = new.trim()
        return ok
    }

    fun deleteFolder(name: String) {
        manager.deleteFavoriteFolder(name)
        if (selectedFolder == name) selectedFolder = null
    }
}

class HistoryViewModel(manager: DatabaseManager) : QuoteListViewModel(manager) {

    /**
     * Per-screen temporary content-filter bypass ("Show" on the hidden
     * banner). In-memory only; the screen resets it when leaving.
     */
    private val _showFiltered = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showFiltered: StateFlow<Boolean> = _showFiltered.asStateFlow()

    /** How many history entries the content filter removed; 0 when off/bypassed. */
    private val _hiddenCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    fun setShowFiltered(show: Boolean) {
        if (_showFiltered.value == show) return
        _showFiltered.value = show
    }

    /**
     * v1.53: history entries with the date/time each quote was viewed.
     * [history] derives the plain quote list for export/selection flows.
     */
    val historyItems: StateFlow<List<HistoryItem>> = combine(
        manager.currentDbId,
        manager.contentFilterTick
    ) { id, _ -> id }
        .combine(_showFiltered) { id, show -> id to show }
        .flatMapLatest { (id, show) ->
            // v1.77: the virtual "all" view mixes databases, so history is
            // loaded across every database with each entry keeping its
            // true source dbId (the old path only showed the first bundled
            // database's history, mis-themed). Single-database mode is
            // unchanged.
            val raw: kotlinx.coroutines.flow.Flow<List<HistoryItem>> =
                if (id != DatabaseManager.ALL_DBS_ID) {
                    manager.repoFor(id).historyWithTimes()
                } else {
                    manager.metaDb.metaDao().allHistoryEntries().map { entries ->
                        val dao = manager.metaDb.metaDao()
                        entries.mapNotNull { e ->
                            val q = try {
                                manager.repoFor(e.dbId).getQuote(e.number)
                            } catch (_: Exception) {
                                null
                            } ?: return@mapNotNull null
                            HistoryItem(q, e.viewedAt, dao.viewCount(e.dbId, e.number), e.dbId)
                        }
                    }
                }
            raw.map { list ->
                // "Never show again" always hides; "Show anyway" bypasses
                // only the offensive-word filter.
                var wordHidden = 0
                val visible = list.filter { item ->
                    val q = item.quote
                    // v1.77: per-entry source db (falls back to the current
                    // id for legacy single-DB items).
                    val itemDbId = item.dbId.ifEmpty { id }
                    if (manager.isQuoteHiddenOrMuted(itemDbId, q.number, q.text)) return@filter false
                    val hidden = !show && manager.isHiddenByContentFilter(q.text)
                    if (hidden) wordHidden++
                    !hidden
                }
                _hiddenCount.value = wordHidden
                visible
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Plain quote list (no timestamps), for export and selection flows. */
    val history: StateFlow<List<Quote>> = historyItems
        .map { items -> items.map { it.quote } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            // v1.77: in the virtual "all" view, clear every database's
            // history, not just the first bundled database's.
            val id = manager.currentDbId.value
            if (id == DatabaseManager.ALL_DBS_ID) {
                val dao = manager.metaDb.metaDao()
                dao.clearAllHistory()
                dao.clearAllHistoryViews()
            } else {
                manager.repoFor(id).clearHistory()
            }
        }
    }

    /** Every recorded view datetime for one quote, newest first (v1.53). */
    suspend fun viewTimesFor(dbId: String, number: Int): List<Long> =
        manager.repoFor(dbId).viewTimes(number)
}

/** One exact-text match while adding a quote. */
data class DuplicateHit(val dbId: String, val number: Int, val label: String)

/** Manages the built-in "My Quotes" database: the user's own quotes. */
class MyQuotesViewModel(
    private val manager: DatabaseManager,
    // v1.88.36: generalized for user-created databases ("My Databases") —
    // each user DB gets its own VM instance keyed by dbId.
    val dbId: String = DatabaseManager.MY_QUOTES_ID
) : ViewModel() {

    private val myRepo: QuoteRepository
        get() = manager.repoFor(dbId)

    private val _tick = kotlinx.coroutines.flow.MutableStateFlow(0)

    val quotes: StateFlow<List<Quote>> = _tick
        .flatMapLatest {
            flow {
                emit(
                    myRepo.getAllQuotes()
                        .sortedByDescending { it.number }
                )
            }
        }
        // v1.88.12: myRepo resolves the DB on first access; keep it off Main.
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Exact-text matches across every database (for duplicate warnings). */
    suspend fun findDuplicates(text: String): List<DuplicateHit> {
        val clean = text.trim()
        if (clean.isEmpty()) return emptyList()
        val out = mutableListOf<DuplicateHit>()
        for (id in manager.allDbIds()) {
            val n = manager.repoFor(id).findDuplicate(clean)
            if (n != null) out += DuplicateHit(id, n, manager.shortLabel(id))
        }
        return out
    }

    suspend fun addQuote(text: String, rating: Int?, url: String = ""): Quote {
        val q = myRepo.addQuote(text.trim(), rating, url.trim())
        autoTagMyQuote(q.number, q.text)
        _tick.value += 1
        return q
    }

    /**
     * Heuristic auto-tagging (same rules as the bundled archives): creates
     * any missing categories in My Quotes and assigns the matches.
     */
    private suspend fun autoTagMyQuote(number: Int, text: String) {
        val names = com.quotebrowser.app.ui.AutoTagger.autoTagCategories(text)
        if (names.isEmpty()) return
        val store = myRepo.categories
        val ids = names.map { name ->
            store.findByName(name)?.id ?: store.add(name)
        }
        store.addMappings(number, ids)
    }

    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            myRepo.updateQuote(quote)
            _tick.value += 1
        }
    }

    fun deleteQuote(number: Int) {
        viewModelScope.launch {
            myRepo.deleteQuote(number)
            _tick.value += 1
        }
    }

    /**
     * Imports text blocks as quotes. Returns (imported, skipped) where
     * skipped counts blanks and duplicates.
     */
    suspend fun importText(content: String): Pair<Int, Int> {
        val blocks = parseQuoteBlocks(content)
        if (blocks.isEmpty()) return 0 to 0
        val existing = myRepo.getAllQuotes().map { it.text.trim() }.toMutableSet()
        val seen = mutableSetOf<String>()
        var imported = 0
        var skipped = 0
        for (block in blocks) {
            val t = block.trim()
            if (t.isEmpty() || t in seen || t in existing) {
                skipped++
                continue
            }
            seen += t
            val q = myRepo.addQuote(t, null)
            autoTagMyQuote(q.number, q.text)
            existing += t
            imported++
        }
        _tick.value += 1
        return imported to skipped
    }

    /**
     * Imports a list of quote texts (e.g. from a shared collection file).
     * Returns (imported, skipped) where skipped counts blanks and duplicates.
     * The collection's name is recreated as a category (reusing an existing
     * one with the same name) so the imported quotes stay grouped together.
     */
    suspend fun importCollectionTexts(
        collectionName: String?,
        texts: List<String>
    ): Pair<Int, Int> {
        val existing = myRepo.getAllQuotes().map { it.text.trim() }.toMutableSet()
        val seen = mutableSetOf<String>()
        val importedNumbers = mutableListOf<Int>()
        var imported = 0
        var skipped = 0
        for (raw in texts) {
            val t = raw.trim()
            if (t.isEmpty() || t in seen || t in existing) {
                skipped++
                continue
            }
            seen += t
            val q = myRepo.addQuote(t, null)
            autoTagMyQuote(q.number, q.text)
            importedNumbers += q.number
            existing += t
            imported++
        }
        val name = collectionName?.trim().orEmpty()
        if (name.isNotEmpty() && importedNumbers.isNotEmpty()) {
            val store = myRepo.categories
            val catId = store.findByName(name)?.id ?: store.add(name)
            for (n in importedNumbers) store.addMappings(n, listOf(catId))
        }
        _tick.value += 1
        return imported to skipped
    }
}

/**
 * Splits pasted/file text into quote blocks: separator lines of ---, ===,
 * %%% or *** win when present, otherwise blank lines separate quotes.
 *
 * Also detects the IRC bot-dump line format ("quote text -- Added By nick" /
 * "-- Added: <date> -- By nick" / "-- <date> - nick", with optional embedded
 * IRC color codes): when most non-blank lines end with one of those
 * trailers, every line is its own quote and the trailer + codes are
 * stripped. Without this, a thousands-line dump with no blank lines
 * imports as a single block.
 */
private fun parseQuoteBlocks(content: String): List<String> {
    val normalized = content.replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.lines()
    val ircBlocks = parseIrcDumpLines(lines)
    if (ircBlocks != null) return ircBlocks
    val sep = Regex("""^\s*(-{3,}|={3,}|%{3,}|\*{3,})\s*$""")
    return if (lines.any { it.matches(sep) }) {
        val blocks = mutableListOf<String>()
        val cur = StringBuilder()
        for (line in lines) {
            if (line.matches(sep)) {
                blocks += cur.toString()
                cur.clear()
            } else {
                cur.appendLine(line)
            }
        }
        blocks += cur.toString()
        blocks
    } else {
        normalized.split(Regex("""\n\s*\n"""))
    }
}

/** End-of-line trailer of an IRC bot-dump quote line. */
private val ircTrailerRegex =
    Regex("""\s*(--\s*Added By \S+|--\s*Added:\s*\d{2}/\d{2}/\d{2,4}\s*--\s*By \S+|--\s*\d{2}/\d{2}/\d{2,4}\s*-\s*\S+)\s*$""")
private val ircColorRegex = Regex("\u0003\\d{0,2}(,\\d{1,2})?")
private val ircCodeRegex = Regex("[\u0002\u001f\u000f\u0016]")

/**
 * When the content looks like an IRC bot dump (most non-blank lines end
 * with an "-- Added By ..." style trailer), returns one cleaned quote per
 * line, or null when the format doesn't apply.
 */
private fun parseIrcDumpLines(lines: List<String>): List<String>? {
    val nonBlank = lines.filter { it.isNotBlank() }
    if (nonBlank.size < 3) return null
    val withTrailer = nonBlank.count { ircTrailerRegex.containsMatchIn(it) }
    if (withTrailer < maxOf(3, nonBlank.size / 2)) return null
    return nonBlank.mapNotNull { line ->
        val noCodes = ircCodeRegex.replace(ircColorRegex.replace(line, ""), "")
        val body = ircTrailerRegex.replace(noCodes, "").trim()
        body.ifBlank { null }
    }
}

/** Quote of the Day: deterministic per-date pick + daily reminder toggle. */
class QotdViewModel(private val manager: DatabaseManager) : ViewModel() {

    /**
     * One-shot bypass of the offensive-word filter for today's card, set by
     * the "Show one anyway" button. Muted nicks and "Never show again"
     * still apply. Reset whenever the filter configuration changes.
     */
    private val _bypassWordFilter = kotlinx.coroutines.flow.MutableStateFlow(false)
    val bypassWordFilter: StateFlow<Boolean> = _bypassWordFilter.asStateFlow()

    /** Whether the content filter is on (drives the card's empty-state copy). */
    val contentFilterEnabled: StateFlow<Boolean> = manager.contentFilterEnabled

    /** Show today's quote despite the word filter. */
    fun showDespiteFilter() {
        _bypassWordFilter.value = true
    }

    /**
     * The deterministic Quote of the Day, honoring the random-source
     * setting: the same database all day, rotating daily — matching the
     * notification worker's pick. Databases with no quotes are skipped so
     * the rotation never lands on an empty pool.
     */
    val quote: StateFlow<SourcedQuote?> = combine(
        manager.randomSourceIds,
        manager.availableDatabases(),
        manager.contentFilterTick,
        _bypassWordFilter
    ) { selected, dbs, _, bypass ->
        val all = dbs.map { it.id }
        // No explicit sources chosen: the daily quote draws from every database.
        val ids = (selected?.toList() ?: listOf(manager.defaultDbId()))
            .filter { it in all }.ifEmpty { all }.sorted()
        ids to bypass
    }.map { (ids, bypass) ->
        val nonEmpty = manager.qotdNonEmptyDbIds(ids)
        if (nonEmpty.isEmpty()) {
            null
        } else {
            val today = java.time.LocalDate.now()
            val id = nonEmpty[Math.floorMod(today.toEpochDay(), nonEmpty.size.toLong()).toInt()]
            manager.repoFor(id)
                .visibleQuoteOfTheDay(today) { q ->
                    // v1.65: the word-filter bypass never bypasses the
                    // global maturity ceiling — that's the safety gate.
                    if (!manager.isMaturityAllowed(q)) true
                    else if (bypass) manager.isQuoteHiddenOrMuted(id, q.number, q.text)
                    else manager.isQuoteHiddenFromLists(id, q.number, q.text)
                }
                ?.let { SourcedQuote(id, it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationsEnabled: StateFlow<Boolean> = manager.qotdNotificationsEnabled

    fun setNotificationsEnabled(enabled: Boolean) {
        manager.setQotdNotificationsEnabled(enabled)
    }

    /** QotD delivery time (hour/minute). Changing it reschedules the alarm. */
    val qotdHour: StateFlow<Int> = manager.qotdHour
    val qotdMinute: StateFlow<Int> = manager.qotdMinute

    fun setQotdTime(hour: Int, minute: Int) {
        manager.setQotdTime(hour, minute)
    }

    /** Whether the Browse QotD card is collapsed to a slim row. */
    val cardMinimized: StateFlow<Boolean> = manager.qotdMinimized

    fun setCardMinimized(minimized: Boolean) {
        manager.setQotdMinimized(minimized)
    }

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)

    /**
     * Past daily picks (newest first), computed with the same deterministic
     * rotation as today's quote and the notification worker.
     */
    private val _history =
        kotlinx.coroutines.flow.MutableStateFlow<List<QotdHistoryEntry>>(emptyList())
    val history: StateFlow<List<QotdHistoryEntry>> = _history

    init {
        refreshHistory()
        viewModelScope.launch {
            // Any filter change drops the one-shot word-filter bypass.
            manager.contentFilterTick.collect { _bypassWordFilter.value = false }
        }
    }

    /** Recomputes the history, e.g. after the random-source setting changes. */
    fun refreshHistory(days: Int = 30) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val selected = manager.randomSourceIds.value
            val dbs = try {
                manager.availableDatabases().first()
            } catch (e: Exception) {
                emptyList()
            }
            val all = dbs.map { it.id }
            val labels = dbs.associate { it.id to it.label }
            val today = java.time.LocalDate.now()
            _history.value = (1..days).mapNotNull { back ->
                val date = today.minusDays(back.toLong())
                // Same candidate pool as today's card: skip empty databases.
                val ids = manager.qotdNonEmptyDbIds(
                    (selected?.toList() ?: listOf(manager.defaultDbId()))
                        .filter { it in all }.ifEmpty { all }.sorted()
                )
                if (ids.isEmpty()) return@mapNotNull null
                val id = ids[Math.floorMod(date.toEpochDay(), ids.size.toLong()).toInt()]
                manager.repoFor(id)
                    .visibleQuoteOfTheDay(date) { q ->
                        // v1.65: the global maturity ceiling applies to past
                        // daily picks as well.
                        manager.isQuoteHiddenFromLists(id, q.number, q.text) ||
                            !manager.isMaturityAllowed(q)
                    }
                    ?.let { quote ->
                        QotdHistoryEntry(date, id, labels[id] ?: id, quote)
                    }
            }
        }
    }
}

/** One past Quote of the Day: its date, source database, and the quote. */
data class QotdHistoryEntry(
    val date: java.time.LocalDate,
    val dbId: String,
    val dbLabel: String,
    val quote: com.quotebrowser.app.data.Quote
)

enum class TopBottomMode { TOP, BOTTOM }

class TopBottomViewModel(
    manager: DatabaseManager,
    val mode: TopBottomMode
) : QuoteListViewModel(manager) {

    companion object {
        const val PAGE_SIZE = 100
    }

    private val _quotes = kotlinx.coroutines.flow.MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes

    /** Zero-based page: 0 = ranks 1–100, 1 = ranks 101–200, … */
    var page by mutableStateOf(0)
        private set

    /** Category filter: empty = all quotes. Resets to page 0 on change. */
    var categoryIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    /** True when a next page actually exists (we fetch one extra row to know). */
    private val _hasMore = kotlinx.coroutines.flow.MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    /**
     * Per-screen temporary content-filter bypass ("Show" on the hidden
     * banner). In-memory only; the screen resets it when leaving.
     */
    private val _showFiltered = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showFiltered: StateFlow<Boolean> = _showFiltered.asStateFlow()

    /** How many fetched rows the content filter removed; 0 when off/bypassed. */
    private val _hiddenCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    fun setShowFiltered(show: Boolean) {
        if (_showFiltered.value == show) return
        _showFiltered.value = show
        viewModelScope.launch {
            page = 0
            loadPage()
        }
    }

    init {
        viewModelScope.launch {
            manager.currentDbId.collect {
                page = 0
                loadPage()
            }
        }
        viewModelScope.launch {
            // Reload the page when the content filter or word list changes.
            manager.contentFilterTick.collect {
                page = 0
                loadPage()
            }
        }
    }

    private val _allDbQuotes =
        kotlinx.coroutines.flow.MutableStateFlow<List<SourcedQuote>>(emptyList())
    /**
     * Merged Top/Bottom page for "All databases" mode: the top (or bottom)
     * quotes of every database merged by rating, as (database, quote)
     * pairs so each card can show its source chip. Empty in single-DB mode.
     */
    val allDbQuotes: StateFlow<List<SourcedQuote>> = _allDbQuotes

    /**
     * "All databases" page: pulls the top (or bottom) candidates from every
     * database, merges them by rating, and slices the requested page.
     * Categories are per-database, so the category filter does not apply
     * here (matching the Browse all-databases view).
     */
    private suspend fun loadAllDbPage() {
        val offset = page * PAGE_SIZE
        val bypass = _showFiltered.value
        val filterWords = manager.contentFilterEnabled.value && !bypass
        val hasUserHidden = manager.hiddenQuoteKeys.value.isNotEmpty()
        val filtering = filterWords || hasUserHidden
        val fetchSize = if (filtering) PAGE_SIZE * 3 + 1 else PAGE_SIZE + 1
        // Over-fetch per database so the merged page stays full after the
        // content filter runs.
        val perDb = (page + 1) * fetchSize
        val merged = mutableListOf<SourcedQuote>()
        for (id in manager.allDbIds()) {
            val repo = try {
                manager.repoFor(id)
            } catch (e: Exception) {
                continue
            }
            val rows = if (mode == TopBottomMode.TOP) repo.topRated(perDb, 0)
            else repo.bottomRated(perDb, 0)
            rows.forEach { merged.add(SourcedQuote(repo.dbId, it)) }
        }
        val sorted = if (mode == TopBottomMode.TOP) {
            merged.sortedWith(
                compareByDescending<SourcedQuote> { it.quote.rating == null }
                    .thenByDescending { it.quote.rating }
            )
        } else {
            merged.sortedWith(
                compareByDescending<SourcedQuote> { it.quote.rating == null }
                    .thenBy { it.quote.rating }
            )
        }
        var wordHidden = 0
        val visible = sorted.filter { sq ->
            if (manager.isQuoteHiddenOrMuted(sq.dbId, sq.quote.number, sq.quote.text)) return@filter false
            val hidden = filterWords && manager.isHiddenByContentFilter(sq.quote.text)
            if (hidden) wordHidden++
            !hidden
        }
        _hiddenCount.value = wordHidden
        _hasMore.value = visible.size > offset + PAGE_SIZE
        _allDbQuotes.value = visible.drop(offset).take(PAGE_SIZE)
    }

    private suspend fun loadPage() {
        if (manager.currentDbId.value == DatabaseManager.ALL_DBS_ID) {
            loadAllDbPage()
            return
        }
        val repo = manager.repoFor(manager.currentDbId.value)
        val offset = page * PAGE_SIZE
        val bypass = _showFiltered.value
        // "Never show again" always applies; "Show anyway" bypasses only
        // the offensive-word filter.
        val filterWords = manager.contentFilterEnabled.value && !bypass
        val hasUserHidden = manager.hiddenQuoteKeys.value.isNotEmpty()
        // Over-fetch when the content filter is on: filtering shrinks the
        // page, so pull extra rows to keep pages reasonably full.
        val filtering = filterWords || hasUserHidden
        val fetchSize = if (filtering) PAGE_SIZE * 3 + 1 else PAGE_SIZE + 1
        val rows =
            if (mode == TopBottomMode.TOP) repo.topRated(fetchSize, offset, categoryIds)
            else repo.bottomRated(fetchSize, offset, categoryIds)
        var wordHidden = 0
        val visible = rows.filter { q ->
            if (manager.isQuoteHiddenOrMuted(repo.dbId, q.number, q.text)) return@filter false
            val hidden = filterWords && manager.isHiddenByContentFilter(q.text)
            if (hidden) wordHidden++
            !hidden
        }
        _hiddenCount.value = wordHidden
        _hasMore.value = rows.size >= fetchSize || visible.size > PAGE_SIZE
        _quotes.value = visible.take(PAGE_SIZE)
    }

    fun nextPage() {
        viewModelScope.launch {
            page += 1
            loadPage()
        }
    }

    fun prevPage() {
        if (page == 0) return
        viewModelScope.launch {
            page -= 1
            loadPage()
        }
    }

    fun updateCategories(ids: Set<Long>) {
        categoryIds = ids
        viewModelScope.launch {
            page = 0
            loadPage()
        }
    }
}

/**
 * v1.63: the read-aloud queue engine now lives in the application-scoped
 * [PlaylistPlayer] (shared with Android Auto); this ViewModel is a thin
 * delegate exposing the same API to the phone UI.
 * (PlaylistItem moved to PlaylistPlayer.kt, same package.)
 */
class PlaylistViewModel(private val manager: DatabaseManager) : ViewModel() {
    private val player: com.quotebrowser.app.ui.PlaylistPlayer
        get() = (manager.context.applicationContext as com.quotebrowser.app.QuoteApp).playlistPlayer

    val items: StateFlow<List<com.quotebrowser.app.ui.PlaylistItem>> = player.items
    val index: StateFlow<Int> = player.index
    val playing: StateFlow<Boolean> = player.playing
    val radioMode: StateFlow<Boolean> = player.radioMode
    val radioDbId: StateFlow<String?> = player.radioDbId
    val sleepMinutes: StateFlow<Int?> = player.sleepMinutes
    val sleepEndsAt: StateFlow<Long> = player.sleepEndsAt
    val sleepAfterCurrent: StateFlow<Boolean> = player.sleepAfterCurrent

    fun startRadio(dbId: String) = player.startRadio(dbId)
    fun pausePlayback() = player.pausePlayback()
    fun resumePlayback() = player.resumePlayback()
    fun start(quotes: List<SourcedQuote>) = player.start(quotes)
    fun append(quotes: List<SourcedQuote>) = player.append(quotes)
    fun skip() = player.skip()
    fun stopPlayback() = player.stopPlayback()
    fun setSleepTimer(minutes: Int?) = player.setSleepTimer(minutes)
    fun setSleepAfterCurrent(enabled: Boolean) = player.setSleepAfterCurrent(enabled)
    fun clearSleepTimer() = player.clearSleepTimer()
}

/** Per-database reading progress for the stats screen. */
data class DbReadingStat(
    val dbId: String,
    val label: String,
    val seen: Int,
    val total: Int
)

/** One row of the most-quoted-nicks leaderboard. */
data class NickEntry(val nick: String, val count: Int)

/** Deep-dive stats for one nick. */
data class NickDetail(
    val nick: String,
    val quoteCount: Int,
    val avgRating: Double?,
    val ratedCount: Int,
    /** Newest first, capped for display. */
    val quotes: List<com.quotebrowser.app.data.SourcedQuote>,
    /** Database label -> quote count, most first. */
    val dbCounts: List<Pair<String, Int>>
)

/**
 * Nick deep-dive: per-person quote stats and the most-quoted leaderboard,
 * computed over all databases on IO.
 */
class NickViewModel(private val manager: DatabaseManager) : ViewModel() {

    /** Most-quoted nicks across all databases (top 50), computed once. */
    val leaderboard: StateFlow<List<NickEntry>> =
        kotlinx.coroutines.flow.flow { emit(computeLeaderboard()) }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
            .stateIn(
                viewModelScope,
                kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /** Full detail for one nick (case-insensitive). */
    suspend fun detailFor(nick: String): NickDetail =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val quotes = mutableListOf<com.quotebrowser.app.data.SourcedQuote>()
            val dbCounts = mutableListOf<Pair<String, Int>>()
            for (id in manager.allDbIds()) {
                val repo = manager.repoFor(id)
                val hits = repo.getAllQuotes().filter { q ->
                    com.quotebrowser.app.ui.extractNicks(q.text)
                        .any { it.equals(nick, ignoreCase = true) }
                }
                if (hits.isNotEmpty()) {
                    dbCounts += manager.shortLabel(id) to hits.size
                    hits.forEach { quotes += com.quotebrowser.app.data.SourcedQuote(id, it) }
                }
            }
            val ratings = quotes.mapNotNull { it.quote.rating }
            NickDetail(
                nick = nick,
                quoteCount = quotes.size,
                avgRating = ratings.average().takeIf { ratings.isNotEmpty() },
                ratedCount = ratings.size,
                quotes = quotes.sortedByDescending { it.quote.number }.take(300),
                dbCounts = dbCounts.sortedByDescending { it.second }
            )
        }

    private suspend fun computeLeaderboard(): List<NickEntry> =
        manager.topNicks(50).map { (nick, count) -> NickEntry(nick, count) }

    /**
     * v1.48 nick interaction graph: who [nick] talks to most, as
     * shared-quote co-occurrence — every quote where [nick] and another
     * nick both appear (reusing [extractNicks], the same parser the
     * colorizer and this deep-dive use) increments that pair's count.
     * Top [limit] connections across all databases, self excluded,
     * case-insensitive. Runs on IO; the screen caches the result per
     * nick while open.
     */
    suspend fun topConnections(nick: String, limit: Int = 8): List<NickEntry> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val counts = mutableMapOf<String, Int>()
            val display = mutableMapOf<String, String>()
            val self = nick.lowercase()
            for (id in manager.allDbIds()) {
                val repo = manager.repoFor(id)
                for (q in repo.getAllQuotes()) {
                    val nicks = com.quotebrowser.app.ui.extractNicks(q.text)
                    if (nicks.none { it.equals(nick, ignoreCase = true) }) continue
                    for (other in nicks) {
                        val key = other.lowercase()
                        if (key == self) continue
                        counts[key] = (counts[key] ?: 0) + 1
                        display.getOrPut(key) { other }
                    }
                }
            }
            counts.entries
                .sortedByDescending { it.value }
                .take(limit)
                .map { NickEntry(display.getValue(it.key), it.value) }
        }
}

/** Reading stats: per-database progress, total seen, day streak. */
class StatsViewModel(private val manager: DatabaseManager) : ViewModel() {

    private val _stats =
        kotlinx.coroutines.flow.MutableStateFlow<List<DbReadingStat>>(emptyList())
    val stats: StateFlow<List<DbReadingStat>> = _stats

    private val _totalSeen = kotlinx.coroutines.flow.MutableStateFlow(0)
    val totalSeen: StateFlow<Int> = _totalSeen

    private val _streak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    /** Longest run of consecutive reading days ever. */
    private val _longestStreak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak

    private val _loading = kotlinx.coroutines.flow.MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    /**
     * Quotes read per local day ("YYYY-MM-DD" -> count), newest first —
     * feeds the reading heatmap.
     */
    private val _readLog = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Int>>(emptyMap())
    val readLog: StateFlow<Map<String, Int>> = _readLog

    /** Quotes read today (for the daily-goal progress ring). */
    val todayCount: StateFlow<Int> = _readLog
        .map { it[java.time.LocalDate.now().toString()] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Quotes read in the last 7 days (weekly-goal progress). */
    val weeklyCount: StateFlow<Int> = _readLog
        .map { log ->
            val today = java.time.LocalDate.now()
            (0 until 7).sumOf { back ->
                log[today.minusDays(back.toLong()).toString()] ?: 0
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Quotes read in the current calendar month (monthly-goal progress). */
    val monthlyCount: StateFlow<Int> = _readLog
        .map { log ->
            val prefix = java.time.LocalDate.now().toString().substring(0, 7)
            log.filterKeys { it.startsWith(prefix) }.values.sum()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Daily reading goal (0 = off). */
    val dailyGoal: StateFlow<Int> = manager.dailyGoal

    /** Weekly/monthly reading goals (0 = off) and their feature toggle. */
    val weeklyGoal: StateFlow<Int> = manager.weeklyGoal
    val monthlyGoal: StateFlow<Int> = manager.monthlyGoal
    val readingGoalsEnabled: StateFlow<Boolean> = manager.readingGoalsEnabled

    /**
     * Rating distribution across all databases: (bucket label, quote count).
     */
    private val _ratingDist =
        kotlinx.coroutines.flow.MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val ratingDistribution: StateFlow<List<Pair<String, Int>>> = _ratingDist

    /**
     * v1.64.2: top speakers (by dialogue turns) and top categories across
     * seen quotes, plus the total favorite count.
     */
    private val _topNicks =
        kotlinx.coroutines.flow.MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val topNicks: StateFlow<List<Pair<String, Int>>> = _topNicks
    private val _topCategories =
        kotlinx.coroutines.flow.MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val topCategories: StateFlow<List<Pair<String, Int>>> = _topCategories
    private val _favoritesCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount

    init {
        refresh()
    }

    /** Wipes all reading-stats data, then reloads. */
    fun reset() {
        viewModelScope.launch {
            manager.clearSeen()
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val ids = manager.allDbIds()
                _stats.value = ids.map { id ->
                    val seen = manager.seenCount(id).first()
                    val total = manager.quoteCount(id) ?: 0
                    DbReadingStat(id, manager.shortLabel(id), seen, total)
                }
                _totalSeen.value = _stats.value.sumOf { it.seen }
                _streak.value = manager.readingStreakDays()
                _longestStreak.value = manager.longestReadingStreakDays()
                _readLog.value = manager.readCountsByDay()
                _ratingDist.value = computeRatingDistribution()
                // v1.64.2: top speakers / categories / favorites.
                val (nicks, cats) = computeTopNicksAndCategories()
                _topNicks.value = nicks
                _topCategories.value = cats
                _favoritesCount.value = computeFavoritesCount()
            } finally {
                _loading.value = false
            }
        }
    }

    /** Buckets all rated quotes across databases into rating ranges. */
    private suspend fun computeRatingDistribution(): List<Pair<String, Int>> {
        val buckets = linkedMapOf(
            "≤ 0" to 0,
            "1–5" to 0,
            "6–10" to 0,
            "11–20" to 0,
            "21–50" to 0,
            "51+" to 0
        )
        for (id in manager.allDbIds()) {
            try {
                for (r in manager.repoFor(id).allRatings()) {
                    val key = when {
                        r <= 0 -> "≤ 0"
                        r <= 5 -> "1–5"
                        r <= 10 -> "6–10"
                        r <= 20 -> "11–20"
                        r <= 50 -> "21–50"
                        else -> "51+"
                    }
                    buckets[key] = (buckets[key] ?: 0) + 1
                }
            } catch (e: Exception) {
                // A database that can't be read contributes nothing.
            }
        }
        return buckets.toList()
    }

    /**
     * v1.64.2: top speakers (by dialogue turns) and top categories across
     * seen quotes. The seen set is unbounded, so quote loads are capped to
     * bound the work; counts are representative, not exact.
     */
    private suspend fun computeTopNicksAndCategories():
        Pair<List<Pair<String, Int>>, List<Pair<String, Int>>> {
        val nickCounts = mutableMapOf<String, Int>()
        val catCounts = mutableMapOf<String, Int>()
        var loaded = 0
        val breaks = manager.dialogueLineBreaks.value
        for (id in manager.allDbIds()) {
            if (loaded >= 600) break
            val repo = manager.repoFor(id)
            val numbers = try {
                manager.seenNumbers(id)
            } catch (e: Exception) {
                emptySet()
            }
            for (n in numbers) {
                if (loaded >= 600) break
                loaded++
                try {
                    for (c in repo.categories.forQuote(n)) {
                        catCounts[c.name] = (catCounts[c.name] ?: 0) + 1
                    }
                    val text = repo.getQuote(n)?.text ?: continue
                    for (turn in buildSpokenTurns(text, breaks)) {
                        val nick = turn.nick?.takeIf { it.isNotBlank() }
                            ?: continue
                        nickCounts[nick] = (nickCounts[nick] ?: 0) + 1
                    }
                } catch (e: Exception) {
                    // A quote that can't be read contributes nothing.
                }
            }
        }
        fun top(m: Map<String, Int>): List<Pair<String, Int>> =
            m.entries.sortedByDescending { it.value }.take(8)
                .map { it.key to it.value }
        return top(nickCounts) to top(catCounts)
    }

    /** v1.64.2: total favorites across all databases. */
    private suspend fun computeFavoritesCount(): Int {
        var total = 0
        for (id in manager.allDbIds()) {
            total += try {
                manager.repoFor(id).favorites().first().size
            } catch (e: Exception) {
                0
            }
        }
        return total
    }
}

/** One higher-or-lower round: two quotes from the same database. */
data class GameRound(val a: SourcedQuote, val b: SourcedQuote)

/**
 * Higher-or-lower game: two random quotes, tap the one you think is rated
 * higher. Honors the random-source setting and the content filter; only
 * quotes with a real rating are dealt.
 */
class GameViewModel(private val manager: DatabaseManager) : ViewModel() {

    private val _round = kotlinx.coroutines.flow.MutableStateFlow<GameRound?>(null)
    val round: StateFlow<GameRound?> = _round

    /** The quote the user tapped this round; null = not answered yet. */
    private val _picked = kotlinx.coroutines.flow.MutableStateFlow<SourcedQuote?>(null)
    val picked: StateFlow<SourcedQuote?> = _picked

    private val _score = kotlinx.coroutines.flow.MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _streak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    val bestStreak: StateFlow<Int> = manager.gameBestStreak

    private val _loading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        nextRound()
    }

    fun nextRound() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _loading.value = true
            try {
                _picked.value = null
                _round.value = drawRound()
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun drawRound(): GameRound? {
        val all = manager.allDbIds()
        val selected = manager.randomSourceIds.first()
        val ids = (if (selected == null) all else all.filter { it in selected })
            .ifEmpty { all }
        if (ids.isEmpty()) return null
        repeat(25) {
            val id = ids.random()
            val dealt = try {
                manager.repoFor(id).randomQuotes(12, maturities = listOf(manager.maturityCeiling.value))
                    .filter { q ->
                        q.rating != null &&
                            !manager.isQuoteHiddenFromLists(id, q.number, q.text)
                    }
                    .shuffled()
            } catch (e: Exception) {
                emptyList()
            }
            if (dealt.size >= 2) {
                return GameRound(SourcedQuote(id, dealt[0]), SourcedQuote(id, dealt[1]))
            }
        }
        return null
    }

    /** True when the tapped quote is rated at least as high as the other. */
    fun pick(which: SourcedQuote) {
        val r = _round.value ?: return
        if (_picked.value != null) return
        _picked.value = which
        val other = if (which == r.a) r.b else r.a
        val correct = (which.quote.rating ?: 0) >= (other.quote.rating ?: 0)
        if (correct) {
            _score.value += 1
            _streak.value += 1
            manager.setGameBestStreak(_streak.value)
        } else {
            _streak.value = 0
        }
    }

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)
}

/** One "Who said it?" round: a quote plus four nick options. */
data class WhoSaidRound(
    val quote: SourcedQuote,
    val options: List<String>,
    val answer: String
)

/**
 * "Who said it?" game: a random quote is shown, guess which nick said it
 * from four options. Distractors come from the most-quoted nicks.
 */
class WhoSaidViewModel(private val manager: DatabaseManager) : ViewModel() {

    private val _round = kotlinx.coroutines.flow.MutableStateFlow<WhoSaidRound?>(null)
    val round: kotlinx.coroutines.flow.StateFlow<WhoSaidRound?> = _round

    /** The nick the user tapped this round; null = not answered yet. */
    private val _picked = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val picked: kotlinx.coroutines.flow.StateFlow<String?> = _picked

    private val _score = kotlinx.coroutines.flow.MutableStateFlow(0)
    val score: kotlinx.coroutines.flow.StateFlow<Int> = _score

    private val _streak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val streak: kotlinx.coroutines.flow.StateFlow<Int> = _streak

    val bestStreak: kotlinx.coroutines.flow.StateFlow<Int> = manager.whoSaidBestStreak

    private val _loading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loading: kotlinx.coroutines.flow.StateFlow<Boolean> = _loading

    init {
        nextRound()
    }

    fun nextRound() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _loading.value = true
            try {
                _picked.value = null
                _round.value = drawRound()
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun drawRound(): WhoSaidRound? {
        val top = manager.topNicks(60).map { it.first }
        if (top.size < 4) return null
        val ids = manager.allDbIds()
        if (ids.isEmpty()) return null
        repeat(30) {
            val id = ids.random()
            val quotes = try {
                manager.repoFor(id).randomQuotes(20, maturities = listOf(manager.maturityCeiling.value))
            } catch (e: Exception) {
                emptyList()
            }
            val pick = quotes.firstOrNull { q ->
                extractNicks(q.text).isNotEmpty() &&
                    !manager.isQuoteHiddenFromLists(id, q.number, q.text)
            } ?: return@repeat
            val answer = extractNicks(pick.text).first()
            val distractors = top
                .filter { t -> !t.equals(answer, ignoreCase = true) }
                .shuffled()
                .take(3)
            if (distractors.size < 3) return@repeat
            return WhoSaidRound(
                quote = SourcedQuote(id, pick),
                options = (distractors + answer).shuffled(),
                answer = answer
            )
        }
        return null
    }

    /** True when the tapped nick matches the quote's speaker. */
    fun pick(nick: String) {
        val r = _round.value ?: return
        if (_picked.value != null) return
        _picked.value = nick
        if (nick.equals(r.answer, ignoreCase = true)) {
            _score.value += 1
            _streak.value += 1
            manager.setWhoSaidBestStreak(_streak.value)
        } else {
            _streak.value = 0
        }
    }

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)
}

/** One "Guess the rating" round: a quote and its rating bucket. */
data class RatingGuessRound(
    val quote: SourcedQuote,
    val answerIndex: Int
)

/**
 * "Guess the rating" game: a random rated quote is shown, pick which
 * bucket its rating falls into. Honors the random-source setting and the
 * content filter.
 */
class RatingGuessViewModel(private val manager: DatabaseManager) : ViewModel() {

    companion object {
        val buckets = listOf("Below 0", "0 \u2013 99", "100 \u2013 999", "1,000+")

        fun bucketFor(rating: Int): Int = when {
            rating < 0 -> 0
            rating < 100 -> 1
            rating < 1000 -> 2
            else -> 3
        }
    }

    private val _round = kotlinx.coroutines.flow.MutableStateFlow<RatingGuessRound?>(null)
    val round: kotlinx.coroutines.flow.StateFlow<RatingGuessRound?> = _round

    /** The bucket index the user tapped; null = not answered yet. */
    private val _picked = kotlinx.coroutines.flow.MutableStateFlow<Int?>(null)
    val picked: kotlinx.coroutines.flow.StateFlow<Int?> = _picked

    private val _score = kotlinx.coroutines.flow.MutableStateFlow(0)
    val score: kotlinx.coroutines.flow.StateFlow<Int> = _score

    private val _streak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val streak: kotlinx.coroutines.flow.StateFlow<Int> = _streak

    val bestStreak: kotlinx.coroutines.flow.StateFlow<Int> = manager.ratingGuessBestStreak

    private val _loading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loading: kotlinx.coroutines.flow.StateFlow<Boolean> = _loading

    init {
        nextRound()
    }

    fun nextRound() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _loading.value = true
            try {
                _picked.value = null
                _round.value = drawRound()
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun drawRound(): RatingGuessRound? {
        val all = manager.allDbIds()
        val selected = manager.randomSourceIds.first()
        val ids = (if (selected == null) all else all.filter { it in selected })
            .ifEmpty { all }
        if (ids.isEmpty()) return null
        repeat(25) {
            val id = ids.random()
            val dealt = try {
                manager.repoFor(id).randomQuotes(12, maturities = listOf(manager.maturityCeiling.value))
                    .filter { q ->
                        q.rating != null &&
                            !manager.isQuoteHiddenFromLists(id, q.number, q.text)
                    }
            } catch (e: Exception) {
                emptyList()
            }
            val pick = dealt.randomOrNull() ?: return@repeat
            return RatingGuessRound(
                quote = SourcedQuote(id, pick),
                answerIndex = bucketFor(pick.rating ?: 0)
            )
        }
        return null
    }

    fun pick(index: Int) {
        val r = _round.value ?: return
        if (_picked.value != null) return
        _picked.value = index
        if (index == r.answerIndex) {
            _score.value += 1
            _streak.value += 1
            manager.setRatingGuessBestStreak(_streak.value)
        } else {
            _streak.value = 0
        }
    }

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)
}

/** One personal note with its quote, for the Notes screen. */
data class NoteEntry(
    val dbId: String,
    val number: Int,
    val dbLabel: String,
    val note: String,
    val quoteText: String?
)

/** All personal notes across databases, newest writes last. */
class NotesViewModel(private val manager: DatabaseManager) : ViewModel() {

    val entries: StateFlow<List<NoteEntry>> = manager.notes
        .mapLatest { map ->
            map.mapNotNull { (key, note) ->
                val dbId = key.substringBeforeLast("/")
                val number = key.substringAfterLast("/").toIntOrNull()
                    ?: return@mapNotNull null
                val text = try {
                    manager.repoFor(dbId).getQuote(number)?.text
                } catch (e: Exception) {
                    null
                }
                NoteEntry(dbId, number, manager.shortLabel(dbId), note, text)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteNote(dbId: String, number: Int) {
        manager.setNote(dbId, number, "")
    }
}

/**
 * Flashcard mode: a pool of random quotes (honoring the Random screen's
 * sources/categories plus the content filter); each card shows only the
 * quote number until tapped, then reveals the full quote. "Memorized"
 * removes a card from the pool (persisted until reset).
 */
class FlashcardViewModel(private val manager: DatabaseManager) : ViewModel() {

    private val _pool = kotlinx.coroutines.flow.MutableStateFlow<List<SourcedQuote>>(emptyList())
    val poolSize: StateFlow<Int> = _pool
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _index = kotlinx.coroutines.flow.MutableStateFlow(0)
    val index: StateFlow<Int> = _index

    private val _revealed = kotlinx.coroutines.flow.MutableStateFlow(false)
    val revealed: StateFlow<Boolean> = _revealed

    private val _loading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    val current: StateFlow<SourcedQuote?> =
        kotlinx.coroutines.flow.combine(_pool, _index) { pool, i -> pool.getOrNull(i) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val memorizedCount: StateFlow<Int> = manager.srsCards
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _empty = kotlinx.coroutines.flow.MutableStateFlow(false)
    val empty: StateFlow<Boolean> = _empty

    /**
     * Loads the pool. [fetchBatch] comes from the Random screen so sources,
     * categories, and the content filter are honored the same way. Cards
     * whose SRS due date hasn't arrived yet are excluded.
     */
    fun loadPool(fetchBatch: suspend () -> List<SourcedQuote>) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val now = System.currentTimeMillis()
                val srs = manager.srsCards.first()
                val pool = fetchBatch()
                    .filter { sq ->
                        val c = srs["${sq.dbId}/${sq.quote.number}"]
                        c == null || c.dueAt <= now
                    }
                    .shuffled()
                _deckSource.value = DeckSource.RANDOM
                _deckCategoryId.value = null
                _pool.value = pool
                _index.value = 0
                _revealed.value = false
                _empty.value = pool.isEmpty()
                resetSessionStats(pool.size)
            } finally {
                _loading.value = false
            }
        }
    }

    fun reveal() {
        _revealed.value = true
    }

    // ------------------------------------------------------------------
    // v1.53 memorize mode: deck sources (Favorites / Category), Got it /
    // Missed it self-scoring, and per-session statistics.
    // ------------------------------------------------------------------

    /** Where the memorize deck comes from. */
    enum class DeckSource { RANDOM, FAVORITES, CATEGORY }

    private val _deckSource =
        kotlinx.coroutines.flow.MutableStateFlow(DeckSource.RANDOM)
    val deckSource: StateFlow<DeckSource> = _deckSource

    private val _deckCategoryId =
        kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    val deckCategoryId: StateFlow<Long?> = _deckCategoryId

    private val _sessionGot = kotlinx.coroutines.flow.MutableStateFlow(0)
    val sessionGot: StateFlow<Int> = _sessionGot
    private val _sessionMissed = kotlinx.coroutines.flow.MutableStateFlow(0)
    val sessionMissed: StateFlow<Int> = _sessionMissed
    private val _sessionTotal = kotlinx.coroutines.flow.MutableStateFlow(0)
    val sessionTotal: StateFlow<Int> = _sessionTotal

    private fun resetSessionStats(poolSize: Int) {
        _sessionGot.value = 0
        _sessionMissed.value = 0
        _sessionTotal.value = poolSize
    }

    /** Applies the SRS due-date filter shared by every deck loader. */
    private suspend fun applySrsFilter(pool: List<SourcedQuote>): List<SourcedQuote> {
        val now = System.currentTimeMillis()
        val srs = manager.srsCards.first()
        return pool.filter { sq ->
            val c = srs["${sq.dbId}/${sq.quote.number}"]
            c == null || c.dueAt <= now
        }.shuffled()
    }

    /** Deck from favorites across all databases. */
    fun loadFavoritesPool() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val all = mutableListOf<SourcedQuote>()
                for (id in manager.allDbIds()) {
                    try {
                        val favs = manager.repoFor(id).favorites().first()
                        favs.forEach { q -> all.add(SourcedQuote(id, q)) }
                    } catch (e: Exception) {
                        // A failing database never aborts the sweep.
                    }
                }
                val pool = applySrsFilter(all)
                _deckSource.value = DeckSource.FAVORITES
                _deckCategoryId.value = null
                _pool.value = pool
                _index.value = 0
                _revealed.value = false
                _empty.value = pool.isEmpty()
                resetSessionStats(pool.size)
            } finally {
                _loading.value = false
            }
        }
    }

    /** Deck from one category (current database's category store). */
    fun loadCategoryPool(categoryId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val dbId = manager.currentDbId.value
                val quotes = try {
                    manager.repoFor(dbId).quotesInCategory(categoryId)
                } catch (e: Exception) {
                    emptyList()
                }
                val pool = applySrsFilter(quotes.map { SourcedQuote(dbId, it) })
                _deckSource.value = DeckSource.CATEGORY
                _deckCategoryId.value = categoryId
                _pool.value = pool
                _index.value = 0
                _revealed.value = false
                _empty.value = pool.isEmpty()
                resetSessionStats(pool.size)
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Got it / Missed it self-scoring: maps onto the SM-2 grades
     * (Got it = Easy, Missed it = Again) and tracks session statistics.
     */
    fun gradeSimple(gotIt: Boolean) {
        if (gotIt) {
            _sessionGot.value = _sessionGot.value + 1
            grade(5)
        } else {
            _sessionMissed.value = _sessionMissed.value + 1
            grade(0)
        }
    }

    fun next() {
        val pool = _pool.value
        if (pool.isEmpty()) return
        _revealed.value = false
        _index.value = (_index.value + 1) % pool.size
    }

    fun reshuffle() {
        _pool.value = _pool.value.shuffled()
        _index.value = 0
        _revealed.value = false
    }

    /**
     * Grades the current card with SM-2 (0 = Again, 3 = Hard, 4 = Good,
     * 5 = Easy), rescheduling it; removes it from the current pool.
     */
    fun grade(quality: Int) {
        val sq = current.value ?: return
        manager.gradeFlashcard(sq.dbId, sq.quote.number, quality)
        val pool = _pool.value - sq
        _pool.value = pool
        if (_index.value >= pool.size) _index.value = 0
        _revealed.value = false
        _empty.value = pool.isEmpty()
    }

    /** Legacy shim kept for callers: old "Memorized" = graduated (Easy). */
    fun markMemorized() = grade(5)

    fun resetMemorized(fetchBatch: suspend () -> List<SourcedQuote>) {
        manager.clearMemorizedFlashcards()
        loadPool(fetchBatch)
    }

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)
}
