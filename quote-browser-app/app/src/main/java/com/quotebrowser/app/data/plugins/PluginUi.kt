package com.quotebrowser.app.data.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * v1.83: (theme id, display name) pairs for every enabled theme-pack
 * theme, for the theme pickers (global Themes screen, per-database theme
 * picker, quick theme dialog). Returned as State so pickers re-emit when
 * packs are installed, uninstalled, or toggled.
 */
@Composable
fun rememberPluginThemeOptions(): State<List<Pair<String, String>>> {
    val plugins by PluginManager.plugins.collectAsStateWithLifecycle()
    return remember(plugins) {
        mutableStateOf(
            plugins
                .filter {
                    it.error == null && it.enabled &&
                        it.manifest.type == PluginManager.TYPE_THEME_PACK
                }
                .flatMap { info ->
                    info.themes.map { t -> t.themeId(info.manifest.id) to t.name }
                }
        )
    }
}

/**
 * v1.87: (style id, display name) pairs for every enabled export-style
 * pack style, for the HTML export picker in ExportScreen. Re-emits when
 * packs change.
 */
@Composable
fun rememberPluginExportStyleOptions(): State<List<Pair<String, String>>> {
    val plugins by PluginManager.plugins.collectAsStateWithLifecycle()
    return remember(plugins) {
        mutableStateOf(PluginManager.exportStyleOptions())
    }
}

/**
 * v1.87: (voice id, display name) pairs for every enabled voice-pack
 * voice on [engine] ("gemini" or "builtin"), for the voice pickers.
 * Re-emits when packs change.
 */
@Composable
fun rememberPluginVoiceOptions(engine: String): State<List<Pair<String, String>>> {
    val plugins by PluginManager.plugins.collectAsStateWithLifecycle()
    return remember(plugins) {
        mutableStateOf(PluginManager.pluginVoiceOptions(engine))
    }
}
