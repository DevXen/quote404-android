package com.quotebrowser.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.quotebrowser.app.ui.AppNav
import com.quotebrowser.app.ui.warmVoiceRecordings
import com.quotebrowser.app.ui.StartupDestinations
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.LocalThemeNickPaletteStore
import com.quotebrowser.app.ui.ThemeNickPaletteStore
import com.quotebrowser.app.ui.LocalQuoteClean
import com.quotebrowser.app.ui.QuoteCleanOptions
import com.quotebrowser.app.ui.DisplayTransforms
import com.quotebrowser.app.ui.TransformMode
import com.quotebrowser.app.ui.LocalDisplayTransforms
import com.quotebrowser.app.ui.LocalKidSafeEnabled
import com.quotebrowser.app.ui.LocalQuoteFontFamily
import com.quotebrowser.app.ui.resolveQuoteFontFamily
import com.quotebrowser.app.ui.NickPalette
import com.quotebrowser.app.ui.components.LocalDbThemeSnapshots
import com.quotebrowser.app.ui.components.LocalDbCardsOnly
import com.quotebrowser.app.ui.components.LocalQueuedTagsEnabled
import com.quotebrowser.app.ui.components.LocalVoteControlsEnabled
import com.quotebrowser.app.data.DbThemeSnapshots
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.ui.components.LocalDbTagColors
import com.quotebrowser.app.ui.theme.LocalCompactMode
import com.quotebrowser.app.ui.theme.LocalDevXenBgMode
import com.quotebrowser.app.ui.theme.LocalQuote404Theme
import com.quotebrowser.app.ui.theme.LocalDevXenCustomBg
import com.quotebrowser.app.ui.theme.DevXenBgMode
import com.quotebrowser.app.ui.theme.LocalTextScale
import com.quotebrowser.app.ui.theme.QuoteBrowserTheme
import com.quotebrowser.app.ui.theme.resolveNickColors

class MainActivity : ComponentActivity() {

    companion object {
        /** Opens a specific quote (by number) in the detail screen. */
        const val ACTION_OPEN_QUOTE = "com.quotebrowser.app.OPEN_QUOTE"
        const val EXTRA_QUOTE_NUMBER = "com.quotebrowser.app.EXTRA_QUOTE_NUMBER"

        /** Database id the quote belongs to (QotD notification / widget). */
        const val EXTRA_DB_ID = "com.quotebrowser.app.EXTRA_DB_ID"

        /** Launcher-shortcut actions: open a specific tab. */
        const val ACTION_OPEN_RANDOM = "com.quotebrowser.app.OPEN_RANDOM"
        const val ACTION_OPEN_DECK = "com.quotebrowser.app.OPEN_DECK"
        const val ACTION_OPEN_SEARCH = "com.quotebrowser.app.OPEN_SEARCH"
        /** Launcher-shortcut / quiz-widget actions: open a games screen. */
        const val ACTION_OPEN_GAMES = "com.quotebrowser.app.OPEN_GAMES"
        const val ACTION_OPEN_WHO_SAID = "com.quotebrowser.app.OPEN_WHO_SAID"

        /** Pinned folder shortcut: open Favorites filtered to a folder. */
        const val ACTION_OPEN_FOLDER = "com.quotebrowser.app.OPEN_FOLDER"
        const val EXTRA_FOLDER = "com.quotebrowser.app.EXTRA_FOLDER"
    }

    private val deepLinkNumber = mutableStateOf<Int?>(null)
    private val deepLinkDbId = mutableStateOf<String?>(null)
    private val deepLinkTab = mutableStateOf<String?>(null)
    private val sharedText = mutableStateOf<String?>(null)
    private val deepLinkFolder = mutableStateOf<String?>(null)

    // v1.88.9: onResume is NOT a first-frame signal (v1.88.8 set
    // firstFrameDrawn here and the watchdog stood down while the app was
    // ANR'd on the splash). The real signal is the one-shot pre-draw
    // listener registered in onCreate below.
    override fun onResume() {
        super.onResume()
        StartupWatchdog.mark("MainActivity.onResume")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // v1.88.8: startup watchdog marker.
        StartupWatchdog.mark("MainActivity.onCreate")
        // v1.88.9: the ONLY true first-frame signal for the watchdog — a
        // one-shot pre-draw listener on the decor view. If the main thread
        // wedges anywhere before the first traversal (onCreate, onResume,
        // first composition), this never fires and the watchdog dumps.
        try {
            val decor = window.decorView
            decor.viewTreeObserver.addOnPreDrawListener(
                object : android.view.ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        try {
                            decor.viewTreeObserver.removeOnPreDrawListener(this)
                        } catch (_: Exception) {
                        }
                        StartupWatchdog.firstFrameDrawn = true
                        StartupWatchdog.mark("first frame pre-draw (decor)")
                        return true
                    }
                }
            )
            StartupWatchdog.mark("pre-draw listener registered")
        } catch (_: Exception) {
        }
        val app = application as QuoteApp
        // v1.88.2: trust the crash file, not just the in-memory flag. The flag
        // is sticky for the process lifetime, so after "Dismiss and retry"
        // deletes the report this activity could otherwise bounce straight
        // back to the crash viewer with nothing to show.
        if (app.startupCrashed) {
            if (crashFile(this).exists()) {
                startActivity(Intent(this, CrashActivity::class.java))
                finish()
                return
            }
            // v1.88.2: stale-flag race — "Dismiss and retry" deleted the
            // report and queued a relaunch in a fresh process, but this
            // activity landed in the old process first, where dbManager was
            // never initialized (touching it would crash). Die here; the
            // system recreates this activity in a fresh process, where
            // startupCrashed is false and init runs normally.
            finish()
            android.os.Process.killProcess(android.os.Process.myPid())
            kotlin.system.exitProcess(0)
        }
        deepLinkNumber.value = intent.quoteNumberOrNull()
        deepLinkDbId.value = intent?.getStringExtra(EXTRA_DB_ID)
        deepLinkTab.value = intent.shortcutTabOrNull()
        sharedText.value = intent.sharedTextOrNull()
        deepLinkFolder.value = intent.folderOrNull()
        // v1.61.20: warm the voice-recordings manifest so cards, search,
        // and settings see saved read-aloud audio immediately.
        warmVoiceRecordings(this)
        val manager = app.dbManager
        // Cold-start destination from the "startup_screen" pref. Default
        // "home" resolves to Routes.BROWSE, exactly today's launch behavior;
        // stale/unknown values fall back there too.
        val startupRoute = StartupDestinations.routeFor(manager.startupScreen.value)
        setContent {
            val themeId by manager.themeId.collectAsState(initial = "quote404")
            val customThemes by manager.customThemes.collectAsState(initial = emptyList())
            val textScale by manager.textScale.collectAsState(initial = 1f)
            val density by manager.density.collectAsState(initial = "comfortable")
            // DevXen theme background-art placement.
            val devXenBgMode by manager.devXenBgMode.collectAsState(
                initial = DevXenBgMode.BEHIND_QUOTES
            )
            // v1.61.3: the user's own DevXen background picture. Loaded on
            // a background thread into an ImageBitmap and provided app-wide
            // via LocalDevXenCustomBg; null = bundled DevXen artwork.
            val devXenCustomBgName by manager.devXenCustomBg.collectAsState(
                initial = ""
            )
            var devXenCustomBgBitmap by remember {
                mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
            }
            androidx.compose.runtime.LaunchedEffect(devXenCustomBgName) {
                devXenCustomBgBitmap =
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val file = manager.devXenCustomBgFile()
                            if (devXenCustomBgName.isBlank() || !file.exists()) null
                            else {
                                val bounds = android.graphics.BitmapFactory.Options()
                                    .apply { inJustDecodeBounds = true }
                                android.graphics.BitmapFactory.decodeFile(
                                    file.absolutePath, bounds
                                )
                                var sample = 1
                                while (bounds.outWidth / sample > 1600 ||
                                    bounds.outHeight / sample > 1600
                                ) sample *= 2
                                val opts = android.graphics.BitmapFactory.Options()
                                    .apply { inSampleSize = sample }
                                android.graphics.BitmapFactory.decodeFile(
                                    file.absolutePath, opts
                                )?.asImageBitmap()
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
            }
            val nickRules by manager.nickRules.collectAsState(initial = emptyList())
            val nickEnabled by manager.nickColoringEnabled.collectAsState(initial = true)
            val ircLineBreaks by manager.ircLineBreaks.collectAsState(initial = true)
            val dialogueLineBreaks by manager.dialogueLineBreaks.collectAsState(initial = true)
            val qcColorNames by manager.qcColorNames.collectAsState(initial = true)
            val qcColorActions by manager.qcColorActions.collectAsState(initial = true)
            val qcNormalizeBreaks by manager.qcNormalizeBreaks.collectAsState(initial = true)
            val qcShowTimestamps by manager.qcShowTimestamps.collectAsState(initial = false)
            // v1.86: per-category QuoteClean color controls.
            val qcColorMessage by manager.qcColorMessage.collectAsState(initial = true)
            val qcNameColor by manager.qcNameColor.collectAsState(initial = null)
            val qcMessageColor by manager.qcMessageColor.collectAsState(initial = null)
            val qcActionColor by manager.qcActionColor.collectAsState(initial = null)
            val qcTimestampColor by manager.qcTimestampColor.collectAsState(initial = null)
            val qcColorOther by manager.qcColorOther.collectAsState(initial = true)
            val qcOtherColor by manager.qcOtherColor.collectAsState(initial = null)
            val dbTagColors by manager.dbTagColors.collectAsState(initial = emptyMap())
            val quoteFontValue by manager.quoteFont.collectAsState(initial = "default")
            // v1.51 per-database themes: the root theme follows the current
            // database (palette only; site chrome is per-card). Master
            // override on, feature off, or All-databases mode keeps the
            // global theme.
            val currentDbId by manager.currentDbId.collectAsState(initial = DatabaseManager.ALL_DBS_ID)
            val perDbThemesOn by manager.perDbThemesEnabled.collectAsState(initial = true)
            val masterOverride by manager.masterThemeOverride.collectAsState(initial = false)
            val dbThemeConfigs by manager.dbThemeConfigs.collectAsState(initial = emptyMap())
            val builtInOverrides by manager.builtInThemeOverrides.collectAsState(initial = emptyMap())
            val classicLayoutOn by manager.classicLayoutOption.collectAsState(initial = true)
            // v1.89.5: themes carried inside database files.
            val dbBuiltInThemes by manager.dbBuiltInThemes.collectAsState(initial = emptyMap())
            val dbBuiltInCards by manager.dbBuiltInCards.collectAsState(initial = emptyMap())
            val transformModeName by manager.transformMode.collectAsState(initial = TransformMode.OFF.name)
            val voteControlsOn by manager.plusMinusRatingsEnabled.collectAsState(initial = true)
            val queuedTagsOn by manager.queuedTagsEnabled.collectAsState(initial = true)
            // v1.59: "database themes only on quote cards" (default OFF).
            // When ON, the app root keeps the global theme and site themes
            // style only the quote cards (DbPageBackground paints the
            // global page via LocalDbCardsOnly). Depends on the
            // DatabaseManager.dbThemesCardsOnly StateFlow.
            val dbThemesCardsOnly by manager.dbThemesCardsOnly.collectAsState(initial = false)
            // v1.53 Kid-safe mask (display-only, default OFF).
            val kidSafeOn by manager.kidSafeEnabled.collectAsState(initial = false)
            val dbThemeSnap = remember(
                themeId, perDbThemesOn, masterOverride, dbThemeConfigs,
                customThemes, builtInOverrides, classicLayoutOn,
                dbBuiltInThemes, dbBuiltInCards
            ) {
                DbThemeSnapshots(
                    globalThemeId = themeId,
                    perDbThemesEnabled = perDbThemesOn,
                    masterOverride = masterOverride,
                    configs = dbThemeConfigs,
                    customThemes = customThemes,
                    builtInOverrides = builtInOverrides,
                    classicLayoutEnabled = classicLayoutOn,
                    builtInDbThemes = dbBuiltInThemes,
                    builtInDbCards = dbBuiltInCards
                )
            }
            val resolvedRoot = remember(dbThemeSnap, currentDbId) {
                resolveDbTheme(dbThemeSnap, currentDbId)
            }
            // v1.59: cards-only mode keeps the user's global app theme on
            // the root (chrome); the per-card site themes still resolve
            // via LocalDbThemeSnapshots. Default OFF = current behavior.
            val effectiveThemeId =
                if (dbThemesCardsOnly) themeId else resolvedRoot.paletteThemeId
            val nickDefaults = run {
                // v1.63: the nick color theme (default: Quote404 logo
                // palette) decides the name/bracket colors; the dedicated
                // "color what they say" toggle (off by default) wins over
                // any theme-level flag. "app" = the pre-v1.63 behavior of
                // taking nick colors from the app theme.
                val themeId by manager.nickColorThemeId.collectAsState(
                    initial = com.quotebrowser.app.ui.NICK_THEME_DEFAULT
                )
                val customNickThemes by manager.customNickColorThemes.collectAsState(
                    initial = emptyList()
                )
                val colorMessage by manager.nickColorMessage.collectAsState(
                    initial = false
                )
                com.quotebrowser.app.ui.resolveNickThemePalette(
                    themeId = themeId,
                    customThemes = customNickThemes,
                    colorMessageWithSpeaker = colorMessage,
                    legacy = resolveNickColors(
                        effectiveThemeId, customThemes, builtInOverrides
                    )
                )
            }
            val quoteFontFamily =
                remember(quoteFontValue, applicationContext) {
                    resolveQuoteFontFamily(applicationContext, quoteFontValue)
                }
            CompositionLocalProvider(
                LocalTextScale provides textScale,
                LocalCompactMode provides (density == "compact"),
                LocalDbTagColors provides dbTagColors,
                LocalQuoteFontFamily provides quoteFontFamily,
                // v1.51: per-card per-database theme resolution (pure, no
                // I/O), display-only text transforms, and the vote/queued
                // feature gates — all read from this one snapshot.
                LocalDbThemeSnapshots provides dbThemeSnap,
                // v1.59: database-themes-only-on-cards gate, read by
                // DbPageBackground so the page layer paints the global
                // page instead of the per-database page color/image.
                LocalDbCardsOnly provides dbThemesCardsOnly,
                LocalDisplayTransforms provides DisplayTransforms(
                    mode = runCatching { TransformMode.valueOf(transformModeName) }
                        .getOrDefault(TransformMode.OFF)
                ),
                LocalVoteControlsEnabled provides voteControlsOn,
                LocalQueuedTagsEnabled provides queuedTagsOn,
                // v1.53 Kid-safe mask: display-only profanity masking.
                LocalKidSafeEnabled provides kidSafeOn,
                // v1.88.32: per-card-theme nick palette overrides for the
                // share picker — read/written without a ViewModel param.
                LocalThemeNickPaletteStore provides ThemeNickPaletteStore(
                    overrides = manager.themeNickPalettes,
                    customThemes = manager.customNickColorThemes,
                    colorMessageWithSpeaker = manager.nickColorMessage,
                    globalThemeId = manager.nickColorThemeId,
                    setOverride = manager::setThemeNickPalette,
                    nickStrip = manager.themeNickStrip,
                    setNickStrip = manager::setThemeNickStrip
                ),
                LocalNickPalette provides NickPalette(
                    enabled = nickEnabled,
                    brackets = nickDefaults.brackets,
                    names = nickDefaults.names,
                    bracketPalette = nickDefaults.bracketPalette,
                    action = nickDefaults.action,
                    actionOverridesName = nickDefaults.actionOverridesName,
                    colorMessageWithSpeaker = nickDefaults.colorMessageWithSpeaker,
                    rules = nickRules,
                    ircLineBreaks = ircLineBreaks,
                    dialogueLineBreaks = dialogueLineBreaks
                ),
                LocalQuoteClean provides QuoteCleanOptions(
                    colorNames = qcColorNames,
                    colorActions = qcColorActions,
                    normalizeBreaks = qcNormalizeBreaks,
                    showTimestamps = qcShowTimestamps,
                    colorMessage = qcColorMessage,
                    nameColorHex = qcNameColor,
                    messageColorHex = qcMessageColor,
                    actionColorHex = qcActionColor,
                    timestampColorHex = qcTimestampColor,
                    colorOther = qcColorOther,
                    otherColorHex = qcOtherColor
                ),
                // DevXen background-art mode: active only when DevXen is the
                // effective (root) theme. v1.56: gated on the GLOBAL theme
                // choice, not a per-database palette — picking DevXen as
                // one database's palette must not light up the bundled
                // artwork on every screen (the per-DB image picker is the
                // per-database image mechanism).
                LocalDevXenBgMode provides (
                    if (dbThemeSnap.globalThemeId == "devxen") devXenBgMode else null
                ),
                // v1.61.5: Clint's Quote404 artwork sits behind the lists
                // whenever Quote404 is the effective global theme.
                LocalQuote404Theme provides (dbThemeSnap.globalThemeId == "quote404"),
                // v1.61.3: the user's own DevXen background picture.
                LocalDevXenCustomBg provides devXenCustomBgBitmap
            ) {
                QuoteBrowserTheme(
                    themeId = effectiveThemeId,
                    customThemes = customThemes,
                    builtInOverrides = builtInOverrides
                ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(
                        manager = manager,
                        startDestination = startupRoute,
                        deepLinkNumber = deepLinkNumber.value,
                        deepLinkDbId = deepLinkDbId.value,
                        onDeepLinkConsumed = {
                            deepLinkNumber.value = null
                            deepLinkDbId.value = null
                        },
                        deepLinkTab = deepLinkTab.value,
                        onTabDeepLinkConsumed = { deepLinkTab.value = null },
                        sharedText = sharedText.value,
                        onSharedTextConsumed = { sharedText.value = null },
                        folderLink = deepLinkFolder.value,
                        onFolderLinkConsumed = { deepLinkFolder.value = null }
                    )
                }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkNumber.value = intent.quoteNumberOrNull()
        deepLinkDbId.value = intent.getStringExtra(EXTRA_DB_ID)
        deepLinkTab.value = intent.shortcutTabOrNull()
        sharedText.value = intent.sharedTextOrNull()
        deepLinkFolder.value = intent.folderOrNull()
    }

    private fun Intent?.quoteNumberOrNull(): Int? {
        val n = this?.getIntExtra(EXTRA_QUOTE_NUMBER, -1) ?: -1
        return n.takeIf { it >= 0 }
    }

    /** Maps a launcher-shortcut intent action to a tab key, if any. */
    private fun Intent?.shortcutTabOrNull(): String? = when (this?.action) {
        ACTION_OPEN_RANDOM -> "random"
        ACTION_OPEN_DECK -> "deck"
        ACTION_OPEN_SEARCH -> "search"
        ACTION_OPEN_GAMES -> "games"
        ACTION_OPEN_WHO_SAID -> "who_said"
        else -> null
    }

    /** Folder name from a pinned folder shortcut, if any. */
    private fun Intent?.folderOrNull(): String? =
        if (this?.action == ACTION_OPEN_FOLDER)
            this.getStringExtra(EXTRA_FOLDER)?.takeIf { it.isNotBlank() }
        else null

    /** Text shared from another app (ACTION_SEND), if any. */
    private fun Intent?.sharedTextOrNull(): String? =
        if (this?.action == Intent.ACTION_SEND && this.type?.startsWith("text/") == true)
            this.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }
        else null
}
