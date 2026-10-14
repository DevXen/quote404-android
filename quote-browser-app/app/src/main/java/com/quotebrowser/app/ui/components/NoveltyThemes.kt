package com.quotebrowser.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.quotebrowser.app.ui.LocalThemeNickPaletteStore
import com.quotebrowser.app.ui.noveltyCardThemeKey
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotebrowser.app.data.DisplayPrefs
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.LocalQuoteBackground
import com.quotebrowser.app.ui.LocalQuoteClean
import com.quotebrowser.app.ui.cleanDisplayText
import com.quotebrowser.app.ui.quoteTextStyle
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import kotlin.random.Random

/**
 * v1.88.23 novelty quote-card themes: bespoke card presentations for the
 * ten new themes (DOS, macOS, Macintosh, iMessage, Google Messages, BSOD,
 * Linux, mIRC, Terminal, Green Monochrome). Routed from [SiteQuoteCard]
 * via [SiteStyle]; the per-database Site layout and the theme id mapping
 * in [com.quotebrowser.app.data.siteStyleFor] select the presentation.
 *
 * Text always flows through the same pipeline as the normal card body:
 * translation (via [translatedBodyText]), kid-safe masking, quote
 * cleaning and the v1.88.22 paragraph breaks (via
 * [rememberNickAnnotatedText]), and the database's nick palette — so the
 * "Break between quotes" setting, translations, and nick colors keep
 * working inside every novelty theme. Bubble themes split the
 * paragraphized turns into one bubble per turn.
 */
@Composable
fun NoveltyThemeCard(
    style: SiteStyle,
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    quoteBackground: Color,
    headerActions: @Composable RowScope.() -> Unit,
    dbId: String?,
    modifier: Modifier = Modifier,
    /**
     * v1.89.2: the structure theme id the card was resolved from, when
     * the caller knows it (exports thread it explicitly; on screen the
     * Friends palette resolves from the theme snapshots + [dbId]).
     */
    structureThemeId: String? = null
) {
    // v1.88.32: "remove nicks from message text" for the chat themes. The
    // export path provides LocalNoveltyStripNicks explicitly (its
    // off-screen composition has no store); on screen it resolves from
    // the per-theme store, keyed by the theme's own novelty key so it
    // applies whether the theme renders as the site card or from the
    // Novelty section.
    val stripStore = LocalThemeNickPaletteStore.current
    val stripSet by (stripStore?.nickStrip
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptySet<String>()) })
        .collectAsState()
    val stripNicks = LocalNoveltyStripNicks.current ||
        stripSet.contains(style.noveltyCardThemeKey())
    when (style) {
        SiteStyle.DOS -> DosCard(quote, showNumber, showRating, headerActions, modifier)
        SiteStyle.MACOS -> MacOsCard(quote, showNumber, headerActions, modifier)
        SiteStyle.MACINTOSH -> MacintoshCard(quote, showNumber, headerActions, modifier)
        SiteStyle.IMESSAGE -> IMessageCard(quote, showNumber, headerActions, modifier)
        SiteStyle.GMESSAGES -> GMessagesCard(quote, showNumber, headerActions, modifier)
        SiteStyle.BSOD -> BsodCard(quote, showNumber, headerActions, modifier)
        SiteStyle.LINUX -> LinuxCard(quote, showNumber, headerActions, modifier)
        SiteStyle.MIRC -> MircCard(quote, showNumber, headerActions, modifier)
        SiteStyle.TERMINAL -> TerminalCard(quote, showNumber, headerActions, modifier)
        SiteStyle.GREEN_MONO -> GreenMonoCard(quote, showNumber, headerActions, modifier)
        // v1.88.25 textmode additions.
        SiteStyle.ANSI -> AnsiCard(quote, showNumber, showRating, headerActions, modifier)
        SiteStyle.ASCII -> AsciiCard(quote, showNumber, showRating, headerActions, modifier)
        SiteStyle.MATRIX -> MatrixCard(quote, showNumber, showRating, headerActions, modifier)
        SiteStyle.GREEN_TERM -> GreenTermCard(quote, showNumber, headerActions, modifier)
        // v1.88.27 Messenger chat themes.
        SiteStyle.MESSENGER_LIGHT -> MessengerCard(quote, showNumber, showRating, headerActions, modifier, dark = false)
        SiteStyle.MESSENGER_DARK -> MessengerCard(quote, showNumber, showRating, headerActions, modifier, dark = true)
        // v1.88.28 classic IM client themes.
        SiteStyle.AIM -> AimCard(quote, showNumber, showRating, headerActions, modifier, stripNicks)
        SiteStyle.ICQ -> IcqCard(quote, showNumber, showRating, headerActions, modifier, stripNicks)
        SiteStyle.MSN_MESSENGER -> MsnCard(quote, showNumber, showRating, headerActions, modifier, stripNicks)
        // v1.88.38: Clint's personal Friends database theme.
        // v1.89.2: two palettes — Sunset Hangout and Friends — Wall.
        SiteStyle.FRIENDS -> FriendsCard(
            quote, showNumber, showRating, headerActions, modifier,
            wall = friendsWallPalette(structureThemeId, dbId)
        )
        else -> {
            // Unreachable: SiteQuoteCard only routes novelty styles here.
        }
    }
}

// ---------------------------------------------------------------------------
// Shared text pipeline helpers.
// ---------------------------------------------------------------------------

/** The paragraphized display turns of the quote (blank lines removed). */
@Composable
private fun noveltyTurns(quote: Quote): List<String> {
    val palette = LocalNickPalette.current
    val clean = LocalQuoteClean.current
    val raw = translatedBodyText(quote)
    return remember(raw, palette, clean) {
        cleanDisplayText(raw, palette.ircLineBreaks, palette.dialogueLineBreaks, clean)
            .split('\n')
            .filter { it.isNotBlank() }
    }
}

/** Each turn through the full nick-coloring pipeline. */
@Composable
private fun noveltyAnnotatedTurns(
    turns: List<String>,
    style: TextStyle
): List<AnnotatedString> = turns.map { turn -> rememberNickAnnotatedText(turn, style) }

/**
 * v1.88.32: when true, the AIM/ICQ/MSN chat cards strip the redundant
 * speaker prefix from each turn's body text (the per-turn header already
 * shows the nick). Provided explicitly by the export path; on screen it
 * resolves from the per-theme store in [NoveltyThemeCard].
 */
val LocalNoveltyStripNicks = compositionLocalOf { false }

/**
 * v1.88.32: parses a chat turn's speaker nick — either an angle-bracket
 * prefix ("<nick> rest") or a colon prefix ("Nick: rest"). Returns null
 * when the turn carries no parseable nick; callers omit the header line
 * for that turn instead of inventing a "FriendN" name.
 */
fun noveltyTurnNick(turn: String): String? {
    val t = turn.trimStart()
    if (t.startsWith("<")) {
        val close = t.indexOf('>')
        if (close > 1) return t.substring(1, close).trim().takeIf { it.isNotEmpty() }
    }
    val colon = t.indexOf(':')
    if (colon > 0 && colon < 24) {
        return t.substring(0, colon).trim().takeIf { it.isNotEmpty() }
    }
    return null
}

/**
 * v1.88.32: strips a leading speaker prefix ("<nick> ", "Nick: ",
 * "* nick ") from a chat turn's body text. Returns the text unchanged
 * when no prefix is found. Never merges turns — each turn keeps its own
 * block, header, and color; only the redundant prefix text is removed.
 */
fun noveltyStripNickPrefix(text: String): String {
    val t = text.trimStart()
    // "<nick> rest"
    if (t.startsWith("<")) {
        val close = t.indexOf('>')
        if (close > 1) return t.substring(close + 1).trimStart()
    }
    // "* nick rest" (IRC action line)
    if (t.startsWith("* ")) {
        val rest = t.substring(2)
        val end = rest.indexOfFirst { it == ' ' || it == ':' }
        if (end > 0) return rest.substring(end).trimStart(' ', ':')
    }
    // "Nick: rest"
    val colon = t.indexOf(':')
    if (colon > 0 && colon < 24) {
        return t.substring(colon + 1).trimStart()
    }
    return text
}

/**
 * Single annotated string of all turns joined by newlines, each prefixed
 * (e.g. "$ " prompts, "[hh:mm]" timestamps). One Text keeps the layout
 * simple and preserves the nick color spans.
 */
@Composable
private fun noveltyPrefixedText(
    turns: List<AnnotatedString>,
    prefixFor: (Int) -> Pair<String, Color>
): AnnotatedString = remember(turns) {
    buildAnnotatedString {
        turns.forEachIndexed { i, turn ->
            if (i > 0) append("\n")
            val (prefix, color) = prefixFor(i)
            val start = length
            append(prefix)
            addStyle(
                SpanStyle(color = color, fontFamily = FontFamily.Monospace),
                start, length
            )
            append(turn)
        }
    }
}

/** Bubble spacing honors the v1.88.22 "Break between quotes" setting. */
@Composable
private fun noveltyBubbleSpacing(): Dp {
    val setting by DisplayPrefs.breakBetweenQuotes.collectAsState()
    return when (setting) {
        DisplayPrefs.BREAK_OFF -> 2.dp
        DisplayPrefs.BREAK_LARGE -> 10.dp
        DisplayPrefs.BREAK_MEDIUM -> 6.dp
        else -> 4.dp // small
    }
}

private val NoveltyMono = FontFamily.Monospace

@Composable
private fun noveltyMonoStyle(color: Color, size: Int = 15): TextStyle =
    quoteTextStyle(MaterialTheme.typography.bodyLarge).copy(
        fontFamily = NoveltyMono,
        color = color,
        fontSize = size.sp,
        lineHeight = (size + 7).sp
    )

@Composable
private fun noveltyPlainStyle(color: Color, size: Int = 15): TextStyle =
    quoteTextStyle(MaterialTheme.typography.bodyLarge).copy(
        color = color,
        fontSize = size.sp,
        lineHeight = (size + 7).sp
    )

/** Deterministic per-turn [hh:mm] stamp for the mIRC theme. */
private fun mircStamp(quoteNumber: Int, index: Int): String {
    val mins = (9 * 60 + quoteNumber * 37 + index * 17).floorMod(1440)
    return "[%02d:%02d] ".format(mins / 60, mins % 60)
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other

/**
 * The header-actions row (share / favorite / overflow) tinted for the
 * card, with an optional leading label.
 */
@Composable
private fun RowScope.noveltyActionRow(
    tint: Color,
    headerActions: @Composable RowScope.() -> Unit,
    label: @Composable RowScope.() -> Unit = {}
) {
    label()
    Spacer(Modifier.weight(1f))
    CompositionLocalProvider(LocalContentColor provides tint) {
        headerActions()
    }
}

// ---------------------------------------------------------------------------
// DOS — white monospace on black, C:\> header line, no title bar.
// ---------------------------------------------------------------------------

@Composable
private fun DosCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val bodyStyle = noveltyMonoStyle(Color(0xFFE8E8E8))
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, Color(0xFF555555)),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                noveltyActionRow(
                    tint = Color(0xFFE8E8E8),
                    headerActions = headerActions
                ) {
                    Text(
                        text = if (showNumber) "C:\\> #${quote.number}" else "C:\\>",
                        style = noveltyMonoStyle(Color(0xFFF5F5F5)).copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            CompositionLocalProvider(LocalQuoteBackground provides Color.Black) {
                Text(text = annotated, style = bodyStyle)
            }
            if (showRating && quote.rating != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "score: ${quote.rating}",
                    style = noveltyMonoStyle(Color(0xFF888888), 12)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// macOS — modern light window, traffic lights, rounded corners.
// ---------------------------------------------------------------------------

@Composable
private fun MacOsCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val bodyStyle = noveltyPlainStyle(Color(0xFF1D1D1F))
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFD1D1D6)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F7))
    ) {
        Column {
            // Title bar: traffic lights left, title centered, actions right.
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8E8ED))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Color(0xFFFF5F57),
                            Color(0xFFFFBD2E),
                            Color(0xFF28C840)
                        ).forEach { dot ->
                            Box(
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(dot)
                                    .drawBehind {
                                        drawCircle(
                                            Color.Black.copy(alpha = 0.12f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = 1.dp.toPx()
                                            )
                                        )
                                    }
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (showNumber) "Quote #${quote.number}" else "Quote",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3A3A3C)
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    CompositionLocalProvider(
                        LocalContentColor provides Color(0xFF3A3A3C)
                    ) {
                        headerActions()
                    }
                }
            }
            CompositionLocalProvider(
                LocalQuoteBackground provides Color(0xFFF5F5F7)
            ) {
                Text(
                    text = annotated,
                    style = bodyStyle,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Macintosh — 1984: pinstripe title bar, thick black border, 1-bit look.
// ---------------------------------------------------------------------------

@Composable
private fun MacintoshCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val bodyStyle = noveltyMonoStyle(Color.Black)
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(3.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Pinstripe title bar with the title knocked out in a bordered box.
            Box(
                Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val step = 4.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                Color.Black,
                                Offset(0f, y),
                                Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                            y += step
                        }
                    }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showNumber) " Quote #${quote.number} " else " Quote ",
                    style = noveltyMonoStyle(Color.Black, 13).copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .background(Color.White)
                        .border(BorderStroke(1.dp, Color.Black))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            // Header actions sit just under the bar, right-aligned.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = Color.Black, headerActions = headerActions)
            }
            CompositionLocalProvider(LocalQuoteBackground provides Color.White) {
                Text(
                    text = annotated,
                    style = bodyStyle,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// iMessage — one bubble per turn, blue/white alternating with gray.
// ---------------------------------------------------------------------------

@Composable
private fun IMessageCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val turns = noveltyTurns(quote)
    val bubbleBlue = Color(0xFF0A84FF)
    val bubbleGray = Color(0xFFE9E9EB)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                noveltyActionRow(
                    tint = Color(0xFF8E8E93),
                    headerActions = headerActions
                ) {
                    Text(
                        text = if (showNumber) "iMessage · #${quote.number}"
                        else "iMessage",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF8E8E93)
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(noveltyBubbleSpacing())) {
                turns.forEachIndexed { i, turn ->
                    val mine = i % 2 == 0
                    val bubble = if (mine) bubbleBlue else bubbleGray
                    val onBubble = if (mine) Color.White else Color(0xFF1A1A1A)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mine) Arrangement.End
                        else Arrangement.Start
                    ) {
                        // Nick spans are contrast-adjusted against the
                        // actual bubble color.
                        CompositionLocalProvider(
                            LocalQuoteBackground provides bubble
                        ) {
                            val annotated = rememberNickAnnotatedText(
                                turn, noveltyPlainStyle(onBubble)
                            )
                            Text(
                                text = annotated,
                                style = noveltyPlainStyle(onBubble),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(bubble)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Google Messages — Material You RCS: expressive asymmetric bubbles.
// ---------------------------------------------------------------------------

@Composable
private fun GMessagesCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val turns = noveltyTurns(quote)
    val sentBubble = Color(0xFFD9E2FF)
    val sentText = Color(0xFF041E49)
    val recvBubble = Color(0xFFE8EAED)
    val recvText = Color(0xFF1A1C1E)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                noveltyActionRow(
                    tint = Color(0xFF44474E),
                    headerActions = headerActions
                ) {
                    Text(
                        text = if (showNumber) "RCS · #${quote.number}" else "RCS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF44474E)
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(noveltyBubbleSpacing())) {
                turns.forEachIndexed { i, turn ->
                    val sent = i % 2 == 0
                    val bubble = if (sent) sentBubble else recvBubble
                    val onBubble = if (sent) sentText else recvText
                    // Expressive: one corner pulled tight, unlike iMessage's
                    // symmetric rounding.
                    val shape = if (sent) RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = 20.dp, bottomEnd = 6.dp
                    ) else RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = 6.dp, bottomEnd = 20.dp
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (sent) Arrangement.End
                        else Arrangement.Start
                    ) {
                        // Nick spans are contrast-adjusted against the
                        // actual bubble color.
                        CompositionLocalProvider(
                            LocalQuoteBackground provides bubble
                        ) {
                            val annotated = rememberNickAnnotatedText(
                                turn, noveltyPlainStyle(onBubble)
                            )
                            Text(
                                text = annotated,
                                style = noveltyPlainStyle(onBubble),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(shape)
                                    .background(bubble)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BSOD — :( + QUOTE_FAULT on classic blue, white monospace.
// ---------------------------------------------------------------------------

@Composable
private fun BsodCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val blue = Color(0xFF0000AA)
    val bodyStyle = noveltyMonoStyle(Color.White)
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = blue)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = ":(",
                style = MaterialTheme.typography.displayLarge.copy(color = Color.White),
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "QUOTE_FAULT",
                style = noveltyMonoStyle(Color.White).copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(16.dp))
            CompositionLocalProvider(LocalQuoteBackground provides blue) {
                Text(text = annotated, style = bodyStyle)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                noveltyActionRow(
                    tint = Color(0xFFB0B0FF),
                    headerActions = headerActions
                ) {
                    if (showNumber) {
                        Text(
                            text = "#${quote.number}",
                            style = noveltyMonoStyle(Color(0xFFB0B0FF), 12)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Linux — black terminal, green monospace, $ prompts, thin status bar.
// ---------------------------------------------------------------------------

@Composable
private fun LinuxCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val green = Color(0xFF33FF66)
    val dimGreen = Color(0xFF1E7A38)
    val bodyStyle = noveltyMonoStyle(green)
    val turns = noveltyTurns(quote)
    val annotatedTurns = noveltyAnnotatedTurns(turns, bodyStyle)
    val text = noveltyPrefixedText(annotatedTurns) { "$ " to dimGreen }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color(0xFF1E5C2E)),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column {
            // Thin status bar.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "user@quote404:~",
                    style = noveltyMonoStyle(Color(0xFF888888), 12),
                    modifier = Modifier.weight(1f)
                )
                if (showNumber) {
                    Text(
                        text = "#${quote.number}",
                        style = noveltyMonoStyle(Color(0xFF888888), 12)
                    )
                }
            }
            CompositionLocalProvider(LocalQuoteBackground provides Color.Black) {
                Text(
                    text = text,
                    style = bodyStyle,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = dimGreen, headerActions = headerActions)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// mIRC — white channel window, navy #quote404 bar, [hh:mm] timestamps,
// nicks cycling the classic mIRC palette (set on the theme preset).
// ---------------------------------------------------------------------------

@Composable
private fun MircCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val navy = Color(0xFF000080)
    val bodyStyle = noveltyMonoStyle(Color.Black)
    val turns = noveltyTurns(quote)
    val annotatedTurns = noveltyAnnotatedTurns(turns, bodyStyle)
    val text = noveltyPrefixedText(annotatedTurns) { i ->
        mircStamp(quote.number, i) to Color(0xFF808080)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, navy),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Classic mIRC title bar.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(navy)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#quote404",
                    style = noveltyMonoStyle(Color.White, 13).copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (showNumber) {
                    Text(
                        text = "#${quote.number}",
                        style = noveltyMonoStyle(Color.White, 12)
                    )
                }
            }
            CompositionLocalProvider(LocalQuoteBackground provides Color.White) {
                Text(
                    text = text,
                    style = bodyStyle,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = Color(0xFF666666), headerActions = headerActions)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Terminal — generic modern terminal: slim title bar, block cursor.
// ---------------------------------------------------------------------------

@Composable
private fun TerminalCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val bg = Color(0xFF0D1117)
    val barBg = Color(0xFF161B22)
    val bodyStyle = noveltyMonoStyle(Color(0xFFE6EDF3))
    val base = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    // Block cursor at the end of the output.
    val withCursor = remember(base) {
        buildAnnotatedString {
            append(base)
            append(" ")
            val start = length
            append("▊")
            addStyle(SpanStyle(color = Color(0xFF58A6FF)), start, length)
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column {
            // Slim title bar, no traffic lights (unlike macOS).
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(barBg)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showNumber) "quote #${quote.number}" else "quote",
                    style = noveltyMonoStyle(Color(0xFF8B949E), 12)
                )
            }
            CompositionLocalProvider(LocalQuoteBackground provides bg) {
                Text(
                    text = withCursor,
                    style = bodyStyle,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(
                    tint = Color(0xFF8B949E),
                    headerActions = headerActions
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Green Monochrome — P1 phosphor: pure black, glowing green, no chrome.
// ---------------------------------------------------------------------------

@Composable
private fun GreenMonoCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val phosphor = Color(0xFF33FF33)
    val bodyStyle = noveltyMonoStyle(phosphor).copy(
        shadow = Shadow(color = phosphor.copy(alpha = 0.8f), blurRadius = 6f)
    )
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Actions only — no title chrome on the phosphor screen.
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(
                    tint = Color(0xFF1E7A1E),
                    headerActions = headerActions
                )
            }
            Spacer(Modifier.height(4.dp))
            CompositionLocalProvider(LocalQuoteBackground provides Color.Black) {
                Text(text = annotated, style = bodyStyle)
            }
            if (showNumber) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "#${quote.number}",
                        style = noveltyMonoStyle(Color(0xFF1E7A1E), 11)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ANSI — BBS ANSI-art: black background, bright multi-colored monospace
// (ANSI bright palette cycled per turn), block-element decorations framing
// the header bar with the quote number.
// ---------------------------------------------------------------------------

/** The bright ANSI palette, cycled per turn. */
private val AnsiBrightPalette = listOf(
    Color(0xFFFF5555), Color(0xFF55FF55), Color(0xFFFFFF55),
    Color(0xFF5555FF), Color(0xFFFF55FF), Color(0xFF55FFFF),
    Color(0xFFFFFFFF)
)

@Composable
private fun AnsiCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val turns = noveltyTurns(quote)
    // Each turn gets its own bright ANSI color as a base span; the nick
    // pipeline's spans are appended after, so nicks keep their own colors
    // on top of the per-turn base color. (rememberNickAnnotatedText only
    // adds nick/action spans; the base color comes from spans we add here.)
    val annotatedTurns = turns.map { turn ->
        rememberNickAnnotatedText(turn, noveltyMonoStyle(Color.White))
    }
    val text = remember(annotatedTurns) {
        buildAnnotatedString {
            annotatedTurns.forEachIndexed { i, t ->
                if (i > 0) append("\n")
                val start = length
                addStyle(
                    SpanStyle(color = AnsiBrightPalette[i % AnsiBrightPalette.size]),
                    start, start + t.length
                )
                append(t)
            }
        }
    }
    val title = if (showNumber) " QUOTE #${quote.number} " else " QUOTE "
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, Color(0xFF555555)),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Block-element header bar with the quote number.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "▓▒░",
                    style = noveltyMonoStyle(Color(0xFF55FFFF)).copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = title,
                    style = noveltyMonoStyle(Color(0xFFFFFF55)).copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "░▒▓",
                    style = noveltyMonoStyle(Color(0xFFFF55FF)).copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            CompositionLocalProvider(LocalQuoteBackground provides Color.Black) {
                Text(
                    text = text,
                    style = noveltyMonoStyle(Color.White)
                )
            }
            if (showRating && quote.rating != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "score: ${quote.rating}",
                    style = noveltyMonoStyle(Color(0xFF888888), 12)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = Color(0xFF55FFFF), headerActions = headerActions)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ASCII — classic .nfo/textmode: dark background, light-gray monospace, the
// quote framed in an ASCII-art box with the quote number set into the
// frame. Strictly monochrome.
// ---------------------------------------------------------------------------

@Composable
private fun AsciiCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val bg = Color(0xFF0C0C0C)
    val gray = Color(0xFFCCCCCC)
    val dim = Color(0xFF888888)
    val frameStyle = noveltyMonoStyle(dim)
    val bodyStyle = noveltyMonoStyle(gray)
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    val inner = 46
    val rule = "+" + "-".repeat(inner) + "+"
    val label = if (showNumber) "QUOTE #${quote.number}" else "QUOTE"
    val titleStyle = frameStyle.copy(color = gray)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, dim),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(Modifier.padding(12.dp)) {
            // v1.88.32: the title row no longer pads with spaces — space
            // glyphs render narrower than the other monospace glyphs, so
            // character-count alignment put the pipes well inside the
            // rule's + corners. The frame is width-locked to the rule
            // (IntrinsicSize.Max) and the title row pins its pipes to the
            // measured edges by construction, with the label truly
            // centered between them.
            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(text = rule, style = frameStyle)
                Row(Modifier.fillMaxWidth()) {
                    Text("|", style = titleStyle)
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = titleStyle
                    )
                    Text("|", style = titleStyle)
                }
                Text(text = rule, style = frameStyle)
            }
            Spacer(Modifier.height(8.dp))
            CompositionLocalProvider(LocalQuoteBackground provides bg) {
                Text(text = annotated, style = bodyStyle)
            }
            if (showRating && quote.rating != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "score: ${quote.rating}",
                    style = noveltyMonoStyle(dim, 12)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(text = rule, style = frameStyle)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = dim, headerActions = headerActions)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Matrix — digital rain: black background, faint static green katakana
// columns behind the text, the quote in bright green monospace on top.
// ---------------------------------------------------------------------------

@Composable
private fun MatrixCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val matrixGreen = Color(0xFF00FF41)
    val dimGreen = Color(0xFF008F11)
    val rainColor = matrixGreen.copy(alpha = 0.12f)
    val glyphs = "アイウエオカキクケコサシスセソタチツテトナニヌネノ0123456789"
    // Deterministic rain columns per quote: static, subtle, non-distracting.
    val columns = remember(quote.number) {
        val rnd = Random(quote.number * 31 + 7)
        List(14) {
            buildString {
                repeat(24) {
                    append(glyphs[rnd.nextInt(glyphs.length)])
                    append('\n')
                }
            }
        }
    }
    val bodyStyle = noveltyMonoStyle(matrixGreen).copy(
        shadow = Shadow(color = Color.Black, blurRadius = 3f)
    )
    val annotated = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color(0xFF003300)),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box {
            // Faint katakana rain behind the text, clipped to the card.
            Box(
                Modifier
                    .matchParentSize()
                    .clipToBounds()
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    columns.forEach { col ->
                        Text(text = col, style = noveltyMonoStyle(rainColor, 11))
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    noveltyActionRow(tint = dimGreen, headerActions = headerActions) {
                        Text(
                            text = if (showNumber) "> quote #${quote.number}" else "> quote",
                            style = noveltyMonoStyle(dimGreen, 12)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                CompositionLocalProvider(LocalQuoteBackground provides Color.Black) {
                    Text(text = annotated, style = bodyStyle)
                }
                if (showRating && quote.rating != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "score: ${quote.rating}",
                        style = noveltyMonoStyle(dimGreen, 12)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Green Terminal — terminal emulator window like Terminal (slim title bar
// with the quote number, block cursor) but green monochrome on black.
// ---------------------------------------------------------------------------

@Composable
private fun GreenTermCard(
    quote: Quote,
    showNumber: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier
) {
    val bg = Color(0xFF000000)
    val barBg = Color(0xFF0A1A0A)
    val green = Color(0xFF33FF33)
    val dimGreen = Color(0xFF1E7A1E)
    val bodyStyle = noveltyMonoStyle(green)
    val base = rememberNickAnnotatedText(translatedBodyText(quote), bodyStyle)
    // Block cursor at the end of the output.
    val withCursor = remember(base) {
        buildAnnotatedString {
            append(base)
            append(" ")
            val start = length
            append("▊")
            addStyle(SpanStyle(color = green), start, length)
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF1E4D1E)),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column {
            // Slim title bar with the quote number.
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(barBg)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showNumber) "quote #${quote.number}" else "quote",
                    style = noveltyMonoStyle(dimGreen, 12)
                )
            }
            CompositionLocalProvider(LocalQuoteBackground provides bg) {
                Text(
                    text = withCursor,
                    style = bodyStyle,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = dimGreen, headerActions = headerActions)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Messenger — Facebook Messenger chat: light and dark mode. White (or
// near-black) conversation background, gray incoming bubbles, and the
// signature blue -> purple -> pink gradient on outgoing bubbles. One
// bubble per turn, like iMessage.
// ---------------------------------------------------------------------------

@Composable
private fun MessengerCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    dark: Boolean
) {
    val turns = noveltyTurns(quote)
    val bg = if (dark) Color(0xFF1C1C1E) else Color.White
    val incomingBubble = if (dark) Color(0xFF3E4042) else Color(0xFFF0F0F0)
    val incomingText = if (dark) Color.White else Color(0xFF050505)
    val labelColor = if (dark) Color(0xFFB0B3B8) else Color(0xFF65676B)
    val outgoingGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0099FF),
            Color(0xFFA033FF),
            Color(0xFFFF5280)
        )
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Messenger-style header: back chevron, name, active status.
            Row(verticalAlignment = Alignment.CenterVertically) {
                noveltyActionRow(
                    tint = Color(0xFF0084FF),
                    headerActions = headerActions
                ) {
                    Text(
                        text = "‹",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF0084FF)
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (showNumber) "Messenger · #${quote.number}"
                            else "Messenger",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (dark) Color.White else Color(0xFF050505)
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF31A24C))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Active now",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = labelColor
                                )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(noveltyBubbleSpacing())) {
                turns.forEachIndexed { i, turn ->
                    val mine = i % 2 == 0
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mine) Arrangement.End
                        else Arrangement.Start
                    ) {
                        if (mine) {
                            // Outgoing: signature gradient bubble, white text.
                            CompositionLocalProvider(
                                LocalQuoteBackground provides Color(0xFF7B4DFF)
                            ) {
                                val annotated = rememberNickAnnotatedText(
                                    turn, noveltyPlainStyle(Color.White)
                                )
                                Text(
                                    text = annotated,
                                    style = noveltyPlainStyle(Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(outgoingGradient)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        } else {
                            // Incoming: gray bubble. Nick spans are
                            // contrast-adjusted against the bubble color.
                            CompositionLocalProvider(
                                LocalQuoteBackground provides incomingBubble
                            ) {
                                val annotated = rememberNickAnnotatedText(
                                    turn, noveltyPlainStyle(incomingText)
                                )
                                Text(
                                    text = annotated,
                                    style = noveltyPlainStyle(incomingText),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(incomingBubble)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            if (showRating && quote.rating != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${quote.rating}",
                    style = MaterialTheme.typography.labelSmall.copy(color = labelColor)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AIM — classic AOL Instant Messenger IM window: blue title bar, red/blue
// nicks, formatting toolbar, Warn/Block/Add Buddy/Talk/Get Info/Send row.
// ---------------------------------------------------------------------------

@Composable
private fun AimCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    stripNicks: Boolean = false
) {
    val titleBrush = Brush.verticalGradient(
        listOf(Color(0xFF3B6EA5), Color(0xFF1E4E79), Color(0xFF2E6DA4))
    )
    val bodyStyle = noveltyPlainStyle(Color.Black, 14)
    val turns = noveltyTurns(quote)
    // v1.88.32: with "remove nicks from message text" on, the redundant
    // speaker prefix is stripped from each body — turns stay separate,
    // each keeping its own header and alternating color.
    val bodyTurns = remember(turns, stripNicks) {
        if (stripNicks) turns.map { noveltyStripNickPrefix(it) } else turns
    }
    val annotatedTurns = noveltyAnnotatedTurns(bodyTurns, bodyStyle)
    val turnNicks = remember(turns) { turns.map { noveltyTurnNick(it) } }
    val myNickColor = Color(0xFFE60000) // red, like "MyFriend600"
    val theirNickColor = Color(0xFF0000CC) // blue, like "Palsm20"
    val labelColor = Color(0xFF666666)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, Color(0xFF1E4E79)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F0))
    ) {
        Column {
            // Blue gradient title bar.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(titleBrush)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instant Message",
                    style = noveltyPlainStyle(Color.White, 13).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(text = "—", style = noveltyPlainStyle(Color.White, 12))
                Spacer(Modifier.width(8.dp))
                Text(text = "▢", style = noveltyPlainStyle(Color.White, 12))
                Spacer(Modifier.width(8.dp))
                Text(text = "✕", style = noveltyPlainStyle(Color.White, 12).copy(fontWeight = FontWeight.Bold))
            }
            // Menu row.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDEDE5))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                listOf("File", "Edit", "Insert", "People").forEach { item ->
                    Text(
                        text = item,
                        style = noveltyPlainStyle(Color(0xFF333333), 12),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
            // Message log.
            CompositionLocalProvider(LocalQuoteBackground provides Color.White) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Color(0xFF999999))
                        .padding(8.dp)
                ) {
                    annotatedTurns.forEachIndexed { i, turn ->
                        val nickColor = if (i % 2 == 0) myNickColor else theirNickColor
                        // v1.88.32: just the nick — no fake timestamp, no
                        // invented "FriendN". No parseable nick = no header.
                        val nick = turnNicks[i]
                        if (nick != null) {
                            Text(
                                text = "$nick:",
                                style = noveltyPlainStyle(nickColor, 14).copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(text = turn, style = bodyStyle)
                        if (i < annotatedTurns.size - 1) Spacer(Modifier.height(6.dp))
                    }
                    if (showNumber) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "#${quote.number}",
                            style = noveltyPlainStyle(labelColor, 11)
                        )
                    }
                }
            }
            // Formatting toolbar.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("B", "I", "U").forEach { b ->
                    Text(
                        text = b,
                        style = noveltyPlainStyle(Color(0xFF333333), 13).copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = if (b == "I") androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                        ),
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                Text(text = "link", style = noveltyPlainStyle(Color(0xFF0000CC), 12).copy(fontWeight = FontWeight.Bold))
            }
            // Bottom button row with Send.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Warn", "Block", "Add Buddy", "Talk", "Get Info").forEach { b ->
                    Text(
                        text = b,
                        style = noveltyPlainStyle(Color(0xFF333333), 11),
                        modifier = Modifier
                            .border(1.dp, Color(0xFF999999), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .padding(end = 4.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Send",
                    style = noveltyPlainStyle(Color.White, 13).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .background(Color(0xFF3B6EA5), RoundedCornerShape(3.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = Color(0xFF666666), headerActions = headerActions)
            }
            if (showRating && quote.rating != null) {
                Text(
                    text = "${quote.rating}",
                    style = MaterialTheme.typography.labelSmall.copy(color = labelColor),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FRIENDS — v1.88.38: Clint's personal database (Jordyn & Tiffany banter).
// Chat bubbles: right-aligned gradient bubbles for <Me>/<Clint>, left-
// aligned bubbles with a nick label for everyone else, centered italic
// status lines for narration/action turns.
// v1.89.2: two palettes over the same layout — "Sunset Hangout" (deep
// plum card, sunset-gradient outgoing bubbles, amber labels) and
// "Friends — Wall" (white card, classic-Facebook-blue gradient outgoing
// bubbles, pale blue-gray incoming bubbles), the database's new default.
// ---------------------------------------------------------------------------

/**
 * v1.89.2: which FriendsCard palette to paint. The explicit structure
 * theme id wins (exports thread it so the bitmap matches the screen);
 * otherwise the per-database theme resolution decides; with no signal
 * at all, Friends databases get their wall default and anything else
 * keeps the original sunset novelty look.
 */
@Composable
private fun friendsWallPalette(structureThemeId: String?, dbId: String?): Boolean {
    if (structureThemeId != null) return structureThemeId == "db_friends_wall"
    if (dbId != null) {
        val resolved = resolveDbTheme(LocalDbThemeSnapshots.current, dbId)
        resolved.structureThemeId?.let { return it == "db_friends_wall" }
        return dbId == "Friends" || dbId == "asset:friends.db"
    }
    return true
}

// Safety split for un-normalized Friends text: the normalized database
// stores one turn per line, but older/imported copies can hold a whole
// conversation on one line with inline <Nick> markers, which would render
// mashed into a single bubble. These two regexes find the split points —
// sentence-ending punctuation followed by an inline <Nick> marker, and a
// slash separator followed by an inline <Nick> marker.
private val friendsInlineTurnRegex1 =
    Regex("""(?<=[.!?…])[ \t]+(?=<[A-Za-z][^<>\n]{0,31}>[ \t])""")
private val friendsInlineTurnRegex2 =
    Regex("""[ \t]*/[ \t]*(?=<[A-Za-z][^<>\n]{0,31}>[ \t])""")

/** Friends turns: the usual newline turns, each further split at inline
 *  <Nick> turn boundaries so one-line conversations still get one bubble
 *  per speaker turn. */
@Composable
private fun friendsTurns(quote: Quote): List<String> =
    noveltyTurns(quote).flatMap { turn ->
        turn.replace(friendsInlineTurnRegex1, "\n")
            .replace(friendsInlineTurnRegex2, "\n")
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

@Composable
private fun FriendsCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    // v1.89.2: false = Sunset Hangout (the original), true = Wall.
    wall: Boolean = false
) {
    val turns = friendsTurns(quote)
    // v1.89.2: one layout, two palettes (see the section header above).
    val cardBg = if (wall) Color(0xFFFFFFFF) else Color(0xFF241640)
    // Top strip, header text, and incoming nick labels.
    val accent = if (wall) Color(0xFF3B5998) else Color(0xFFFFB020)
    // Incoming bubble text.
    val bodyText = if (wall) Color(0xFF333333) else Color(0xFFFFF6EC)
    // Narration lines and the rating.
    val statusColor = if (wall) Color(0xFF6D84B4) else Color(0xFFB9A8D9)
    val incomingBubble = if (wall) Color(0xFFE7EBF2) else Color(0xFF3A2A5C)
    // Mid-gradient color, also the quote-background local for nick spans.
    val outgoingMid = if (wall) Color(0xFF2B5EA7) else Color(0xFFFF7A3D)
    val outgoingGradient = if (wall) {
        // Classic Facebook blue gradient for Clint's own lines.
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF1877F2), Color(0xFF3B5998))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFF4D8D), Color(0xFFFF7A3D), Color(0xFFFFB020)
            )
        )
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        // v1.89.2: the white wall card needs a hairline edge on light pages.
        border = if (wall) BorderStroke(1.dp, Color(0xFFD3D9E6)) else null
    ) {
        Column {
            // Accent top strip (amber for sunset, Facebook blue for wall).
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(accent)
            )
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    noveltyActionRow(tint = accent, headerActions = headerActions) {
                        Text(
                            text = if (showNumber) "✦ Friends · #${quote.number}"
                            else "✦ Friends",
                            style = noveltyPlainStyle(accent, 15).copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(noveltyBubbleSpacing())) {
                    turns.forEach { turn ->
                        val nick = noveltyTurnNick(turn)
                        val isStatus = turn.trimStart().startsWith("*") || nick == null
                        if (isStatus) {
                            // Narration / action line: centered, muted italic
                            // (mauve for sunset, steel blue for wall).
                            val statusStyle = noveltyPlainStyle(statusColor, 13).copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                            val annotated = rememberNickAnnotatedText(turn, statusStyle)
                            Text(
                                text = annotated,
                                style = statusStyle,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val body = noveltyStripNickPrefix(turn)
                            val mine = nick.equals("me", ignoreCase = true) ||
                                nick.equals("clint", ignoreCase = true)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = if (mine) Arrangement.End
                                else Arrangement.Start
                            ) {
                                if (mine) {
                                    // Outgoing: gradient bubble, white text
                                    // (sunset gradient, or Facebook blue
                                    // for the wall palette).
                                    CompositionLocalProvider(
                                        LocalQuoteBackground provides outgoingMid
                                    ) {
                                        val annotated = rememberNickAnnotatedText(
                                            body, noveltyPlainStyle(Color.White)
                                        )
                                        Text(
                                            text = annotated,
                                            style = noveltyPlainStyle(Color.White),
                                            modifier = Modifier
                                                .fillMaxWidth(0.85f)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(outgoingGradient)
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                } else {
                                    // Incoming: accent nick label above the
                                    // bubble (plum for sunset, pale blue-
                                    // gray for wall).
                                    Column(Modifier.fillMaxWidth(0.85f)) {
                                        Text(
                                            text = nick.orEmpty(),
                                            style = noveltyPlainStyle(accent, 12).copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        CompositionLocalProvider(
                                            LocalQuoteBackground provides incomingBubble
                                        ) {
                                            val annotated = rememberNickAnnotatedText(
                                                body, noveltyPlainStyle(bodyText)
                                            )
                                            Text(
                                                text = annotated,
                                                style = noveltyPlainStyle(bodyText),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .background(incomingBubble)
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (showRating && quote.rating != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${quote.rating}",
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ICQ — classic ICQ client: light-blue gradient, flower logo, timestamped
// nicks in alternating colors, avatar panel, emoticon input, Send button.
// ---------------------------------------------------------------------------

@Composable
private fun IcqCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    stripNicks: Boolean = false
) {
    val windowBrush = Brush.verticalGradient(
        listOf(Color(0xFFE8F4FC), Color(0xFFCDE8F7), Color(0xFFB8DCF0))
    )
    val bodyStyle = noveltyPlainStyle(Color(0xFF1A1A1A), 14)
    val turns = noveltyTurns(quote)
    // v1.88.32: "remove nicks from message text" — strip the redundant
    // speaker prefix from each body; turns stay separate blocks.
    val bodyTurns = remember(turns, stripNicks) {
        if (stripNicks) turns.map { noveltyStripNickPrefix(it) } else turns
    }
    val annotatedTurns = noveltyAnnotatedTurns(bodyTurns, bodyStyle)
    val turnNicks = remember(turns) { turns.map { noveltyTurnNick(it) } }
    val nickA = Color(0xFF0066CC) // blue nick
    val nickB = Color(0xFFE65C00) // orange nick
    val labelColor = Color(0xFF666666)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(2.dp, Color(0xFF7FB8D9)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FC))
    ) {
        Column(Modifier.background(windowBrush)) {
            // Title bar with flower.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "❀", style = noveltyPlainStyle(Color(0xFF7FB800), 16))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "ICQ — Message",
                    style = noveltyPlainStyle(Color(0xFF1A4D6E), 13).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                if (showNumber) {
                    Text(text = "#${quote.number}", style = noveltyPlainStyle(Color(0xFF1A4D6E), 12))
                }
            }
            // Toolbar icons row.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("✉", "☎", "🎮", "😊", "📁").forEach { icon ->
                    Text(
                        text = icon,
                        style = noveltyPlainStyle(Color(0xFF1A4D6E), 15),
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
            }
            // Message area with avatar panel.
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Column(
                    Modifier
                        .weight(1f)
                        .background(Color.White)
                        .border(1.dp, Color(0xFF999999))
                        .padding(8.dp)
                ) {
                    annotatedTurns.forEachIndexed { i, turn ->
                        val nickColor = if (i % 2 == 0) nickA else nickB
                        // v1.88.32: just the nick — the fake "(0:mm)"
                        // timestamp (derived from the quote number) is
                        // gone, and turns without a parseable nick get no
                        // header instead of an invented "FriendN".
                        val nick = turnNicks[i]
                        if (nick != null) {
                            Text(
                                text = "$nick:",
                                style = noveltyPlainStyle(nickColor, 13).copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(text = turn, style = bodyStyle)
                        if (i < annotatedTurns.size - 1) Spacer(Modifier.height(6.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                // Avatar panel.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .background(Color(0xFFD6EBF7), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF999999), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "😎", style = noveltyPlainStyle(Color.Black, 28))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = "online", style = noveltyPlainStyle(Color(0xFF2E8B57), 10))
                }
            }
            // Input area with emoticon.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .background(Color.White)
                        .border(1.dp, Color(0xFF999999))
                        .padding(8.dp)
                ) {
                    Text(text = "😊", style = noveltyPlainStyle(Color.Black, 18))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Send",
                    style = noveltyPlainStyle(Color.White, 13).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .background(Color(0xFF4A9EDE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = Color(0xFF666666), headerActions = headerActions)
            }
            if (showRating && quote.rating != null) {
                Text(
                    text = "${quote.rating}",
                    style = MaterialTheme.typography.labelSmall.copy(color = labelColor),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// MSN Messenger — classic MSN chat window: butterfly logo, "says:" lines,
// display pictures on the sides, formatting toolbar, input box, Send.
// ---------------------------------------------------------------------------

@Composable
private fun MsnCard(
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    headerActions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    stripNicks: Boolean = false
) {
    val bodyStyle = noveltyPlainStyle(Color(0xFF1A1A1A), 14)
    val turns = noveltyTurns(quote)
    // v1.88.32: "remove nicks from message text" — strip the redundant
    // speaker prefix from each body; turns stay separate blocks.
    val bodyTurns = remember(turns, stripNicks) {
        if (stripNicks) turns.map { noveltyStripNickPrefix(it) } else turns
    }
    val annotatedTurns = noveltyAnnotatedTurns(bodyTurns, bodyStyle)
    val turnNicks = remember(turns) { turns.map { noveltyTurnNick(it) } }
    val nickColor = Color(0xFF0B7DDA)
    val labelColor = Color(0xFF666666)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, Color(0xFF0B7DDA)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Title bar with butterfly.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F2FB))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🦋", style = noveltyPlainStyle(Color.Black, 15))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "MSN Messenger — Conversation",
                    style = noveltyPlainStyle(Color(0xFF0B3D62), 13).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                if (showNumber) {
                    Text(text = "#${quote.number}", style = noveltyPlainStyle(Color(0xFF0B3D62), 12))
                }
            }
            // Toolbar row.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F7FC))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Invite", "Send a file", "Video", "Games").forEach { item ->
                    Text(
                        text = item,
                        style = noveltyPlainStyle(Color(0xFF0B7DDA), 12),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
            // Message area flanked by display pictures.
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                // My display picture.
                Box(
                    Modifier
                        .size(52.dp)
                        .background(Color(0xFFD6EBF7), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF999999), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🙂", style = noveltyPlainStyle(Color.Black, 26))
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalQuoteBackground provides Color.White) {
                        annotatedTurns.forEachIndexed { i, turn ->
                            // v1.88.32: just the nick — no invented
                            // "FriendN" fallback; no parseable nick means
                            // no header line for that turn.
                            val nick = turnNicks[i]
                            if (nick != null) {
                                Text(
                                    text = "$nick says:",
                                    style = noveltyPlainStyle(nickColor, 13).copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(text = turn, style = bodyStyle)
                            if (i < annotatedTurns.size - 1) Spacer(Modifier.height(6.dp))
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                // Their display picture.
                Box(
                    Modifier
                        .size(52.dp)
                        .background(Color(0xFFF7E8D6), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF999999), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "😎", style = noveltyPlainStyle(Color.Black, 26))
                }
            }
            // Formatting toolbar + input + Send.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("B", "I", "U", "😊").forEach { b ->
                    Text(
                        text = b,
                        style = noveltyPlainStyle(Color(0xFF333333), 13).copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Send",
                    style = noveltyPlainStyle(Color.White, 13).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .background(Color(0xFF0B7DDA), RoundedCornerShape(3.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                noveltyActionRow(tint = Color(0xFF666666), headerActions = headerActions)
            }
            if (showRating && quote.rating != null) {
                Text(
                    text = "${quote.rating}",
                    style = MaterialTheme.typography.labelSmall.copy(color = labelColor),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
