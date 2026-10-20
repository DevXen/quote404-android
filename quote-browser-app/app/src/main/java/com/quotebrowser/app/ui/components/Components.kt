package com.quotebrowser.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.data.TranslationRepository
import com.quotebrowser.app.data.layouts.LayoutStore
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.ui.layouts.LayoutCard
import com.quotebrowser.app.ui.layouts.LayoutCard
import com.quotebrowser.app.ui.LocalSiteShareCardsEnabled
import com.quotebrowser.app.ui.VoiceRecordings
import com.quotebrowser.app.ui.ExplanationCache
import com.quotebrowser.app.ui.ReadAlongDialog
import com.quotebrowser.app.data.pageBackgroundFor
import com.quotebrowser.app.data.pageBackgroundImageFor
import com.quotebrowser.app.data.quoteBackgroundForDb
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.data.siteStyleFor
import com.quotebrowser.app.ui.ExplainResult
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.LocalQuoteBackground
import com.quotebrowser.app.ui.explainQuote
import com.quotebrowser.app.ui.rhymeQuote
import com.quotebrowser.app.ui.roastQuote
import com.quotebrowser.app.ui.theme.resolveNickColors
import com.quotebrowser.app.ui.theme.themeColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.DatabaseThemeConfig
import com.quotebrowser.app.data.SiteLayout
import com.quotebrowser.app.data.siteThemeFor
import com.quotebrowser.app.ui.theme.CustomTheme
import com.quotebrowser.app.ui.theme.ThemeOption
import com.quotebrowser.app.ui.theme.ThemePresets
import com.quotebrowser.app.ui.theme.customThemeId
import com.quotebrowser.app.ui.theme.groupedDbThemeOptions
import com.quotebrowser.app.data.FlagInfo
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.copyQuote
import com.quotebrowser.app.ui.shareQuote
import com.quotebrowser.app.ui.renderAndShareSiteQuoteCard
import com.quotebrowser.app.ui.rememberNoveltyExportLocals
import com.quotebrowser.app.ui.PersonalRatingStars
import com.quotebrowser.app.ui.siteDisplayName
import com.quotebrowser.app.ui.openWebLink
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.QuoteCardStyleDialog
import com.quotebrowser.app.ui.ShareOptions
import com.quotebrowser.app.ui.hexToColorOrNull
import com.quotebrowser.app.ui.theme.contentOn
import com.quotebrowser.app.ui.theme.DevXenBgMode
import com.quotebrowser.app.ui.theme.DevXenDimmedBackground
import com.quotebrowser.app.ui.theme.LocalDevXenBgMode
import com.quotebrowser.app.ui.theme.LocalQuote404Theme
import com.quotebrowser.app.ui.quoteTextStyle
import com.quotebrowser.app.ui.theme.scaledTextStyle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.TextUnit
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DbThemeSnapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import com.quotebrowser.app.ui.theme.LocalCompactMode
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * The #quote-number as a pill: the theme's "Quote number" color as the
 * background, with black/white text chosen for contrast.
 */
@Composable
fun QuoteNumberBadge(
    number: Int,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall
) {
    val bg = MaterialTheme.colorScheme.tertiary
    val luminance = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f
    val content = if (luminance > 0.5f) Color.Black else Color.White
    Surface(
        color = bg,
        contentColor = content,
        shape = CircleShape
    ) {
        Text(
            text = "#$number",
            style = style,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}

@Composable
fun RatingBadge(rating: Int?, modifier: Modifier = Modifier) {
    val container: androidx.compose.ui.graphics.Color
    val content: androidx.compose.ui.graphics.Color
    if (rating == null) {
        // Unrated quotes stay neutral; every rated quote uses the themeable
        // rating background.
        container = MaterialTheme.colorScheme.surfaceContainerHighest
        content = MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        container = MaterialTheme.colorScheme.tertiaryContainer
        content = MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = CircleShape
    ) {
        Text(
            // v1.77: unrated quotes show 0, never a blank/"—".
            text = rating?.toString() ?: "0",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * v1.89.6: the rating display for a database that carries its own rating
 * format in its theme spec (standard card + generic detail header): the
 * formatted text ("I agree (123)") in the rating badge's pill instead of
 * the bare number. An empty format renders nothing — the database
 * declares no header rating label. When the database's vote style is
 * "plusminus" and a rate handler exists, "−" / "+" text vote links
 * flank the pill, wired to the same offline personal-rating handler
 * the site cards use; with no handler the text renders without the
 * glyphs rather than as dead buttons.
 */
@Composable
fun RowScope.CarriedRatingDisplay(
    format: String,
    rating: Int?,
    plusMinus: Boolean,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    if (format.isEmpty()) return
    val linkColor = MaterialTheme.colorScheme.primary
    if (plusMinus && onRate != null) {
        textVoteLink("−", -1, linkColor, myRating, onRate)
    }
    val container: Color
    val content: Color
    if (rating == null) {
        container = MaterialTheme.colorScheme.surfaceContainerHighest
        content = MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        container = MaterialTheme.colorScheme.tertiaryContainer
        content = MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(
        color = container,
        contentColor = content,
        shape = CircleShape
    ) {
        Text(
            text = format.replace("{rating}", (rating ?: 0).toString()),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
    if (plusMinus && onRate != null) {
        textVoteLink("+", 1, linkColor, myRating, onRate)
    }
}

/**
 * Small tonal pill naming the database a quote came from
 * ("Bash.org", "My Quotes", ...).
 */
/**
 * Database source-tag colors (dbId -> RRGGBB), provided once at the app
 * root. Missing key = default tag style (off).
 */
val LocalDbTagColors = compositionLocalOf { emptyMap<String, String>() }

/**
 * v1.59: "database themes only on quote cards" (default OFF). When true,
 * the app root keeps the user's global theme and database site themes
 * style only the quote cards. Provided once at the app root (MainActivity);
 * read by [DbPageBackground] so single-database pages paint the global
 * page instead of the database's own page color/image.
 */
val LocalDbCardsOnly = compositionLocalOf { false }

/**
 * v1.61.10: jumping to Browse filtered to one database — the bottom-right
 * database tag button. Provided by AppNav; null outside the app tree
 * (previews, bitmap rendering) where the button is inert.
 */
val LocalBrowseDatabase = compositionLocalOf<((String) -> Unit)?> { null }

/**
 * v1.61.20: actions for the voice-recording icon on quote cards. Provided
 * once at the nav host: tap jumps to Search filtered to voiced quotes,
 * long-press opens per-quote recording management.
 */
data class VoiceIconActions(
    val onVoiceSearch: () -> Unit = {},
    val onVoiceManage: (dbId: String, number: Int) -> Unit = { _, _ -> }
)
val LocalVoiceIconActions = compositionLocalOf { VoiceIconActions() }

@Composable
fun SourceChip(
    label: String,
    dbId: String? = null,
    /**
     * v1.61.10: when non-null the tag is a button (tap jumps to Browse
     * filtered to that database); null renders the plain label.
     */
    onClick: (() -> Unit)? = null
) {
    val tagColor = dbId?.let { LocalDbTagColors.current[it] }
        ?.let { hexToColorOrNull(it) }
    // v1.57: the chip text color is always derived from the chip
    // background via contentOn() (black on light chips, white on dark).
    // The old onSurfaceVariant fallback broke on site palettes whose
    // surface tones don't pair with the baseline variant color — e.g.
    // furcqdb's tan surface (0xFFD8CFC0) with dark-scheme pale-lavender
    // onSurfaceVariant was unreadable. Audited across the dark site
    // cards: CrapShack's near-black surface now yields white text.
    val chipBg = tagColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
    @Composable
    fun chipText() {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            color = chipBg,
            contentColor = contentOn(chipBg),
            shape = CircleShape
        ) {
            chipText()
        }
    } else {
        Surface(
            color = chipBg,
            contentColor = contentOn(chipBg),
            shape = CircleShape
        ) {
            chipText()
        }
    }
}

/**
 * v1.61: quote translations.
 *
 * [LocalTranslations] carries the [TranslationRepository] (null outside the
 * app tree, e.g. bitmap rendering). Cards resolve the translation for their
 * quote via [rememberQuoteTranslation]; the body text itself is swapped
 * through [LocalTranslatedText], which body lambdas read via
 * [translatedBodyText].
 */
val LocalTranslations = compositionLocalOf<TranslationRepository?> { null }

/**
 * The translated body text to show for the current quote, or null to show
 * the original. Provided around the card body by [SiteThemeQuoteCard] and
 * the detail card.
 */
val LocalTranslatedText = compositionLocalOf<String?> { null }

/** Body lambdas call this instead of `quote.text` to honor translations. */
@Composable
fun translatedBodyText(quote: Quote): String =
    LocalTranslatedText.current ?: quote.text

/**
 * v1.61: snapshot of translation state for one quote card. The per-quote
 * "view original" toggle is hoisted here so the badge and the body agree.
 */
data class QuoteTranslationUi(
    val translatedText: String?,
    val translating: Boolean,
    val displayMode: String,
    val forceOriginal: Boolean,
    val onToggleForceOriginal: () -> Unit,
    /** v1.61.18: per-quote "show both" from a badge long-press. */
    val showBoth: Boolean,
    val onToggleShowBoth: () -> Unit,
    val onTranslateNow: () -> Unit
) {
    /** Whether the translated text is what the body should show. */
    val showingTranslated: Boolean
        get() = translatedText != null &&
            displayMode != TranslationRepository.DISPLAY_ORIGINAL &&
            !forceOriginal

    /** Text for [LocalTranslatedText], or null for the original. */
    val effectiveText: String?
        get() = if (showingTranslated) translatedText else null

    /** The "Translated" badge shows whenever a translation exists and the
     * display mode isn't original-only. Tapping it flips to the original. */
    val showBadge: Boolean
        get() = translatedText != null &&
            displayMode != TranslationRepository.DISPLAY_ORIGINAL

    /**
     * v1.61.18: the original prints below the translation, smaller — either
     * from the global "Both" display mode or a long-press on this quote's
     * Translated badge.
     */
    val showOriginalBelow: Boolean
        get() = (displayMode == TranslationRepository.DISPLAY_BOTH || showBoth) &&
            showingTranslated

    /** "Translating…" shimmer chip while the first translation lands. */
    val showTranslating: Boolean
        get() = translating && translatedText == null &&
            displayMode != TranslationRepository.DISPLAY_ORIGINAL
}

/**
 * v1.61: resolves (and auto-requests) the translation for one quote.
 * Auto-translate fires only for databases with the per-database toggle on,
 * and never in original-only display mode.
 */
@Composable
fun rememberQuoteTranslation(dbId: String?, quote: Quote): QuoteTranslationUi {
    val repo = LocalTranslations.current
    if (repo == null || dbId == null) {
        var forceOriginal by remember { mutableStateOf(false) }
        return QuoteTranslationUi(
            translatedText = null,
            translating = false,
            displayMode = TranslationRepository.DISPLAY_ORIGINAL,
            forceOriginal = forceOriginal,
            onToggleForceOriginal = { forceOriginal = !forceOriginal },
            showBoth = false,
            onToggleShowBoth = {},
            onTranslateNow = {}
        )
    }
    return rememberQuoteTranslationInner(repo, dbId, quote)
}

/**
 * v1.61: inner resolver — split out so the target language is collected as
 * Compose state (a bare `.value` read would never recompose on language
 * switch).
 */
@Composable
private fun rememberQuoteTranslationInner(
    repo: TranslationRepository,
    dbId: String,
    quote: Quote
): QuoteTranslationUi {
    // v1.61: collected as state so switching the target language in
    // Settings recomposes every card to its other-language translation.
    val targetLang by repo.targetLang.collectAsStateWithLifecycle()
    var forceOriginal by remember(dbId, quote.number, targetLang) {
        mutableStateOf(false)
    }
    // v1.61.18: long-pressing the Translated badge shows the original
    // under the translation for this quote only.
    var showBoth by remember(dbId, quote.number, targetLang) {
        mutableStateOf(false)
    }
    val displayMode by repo.displayMode.collectAsStateWithLifecycle()
    val cache by repo.cache.collectAsStateWithLifecycle()
    val inFlight by repo.inFlight.collectAsStateWithLifecycle()
    val autoOn by repo.autoTranslateFlow(dbId).collectAsStateWithLifecycle()
    val key = TranslationRepository.Key(dbId, quote.number, targetLang)
    val translated = cache[key]
    val translating = inFlight.contains(key)
    val wantAuto = displayMode != TranslationRepository.DISPLAY_ORIGINAL && autoOn
    LaunchedEffect(dbId, quote.number, targetLang, wantAuto) {
        if (wantAuto && quote.text.isNotBlank()) {
            // ensureTranslated is idempotent (cached/in-flight/pending are
            // no-ops); translatePairs warms Room before any network call.
            repo.ensureTranslated(dbId, quote.number, quote.text)
        }
    }
    return QuoteTranslationUi(
        translatedText = translated,
        translating = translating,
        displayMode = displayMode,
        forceOriginal = forceOriginal,
        onToggleForceOriginal = { forceOriginal = !forceOriginal },
        showBoth = showBoth,
        onToggleShowBoth = { showBoth = !showBoth },
        // v1.61: fire-and-forget in the repo's scope — a card-scoped launch
        // would be cancelled if the card leaves composition mid-flight.
        onTranslateNow = { repo.translateAsync(dbId, quote.number, quote.text) }
    )
}

/**
 * v1.61: the "Translated" mark. Tapping it shows the original (the label
 * flips to "Original" — tap again to go back). v1.61.18: long-pressing it
 * shows the original under the translation ("both") for that quote; long-
 * press again to go back to translated-only. While a translation is being
 * fetched it reads "Translating…" and isn't clickable.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranslatedBadge(
    showingTranslated: Boolean,
    translating: Boolean,
    onToggle: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val label = when {
        translating -> "Translating…"
        showingTranslated -> "Translated"
        else -> "Original"
    }
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = CircleShape,
        modifier = modifier.then(
            if (translating) Modifier
            else Modifier.combinedClickable(
                onClick = onToggle,
                onLongClick = onLongClick,
                role = Role.Button
            )
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Translate,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

/** v1.61: the original text under a translation in "Both" display mode. */
@Composable
fun TranslationOriginalBelow(originalText: String) {
    val small = MaterialTheme.typography.bodySmall
    Column(modifier = Modifier.padding(top = 10.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Original",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = rememberNickAnnotatedText(originalText, small),
            style = small,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Configurable quote-row swipe actions. NONE disables that direction.
 */
enum class SwipeAction(val id: String, val label: String) {
    NONE("none", "None"),
    FAVORITE("favorite", "Favorite / unfavorite"),
    SHARE("share", "Share"),
    COPY("copy", "Copy text"),
    BASKET("basket", "Add to export tray"),
    HIDE("hide", "Never show again"),
    UP_NEXT("up_next", "Add to Up Next"),
    NEXT_RANDOM("next", "Next random quote"),
    READ_ALOUD("read_aloud", "Read aloud");

    companion object {
        fun fromId(id: String?): SwipeAction =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

/** The configured left/right swipe actions for quote rows. */
data class RowSwipeConfig(
    val left: SwipeAction = SwipeAction.NONE,
    val right: SwipeAction = SwipeAction.NONE
)

/** Performs a swipe action on a quote; provided once at the app root. */
val LocalRowSwipeHandler =
    compositionLocalOf<((action: SwipeAction, dbId: String, quote: Quote) -> Unit)?> { null }

/** Current swipe configuration; provided once at the app root. */
val LocalRowSwipeConfig = compositionLocalOf { RowSwipeConfig() }

/**
 * v1.50 rate-as-you-read: the shared personal-ratings store, provided
 * once at the app root. Null (the default) means no star row renders —
 * e.g. in previews.
 */
val LocalPersonalRatings =
    compositionLocalOf<com.quotebrowser.app.data.PersonalRatings?> { null }

/**
 * v1.50 rate-as-you-read feature toggle, provided once at the app root
 * from Settings. Default true so previews keep the row visible.
 */
val LocalPersonalRatingsEnabled = compositionLocalOf { true }

/**
 * v1.49 tap gestures (double-tap to favorite, two-finger tap to share);
 * provided once at the app root from Settings. Default true so previews
 * and tests keep the gestures working.
 */
val LocalGesturesEnabled = compositionLocalOf { true }

/**
 * v1.53 "Explain this quote" feature gate; provided once at the app root
 * from Settings. Default true so previews keep working.
 */
val LocalExplainEnabled = compositionLocalOf { true }

/**
 * v1.53 report/flag feature gate; provided once at the app root from
 * Settings. Default true so previews keep working.
 */
val LocalFlagEnabled = compositionLocalOf { true }

/**
 * Double-tap detector that leaves single taps alone: used on the detail
 * quote body so nick clicks (single tap) keep working while double-tap
 * favorites.
 */
fun Modifier.onDoubleTapGesture(onDoubleTap: () -> Unit): Modifier =
    pointerInput(onDoubleTap) {
        detectTapGestures(onDoubleTap = { onDoubleTap() })
    }

/**
 * Two-finger tap detector (v1.49): both fingers must land within ~300ms
 * and lift without significant movement. Used for two-finger tap = share
 * on the detail quote body.
 */
fun Modifier.onTwoFingerTap(onTap: () -> Unit): Modifier =
    pointerInput(onTap) {
        awaitEachGesture {
            val first = awaitFirstDown(requireUnconsumed = false)
            // Short window for the second finger to land.
            var second: PointerInputChange? = null
            withTimeoutOrNull(300) {
                while (second == null) {
                    val event = awaitPointerEvent()
                    second = event.changes.firstOrNull {
                        it.id != first.id && it.pressed
                    }
                    if (event.changes.none { it.pressed }) break
                }
            }
            if (second == null) return@awaitEachGesture
            // Both fingers must lift without significant movement.
            var moved = false
            while (true) {
                val event = awaitPointerEvent()
                if (event.changes.any { it.positionChange().getDistance() > 32f }) {
                    moved = true
                }
                if (event.changes.none { it.pressed }) break
            }
            if (!moved) onTap()
        }
    }

/** Bounds for the quote-detail pinch-zoom scale (1.0 = base font size). */
private const val MIN_DETAIL_ZOOM = 0.8f
private const val MAX_DETAIL_ZOOM = 2.5f

/**
 * Two-finger pinch-zoom detector (v1.50). Only zoom deltas are reported —
 * pan and rotation are ignored entirely — so single-finger scrolling still
 * works and a two-finger pinch only zooms.
 *
 * Nothing is consumed until two fingers are down AND the pinch passes touch
 * slop, so single taps, double-tap-to-favorite, nick clicks, and quick
 * two-finger taps (share) all pass through untouched. The zoom is reported
 * per-frame via [onZoomDelta]; [onZoomStart]/[onZoomEnd] bracket each
 * engaged gesture so callers can persist once per gesture instead of every
 * frame (and never fire for taps that don't engage).
 */
private suspend fun PointerInputScope.detectPinchZoom(
    onZoomDelta: (Float) -> Unit,
    onZoomStart: () -> Unit,
    onZoomEnd: () -> Unit
) {
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        // Phase 1: wait for a second finger without consuming anything, so
        // single-finger gestures are completely unaffected.
        awaitFirstDown(requireUnconsumed = false)
        var twoFingers = false
        while (!twoFingers) {
            val event = awaitPointerEvent()
            if (event.changes.none { it.pressed }) return@awaitEachGesture
            twoFingers = event.changes.count { it.pressed } >= 2
        }
        // Phase 2: engage only on real pinch motion past touch slop, so a
        // quick two-finger tap still reaches the share gesture.
        var engaged = false
        var zoomSoFar = 1f
        try {
            while (true) {
                val event = awaitPointerEvent()
                if (event.changes.count { it.pressed } < 2) break
                val zoomChange = event.calculateZoom()
                if (!engaged) {
                    zoomSoFar *= zoomChange
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    if (abs(1f - zoomSoFar) * centroidSize > touchSlop) {
                        engaged = true
                        onZoomStart()
                        event.changes.forEach { if (it.pressed) it.consume() }
                    }
                } else if (zoomChange != 1f) {
                    onZoomDelta(zoomChange)
                    event.changes.forEach { if (it.pressed) it.consume() }
                }
            }
        } finally {
            if (engaged) onZoomEnd()
        }
    }
}

/**
 * v1.55.1: site-theme vote controls shared by the Browse/Random list cards
 * and the single-quote detail view. The detail view renders the same site
 * card as the lists whenever the database's site theme is on (product
 * convention: site themes and the single-quote view stay in lockstep), so
 * the per-site vote affordances live here once, wired to the offline
 * personal rating ([myRating]/[onRate]).
 */

/** The offline personal +/- rating thumbs (PersonalRatings only). */
@Composable
fun VoteThumbButtons(
    myRating: Int,
    onRate: ((Int) -> Unit)?,
    activeTint: Color = MaterialTheme.colorScheme.primary,
    idleTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    buttonSize: Dp = 36.dp,
    iconSize: Dp = 24.dp
) {
    if (onRate != null) {
        IconButton(
            onClick = { onRate(if (myRating == 1) 0 else 1) },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Filled.ThumbUp,
                contentDescription = "Thumbs up",
                tint = if (myRating == 1) activeTint else idleTint,
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(
            onClick = { onRate(if (myRating == -1) 0 else -1) },
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Filled.ThumbDown,
                contentDescription = "Thumbs down",
                tint = if (myRating == -1) activeTint else idleTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/** One qdb.us "-" / "+" vote button. */
@Composable
fun RowScope.qdbVoteButton(
    label: String,
    delta: Int,
    blue: Color,
    myRating: Int,
    onRate: (Int) -> Unit
) {
    val active = myRating == delta
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (active) blue else blue.copy(alpha = 0.45f))
            .clickable { onRate(if (active) 0 else delta) }
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * qdb.us blue-era "-" / "+" vote buttons: the site's own affordances,
 * blue button bg, white bold label; wired to the same offline personal
 * rating as the thumbs.
 */
@Composable
fun RowScope.qdbVoteButtons(
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    if (onRate != null) {
        val blue = Color(0xFF7A9CC6)
        qdbVoteButton("-", -1, blue, myRating, onRate)
        qdbVoteButton("+", 1, blue, myRating, onRate)
    }
}

/**
 * One site-style text vote link ("+"/"-"): the site's own affordance
 * (furcqdb, I-Rox), wired to the same offline personal rating as the
 * thumbs. Active state renders full-strength, inactive dimmed.
 */
@Composable
fun RowScope.textVoteLink(
    label: String,
    delta: Int,
    color: Color,
    myRating: Int,
    onRate: (Int) -> Unit
) {
    val active = myRating == delta
    TextButton(
        onClick = { onRate(if (active) 0 else delta) },
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (active) color else color.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** furcqdb-style "+"/"−" text vote links in the site's orange. */
@Composable
fun RowScope.textVotePair(
    color: Color,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    if (onRate != null) {
        textVoteLink("+", 1, color, myRating, onRate)
        textVoteLink("−", -1, color, myRating, onRate)
    }
}

@Composable
fun RowScope.tightTextVoteLink(
    label: String,
    delta: Int,
    color: Color,
    myRating: Int,
    onRate: (Int) -> Unit
) {
    val active = myRating == delta
    Text(
        text = label,
        fontWeight = FontWeight.Bold,
        color = if (active) color else color.copy(alpha = 0.55f),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onRate(if (active) 0 else delta) }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

/**
 * v1.55: tight "+"/"−" vote links for the furcqdb footer — snug
 * together like the site's "Current score: 2 + -" line. Same offline
 * personal-rating wiring as [textVotePair], but without TextButton's
 * 48dp minimum touch target.
 */
@Composable
fun RowScope.tightTextVotePair(
    color: Color,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    if (onRate != null) {
        tightTextVoteLink("+", 1, color, myRating, onRate)
        tightTextVoteLink("−", -1, color, myRating, onRate)
    }
}

/**
 * I-Rox inline vote segment: the site's own "#N - score + [x]" line —
 * orange "-" link, orange score, orange "+" link, all wired to the
 * offline personal rating.
 */
@Composable
fun RowScope.iroxVoteSegment(
    rating: Int?,
    color: Color,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    // v1.77: the score always shows — 0 when the quote has no rating.
    val score = rating ?: 0
    if (onRate != null) {
        textVoteLink("-", -1, color, myRating, onRate)
    }
    Text(
        text = "$score",
        color = color,
        style = MaterialTheme.typography.labelMedium
    )
    if (onRate != null) {
        textVoteLink("+", 1, color, myRating, onRate)
    }
}

/**
 * Quotes.BurntElectrons.org vote segment: the site's own "Up!" /
 * "Down↓" text vote links (the site's welcome text: "vote for the
 * quotes you love by clicking on the Up! link ... the old thumbs
 * down using the Down↓ link"), wired to the offline personal rating.
 * All links are the site's red, like the original.
 */
@Composable
fun RowScope.burntElectronsVoteSegment(
    rating: Int?,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    if (rating == null) return
    if (onRate != null) {
        val red = Color(0xFF990000)
        textVoteLink("Up!", 1, red, myRating, onRate)
        textVoteLink("Down↓", -1, red, myRating, onRate)
    }
}

/**
 * bash.org vote segment: the site's own iconic "#N +(score)- [X]"
 * first line. "+" and "-" hug the parenthesized score (the site's
 * vote links) and "[X]" sits right after — all wired to the offline
 * personal rating / flag action. Gold (#CD8C00 — v1.61.15 brightened
 * it a touch; the old dark gold read too dark on exports) for the links, green for the score number,
 * exactly like the original.
 *
 * v1.55: tight clickable text links instead of TextButtons — the
 * 48dp minimum touch target was spacing "+" and "-" far apart.
 */
@Composable
fun RowScope.bashVoteSegment(
    rating: Int?,
    myRating: Int,
    onRate: ((Int) -> Unit)?,
    onFlagClick: (() -> Unit)?
) {
    // v1.77: the score always shows — 0 when the quote has no rating.
    val score = rating ?: 0
    val gold = Color(0xFFCD8C00)
    val scoreGreen = Color(0xFF3D7A3D)
    @Composable
    fun tightLink(label: String, onClick: () -> Unit, color: Color) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 2.dp, vertical = 6.dp)
        )
    }
    if (onRate != null) {
        tightLink(
            "+",
            onClick = { onRate(if (myRating == 1) 0 else 1) },
            color = if (myRating == 1) gold else gold.copy(alpha = 0.6f)
        )
    }
    Text(
        text = "($score)",
        color = scoreGreen,
        style = MaterialTheme.typography.bodyMedium
    )
    if (onRate != null) {
        tightLink(
            "-",
            onClick = { onRate(if (myRating == -1) 0 else -1) },
            color = if (myRating == -1) gold else gold.copy(alpha = 0.6f)
        )
    }
    if (onFlagClick != null) {
        Spacer(Modifier.width(4.dp))
        tightLink(
            "[X]",
            onClick = onFlagClick,
            color = gold
        )
    }
}

/**
 * xkcdb.com vote segment: the site's own "#N ⇧ (+up/-down) ⇩" line —
 * green up-arrow glyph and red down-arrow glyph as text vote links
 * (wired to the offline personal rating), the count in dark between
 * them, exactly like the site's quote headers.
 */
@Composable
fun RowScope.xkcdbVoteSegment(
    rating: Int?,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    // v1.77: the score always shows — 0 when the quote has no rating.
    val score = rating ?: 0
    if (onRate != null) {
        textVoteLink("⇧", 1, Color(0xFF2E9E44), myRating, onRate)
    }
    Text(
        text = if (score >= 0) "(+$score)" else "($score)",
        color = Color(0xFF222222),
        style = MaterialTheme.typography.labelMedium
    )
    if (onRate != null) {
        textVoteLink("⇩", -1, Color(0xFFD42A2A), myRating, onRate)
    }
}

/**
 * warpdrive.se inline vote segment: the site's own "+ score -" gray/cyan
 * text vote links around the score, wired to the offline personal
 * rating.
 */
@Composable
fun RowScope.warpdriveVoteSegment(
    rating: Int?,
    color: Color,
    myRating: Int,
    onRate: ((Int) -> Unit)?
) {
    // v1.77: the score always shows — 0 when the quote has no rating.
    val score = rating ?: 0
    if (onRate != null) {
        textVoteLink("+", 1, color, myRating, onRate)
    }
    Text(
        text = "$score",
        color = Color(0xFF777777),
        style = MaterialTheme.typography.labelMedium
    )
    if (onRate != null) {
        textVoteLink("-", -1, color, myRating, onRate)
    }
}

/**
 * v1.55.1: the per-site vote-control dispatch for [SiteQuoteCard]
 * headers — the same control set on list cards and the detail view.
 * qdb.us gets the site's own "-" / "+" buttons instead of thumbs;
 * I-Rox and furcqdb use their sites' own orange text vote links;
 * warpdrive, xkcdb, BurntElectrons and bash.org use their own
 * segments; everything else gets tinted thumbs.
 */
@Composable
fun RowScope.siteVoteControls(
    siteStyle: SiteStyle,
    rating: Int?,
    /** Header tint for the fallback thumbs. */
    tint: Color,
    myRating: Int,
    onRate: ((Int) -> Unit)?,
    onFlagClick: (() -> Unit)?
) {
    when (siteStyle) {
        SiteStyle.CLASSIC_QDB -> qdbVoteButtons(myRating, onRate)
        SiteStyle.IROX -> iroxVoteSegment(
            rating,
            Color(0xFFE8721C),
            myRating,
            onRate
        )
        // v1.55: furcqdb's tight "+"/"−" vote links for the
        // footer site line (snug like "Current score: 2 + -").
        SiteStyle.FURCQDB -> tightTextVotePair(Color(0xFFFFA726), myRating, onRate)
        SiteStyle.WARPDRIVE -> warpdriveVoteSegment(
            rating,
            Color(0xFF0099CC),
            myRating,
            onRate
        )
        // v1.88: bash.im uses the same "+ score −" text vote links,
        // in the site's gray.
        SiteStyle.BASHIM -> warpdriveVoteSegment(
            rating,
            Color(0xFF8A8A8A),
            myRating,
            onRate
        )
        // v1.54: xkcdb uses its own green ⇧ / red ⇩ text
        // vote links around the counts, not thumbs.
        SiteStyle.XKCDB -> xkcdbVoteSegment(rating, myRating, onRate)
        // v1.54: BurntElectrons uses its own "Up!"/"Down↓"
        // text vote links, not thumbs.
        SiteStyle.BURNT_ELECTRONS ->
            burntElectronsVoteSegment(rating, myRating, onRate)
        // v1.54: bash.org uses its own "#N +(score)- [X]"
        // line, not thumbs.
        SiteStyle.BASH -> bashVoteSegment(rating, myRating, onRate, onFlagClick)
        else -> VoteThumbButtons(
            myRating = myRating,
            onRate = onRate,
            activeTint = tint,
            idleTint = tint.copy(alpha = 0.55f),
            buttonSize = 32.dp,
            iconSize = 18.dp
        )
    }
}

/** Overflow + favorite buttons (no rating thumbs). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.MoreFavoriteButtons(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    showFavoriteAction: Boolean = true,
    onMoreClick: (() -> Unit)? = null,
    /**
     * v1.61.9: one-tap share, shown immediately left of the favorite
     * button. Null hides it (screens without share plumbing).
     * v1.61.11: order is share, favorite, menu (⋯) — menu always last at
     * the far right of the control row.
     */
    onShareClick: (() -> Unit)? = null,
    /**
     * v1.61.20: when both are set and the quote has a generated voice
     * recording, a voice icon shows left of the share button — tap jumps
     * to Search filtered to voiced quotes, long-press manages this
     * quote's recordings. The AudioFile glyph + teal tint keep it
     * visually distinct from the read-aloud speaker icon.
     */
    voiceDbId: String? = null,
    voiceNumber: Int? = null,
    /**
     * v1.75: when set (alongside voiceDbId/voiceNumber for the cache
     * key) and the quote has a saved Gemini explanation, an AutoAwesome
     * icon shows next to the voice icon — tap opens the cached
     * explanation instantly. Purple tint keeps it distinct from the
     * teal voice icon.
     */
    explainQuote: Quote? = null
) {
    // v1.75: saved-explanation dialog state, hoisted so the icon block
    // below stays conditional.
    var showExplainDialog by remember { mutableStateOf(false) }
    if (voiceDbId != null && voiceNumber != null) {
        val recordings by VoiceRecordings.recordings.collectAsStateWithLifecycle()
        // v1.74.9: use the validated hasRecording check (files must exist
        // on disk, not just be listed in the manifest) so the icon only
        // shows when there's actually playable audio.
        val hasVoice = remember(recordings, voiceDbId, voiceNumber) {
            VoiceRecordings.hasRecording(voiceDbId, voiceNumber)
        }
        if (hasVoice) {
            val actions = LocalVoiceIconActions.current
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .combinedClickable(
                        onClick = actions.onVoiceSearch,
                        onLongClick = {
                            actions.onVoiceManage(voiceDbId, voiceNumber)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AudioFile,
                    contentDescription = "Voice recording: tap to find voiced " +
                        "quotes, long-press to manage this quote's recordings",
                    tint = Color(0xFF26A69A)
                )
            }
        }
        // v1.75: saved-explanation icon — same "$dbId|$number" key as
        // the voice icon, shown only when a real cached explanation
        // exists. Tap opens it instantly from the cache.
        val explainTargetQuote = explainQuote
        if (explainTargetQuote != null) {
            val explanations by ExplanationCache.explanations.collectAsStateWithLifecycle()
            val hasExplanation = remember(explanations, voiceDbId, voiceNumber) {
                ExplanationCache.hasExplanation(voiceDbId, voiceNumber)
            }
            if (hasExplanation) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { showExplainDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Saved explanation: tap to view",
                        tint = Color(0xFF7E57C2)
                    )
                }
            }
        }
    }
    if (showExplainDialog && explainQuote != null) {
        ExplainQuoteDialog(
            quote = explainQuote,
            onDismiss = { showExplainDialog = false },
            dbId = voiceDbId
        )
    }
    if (onShareClick != null) {
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share quote",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (showFavoriteAction) {
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite
                else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites"
                else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (onMoreClick != null) {
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Quote actions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * v1.56: per-database page background layer for single-database contexts
 * (Browse / Random / single-quote detail).
 *
 * - [dbId] null or "all databases" = mixed mode: no layer, so the caller
 *   keeps the global theme page (including the global DevXen behind-quotes
 *   artwork — a future multi-database selection lands in this same bucket).
 * - Otherwise paints the database's own page color: the explicit
 *   per-database "Page background" when set, else the database's authentic
 *   site page color (CrapShack black, qdb.us white, ...). The "black page
 *   outside quotes" option keeps its existing meaning and is applied by
 *   the caller on top of this layer.
 * - When a background image is set (Settings → database themes), the
 *   app-private image is drawn cropped to fill and dimmed with a black
 *   scrim (~0.6) so cards stay readable.
+ * - v1.87.2: the "Black background outside quotes" setting overrides the
+ *   page color entirely — the page is black, never the database's
+ *   authentic color, so the setting's promise holds in every
+ *   single-database view (including the detail screen from notifications).
 * - v1.59: [forceGlobal] (or the [LocalDbCardsOnly] flag, set when the
 *   "database themes only on quote cards" toggle is ON) skips the
 *   per-database page color/image entirely, so the caller keeps the
 *   global theme page. No call-site changes needed: the flag is read
 *   from the CompositionLocal.
 */
@Composable
fun DbPageBackground(
    dbId: String?,
    manager: DatabaseManager,
    modifier: Modifier = Modifier,
    forceGlobal: Boolean = false,
    content: @Composable () -> Unit
) {
    // v1.59: cards-only mode behaves like mixed mode — the per-database
    // page layer is skipped and the global page shows through.
    // v1.87.2: "Black background outside quotes" wins over everything: the
    // setting promises a black page, so the database's authentic page color
    // (e.g. qdb.us white) must not show around the card.
    val blackOutside = LocalBlackOutsideQuotes.current
    val cardsOnly = LocalDbCardsOnly.current
    val effectiveDbId = if (forceGlobal || cardsOnly) null else dbId
    val snap = LocalDbThemeSnapshots.current
    val pageColor = remember(snap, effectiveDbId) { pageBackgroundFor(effectiveDbId, snap) }
    val imageName = remember(snap, effectiveDbId) { pageBackgroundImageFor(effectiveDbId, snap) }
    val imageBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = null, imageName
    ) {
        value = withContext(Dispatchers.IO) {
            val file = imageName?.let { manager.dbBackgroundImageFile(it) }
                ?.takeIf { it.exists() } ?: return@withContext null
            runCatching { decodeBackgroundFile(file) }.getOrNull()
        }
    }
    Box(modifier) {
        if (blackOutside) {
            Box(Modifier.matchParentSize().background(Color.Black))
        } else if (pageColor != null) {
            Box(Modifier.matchParentSize().background(pageColor))
        }
        // v1.87.2: the image layer draws on top of the page color, so it must
        // also yield to the black-outside promise — otherwise a database
        // background image would cover the black page.
        if (!blackOutside && imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            // Dim the image so quote cards stay readable.
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.6f)))
        }
        content()
    }
}

/** Decodes a page-background image file, downsampled so the long side fits ~1600px. */
private fun decodeBackgroundFile(file: java.io.File): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(file.absolutePath, opts)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

/**
 * v1.55.1: the quote rendered as a mini version of its database's
 * original site — the exact card the Browse/Random lists show, shared
 * with the single-quote detail view. The detail view renders this same
 * card (full per-site header and footer) whenever the quote's database
 * has its site theme enabled; site themes and the single-quote view
 * stay in lockstep by construction.
 *
 * @param dbId id of the database the quote came from: resolves the
 * per-database theme and the footer total.
 * @param body the quote body content.
 * @param onFlagClick opens the report/flag dialog; null hides the flag
 * affordance (including bash.org's [X]).
 */
@Composable
fun SiteThemeQuoteCard(
    quote: Quote,
    dbId: String?,
    modifier: Modifier = Modifier,
    showNumber: Boolean = true,
    showRating: Boolean = true,
    showQueuedTag: Boolean = false,
    /** WQDB alternating row shades: even/odd list index, null = even. */
    listIndex: Int? = null,
    sourceLabel: String? = null,
    sourceDbId: String? = null,
    /** Local thumbs rating: -1 (down), 0 (none), 1 (up). */
    myRating: Int = 0,
    /**
     * When non-null, the site's vote controls show so the quote can be
     * rated. Receives the new delta (1/-1/0 to clear). Gated by the
     * "+/- vote controls" feature toggle.
     */
    onRate: ((Int) -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    showFavoriteAction: Boolean = true,
    /** Overflow button opening the quick-action sheet. */
    onMoreClick: (() -> Unit)? = null,
    /** v1.61.9: one-tap share in the header, left of the favorite button. */
    onShareClick: (() -> Unit)? = null,
    onFlagClick: (() -> Unit)? = null,
    /** v1.77: Favorites pin toggle; null hides the pin in the header. */
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null,
    /**
     * v1.77: ungated rate handler — bash.org's +/- vote links always show
     * (authentic site chrome), bypassing the "+/- vote controls" toggle.
     */
    rawOnRate: ((Int) -> Unit)? = null,
    body: @Composable () -> Unit
) {
    // Per-database theming: each card resolves its own source database's
    // theme (pure, no I/O).
    val snap = LocalDbThemeSnapshots.current
    val resolved = remember(snap, dbId) { resolveDbTheme(snap, dbId) }
    // v1.89.6: the rating format the quote's database carries in its
    // theme spec, when it declares one (null = built-in per-style text).
    val carriedRatingFormat = remember(snap, dbId, sourceDbId) {
        (dbId ?: sourceDbId)?.let { snap.builtInDbRatingFormats[it] }
    }
    // v1.56: the per-database "Quote background" theme slot — the surface
    // behind the quote body text. Site themes use their authentic body
    // colors as the built-in default; an explicit per-database editor value
    // (Settings → database themes) wins in every mode.
    val quoteBackground = remember(resolved, snap, dbId) {
        quoteBackgroundForDb(resolved, dbId, snap)
    }
    val siteStyle = remember(resolved) {
        if (resolved.bypassed) SiteStyle.MODERN_CARD
        else siteStyleFor(resolved.structureThemeId)
    }
    // Site header art renders only inside that site theme's own Site-layout
    // presentation (per-DB themes on, no master override, DB theming on,
    // Site layout); everywhere else headers stay plain.
    val siteArt = remember(resolved) {
        if (siteArtAllowed(resolved)) siteHeaderArt(resolved.structureThemeId) else null
    }
    val wrapPalette = !resolved.bypassed && resolved.paletteThemeId != snap.globalThemeId
    val paletteScheme =
        themeColorScheme(resolved.paletteThemeId, snap.customThemes, snap.builtInOverrides)
    // Nick colors resolve per card too, from the card's palette theme.
    val rootNicks = LocalNickPalette.current
    val cardNicks = remember(resolved, snap, rootNicks) {
        if (!wrapPalette) rootNicks
        else {
            val r = resolveNickColors(
                resolved.paletteThemeId, snap.customThemes, snap.builtInOverrides
            )
            rootNicks.copy(
                brackets = r.brackets,
                names = r.names,
                bracketPalette = r.bracketPalette,
                action = r.action,
                actionOverridesName = r.actionOverridesName,
                colorMessageWithSpeaker = r.colorMessageWithSpeaker
            )
        }
    }
    // v1.51: the "+/- vote controls" feature toggle gates the offline
    // thumbs controls everywhere they appear (adjust PersonalRatings
    // only; no network, no "vote submitted" copy anywhere).
    val gatedRate = if (LocalVoteControlsEnabled.current) onRate else null
    // v1.61: quote translation state for this card. Auto-translate fires
    // from here for databases with the toggle on.
    val tr = rememberQuoteTranslation(dbId ?: sourceDbId, quote)

    val content: @Composable () -> Unit = {
        // v1.55: the quote background is available to the nick
        // colorizer (for contrast adjustment) throughout the card.
        CompositionLocalProvider(LocalQuoteBackground provides quoteBackground) {
            SiteQuoteCard(
                style = siteStyle,
                quote = quote,
                centeredLogoArt = if (resolved.centeredHeaderLogo) siteArt else null,
                hideTldInHeader = resolved.hideTldInHeader,
                showNumber = showNumber,
                showRating = showRating,
                showQueuedTag = showQueuedTag,
                listIndex = listIndex,
                dbId = dbId ?: sourceDbId,
                modifier = modifier,
                // v1.61: the body reads LocalTranslatedText, so translated
                // quotes render here; "Both" mode appends the original.
                // v1.61.2: wrapped in a Column — SiteBodyBox lays the body
                // slot in a Box, which would stack the translation and the
                // original on top of each other.
                body = {
                    Column {
                        CompositionLocalProvider(
                            LocalTranslatedText provides tr.effectiveText
                        ) {
                            body()
                        }
                        if (tr.showOriginalBelow) {
                            TranslationOriginalBelow(quote.text)
                        }
                    }
                },
                // v1.53 report/flag: the [X]-style flag affordance in the
                // site header opens the flag dialog.
                onFlagClick = onFlagClick,
                actions = {},
                // v1.55: overflow + favorite sit at the top right of every
                // site card, just under the header bar (no bottom action
                // strip on any site theme).
                // v1.61.10: the database tag moved out of the header —
                // it sits bottom-right across from the star rating, as a
                // button that jumps to Browse filtered to that database.
                headerActions = {
                    // v1.61: the "Translated" mark — tap to view original.
                    // v1.61.18: long-press shows both for this quote.
                    if (tr.showBadge) {
                        TranslatedBadge(
                            showingTranslated = tr.showingTranslated,
                            translating = false,
                            onToggle = tr.onToggleForceOriginal,
                            onLongClick = tr.onToggleShowBoth
                        )
                    } else if (tr.showTranslating) {
                        TranslatedBadge(
                            showingTranslated = false,
                            translating = true,
                            onToggle = {}
                        )
                    }
                    MoreFavoriteButtons(
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        showFavoriteAction = showFavoriteAction,
                        onMoreClick = onMoreClick,
                        onShareClick = onShareClick,
                        voiceDbId = dbId ?: sourceDbId,
                        voiceNumber = quote.number,
                        // v1.75: saved-explanation icon for this card.
                        explainQuote = quote
                    )
                },
                // v1.53: working offline +/- controls in the themed
                // header, tinted for the site bar they sit on.
                voteControls = { tint, _ ->
                    siteVoteControls(siteStyle, quote.rating, tint, myRating, gatedRate, onFlagClick)
                },
                // v1.77: Favorites pin in the card header; bash.org's
                // always-on vote links get the ungated handler.
                isPinned = isPinned,
                onTogglePin = onTogglePin,
                bashMyRating = myRating,
                bashOnRate = rawOnRate,
                // v1.55: the per-database "Quote background" theme slot.
                quoteBackground = quoteBackground,
                // v1.89.6: the database-carried rating format, if any.
                ratingFormat = carriedRatingFormat
            )
        }
    }
    if (!wrapPalette) content()
    else MaterialTheme(colorScheme = paletteScheme) {
        CompositionLocalProvider(LocalNickPalette provides cardNicks) {
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun QuoteRow(
    quote: Quote,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteAction: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    showNumber: Boolean = false,
    showRating: Boolean = false,
    sourceLabel: String? = null,
    sourceDbId: String? = null,
    /** Multi-select: when true, taps toggle selection and a checkbox shows. */
    selectionActive: Boolean = false,
    isSelected: Boolean = false,
    onSelectionToggle: (() -> Unit)? = null,
    /** Overflow button opening the quick-action sheet (long-press now selects). */
    onMoreClick: (() -> Unit)? = null,
    /** v1.61.9: one-tap share in the header, left of the favorite button. */
    onShareClick: (() -> Unit)? = null,
    /** Database id, enabling configurable swipe actions (null = no swipe). */
    dbId: String? = null,
    /** This row's local thumbs rating (-1/0/1). */
    myRating: Int = 0,
    /**
     * When non-null, thumbs up/down buttons show on the row so the quote
     * can be rated without opening the detail screen. Receives the new
     * delta (1/-1/0 to clear). Gated by the "+/- vote controls" feature
     * toggle (LocalVoteControlsEnabled).
     */
    onRate: ((Int) -> Unit)? = null,
    /**
     * v1.51: list index for the WQDB site style's alternating row shades.
     * Null = no alternation.
     */
    listIndex: Int? = null,
    /**
     * v1.53 report/flag: whether this quote is currently flagged, its flag
     * details for the tag, and the handler invoked from the flag dialog.
     * When onFlagQuote is set (and the feature is enabled), site-theme
     * headers show the flag affordance and the card shows the Flagged tag.
     */
    isFlagged: Boolean = false,
    flagInfo: FlagInfo? = null,
    onFlagQuote: ((reason: String, customText: String) -> Unit)? = null,
    /**
     * v1.64: tapping a nick name in the quote body navigates to that
     * nick's page. Previously the body was plain Text with no click
     * handling, so nick taps did nothing on quote cards.
     */
    onNickClick: (String) -> Unit = {},
    /** v1.77: Favorites pin — when non-null, a pin button shows in the card header. */
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null
) {
    val compact = LocalCompactMode.current
    // v1.51: the "+/- vote controls" feature toggle gates the offline
    // thumbs controls everywhere they appear (adjust PersonalRatings
    // only; no network, no "vote submitted" copy anywhere).
    // v1.77: bash.org's vote links always show, so the raw handler is
    // kept alongside the gated one.
    val gatedRate = if (LocalVoteControlsEnabled.current) onRate else null
    // v1.51: "Queued" tag on unapproved quotes, with tap-for-explanation.
    val showQueuedTag = LocalQueuedTagsEnabled.current && quote.status == "queued"
    // v1.53 report/flag: the Flagged tag and the header flag affordance.
    val flaggingOn = LocalFlagEnabled.current && onFlagQuote != null
    val showFlaggedTag = flaggingOn && isFlagged
    var showFlagDialog by remember { mutableStateOf(false) }
    val tapAction = if (selectionActive && onSelectionToggle != null) onSelectionToggle else onClick
    // v1.64: nick-tap routing. The body Text records each tap-down (never
    // consuming, so row gestures are untouched) together with the current
    // tap sequence number; the row tap handler hit-tests the recorded
    // position against the body's nick annotations and navigates to the
    // nick page instead of opening the quote. The sequence guard keeps a
    // stale position (e.g. a down that turned into a scroll) from
    // hijacking a later tap elsewhere on the row.
    var tapSeq by remember { mutableIntStateOf(0) }
    var bodyTap by remember { mutableStateOf<Pair<Int, Offset>?>(null) }
    var bodyLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var bodyAnnotated by remember { mutableStateOf<AnnotatedString?>(null) }

    val rowUriHandler = LocalUriHandler.current

    /** Row tap: URL taps open the link, nick taps go to the nick page,
     *  everything else is the card's normal tap action. */
    fun handleRowTap() {
        val tap = bodyTap
        val layout = bodyLayout
        val annotated = bodyAnnotated
        bodyTap = null
        if (!selectionActive && tap != null && tap.first == tapSeq &&
            layout != null && annotated != null
        ) {
            val offset = layout.getOffsetForPosition(tap.second)
            if (offset in annotated.indices) {
                // v1.89.7: a URL tap opens the browser and never falls
                // through to the card's tap action.
                val url = annotated.getStringAnnotations(
                    "url", offset, (offset + 1).coerceAtMost(annotated.length)
                ).firstOrNull()?.item
                if (url != null) {
                    try {
                        rowUriHandler.openUri(url)
                    } catch (_: Exception) {
                    }
                    return
                }
                // 1-wide query: getStringAnnotations only matches a
                // zero-width query strictly inside the range, so a tap on
                // the nick's first letter would miss.
                val nick = annotated.getStringAnnotations(
                    "nick", offset, (offset + 1).coerceAtMost(annotated.length)
                ).firstOrNull()?.item
                if (nick != null) {
                    onNickClick(nick)
                    return
                }
            }
        }
        tapAction()
    }
    val swipeConfig = LocalRowSwipeConfig.current
    val swipeHandler = LocalRowSwipeHandler.current
    val swipeEnabled = !selectionActive && dbId != null && swipeHandler != null &&
        (swipeConfig.left != SwipeAction.NONE || swipeConfig.right != SwipeAction.NONE)
    val gesturesOn = LocalGesturesEnabled.current

    // v1.51 per-database theming: in All-databases mode each card resolves
    // its own source database's theme (pure, no I/O). The root keeps the
    // global theme; single-database screens resolve to the root palette
    // and skip the extra MaterialTheme layer.
    val snap = LocalDbThemeSnapshots.current
    val cardDbId = dbId ?: sourceDbId
    // v1.85: quote layout — per-database override, else the global layout,
    // else Classic. Classic keeps the exact pre-1.85 card path below;
    // every other layout renders through LayoutCard.
    val layoutContext = LocalContext.current
    // v1.85: prefsVersion keeps this reactive — changing a layout in
    // Settings recomputes the card immediately, even when the list
    // composition survives navigation.
    val cardLayout = remember(layoutContext, cardDbId, LayoutStore.prefsVersion) {
        LayoutStore.layoutForDb(layoutContext, cardDbId, PluginManager.pluginLayouts)
    }
    // v1.61: quote translation state for this row. The site-card path
    // below goes through SiteThemeQuoteCard (own wiring); the modern path
    // wires it here.
    val rowTr = rememberQuoteTranslation(cardDbId, quote)
    val resolved = remember(snap, cardDbId) { resolveDbTheme(snap, cardDbId) }
    // v1.56: the per-database "Quote background" theme slot — the surface
    // behind the quote body text. Site themes use their authentic body
    // colors as the built-in default; an explicit per-database editor value
    // (Settings → database themes) wins in every mode.
    val quoteBackground = remember(resolved, snap, cardDbId) {
        quoteBackgroundForDb(resolved, cardDbId, snap)
    }
    val siteStyle = remember(resolved) {
        if (resolved.bypassed) SiteStyle.MODERN_CARD
        else siteStyleFor(resolved.structureThemeId)
    }
    // v1.89.6: the rating format / vote style the quote's database
    // carries in its theme spec (null format = no carried format). The
    // standard card shows the formatted text instead of the bare badge
    // number, flanked by the database's own +/− vote links when its
    // vote style asks for them (then the generic header thumbs would
    // duplicate them, so they stand down).
    val carriedFormat = remember(snap, cardDbId) {
        cardDbId?.let { snap.builtInDbRatingFormats[it] }
    }
    val carriedPlusMinus = remember(snap, cardDbId) {
        cardDbId?.let { snap.builtInDbVoteStyles[it] } == "plusminus"
    }
    val carriedFlankActive = showRating && !carriedFormat.isNullOrEmpty() &&
        carriedPlusMinus && gatedRate != null
    // Site header art renders only inside that site theme's own Site-layout
    // presentation (per-DB themes on, no master override, DB theming on,
    // Site layout); everywhere else headers stay plain.
    val siteArt = remember(resolved) {
        if (siteArtAllowed(resolved)) siteHeaderArt(resolved.structureThemeId) else null
    }
    val wrapPalette = !resolved.bypassed && resolved.paletteThemeId != snap.globalThemeId
    val paletteScheme =
        themeColorScheme(resolved.paletteThemeId, snap.customThemes, snap.builtInOverrides)
    // Nick colors resolve per card too, from the card's palette theme.
    val rootNicks = LocalNickPalette.current
    val cardNicks = remember(resolved, snap, rootNicks) {
        if (!wrapPalette) rootNicks
        else {
            val r = resolveNickColors(
                resolved.paletteThemeId, snap.customThemes, snap.builtInOverrides
            )
            rootNicks.copy(
                brackets = r.brackets,
                names = r.names,
                bracketPalette = r.bracketPalette,
                action = r.action,
                actionOverridesName = r.actionOverridesName,
                colorMessageWithSpeaker = r.colorMessageWithSpeaker
            )
        }
    }

    @Composable
    fun themedCard(content: @Composable () -> Unit) {
        if (!wrapPalette) content()
        else MaterialTheme(colorScheme = paletteScheme) {
            CompositionLocalProvider(LocalNickPalette provides cardNicks) {
                content()
            }
        }
    }

    /** Single/double/long-press gestures shared by both card styles. */
    fun tapGestures() = Modifier
        // v1.64: sequence-stamps every tap-down on the row so the body
        // Text's recorded tap position can be validated as current.
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                tapSeq++
            }
        }
        .then(
            Modifier.combinedClickable(
                onClick = { handleRowTap() },
        onDoubleClick = {
            if (selectionActive) onSelectionToggle?.invoke()
            // Double-tap favorites, unless tap gestures are off
            // (the empty branch still consumes the double-tap so
            // it doesn't fall through as two single taps).
            else if (gesturesOn) onToggleFavorite()
        },
        onLongClick = {
            if (selectionActive) onSelectionToggle?.invoke()
            else onLongClick?.invoke()
        }
            )
        )

    /** The right-side action buttons: vote thumbs, overflow, favorite. */
    @Composable
    fun actionButtons() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // v1.89.6: the database's own +/− links flank its carried
            // rating format — the generic thumbs stand down then.
            if (!carriedFlankActive) {
                VoteThumbButtons(myRating = myRating, onRate = gatedRate)
            }
            // v1.77: Favorites pin in the card header (modern cards have
            // no flag icon, so the pin leads the action group).
            if (onTogglePin != null) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Unpin from top" else "Pin to top",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            MoreFavoriteButtons(
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                showFavoriteAction = showFavoriteAction,
                onMoreClick = onMoreClick,
                onShareClick = onShareClick,
                voiceDbId = dbId ?: sourceDbId,
                voiceNumber = quote.number,
                // v1.75: saved-explanation icon for this card.
                explainQuote = quote
            )
        }
    }

    /** The quote body text plus the rate-as-you-read star row.
     *
     * v1.54: wrapped in its own Column so the star row always lays out in
     * the vertical flow directly under the text. (The site cards render
     * this body inside a Box; without the Column the stars were drawn
     * overlapping the first line of the quote text.)
     */
    @Composable
    fun bodyBlock() {
        val bodyStyle = quoteTextStyle(
            if (compact) MaterialTheme.typography.bodySmall
            else MaterialTheme.typography.bodyMedium
        ).let {
            // Site styles that mimic monospace sites render the body in
            // monospace; the user's custom quote font stays on Modern.
            if (siteStyle != SiteStyle.MODERN_CARD && siteBodyMonospace(siteStyle))
                it.copy(fontFamily = FontFamily.Monospace) else it
        }
        Column {
            val annotatedBody = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
            Text(
                // v1.61: translated body text when a translation shows.
                text = annotatedBody,
                style = bodyStyle,
                onTextLayout = {
                    bodyLayout = it
                    bodyAnnotated = annotatedBody
                },
                // v1.64: records tap-down positions for nick hit-testing.
                // Never consumes: row taps, double-taps and long-presses
                // behave exactly as before.
                modifier = Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        bodyTap = tapSeq to down.position
                    }
                }
            )
            // v1.50 rate-as-you-read: personal star row under the card
            // text. Search's all-results rows carry no dbId, only
            // sourceDbId, so fall back to that.
            // v1.61.10: the database tag sits bottom-right, across from
            // the stars — and it's a button that jumps to Browse filtered
            // to that database. Site themes only; the modern card keeps
            // its top-row chip.
            val ratingsStore = LocalPersonalRatings.current
            val ratingsId = dbId ?: sourceDbId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ratingsStore != null && LocalPersonalRatingsEnabled.current &&
                    ratingsId != null
                ) {
                    PersonalRatingStars(
                        store = ratingsStore,
                        dbId = ratingsId,
                        number = quote.number
                    )
                }
                if (siteStyle != SiteStyle.MODERN_CARD && sourceLabel != null) {
                    val browseDb = LocalBrowseDatabase.current
                    val tagDbId = dbId ?: sourceDbId
                    Spacer(Modifier.weight(1f))
                    SourceChip(
                        label = sourceLabel,
                        dbId = sourceDbId,
                        onClick = if (tagDbId != null && browseDb != null) {
                            { browseDb(tagDbId) }
                        } else null
                    )
                }
            }
        }
    }

    @Composable
    fun rowContent() {
        Row(
            modifier = Modifier.padding(
                horizontal = if (selectionActive) 4.dp else 16.dp,
                vertical = if (compact) 10.dp else 14.dp
            ),
            verticalAlignment = Alignment.Top
        ) {
            if (selectionActive) {
                androidx.compose.material3.Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle?.invoke() },
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Column(Modifier.weight(1f)) {
        if (showNumber || showRating || showFavoriteAction || sourceLabel != null || onMoreClick != null || gatedRate != null || showQueuedTag) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showNumber) {
                        QuoteNumberBadge(number = quote.number)
                    }
                    if (showNumber && showRating) {
                        Spacer(Modifier.width(8.dp))
                    }
                    if (showRating) {
                        // v1.89.6: a database carrying its own rating
                        // format shows that text instead of the bare
                        // badge number (FML: "I agree (123)").
                        if (carriedFormat != null) {
                            CarriedRatingDisplay(
                                format = carriedFormat,
                                rating = quote.rating,
                                plusMinus = carriedPlusMinus,
                                myRating = myRating,
                                onRate = gatedRate
                            )
                        } else {
                            RatingBadge(quote.rating)
                        }
                    }
                    if (showQueuedTag) {
                        Spacer(Modifier.width(8.dp))
                        QueuedTag()
                    }
                    if (showFlaggedTag) {
                        Spacer(Modifier.width(8.dp))
                        FlaggedTag(
                            reason = flagInfo?.reason ?: "Other",
                            customText = flagInfo?.customText ?: ""
                        )
                    }
                    if (sourceLabel != null) {
                        Spacer(Modifier.width(8.dp))
                        // v1.88.2: the database name chip jumps to Browse
                        // filtered to that database (tap = filter, tap
                        // elsewhere on the card = open the quote).
                        val browseDb = LocalBrowseDatabase.current
                        val tagDbId = dbId ?: sourceDbId
                        SourceChip(
                            label = sourceLabel,
                            dbId = sourceDbId,
                            onClick = if (tagDbId != null && browseDb != null) {
                                { browseDb(tagDbId) }
                            } else null
                        )
                    }
                    // v1.61: the "Translated" mark — tap to view original.
                    // v1.61.18: long-press shows both for this quote.
                    if (rowTr.showBadge || rowTr.showTranslating) {
                        Spacer(Modifier.width(8.dp))
                        TranslatedBadge(
                            showingTranslated = rowTr.showingTranslated,
                            translating = rowTr.showTranslating,
                            onToggle = rowTr.onToggleForceOriginal,
                            onLongClick = rowTr.onToggleShowBoth
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    actionButtons()
                }
                Spacer(Modifier.height(8.dp))
            }
            // v1.61: translated body text + "Both" mode original below.
            CompositionLocalProvider(
                LocalTranslatedText provides rowTr.effectiveText
            ) {
                bodyBlock()
            }
            if (rowTr.showOriginalBelow) {
                TranslationOriginalBelow(quote.text)
            }
            }
        }
    }

    @Composable
    fun modernCard(cardModifier: Modifier) {
        // v1.49: double-tap to favorite is part of the tap-gestures toggle.
        themedCard {
            // DevXen theme card treatments: the art either sits behind the
            // lists (cards go semi-transparent dark with blue borders) or
            // inside each card (dimmed for readability).
            val devXenMode = LocalDevXenBgMode.current
            // v1.61.5: the Quote404 theme draws Clint's artwork behind the
            // lists, so its cards get the same semi-transparent treatment
            // and electric-blue border as DevXen's "Behind quotes" mode.
            val quote404 = LocalQuote404Theme.current
            val devXenArtBorder = devXenMode == DevXenBgMode.BEHIND_QUOTES ||
                devXenMode == DevXenBgMode.ON_EACH_QUOTE || quote404
            Card(
                // Themeable 1dp border around each quote card; transparent (None)
                // effectively disables it. DevXen art modes use the electric
                // blue border.
                border = BorderStroke(
                    1.dp,
                    when {
                        quote404 -> Color(0xFF0090FF)
                        devXenArtBorder -> Color(0xFF087AFF)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = cardModifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (compact) 4.dp else 6.dp
                    )
                    // Single tap opens (or toggles selection), double-tap favorites,
                    // long-press enters multi-select (or toggles when active).
                    // combinedClickable is the reliable gesture handler here; the
                    // previous manual detectTapGestures inside the swipe wrapper
                    // dropped taps on some rows.
                    .then(tapGestures()),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        devXenMode == DevXenBgMode.BEHIND_QUOTES || quote404 ->
                            Color.Black.copy(alpha = 0.72f)
                        devXenMode == DevXenBgMode.ON_EACH_QUOTE -> Color.Transparent
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    }
                )
            ) {
                if (devXenMode == DevXenBgMode.ON_EACH_QUOTE) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        DevXenDimmedBackground(
                            modifier = Modifier.matchParentSize(),
                            scrimAlpha = 0.5f
                        )
                        rowContent()
                    }
                } else {
                    rowContent()
                }
            }
        }
    }

    /** v1.51: the quote rendered as a mini version of its site. */
    @Composable
    fun siteCard(cardModifier: Modifier) {
        // v1.55.1: the shared site card — the same card the detail view
        // renders when this database's site theme is on.
        SiteThemeQuoteCard(
            quote = quote,
            dbId = dbId ?: sourceDbId,
            modifier = cardModifier.then(tapGestures()),
            showNumber = showNumber,
            showRating = showRating,
            showQueuedTag = showQueuedTag,
            listIndex = listIndex,
            sourceLabel = sourceLabel,
            sourceDbId = sourceDbId,
            myRating = myRating,
            onRate = gatedRate,
            rawOnRate = onRate,
            isPinned = isPinned,
            onTogglePin = onTogglePin,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            showFavoriteAction = showFavoriteAction,
            onMoreClick = onMoreClick,
            onShareClick = onShareClick,
            onFlagClick = if (flaggingOn) {
                { showFlagDialog = true }
            } else null,
            body = { bodyBlock() }
        )
    }

    @Composable
    fun rowCard(cardModifier: Modifier) {
        // v1.85: non-Classic layouts render through the layout engine
        // (theme colors still come from the resolved palette via
        // themedCard). Classic keeps the exact pre-1.85 path.
        if (!cardLayout.isClassic) {
            themedCard {
                CompositionLocalProvider(
                    LocalTranslatedText provides rowTr.effectiveText
                ) {
                    LayoutCard(
                        quote = quote,
                        layout = cardLayout,
                        siteName = sourceLabel,
                        sourceLabel = sourceLabel,
                        sourceDbId = sourceDbId,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        showFavoriteAction = showFavoriteAction,
                        onMoreClick = onMoreClick,
                        onShareClick = onShareClick,
                        myRating = myRating,
                        onRate = gatedRate,
                        dbId = dbId,
                        isSelected = isSelected,
                        modifier = cardModifier.then(tapGestures())
                    )
                }
            }
        } else if (siteStyle == SiteStyle.MODERN_CARD) modernCard(cardModifier)
        else siteCard(cardModifier)
    }

    // Swipe wrapper: the row itself is never dismissed — the action fires
    // and the card snaps back.
    if (swipeEnabled) {
        // The swipe action only fires after the row travels ~70% of its
        // width, so ordinary vertical scrolling doesn't accidentally
        // trigger share/favorite.
        val dismissState = rememberSwipeToDismissBoxState(
            positionalThreshold = { totalDistance -> totalDistance * 0.7f },
            confirmValueChange = { value ->
                val action = when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> swipeConfig.right
                    SwipeToDismissBoxValue.EndToStart -> swipeConfig.left
                    else -> SwipeAction.NONE
                }
                val id = dbId
                if (action != SwipeAction.NONE && id != null) {
                    swipeHandler?.invoke(action, id, quote)
                }
                false
            }
        )
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = swipeConfig.right != SwipeAction.NONE,
            enableDismissFromEndToStart = swipeConfig.left != SwipeAction.NONE,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val action = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> swipeConfig.right
                    SwipeToDismissBoxValue.EndToStart -> swipeConfig.left
                    else -> null
                }
                if (action != null && action != SwipeAction.NONE) {
                    SwipeActionBackground(action = action, direction = direction)
                }
            },
            modifier = modifier
        ) {
            rowCard(Modifier)
        }
    } else {
        rowCard(modifier)
    }

    // v1.53 report/flag dialog, opened from the site header's flag
    // affordance.
    if (showFlagDialog && onFlagQuote != null) {
        FlagQuoteDialog(
            quote = quote,
            onDismiss = { showFlagDialog = false },
            onConfirm = { reason, customText ->
                showFlagDialog = false
                onFlagQuote(reason, customText)
            }
        )
    }
}

/** Background revealed while swiping a quote row: icon + label. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SwipeActionBackground(
    action: SwipeAction,
    direction: SwipeToDismissBoxValue
) {
    val (icon, bg) = when (action) {
        SwipeAction.FAVORITE -> Icons.Filled.Favorite to Color(0xFFD81B60)
        SwipeAction.SHARE -> Icons.Filled.Share to Color(0xFF1E88E5)
        SwipeAction.COPY -> Icons.Filled.ContentCopy to Color(0xFF00897B)
        SwipeAction.BASKET -> Icons.Filled.Archive to Color(0xFF5E35B1)
        SwipeAction.HIDE -> Icons.Filled.VisibilityOff to Color(0xFF616161)
        SwipeAction.UP_NEXT -> Icons.Filled.Queue to Color(0xFF00ACC1)
        SwipeAction.NEXT_RANDOM -> Icons.Filled.SkipNext to Color(0xFF43A047)
        SwipeAction.READ_ALOUD -> Icons.Filled.VolumeUp to Color(0xFFFB8C00)
        SwipeAction.NONE -> Icons.Filled.DoNotDisturb to Color.Transparent
    }
    val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd)
        Alignment.CenterStart else Alignment.CenterEnd
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                action.label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuoteDetailCard(
    quote: Quote,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
    showUrl: Boolean = true,
    categories: List<com.quotebrowser.app.data.Category> = emptyList(),
    onEditCategories: () -> Unit = {},
    sourceLabel: String? = null,
    sourceDbId: String? = null,
    inBasket: Boolean = false,
    onToggleBasket: () -> Unit = {},
    isSpeaking: Boolean = false,
    onReadAloud: () -> Unit = {},
    /**
     * v1.78: when set, a "Read along" button opens karaoke-style reading
     * with each word highlighted as it is spoken.
     */
    onReadAlong: (() -> Unit)? = null,
    onNickClick: (String) -> Unit = {},
    /** Local thumbs rating: -1 (down), 0 (none), 1 (up). */
    myRating: Int = 0,
    onRate: (Int) -> Unit = {},
    /** v1.50 pinch-to-zoom: the Settings → Features toggle. */
    pinchZoomEnabled: Boolean = false,
    /** v1.50: persisted quote-body scale; 1.0 = the user's base font size. */
    detailTextScale: Float = 1f,
    /** v1.50: called once per pinch gesture with the clamped scale. */
    onDetailTextScaleChange: (Float) -> Unit = {},
    /**
     * v1.53 report/flag: current flag state, its details for the tag, and
     * the handler invoked from the flag dialog. The header shows the
     * Flagged tag and a flag button when the feature toggle is on.
     */
    isFlagged: Boolean = false,
    flagInfo: FlagInfo? = null,
    onFlagQuote: ((reason: String, customText: String) -> Unit)? = null,
    /**
     * v1.66: inline "Explain this quote" (Gemini) button. Shown only when
     * the explain feature toggle is on.
     */
    onExplain: (() -> Unit)? = null,
    /** v1.66: "Add to list" (user quote lists / collections) button. */
    onAddToList: (() -> Unit)? = null
) {
    // v1.49 tap gestures: double-tap the quote body to favorite,
    // two-finger tap to share. Gated by the Settings toggle.
    val gesturesOn = LocalGesturesEnabled.current
    // v1.50 pinch-to-zoom state: the persisted user scale is the baseline
    // and each pinch multiplies a live value off it. Hoisted here so the
    // reset button below shares it with the gesture modifier.
    val persistedZoom = detailTextScale.coerceIn(MIN_DETAIL_ZOOM, MAX_DETAIL_ZOOM)
    var zooming by remember { mutableStateOf(false) }
    var liveZoom by remember { mutableFloatStateOf(persistedZoom) }
    // v1.53 report/flag dialog state.
    var showFlagDialogDetail by remember { mutableStateOf(false) }
    // Re-sync if the persisted value changes outside a gesture.
    LaunchedEffect(persistedZoom) {
        if (!zooming) liveZoom = persistedZoom
    }
    // v1.55.1: when this quote's database has its site theme on, the
    // detail view renders the same site card as Browse/Random (full
    // per-site header and footer); otherwise the generic detail card.
    val themeSnap = LocalDbThemeSnapshots.current
    val detailResolved = remember(themeSnap, sourceDbId) {
        resolveDbTheme(themeSnap, sourceDbId)
    }
    // v1.89.6: the rating format / vote style the quote's database
    // carries in its theme spec, for the generic detail header.
    val detailCarriedFormat = remember(themeSnap, sourceDbId) {
        sourceDbId?.let { themeSnap.builtInDbRatingFormats[it] }
    }
    val detailCarriedPlusMinus = remember(themeSnap, sourceDbId) {
        sourceDbId?.let { themeSnap.builtInDbVoteStyles[it] } == "plusminus"
    }
    val useSiteCard = !detailResolved.bypassed &&
        siteStyleFor(detailResolved.structureThemeId) != SiteStyle.MODERN_CARD
    val detailFlaggingOn = LocalFlagEnabled.current && onFlagQuote != null
    // v1.61: translation state for the generic (non-site) detail path. The
    // site-card path above goes through SiteThemeQuoteCard, which wires its
    // own translation UI from the same repository.
    val detailTr = rememberQuoteTranslation(sourceDbId, quote)
    /**
     * v1.55.1: the detail quote body (selectable text, pinch zoom,
     * nick annotations) — shared by the generic detail card and the
     * site-theme card body slot.
     */
    @Composable
    fun detailBody() {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.then(
                if (gesturesOn) Modifier
                    .onDoubleTapGesture(onToggleFavorite)
                    .onTwoFingerTap(onShare)
                else Modifier
            )
        ) {
            val detailStyle = quoteTextStyle(MaterialTheme.typography.bodyLarge)
            // v1.50 pinch-to-zoom: the user's base font size
            // (quoteTextStyle applies their text-size setting) is the 1.0x
            // reference; the live gesture scale multiplies on top of it.
            val zoomedStyle = remember(detailStyle, liveZoom) {
                if (detailStyle.fontSize != TextUnit.Unspecified)
                    detailStyle.copy(fontSize = detailStyle.fontSize * liveZoom)
                else detailStyle
            }
            val annotatedQuote = rememberNickAnnotatedText(
                // v1.61: translated body text when a translation is showing.
                translatedBodyText(quote),
                zoomedStyle
            )
            // ClickableText does not inherit the content color the way Text
            // does, so the body would render black on dark themes: pass the
            // surface's content color explicitly.
            val bodyColor = MaterialTheme.colorScheme.onSurface
            // The gesture modifier is applied conditionally so nothing is
            // consumed when the feature toggle is off.
            val zoomModifier = if (pinchZoomEnabled) {
                Modifier.pointerInput(Unit) {
                    detectPinchZoom(
                        onZoomDelta = { dz ->
                            liveZoom =
                                (liveZoom * dz).coerceIn(MIN_DETAIL_ZOOM, MAX_DETAIL_ZOOM)
                        },
                        onZoomStart = {
                            zooming = true
                            liveZoom = persistedZoom
                        },
                        // Persist once per gesture (debounced), not per frame.
                        onZoomEnd = {
                            zooming = false
                            onDetailTextScaleChange(liveZoom)
                        }
                    )
                }
            } else Modifier
            SelectionContainer {
                Box(modifier = zoomModifier) {
                    val detailUriHandler = LocalUriHandler.current
                    ClickableText(
                        text = annotatedQuote,
                        style = zoomedStyle.copy(color = bodyColor),
                        modifier = Modifier.padding(20.dp),
                        onClick = { offset ->
                            // v1.89.7: URLs open in the browser; other
                            // taps go to the nick handler as before.
                            val url = annotatedQuote.getStringAnnotations(
                                "url", offset,
                                (offset + 1).coerceAtMost(annotatedQuote.length)
                            ).firstOrNull()?.item
                            if (url != null) {
                                try {
                                    detailUriHandler.openUri(url)
                                } catch (_: Exception) {
                                }
                            } else {
                                annotatedQuote.getStringAnnotations(
                                    "nick", offset,
                                    (offset + 1).coerceAtMost(annotatedQuote.length)
                                ).firstOrNull()?.let { onNickClick(it.item) }
                            }
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // v1.85: quote layouts apply to the detail view too (lockstep with
        // the list cards). Non-Classic layouts render the quote through
        // the layout engine; the detail-only sections below (pinch reset,
        // URL, categories, action buttons) stay exactly as they were.
        val detailContext = LocalContext.current
        val detailLayout = remember(
            detailContext, sourceDbId, LayoutStore.prefsVersion
        ) {
            LayoutStore.layoutForDb(
                detailContext, sourceDbId, PluginManager.pluginLayouts
            )
        }
        if (!detailLayout.isClassic) {
            val wrapDetailPalette = !detailResolved.bypassed &&
                detailResolved.paletteThemeId != themeSnap.globalThemeId
            val detailPaletteScheme = themeColorScheme(
                detailResolved.paletteThemeId,
                themeSnap.customThemes, themeSnap.builtInOverrides
            )
            // v1.61: translated body text when a translation is showing.
            CompositionLocalProvider(
                LocalTranslatedText provides detailTr.effectiveText
            ) {
                if (!wrapDetailPalette) {
                    LayoutCard(
                        quote = quote,
                        layout = detailLayout,
                        siteName = sourceLabel,
                        sourceLabel = sourceLabel,
                        sourceDbId = sourceDbId,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        showFavoriteAction = true,
                        onMoreClick = null,
                        onShareClick = onShare,
                        myRating = myRating,
                        onRate = onRate,
                        dbId = sourceDbId
                    )
                } else {
                    MaterialTheme(colorScheme = detailPaletteScheme) {
                        LayoutCard(
                            quote = quote,
                            layout = detailLayout,
                            siteName = sourceLabel,
                            sourceLabel = sourceLabel,
                            sourceDbId = sourceDbId,
                            isFavorite = isFavorite,
                            onToggleFavorite = onToggleFavorite,
                            showFavoriteAction = true,
                            onMoreClick = null,
                            onShareClick = onShare,
                            myRating = myRating,
                            onRate = onRate,
                            dbId = sourceDbId
                        )
                    }
                }
            }
        } else if (useSiteCard) {
            // The site header already carries the quote number, rating,
            // vote controls, flag affordance and favorite; only the
            // Flagged tag needs a home above the card.
            if (detailFlaggingOn && isFlagged) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlaggedTag(
                        reason = flagInfo?.reason ?: "Other",
                        customText = flagInfo?.customText ?: ""
                    )
                }
            }
            SiteThemeQuoteCard(
                quote = quote,
                dbId = sourceDbId,
                showNumber = true,
                showRating = true,
                // v1.51: "Queued" tag on unapproved quotes, with
                // tap-for-explanation.
                showQueuedTag = LocalQueuedTagsEnabled.current &&
                    quote.status == "queued",
                sourceLabel = sourceLabel,
                sourceDbId = sourceDbId,
                myRating = myRating,
                onRate = onRate,
                // v1.77: bash.org's +/- links always show (detail matches
                // the list card).
                rawOnRate = onRate,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                showFavoriteAction = true,
                onMoreClick = null,
                // v1.61.9: one-tap share in the header, left of favorite.
                onShareClick = onShare,
                onFlagClick = if (detailFlaggingOn) {
                    { showFlagDialogDetail = true }
                } else null,
                body = {
                    detailBody()
                }
            )
            // v1.61.10: the database tag is a button on the detail site
            // card too — bottom-right, jumping to Browse filtered to that
            // database (list cards put it across from the star rating).
            if (sourceLabel != null && sourceDbId != null) {
                val browseDb = LocalBrowseDatabase.current
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    SourceChip(
                        label = sourceLabel,
                        dbId = sourceDbId,
                        onClick = browseDb?.let { browse -> { browse(sourceDbId) } }
                    )
                }
            }
        } else {
        // Header line: quote number, rating with local thumbs up/down, and
        // the source database. Always shown (the share-sheet toggles only
        // affect shared/exported output, not this screen).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuoteNumberBadge(
                number = quote.number,
                style = MaterialTheme.typography.titleMedium
            )
            // v1.89.6: a database carrying its own rating format shows
            // that text instead of the bare badge number; its +/− vote
            // links (vote style "plusminus") then replace the generic
            // thumbs below.
            if (detailCarriedFormat != null) {
                CarriedRatingDisplay(
                    format = detailCarriedFormat,
                    rating = quote.rating?.let { it + myRating },
                    plusMinus = detailCarriedPlusMinus,
                    myRating = myRating,
                    onRate = if (LocalVoteControlsEnabled.current) onRate else null
                )
            } else {
                RatingBadge(quote.rating?.let { it + myRating })
            }
            // v1.51: the "+/- vote controls" feature toggle gates the
            // offline thumbs (PersonalRatings only, no network).
            if (LocalVoteControlsEnabled.current &&
                (detailCarriedFormat.isNullOrEmpty() || !detailCarriedPlusMinus)
            ) {
                val activeTint = MaterialTheme.colorScheme.primary
                val idleTint = MaterialTheme.colorScheme.onSurfaceVariant
                IconButton(
                    onClick = { onRate(if (myRating == 1) 0 else 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ThumbUp,
                        contentDescription = "Rate this quote up",
                        tint = if (myRating == 1) activeTint else idleTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { onRate(if (myRating == -1) 0 else -1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ThumbDown,
                        contentDescription = "Rate this quote down",
                        tint = if (myRating == -1) activeTint else idleTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            // v1.51: "Queued" tag on unapproved quotes, with
            // tap-for-explanation.
            if (LocalQueuedTagsEnabled.current && quote.status == "queued") {
                QueuedTag()
            }
            // v1.53 report/flag: the Flagged tag plus a flag button that
            // opens the report dialog (the dialog says the flag stays on
            // this device and is never sent anywhere).
            val flaggingOn = LocalFlagEnabled.current && onFlagQuote != null
            if (flaggingOn && isFlagged) {
                FlaggedTag(
                    reason = flagInfo?.reason ?: "Other",
                    customText = flagInfo?.customText ?: ""
                )
            }
            if (flaggingOn) {
                IconButton(
                    onClick = { showFlagDialogDetail = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
                        contentDescription = "Report / flag quote",
                        tint = if (isFlagged) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (sourceLabel != null) {
                // v1.88.2: the database name chip jumps to Browse filtered
                // to that database, like the bottom-right tag.
                val browseDb = LocalBrowseDatabase.current
                SourceChip(
                    label = sourceLabel,
                    dbId = sourceDbId,
                    onClick = if (sourceDbId != null && browseDb != null) {
                        { browseDb(sourceDbId) }
                    } else null
                )
            }
            // v1.61: the "Translated" mark — tap to view original.
            // v1.61.18: long-press shows both for this quote.
            if (detailTr.showBadge) {
                TranslatedBadge(
                    showingTranslated = detailTr.showingTranslated,
                    translating = false,
                    onToggle = detailTr.onToggleForceOriginal,
                    onLongClick = detailTr.onToggleShowBoth
                )
            } else if (detailTr.showTranslating) {
                TranslatedBadge(
                    showingTranslated = false,
                    translating = true,
                    onToggle = {}
                )
            }
        }
        // Themed divider separating the header from the quote body.
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        // v1.61: translated body text + "Both" mode original below.
        CompositionLocalProvider(
            LocalTranslatedText provides detailTr.effectiveText
        ) {
            detailBody()
        }
        if (detailTr.showOriginalBelow) {
            TranslationOriginalBelow(quote.text)
        }
        } // else: generic detail card
        // v1.50: reset affordance for pinch-zoom. Double-tap is already
        // favorite and long-press selects text, so a small explicit button
        // is the non-conflicting option; it only shows while zoomed.
        if (pinchZoomEnabled && liveZoom != 1f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    liveZoom = 1f
                    onDetailTextScaleChange(1f)
                }) {
                    Text("Reset text size")
                }
            }
        }
        if (showUrl) {
            val context = LocalContext.current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { openWebLink(context, quote.url) }
            ) {
                Icon(
                    Icons.Filled.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = quote.url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        CategoryChips(
            categories = categories,
            onEditClick = onEditCategories
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailActionButton(
                onClick = onToggleFavorite,
                icon = if (isFavorite) Icons.Filled.Favorite
                else Icons.Outlined.FavoriteBorder,
                label = if (isFavorite) "Saved" else "Favorite"
            )
            DetailActionButton(
                onClick = onCopy,
                icon = Icons.Filled.ContentCopy,
                label = "Copy"
            )
            DetailActionButton(
                onClick = onShare,
                icon = Icons.Filled.Share,
                label = "Share"
            )
            DetailActionButton(
                onClick = onExport,
                icon = Icons.Filled.Download,
                label = "Export"
            )
            DetailActionButton(
                onClick = onToggleBasket,
                icon = if (inBasket) Icons.Filled.PlaylistAddCheck
                else Icons.Filled.PlaylistAdd,
                label = if (inBasket) "Queued" else "Queue"
            )
            DetailActionButton(
                onClick = onReadAloud,
                icon = if (isSpeaking) Icons.Filled.StopCircle
                else Icons.Filled.PlayCircle,
                label = if (isSpeaking) "Stop" else "Read aloud"
            )
            // v1.78: karaoke-style read-along with word highlighting.
            if (onReadAlong != null) {
                DetailActionButton(
                    onClick = onReadAlong,
                    icon = Icons.Filled.ClosedCaption,
                    label = "Read along"
                )
            }
            // v1.66: inline Gemini explanation (same dialog as the
            // long-press sheet's "Explain this quote" row).
            if (onExplain != null && LocalExplainEnabled.current) {
                DetailActionButton(
                    onClick = onExplain,
                    icon = Icons.Filled.Lightbulb,
                    label = "Explain"
                )
            }
            // v1.66: add to a named user quote list ("Collections").
            if (onAddToList != null) {
                DetailActionButton(
                    onClick = onAddToList,
                    icon = Icons.Filled.Layers,
                    label = "Add to list"
                )
            }
        }
        // v1.58: quiet "View original" browser link — only when the quote
        // has a web address. Independent of the "Show web links" setting
        // (which shows the raw URL); blank URLs never show a link.
        if (quote.url.isNotBlank()) {
            val viewContext = LocalContext.current
            Row(
                modifier = Modifier.clickable { openWebLink(viewContext, quote.url) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View original",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    // v1.53 report/flag dialog.
    if (showFlagDialogDetail && onFlagQuote != null) {
        FlagQuoteDialog(
            quote = quote,
            onDismiss = { showFlagDialogDetail = false },
            onConfirm = { reason, customText ->
                showFlagDialogDetail = false
                onFlagQuote(reason, customText)
            }
        )
    }
}

@Composable
private fun DetailActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    FilledTonalButton(onClick = onClick) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text(label, maxLines = 1)
    }
}

/** Consistent screen header: bold headline title with optional actions. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/**
 * v1.57: themed empty state — the no-quotes / not-found panel follows the
 * active theme via the chrome accent (a tinted rounded panel) instead of
 * the old generic bare icon + text.
 */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val tint = LocalChromeAccent.current ?: MaterialTheme.colorScheme.primary
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(tint.copy(alpha = 0.08f))
                .border(
                    BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
                    RoundedCornerShape(24.dp)
                )
                .padding(32.dp)
        ) {
            Icon(
                Icons.Filled.FormatQuote,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/** Long-press quick actions for a quote row: Copy, Share, Favorite, Open. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteActionSheetHost(
    quote: Quote?,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit,
    dbId: String,
    inBasket: Boolean,
    onToggleBasket: () -> Unit,
    modifier: Modifier = Modifier,
    /** Favorite folders; when onMoveToFolder is set a "Move to folder…" row appears. */
    folders: List<String> = emptyList(),
    currentFolder: String? = null,
    onMoveToFolder: ((String?) -> Unit)? = null,
    /** When set, a "Never show again" row hides this quote from every list. */
    onHideQuote: (() -> Unit)? = null,
    /**
     * v1.66: when set, an "Add to list…" row opens the quote-list picker
     * for this quote.
     */
    onAddToList: (() -> Unit)? = null,
    /**
     * v1.74.7: when set, a "Read aloud" row reads this quote with the
     * text-to-speech playlist (same as the card speaker button).
     */
    onReadAloud: (() -> Unit)? = null,
    /**
     * v1.53 report/flag: current flag state and handlers.
     * v1.61.5: the flag confirm runs after the sheet item is cleared, so
     * the database id and quote number are passed in (captured at tap
     * time) instead of being re-read by the caller.
     */
    isFlagged: Boolean = false,
    onFlagQuote: ((dbId: String, number: Int, reason: String, customText: String) -> Unit)? = null,
    onUnflagQuote: (() -> Unit)? = null
) {
    val q = quote
    var shareTarget by remember { mutableStateOf<SourcedQuote?>(null) }
    var showFolderPicker by remember { mutableStateOf(false) }
    // v1.53 "Explain this quote" target.
    // v1.75: SourcedQuote so the explanation can be cached per database.
    var explainTarget by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.78 "Read along" target: karaoke-style reading with word highlighting.
    var readAlongTarget by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.53 report/flag dialog target.
    var flagTarget by remember { mutableStateOf<SourcedQuote?>(null) }
    // v1.61.18: per-quote translate language picker.
    var showTranslateLangPicker by remember { mutableStateOf(false) }
    val sheetContext = LocalContext.current

    if (q != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            Column(Modifier.padding(bottom = 28.dp)) {
                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QuoteNumberBadge(number = q.number)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "rating ${q.rating?.toString() ?: "—"}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    supportingContent = {
                        Text(
                            text = q.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        onOpen()
                        onDismiss()
                    }
                )
                HorizontalDivider()
                ActionSheetRow(
                    icon = Icons.Filled.ContentCopy,
                    label = "Copy",
                    onClick = { shareTarget = SourcedQuote(dbId, q); onDismiss() }
                )
                ActionSheetRow(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    onClick = { shareTarget = SourcedQuote(dbId, q); onDismiss() }
                )
                // v1.74.7: "Read aloud" — the card speaker action, in the
                // menu too.
                if (onReadAloud != null) {
                    ActionSheetRow(
                        icon = Icons.Filled.VolumeUp,
                        label = "Read aloud",
                        onClick = { onReadAloud(); onDismiss() }
                    )
                }
                // v1.78: "Read along" — karaoke-style reading with each word
                // highlighted as it is spoken.
                ActionSheetRow(
                    icon = Icons.Filled.ClosedCaption,
                    label = "Read along",
                    onClick = { readAlongTarget = SourcedQuote(dbId, q); onDismiss() }
                )
                // v1.58: quiet "View original" browser link — only when the
                // quote has a web address (bundled Bash.org/qdb.us records
                // do; many other archives leave it blank, and no empty
                // link is ever shown).
                if (q.url.isNotBlank()) {
                    ActionSheetRow(
                        icon = Icons.Filled.OpenInNew,
                        label = "View original",
                        onClick = { openWebLink(sheetContext, q.url); onDismiss() }
                    )
                }
                // v1.53 "Explain this quote": Gemini breaks down the obscure
                // references and in-jokes. v1.78: the dialog grew Roast and
                // Rhyme tabs — the label names all three. Gated by the
                // feature toggle.
                if (LocalExplainEnabled.current) {
                    ActionSheetRow(
                        icon = Icons.Filled.AutoAwesome,
                        label = "Explain / Roast / Rhyme",
                        onClick = { explainTarget = SourcedQuote(dbId, q); onDismiss() }
                    )
                }
                // v1.61: per-quote translation. Cached in translations.db
                // (translated once per target language); the card's
                // "Translated" badge flips between translation and original.
                // v1.61.18: opens a language picker — the quote is translated
                // into the chosen language.
                run {
                    val transRepo = LocalTranslations.current
                    if (transRepo != null) {
                        ActionSheetRow(
                            icon = Icons.Filled.Translate,
                            label = "Translate",
                            onClick = { showTranslateLangPicker = true }
                        )
                    }
                }
                ActionSheetRow(
                    icon = if (isFavorite) Icons.Filled.Favorite
                    else Icons.Outlined.FavoriteBorder,
                    label = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    onClick = { onToggleFavorite(); onDismiss() }
                )
                if (onMoveToFolder != null) {
                    ActionSheetRow(
                        icon = Icons.Filled.DriveFileMove,
                        label = if (currentFolder != null) "Move to folder… ($currentFolder)"
                        else "Move to folder…",
                        onClick = { showFolderPicker = true }
                    )
                }
                ActionSheetRow(
                    icon = if (inBasket) Icons.Filled.PlaylistAddCheck
                    else Icons.Filled.PlaylistAdd,
                    label = if (inBasket) "Remove from export tray"
                    else "Add to export tray",
                    onClick = { onToggleBasket(); onDismiss() }
                )
                // v1.66: user quote lists ("Collections").
                if (onAddToList != null) {
                    ActionSheetRow(
                        icon = Icons.Filled.Layers,
                        label = "Add to list…",
                        onClick = { onAddToList(); onDismiss() }
                    )
                }
                if (onHideQuote != null) {
                    ActionSheetRow(
                        icon = Icons.Filled.VisibilityOff,
                        label = "Never show again",
                        onClick = { onHideQuote(); onDismiss() }
                    )
                }
                // v1.53 report/flag: flag with a reason, or remove the flag.
                // Gated by the feature toggle; flags stay on this device.
                if (LocalFlagEnabled.current && onFlagQuote != null) {
                    if (isFlagged && onUnflagQuote != null) {
                        ActionSheetRow(
                            icon = Icons.Filled.Flag,
                            label = "Remove flag",
                            onClick = { onUnflagQuote(); onDismiss() }
                        )
                    } else if (!isFlagged) {
                        ActionSheetRow(
                            icon = Icons.Outlined.Flag,
                            label = "Report / flag",
                            onClick = { flagTarget = SourcedQuote(dbId, q); onDismiss() }
                        )
                    }
                }
            }
        }
    }

    if (showTranslateLangPicker && q != null) {
        val transRepo = LocalTranslations.current
        if (transRepo != null) {
            TranslateLanguageDialog(
                repo = transRepo,
                onDismiss = { showTranslateLangPicker = false },
                onSelect = { code ->
                    // v1.61.18: the card shows the translation for the
                    // current target language, so the pick becomes the new
                    // target; the translation is cached per language.
                    transRepo.setTargetLang(code)
                    transRepo.translateAsync(dbId, q.number, q.text)
                    showTranslateLangPicker = false
                    onDismiss()
                }
            )
        } else {
            showTranslateLangPicker = false
        }
    }

    if (showFolderPicker && q != null && onMoveToFolder != null) {
        FolderPickerDialog(
            folders = folders,
            currentFolder = currentFolder,
            onDismiss = { showFolderPicker = false },
            onSelect = { folder ->
                onMoveToFolder(folder)
                showFolderPicker = false
                onDismiss()
            }
        )
    }

    shareTarget?.let { target ->
        ShareOptionsDialog(
            quote = target.quote,
            onDismiss = { shareTarget = null },
            // v1.61.5: the database captured when Share was tapped — the
            // sheet item is cleared on dismiss, so recomputing dbId here
            // would lose it (Random/Home showed no Quote theme tile).
            dbId = target.dbId
        )
    }

    explainTarget?.let { target ->
        ExplainQuoteDialog(
            quote = target.quote,
            onDismiss = { explainTarget = null },
            dbId = target.dbId
        )
    }

    readAlongTarget?.let { target ->
        ReadAlongDialog(
            dbId = target.dbId,
            number = target.quote.number,
            displayText = target.quote.text,
            onDismiss = { readAlongTarget = null }
        )
    }

    flagTarget?.let { target ->
        FlagQuoteDialog(
            quote = target.quote,
            onDismiss = { flagTarget = null },
            onConfirm = { reason, customText ->
                flagTarget = null
                // v1.61.5: the sheet item is already cleared when the
                // deferred confirm runs — pass the captured identity.
                onFlagQuote?.invoke(target.dbId, target.quote.number, reason, customText)
            }
        )
    }
}

/**
 * v1.61.18: per-quote translate language picker. Choosing a language makes
 * it the "Translate into" target (the card shows the translation for the
 * current target) and translates the quote into it. Translations are cached
 * per language in translations.db, so each language is fetched once.
 */
@Composable
fun TranslateLanguageDialog(
    repo: TranslationRepository,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTarget by repo.targetLang.collectAsStateWithLifecycle()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Translate into") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TranslationRepository.LANGUAGES.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == currentTarget,
                            onClick = { onSelect(code) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * v1.53 "Explain this quote" dialog: asks Gemini for a plain-English
 * breakdown of the quote's references and in-jokes. Shows a friendly
 * message when no API key is stored or the device is offline.
 *
 * v1.78: grew a tab row — Explain | Roast | Rhyme. Explain keeps the
 * v1.75 on-device cache; Roast and Rhyme are fetched fresh every time
 * the tab is opened and are never cached.
 */
private enum class GeminiDialogMode { EXPLAIN, ROAST, RHYME }

@Composable
fun ExplainQuoteDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    // v1.75: source database id, so the explanation can be cached per
    // quote ("dbId|number") and re-opened instantly without re-fetching.
    dbId: String? = null
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(GeminiDialogMode.EXPLAIN) }
    var explainResult by remember(quote.number, quote.text) {
        mutableStateOf<ExplainResult?>(null)
    }
    // v1.78: roast/rhyme fetch state — fetched lazily on first tab open,
    // re-fetched per quote, never cached.
    var roastRequested by remember(quote.number, quote.text) { mutableStateOf(false) }
    var roastResult by remember(quote.number, quote.text) {
        mutableStateOf<ExplainResult?>(null)
    }
    var rhymeRequested by remember(quote.number, quote.text) { mutableStateOf(false) }
    var rhymeResult by remember(quote.number, quote.text) {
        mutableStateOf<ExplainResult?>(null)
    }
    LaunchedEffect(quote.number, quote.text) {
        // v1.75: serve a saved explanation instantly when one exists;
        // only hit the network on a cache miss.
        val cached = ExplanationCache.get(dbId, quote.number)
        explainResult = if (cached != null) {
            ExplainResult.Ok(cached)
        } else {
            when (val r = explainQuote(context, quote.text)) {
                is ExplainResult.Ok -> {
                    // Cache only successful explanations.
                    if (dbId != null) {
                        ExplanationCache.save(context, dbId, quote.number, r.text)
                    }
                    r
                }
                else -> r
            }
        }
    }
    // v1.78: roast and rhyme are fetched on demand (tab first open) and
    // never touch ExplanationCache — every open is a fresh take.
    LaunchedEffect(mode, quote.number, quote.text) {
        if (mode == GeminiDialogMode.ROAST && !roastRequested) {
            roastRequested = true
            roastResult = roastQuote(context, quote.text)
        }
        if (mode == GeminiDialogMode.RHYME && !rhymeRequested) {
            rhymeRequested = true
            rhymeResult = rhymeQuote(context, quote.text)
        }
    }
    val title = when (mode) {
        GeminiDialogMode.EXPLAIN -> "Explain quote #${quote.number}"
        GeminiDialogMode.ROAST -> "Roast quote #${quote.number}"
        GeminiDialogMode.RHYME -> "Rhyme quote #${quote.number}"
    }
    val result = when (mode) {
        GeminiDialogMode.EXPLAIN -> explainResult
        GeminiDialogMode.ROAST -> roastResult
        GeminiDialogMode.RHYME -> rhymeResult
    }
    val workingText = when (mode) {
        GeminiDialogMode.EXPLAIN -> "Asking Gemini…"
        GeminiDialogMode.ROAST -> "Sharpening the knives…"
        GeminiDialogMode.RHYME -> "Finding the rhymes…"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Column {
                Text(title)
                if (mode != GeminiDialogMode.EXPLAIN) {
                    Text(
                        text = "Fresh every time — never cached",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column {
                TabRow(
                    selectedTabIndex = mode.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GeminiDialogMode.values().forEach { m ->
                        Tab(
                            selected = m == mode,
                            onClick = { mode = m },
                            text = {
                                Text(
                                    when (m) {
                                        GeminiDialogMode.EXPLAIN -> "Explain"
                                        GeminiDialogMode.ROAST -> "Roast"
                                        GeminiDialogMode.RHYME -> "Rhyme"
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                when (val r = result) {
                    null -> Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(workingText)
                    }
                    is ExplainResult.Ok -> Text(r.text)
                    is ExplainResult.NeedKey -> Text(r.message)
                    is ExplainResult.Error -> Text(r.message)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/** Preset reasons offered by the v1.53 report/flag dialog. */
val FLAG_REASONS = listOf(
    "Offensive content",
    "Spam/garbage",
    "Wrong database",
    "Duplicate quote",
    "Broken formatting",
    "Other"
)

/**
 * v1.53 report/flag dialog: pick a preset reason and/or type your own.
 * Flags are stored on this device only — the dialog says so plainly,
 * since these are dead archived sites with nobody to report to.
 */
@Composable
fun FlagQuoteDialog(
    quote: Quote,
    initialReason: String = "Offensive content",
    onDismiss: () -> Unit,
    onConfirm: (reason: String, customText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var reason by remember { mutableStateOf(initialReason) }
    var customText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Flag quote #${quote.number}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Why are you flagging this quote?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FLAG_REASONS.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = preset }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reason == preset,
                            onClick = { reason = preset }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(preset, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    label = { Text("Your own reason (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Flags are stored only on this device — they are " +
                        "never sent anywhere.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason, customText.trim()) }
            ) { Text("Flag quote") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * "Flagged" tag shown on quotes the user flagged (v1.53). Tapping shows
 * the recorded reason.
 */
@Composable
fun FlaggedTag(
    reason: String,
    customText: String,
    modifier: Modifier = Modifier
) {
    var showInfo by remember { mutableStateOf(false) }
    AssistChip(
        onClick = { showInfo = true },
        label = { Text("Flagged") },
        leadingIcon = {
            Icon(
                Icons.Filled.Flag,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier
    )
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Flagged quote") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Reason: $reason",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (customText.isNotBlank()) {
                        Text(
                            text = "Note: $customText",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "Stored only on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Close") }
            }
        )
    }
}

/** Picks which favorite folder a quote belongs to (or none). */
@Composable
fun FolderPickerDialog(
    folders: List<String>,
    currentFolder: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to folder") },
        text = {
            Column {
                FolderPickerRow(
                    label = "No folder",
                    selected = currentFolder == null,
                    onClick = { onSelect(null) }
                )
                folders.forEach { folder ->
                    FolderPickerRow(
                        label = folder,
                        selected = folder == currentFolder,
                        onClick = { onSelect(folder) }
                    )
                }
                if (folders.isEmpty()) {
                    Text(
                        text = "No folders yet — create one from the Favorites tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FolderPickerRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.CheckCircle
            else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ActionSheetRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/** Share sheet: quote text is always included; number/rating/URL are optional. */
@Composable
fun ShareOptionsDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    categories: List<com.quotebrowser.app.data.Category> = emptyList(),
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    dbId: String? = null
) {
    val context = LocalContext.current
    var includeNumber by remember { mutableStateOf(false) }
    var includeRating by remember { mutableStateOf(false) }
    var includeUrl by remember { mutableStateOf(false) }
    var stripUsernames by remember { mutableStateOf(false) }
    var spaceOutLines by remember { mutableStateOf(false) }
    var includeCategories by remember { mutableStateOf(false) }
    var showCardPicker by remember { mutableStateOf(false) }
    val categoryNames = categories.map { it.name }
    val options = ShareOptions(includeNumber, includeRating, includeUrl, stripUsernames, includeCategories, spaceOutLines)
    val shareScope = rememberCoroutineScope()
    // v1.58: the site-themed image card is the default share format (the
    // v1.53 site-cards feature gate stays ON by default); plain text
    // remains one tap away. The card mirrors the on-screen site card —
    // same style, footer totals, and capture-date line.
    val siteCardsOn = LocalSiteShareCardsEnabled.current
    val outsideBlack = LocalBlackOutsideQuotes.current
    val shareDbTotal = LocalDbTotals.current[dbId]
    val shareServedDate = dbId?.let { LocalServedDates.current["$it/${quote.number}"] }
    val siteStyle = if (siteCardsOn && dbId != null) {
        val snap = LocalDbThemeSnapshots.current
        val resolved = resolveDbTheme(snap, dbId)
        if (resolved.bypassed) SiteStyle.MODERN_CARD
        else siteStyleFor(resolved.structureThemeId)
    } else {
        SiteStyle.MODERN_CARD
    }
    // v1.89.6: the rating format the quote's database carries in its
    // theme spec, so the shared image's rating text matches the screen.
    val shareRatingFormat = dbId?.let {
        LocalDbThemeSnapshots.current.builtInDbRatingFormats[it]
    }
    val siteCardAvailable = siteStyle != SiteStyle.MODERN_CARD
    // v1.61.5: the effective per-database "Quote background" behind the
    // quote body — the user's pick when set, the site theme's authentic
    // color otherwise. Mirrors the on-screen site card.
    val shareQuoteBackgroundArgb = if (dbId != null) {
        val snap = LocalDbThemeSnapshots.current
        quoteBackgroundForDb(resolveDbTheme(snap, dbId), dbId, snap).toArgb()
    } else {
        null
    }
    var shareAsCard by remember(siteCardAvailable) { mutableStateOf(siteCardAvailable) }
    // v1.61.5: nick colorization for the site-theme image card, "with the
    // option to do colorizations or not" — defaults to the global nick
    // coloring setting, like the on-screen site card.
    val shareNickPalette = LocalNickPalette.current
    var colorizeNicks by remember { mutableStateOf(shareNickPalette.enabled) }
    // v1.88.24: ambient composition locals for the novelty-theme export
    // path, so the shared card matches the on-screen one.
    val shareNoveltyLocals = rememberNoveltyExportLocals()

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Copy or share quote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (siteCardAvailable) {
                    Text(
                        text = "Format",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { shareAsCard = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = shareAsCard,
                            onClick = { shareAsCard = true }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Image card (site theme)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { shareAsCard = false }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !shareAsCard,
                            onClick = { shareAsCard = false }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Plain text",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = "Quote text is always included.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ShareOptionRow(
                    label = "Quote number",
                    checked = includeNumber,
                    onCheckedChange = { includeNumber = it }
                )
                ShareOptionRow(
                    label = "Rating",
                    checked = includeRating,
                    onCheckedChange = { includeRating = it }
                )
                ShareOptionRow(
                    label = "Web address",
                    checked = includeUrl,
                    onCheckedChange = { includeUrl = it }
                )
                // v1.61.5: the site-theme image card can colorize
                // nicknames like the on-screen card — or leave them plain.
                if (siteCardAvailable && shareAsCard) {
                    ShareOptionRow(
                        label = "Colorize nicknames",
                        checked = colorizeNicks,
                        onCheckedChange = { colorizeNicks = it }
                    )
                }
                ShareOptionRow(
                    label = "Strip usernames (<nick>, nick:)",
                    checked = stripUsernames,
                    onCheckedChange = { stripUsernames = it }
                )
                ShareOptionRow(
                    label = "Space out lines (blank line between each)",
                    checked = spaceOutLines,
                    onCheckedChange = { spaceOutLines = it }
                )
                if (categories.isNotEmpty()) {
                    ShareOptionRow(
                        label = "Categories (${categories.joinToString(", ") { it.name }})",
                        checked = includeCategories,
                        onCheckedChange = { includeCategories = it }
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showCardPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share as designed image…")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (shareAsCard && siteCardAvailable) {
                    shareScope.launch {
                        renderAndShareSiteQuoteCard(
                            context, quote, siteStyle,
                            // v1.61.4: the theme card always carries the
                            // quote's number and rating — they're part of
                            // the site theme's look. The checkboxes below
                            // still control plain-text and designed cards.
                            includeNumber = true,
                            includeRating = true,
                            // v1.61.5: the "Web address" checkbox applies
                            // to the image card too.
                            includeUrl = includeUrl,
                            ircLineBreaks = ircLineBreaks,
                            dialogueLineBreaks = dialogueLineBreaks,
                            outsideBlack = outsideBlack,
                            dbTotal = shareDbTotal,
                            servedDate = shareServedDate,
                            quoteBackgroundArgb = shareQuoteBackgroundArgb,
                            colorizeNicks = colorizeNicks,
                            nickPalette = shareNickPalette,
                            noveltyLocals = shareNoveltyLocals,
                            ratingFormat = shareRatingFormat
                        )
                    }
                } else {
                    shareQuote(context, quote, options, categoryNames)
                }
                onDismiss()
            }) {
                Text(if (shareAsCard && siteCardAvailable) "Share image" else "Share")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                copyQuote(context, quote, options, categoryNames)
                onDismiss()
            }) {
                Text("Copy")
            }
        }
    )

    if (showCardPicker) {
        QuoteCardStyleDialog(
            quote = quote,
            includeNumber = includeNumber,
            includeRating = includeRating,
            includeUrl = includeUrl,
            ircLineBreaks = ircLineBreaks,
            dialogueLineBreaks = dialogueLineBreaks,
            onDismiss = { showCardPicker = false },
            dbId = dbId,
            shareScope = shareScope
        )
    }
}

@Composable
private fun ShareOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Database switcher pill shown in the top app bar. */
// ---------------------------------------------------------------------------
// v1.57 chrome: rounded, accent-tinted menus, dialogs, and info panels.
// ---------------------------------------------------------------------------

/**
 * v1.57: a database's accent color for chrome tinting (the database pill,
 * the Shuffle button, the active bottom tab): the site's signature bar
 * color. Returns null when the database has no site accent (bypassed /
 * global theme, unknown id, or the ALL sentinel) — callers stay neutral
 * then.
 */
fun dbAccentColor(snap: DbThemeSnapshots, dbId: String?): androidx.compose.ui.graphics.Color? {
    if (dbId == null || dbId == DatabaseManager.ALL_DBS_ID) return null
    val resolved = resolveDbTheme(snap, dbId)
    if (resolved.bypassed) return null
    val style = siteStyleFor(resolved.structureThemeId)
    // v1.89.2: Friends has two palettes — Sunset Hangout keeps its deep
    // plum accent; the wall default uses classic Facebook blue (which is
    // also siteBarColor's FRIENDS value).
    if (style == SiteStyle.FRIENDS && resolved.structureThemeId == "db_friends") {
        return androidx.compose.ui.graphics.Color(0xFF1E1033)
    }
    return siteBarColor(style)
}

/**
 * v1.57: the chrome accent for the current screen, provided by screens
 * that tint their chrome to the selected database (null = neutral theme
 * chrome). Read by [AccentDropdownMenu], [AccentAlertDialog], and the
 * themed [EmptyState].
 */
val LocalChromeAccent = compositionLocalOf<androidx.compose.ui.graphics.Color?> { null }

/**
 * v1.57: Material3 1.2.1 keeps its menu position provider internal, so
 * AccentDropdownMenu ships its own, mirroring the stock algorithm: below
 * the anchor when there's room (above/centered otherwise),
 * start-aligned in LTR, kept on-screen with a 48.dp margin.
 */
private class AccentMenuPositionProvider(
    val density: Density,
    val onPositionCalculated: (parentBounds: IntRect, menuBounds: IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val verticalMargin = with(density) { 48.dp.roundToPx() }
        val toRight = anchorBounds.left
        val toLeft = anchorBounds.right - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                if (anchorBounds.left >= 0) toDisplayRight else 0
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                if (anchorBounds.right <= windowSize.width) 0 else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft
        val toBottom = maxOf(anchorBounds.bottom, verticalMargin)
        val toTop = anchorBounds.top - popupContentSize.height
        val toCenter = anchorBounds.top - popupContentSize.height / 2
        val toDisplayBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
            it >= verticalMargin &&
                it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: toTop
        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
        )
        return IntOffset(x, y)
    }
}

/**
 * v1.57: scale-animation pivot for [AccentDropdownMenu], mirroring M3's
 * (internal) calculateTransformOrigin: the menu grows from the anchor edge.
 */
private fun accentTransformOrigin(parentBounds: IntRect, menuBounds: IntRect): TransformOrigin {
    val pivotX = when {
        menuBounds.left >= parentBounds.right -> 0f
        menuBounds.right <= parentBounds.left -> 1f
        menuBounds.width == 0 -> 0f
        else -> {
            val intersectionCenter =
                (max(parentBounds.left, menuBounds.left) +
                    min(parentBounds.right, menuBounds.right)) / 2
            (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
        }
    }
    val pivotY = when {
        menuBounds.top >= parentBounds.bottom -> 0f
        menuBounds.bottom <= parentBounds.top -> 1f
        menuBounds.height == 0 -> 0f
        else -> {
            val intersectionCenter =
                (max(parentBounds.top, menuBounds.top) +
                    min(parentBounds.bottom, menuBounds.bottom)) / 2
            (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
        }
    }
    return TransformOrigin(pivotX, pivotY)
}

/**
 * v1.57: overflow menu with rounded corners and an accent-tinted surface.
 * Drop-in replacement for Material3 DropdownMenu on user-visible menus.
 */
@Composable
fun AccentDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color? = LocalChromeAccent.current,
    content: @Composable ColumnScope.() -> Unit
) {
    val tint = accent ?: MaterialTheme.colorScheme.primary
    val container = tint.copy(alpha = 0.10f)
        .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh)
    // v1.57: the container is a custom accent mix, so pin item text/icons
    // to a real contrast color derived from it.
    val onContainer = contentOn(container)

    // v1.57: Material3 1.2.1's DropdownMenu has no shape/containerColor
    // parameters (those arrived in 1.3.x), so this reimplements the stock
    // menu — Popup + open/close animation + tinted Surface — on public
    // M3 1.2.1 APIs (DropdownMenuPositionProvider,
    // calculateTransformOrigin), mirroring MenuKt's DropdownMenu.
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded
    if (expandedStates.currentState || expandedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider = remember(density) {
            AccentMenuPositionProvider(
                density = density,
                onPositionCalculated = { parentBounds, menuBounds ->
                    transformOriginState.value =
                        accentTransformOrigin(parentBounds, menuBounds)
                }
            )
        }
        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = popupPositionProvider,
            properties = PopupProperties(focusable = true),
        ) {
            val transition = updateTransition(expandedStates, "AccentDropdownMenu")
            val scale by transition.animateFloat(
                transitionSpec = {
                    if (false isTransitioningTo true) {
                        tween(
                            durationMillis = 120,
                            easing = LinearOutSlowInEasing
                        )
                    } else {
                        tween(durationMillis = 1, delayMillis = 75 - 1)
                    }
                },
                label = "scale"
            ) { if (it) 1f else 0.8f }
            val alpha by transition.animateFloat(
                transitionSpec = {
                    if (false isTransitioningTo true) {
                        tween(durationMillis = 30)
                    } else {
                        tween(durationMillis = 75)
                    }
                },
                label = "alpha"
            ) { if (it) 1f else 0f }
            Surface(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    transformOrigin = transformOriginState.value
                },
                shape = RoundedCornerShape(20.dp),
                color = container,
                contentColor = onContainer,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
            ) {
                Column(
                    modifier = modifier
                        .padding(vertical = 8.dp)
                        .width(IntrinsicSize.Max)
                        .verticalScroll(rememberScrollState()),
                    content = {
                        CompositionLocalProvider(
                            LocalContentColor provides onContainer
                        ) {
                            content()
                        }
                    }
                )
            }
        }
    }
}

/**
 * v1.57: dialog with rounded corners and an accent-tinted surface.
 * Drop-in replacement for Material3 AlertDialog on user-visible dialogs.
 */
@Composable
fun AccentAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    accent: androidx.compose.ui.graphics.Color? = LocalChromeAccent.current,
) {
    val tint = accent ?: MaterialTheme.colorScheme.primary
    val container = tint.copy(alpha = 0.10f)
        .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh)
    // v1.57: the container is a custom accent mix, so the theme's
    // onSurface/onSurfaceVariant can wash out on it (pale labels on the
    // tan container) — derive real contrast colors from the container.
    val onContainer = contentOn(container)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = RoundedCornerShape(28.dp),
        containerColor = container,
        titleContentColor = onContainer,
        textContentColor = onContainer,
    )
}

@Composable
fun DbSwitcher(
    currentLabel: String,
    databases: List<DbInfo>,
    onSelect: (String) -> Unit,
    onImportClick: () -> Unit,
    onInfoClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    // v1.59: quick per-database theme picker. When [onDbThemeConfigChange]
    // is non-null, each real database row gains a palette IconButton that
    // opens the minimal theme editor dialog for that database without
    // leaving the switcher flow. Null (default) = previous behavior.
    dbThemeConfigs: Map<String, DatabaseThemeConfig> = emptyMap(),
    customThemes: List<CustomTheme> = emptyList(),
    onDbThemeConfigChange: ((String, DatabaseThemeConfig) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    // v1.59: id of the database whose quick theme dialog is open (the
    // switcher menu stays open underneath — the dialog is a separate
    // window above the menu popup).
    var themePickerDbId by remember { mutableStateOf<String?>(null) }
    Box(modifier = modifier) {
        FilledTonalButton(onClick = { expanded = true }) {
            Text(
                text = currentLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch database")
        }
        AccentDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // v1.58: the menu's real contrast color, captured outside the
            // items (M3's item colors would otherwise override it inside
            // the slots); the old onSurfaceVariant washed out on
            // tan/orange accent menus.
            val menuContentColor = LocalContentColor.current
            databases.forEach { db ->
                DropdownMenuItem(
                    text = {
                        Text(
                            buildString {
                                append(db.displayTitle())
                                if (db.quoteCount != null) append(" (${db.quoteCount})")
                            }
                        )
                    },
                    onClick = { expanded = false; onSelect(db.id) },
                    leadingIcon = {
                        Icon(Icons.Filled.Storage, contentDescription = null)
                    },
                    // v1.55: info affordance per database — opens the stats panel.
                    // v1.59: optional quick-theme palette button (kept next
                    // to the info button); the menu stays open underneath.
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onDbThemeConfigChange != null &&
                                db.id != DatabaseManager.ALL_DBS_ID
                            ) {
                                IconButton(
                                    onClick = { themePickerDbId = db.id }
                                ) {
                                    Icon(
                                        Icons.Filled.Palette,
                                        contentDescription = "Database theme",
                                        tint = menuContentColor
                                    )
                                }
                            }
                            IconButton(
                                onClick = { expanded = false; onInfoClick(db.id) }
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Database info",
                                    tint = menuContentColor
                                )
                            }
                        }
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Import database…") },
                onClick = { expanded = false; onImportClick() },
                leadingIcon = {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            )
        }
    }
    // v1.59: quick theme dialog for the chosen database. Color slots,
    // content mode, and the background image picker stay in Settings.
    val pickerDb = themePickerDbId?.let { id -> databases.find { it.id == id } }
    if (pickerDb != null && onDbThemeConfigChange != null) {
        DbQuickThemeDialog(
            db = pickerDb,
            config = dbThemeConfigs[pickerDb.id] ?: DatabaseThemeConfig(),
            customThemes = customThemes,
            onConfigChange = { onDbThemeConfigChange(pickerDb.id, it) },
            onDismiss = { themePickerDbId = null }
        )
    }
}

/**
 * v1.59: quick per-database theme picker, opened from the DbSwitcher
 * palette button. A minimal subset of the Settings "Database themes"
 * row (SettingsScreen.DbThemeRow, which is private to that file):
 * enable switch, theme dropdown (site default / presets / custom),
 * site-colors toggle, and layout picker — all wired straight to the
 * per-database theme setters. Quote/page color slots, content mode,
 * the centered-logo toggle, and the background image picker stay in
 * Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbQuickThemeDialog(
    db: DbInfo,
    config: DatabaseThemeConfig,
    customThemes: List<CustomTheme>,
    onConfigChange: (DatabaseThemeConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var themeMenuOpen by remember { mutableStateOf(false) }
    val siteThemeId = siteThemeFor(db.id)
    val siteThemeName =
        ThemePresets.find { it.id == siteThemeId }?.name ?: "Site default"
    // v1.83: plugin theme packs available as per-database palette themes.
    val pluginThemeOptions by com.quotebrowser.app.data.plugins.rememberPluginThemeOptions()
    // v1.89.5: the database's own themes (carried in its file + the
    // user's per-database themes) join the picker list; managing them
    // (add / edit / promote) lives in the Settings row.
    val context = LocalContext.current
    val manager = remember(context) { DatabaseManager.getInstance(context) }
    val builtInThemes by manager.dbBuiltInThemes.collectAsState(initial = emptyMap())
    val allDbUserThemes by manager.dbUserThemes.collectAsState(initial = emptyMap())
    val builtInTheme = builtInThemes[db.id]
    val userThemes = allDbUserThemes[db.id].orEmpty()
    val siteDefaultLabel =
        if (builtInTheme != null) "Site default (${builtInTheme.name})"
        else "Site default ($siteThemeName)"
    // v1.88.23: grouped into labeled sections (shared with Settings).
    val themeOptions = remember(
        customThemes, pluginThemeOptions, siteDefaultLabel, builtInTheme, userThemes, db.id
    ) {
        groupedDbThemeOptions(
            siteDefaultLabel, customThemes, pluginThemeOptions,
            builtInTheme = builtInTheme?.let {
                com.quotebrowser.app.data.dbBuiltInThemeId(db.id) to it.name
            },
            dbUserThemeOptions = userThemes.mapIndexed { index, theme ->
                com.quotebrowser.app.data.dbUserThemeId(db.id, index) to theme.name
            }
        )
    }
    val selectedThemeName = themeOptions
        .filterIsInstance<ThemeOption.Item>()
        .find { it.id == config.themeId }?.name
        ?: siteDefaultLabel
    AccentAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${db.label} theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Database theme",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = config.themeEnabled,
                        onCheckedChange = { onConfigChange(config.copy(themeEnabled = it)) }
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = themeMenuOpen,
                    onExpandedChange = { if (config.themeEnabled) themeMenuOpen = it }
                ) {
                    OutlinedTextField(
                        value = selectedThemeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Theme") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuOpen)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = config.themeEnabled
                    )
                    ExposedDropdownMenu(
                        expanded = themeMenuOpen,
                        onDismissRequest = { themeMenuOpen = false }
                    ) {
                        // v1.88.23: labeled section headers (disabled)
                        // between the grouped theme options.
                        themeOptions.forEach { opt ->
                            when (opt) {
                                is ThemeOption.Header -> DropdownMenuItem(
                                    text = {
                                        Text(
                                            opt.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {},
                                    enabled = false
                                )
                                is ThemeOption.Item -> DropdownMenuItem(
                                    text = { Text(opt.name) },
                                    onClick = {
                                        onConfigChange(config.copy(themeId = opt.id))
                                        themeMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Site colors",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = config.siteColors,
                        onCheckedChange = { onConfigChange(config.copy(siteColors = it)) },
                        enabled = config.themeEnabled
                    )
                }
                Text(
                    text = "Layout",
                    style = MaterialTheme.typography.bodyMedium
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SiteLayout.entries.forEachIndexed { i, layout ->
                        SegmentedButton(
                            selected = config.layout == layout,
                            onClick = { onConfigChange(config.copy(layout = layout)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = i,
                                count = SiteLayout.entries.size
                            ),
                            enabled = config.themeEnabled
                        ) {
                            Text(layout.label, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

/**
 * v1.55: per-database info dialog — collection date, quote counts
 * (total/approved/queued), favorites in this database, and category count.
 */
@Composable
fun DatabaseInfoDialog(
    stats: com.quotebrowser.app.data.DatabaseManager.DatabaseStats?,
    onDismiss: () -> Unit
) {
    // v1.57: rounded, accent-tinted info panel.
    AccentAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stats?.label ?: "Database info") },
        text = {
            if (stats == null) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("Collection date", stats.collectionDate ?: "Unknown")
                    InfoRow("Total quotes", stats.totalQuotes?.toString() ?: "—")
                    InfoRow("Approved", stats.approvedCount?.toString() ?: "—")
                    InfoRow("Queued", stats.queuedCount?.toString() ?: "—")
                    InfoRow("Favorites", stats.favoritesCount.toString())
                    InfoRow("Categories", stats.categoryCount.toString())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
            // v1.58: inherit the dialog's content color — the old
            // onSurfaceVariant washed out on tan/orange accent containers.
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Min/max quote-length filter row (characters). Used on Browse and Search.
 * Empty field = no bound; updates propagate on every keystroke.
 */
@Composable
fun LengthFilterRow(
    minLength: Int?,
    maxLength: Int?,
    onMinChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            "Length",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        var minText by remember(minLength) { mutableStateOf(minLength?.toString() ?: "") }
        var maxText by remember(maxLength) { mutableStateOf(maxLength?.toString() ?: "") }
        OutlinedTextField(
            value = minText,
            onValueChange = { t ->
                minText = t.filter { it.isDigit() }.take(4)
                onMinChange(minText.toIntOrNull())
            },
            placeholder = { Text("Min") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .width(76.dp)
                .height(48.dp)
        )
        Text("–", modifier = Modifier.padding(horizontal = 4.dp))
        OutlinedTextField(
            value = maxText,
            onValueChange = { t ->
                maxText = t.filter { it.isDigit() }.take(4)
                onMaxChange(maxText.toIntOrNull())
            },
            placeholder = { Text("Max") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .width(76.dp)
                .height(48.dp)
        )
        Text(
            "words",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp)
        )
        if (minLength != null || maxLength != null) {
            TextButton(onClick = { onMinChange(null); onMaxChange(null) }) {
                Text("Clear")
            }
        }
    }
}

/**
 * Quote-length filter: a preset dropdown (All lengths by default, Longer,
 * Shorter, 1 line, Custom). The Longer/Shorter word thresholds are
 * configurable in Settings; Custom reveals the min/max word boxes.
 */
@Composable
fun LengthModeDropdown(
    mode: com.quotebrowser.app.data.LengthMode,
    onModeChange: (com.quotebrowser.app.data.LengthMode) -> Unit,
    minLength: Int?,
    maxLength: Int?,
    onMinChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "Length",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(mode.label)
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Change length filter"
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    com.quotebrowser.app.data.LengthMode.entries.forEach { entry ->
                        DropdownMenuItem(
                            text = { Text(entry.label) },
                            onClick = { expanded = false; onModeChange(entry) }
                        )
                    }
                }
            }
        }
        if (mode == com.quotebrowser.app.data.LengthMode.CUSTOM) {
            LengthFilterRow(
                minLength = minLength,
                maxLength = maxLength,
                onMinChange = onMinChange,
                onMaxChange = onMaxChange
            )
        }
    }
}

/**
 * Min-rating filter: a dropdown of preset thresholds plus a compact
 * "Custom" button that opens a numeric input dialog. When a custom
 * (non-preset) value is active, the button reflects it.
 * [selected] is the current minimum rating (null = all).
 */
@Composable
fun RatingFilterRow(
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets: List<Pair<String, Int?>> = listOf(
        "All" to null,
        "0" to 0,
        "50" to 50,
        "100" to 100,
        "500" to 500,
        "1,000" to 1000,
        "1,500" to 1500,
        "2,000" to 2000,
        "3,000" to 3000,
        "3,500" to 3500
    )
    val isCustomValue = selected != null && presets.none { it.second == selected }
    val currentLabel = presets.firstOrNull { it.second == selected }?.first
        ?: selected?.let { formatRatingNumber(it) }
        ?: "All"
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            "Min rating",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        var expanded by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(currentLabel)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose minimum rating")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presets.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = { expanded = false; onSelect(value) }
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        var customDialogOpen by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { customDialogOpen = true }) {
            Text(if (isCustomValue && selected != null) formatRatingNumber(selected) else "Custom")
        }
        if (customDialogOpen) {
            CustomRatingDialog(
                initial = if (isCustomValue && selected != null) selected.toString() else "",
                hasCustomValue = isCustomValue,
                onDismiss = { customDialogOpen = false },
                onApply = { value -> customDialogOpen = false; onSelect(value) }
            )
        }
    }
}

/** Thousands-separated rating display, matching the preset labels ("1,000"). */
private fun formatRatingNumber(value: Int): String =
    java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(value)

/** Numeric input dialog for a custom minimum rating. Empty = all ratings. */
@Composable
private fun CustomRatingDialog(
    initial: String,
    hasCustomValue: Boolean,
    onDismiss: () -> Unit,
    onApply: (Int?) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom minimum rating") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { t -> text = t.filter { it.isDigit() }.take(6) },
                label = { Text("Minimum rating") },
                placeholder = { Text("e.g. 250") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onApply(text.toIntOrNull()) }) { Text("Apply") }
        },
        dismissButton = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                if (hasCustomValue) {
                    TextButton(onClick = { onApply(null) }) { Text("Clear") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

/**
 * "Person" filter entry point: a button that opens a searchable most-quoted
 * nicks picker. Picking a nick calls [onNickClick] (the caller navigates to
 * that person's quote list).
 */
@Composable
fun PersonFilterButton(
    manager: com.quotebrowser.app.data.DatabaseManager,
    onNickClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: com.quotebrowser.app.ui.NickViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.quotebrowser.app.ui.vmFactory {
                com.quotebrowser.app.ui.NickViewModel(manager)
            }
        )
    val leaderboard by vm.leaderboard.collectAsState()
    var pickerOpen by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            "Person",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        TextButton(onClick = { pickerOpen = true; search = "" }) {
            Icon(
                androidx.compose.material.icons.Icons.Filled.Person,
                contentDescription = "Pick a person"
            )
            Spacer(Modifier.width(4.dp))
            Text("All people")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose person")
        }
    }

    if (pickerOpen) {
        val query = search.trim().lowercase()
        val shown = if (query.isEmpty()) leaderboard
        else leaderboard.filter { it.nick.lowercase().contains(query) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pickerOpen = false },
            title = { Text("Most quoted people") },
            text = {
                Column {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Search nicks") },
                        leadingIcon = {
                            Icon(
                                androidx.compose.material.icons.Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    if (leaderboard.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(320.dp),
                            flingBehavior = rememberCappedFlingBehavior()) {
                            items(shown, key = { it.nick.lowercase() }) { entry ->
                                androidx.compose.material3.ListItem(
                                    headlineContent = { Text("<${entry.nick}>") },
                                    supportingContent = { Text("${entry.count} quotes") },
                                    modifier = Modifier.clickable {
                                        pickerOpen = false
                                        onNickClick(entry.nick)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickerOpen = false }) { Text("Close") }
            }
        )
    }
}
