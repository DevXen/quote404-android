package com.quotebrowser.app.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.layouts.LayoutBlock
import com.quotebrowser.app.data.layouts.LayoutBlockType
import com.quotebrowser.app.data.layouts.LayoutDef
import com.quotebrowser.app.ui.components.MoreFavoriteButtons
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.RatingBadge
import com.quotebrowser.app.ui.components.SourceChip
import com.quotebrowser.app.ui.components.VoteThumbButtons
import com.quotebrowser.app.ui.components.translatedBodyText
import com.quotebrowser.app.ui.quoteTextStyle
import com.quotebrowser.app.ui.rememberNickAnnotatedText

/**
 * v1.85: renders a quote card from a [LayoutDef] + the active theme.
 *
 * The Classic layout is NOT rendered here — QuoteRow keeps its exact
 * pre-1.85 code path for Classic (pixel-faithful by construction,
 * site-theme lockstep untouched). Every other layout renders through
 * [LayoutCard] below, using the resolved palette theme's colors so theme
 * (colors/fonts) and layout (arrangement) stay independent.
 */

private fun blockAlignment(align: String): Alignment.Horizontal = when (align) {
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
}

private fun blockArrangement(align: String): Arrangement.Horizontal = when (align) {
    "center" -> Arrangement.Center
    "end" -> Arrangement.End
    "spread" -> Arrangement.SpaceBetween
    else -> Arrangement.Start
}

private fun blockTextAlign(align: String): TextAlign = when (align) {
    "center" -> TextAlign.Center
    "end" -> TextAlign.End
    "justify" -> TextAlign.Justify
    else -> TextAlign.Start
}

@Composable
fun LayoutCard(
    quote: Quote,
    layout: LayoutDef,
    /** Site/database display name for the header block. */
    siteName: String?,
    sourceLabel: String?,
    sourceDbId: String?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    showFavoriteAction: Boolean = true,
    onMoreClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    myRating: Int = 0,
    onRate: ((Int) -> Unit)? = null,
    dbId: String? = null,
    /** Selected rows highlight, matching the classic card behavior. */
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            layout.blocks.forEach { block ->
                LayoutBlockContent(
                    block = block,
                    quote = quote,
                    siteName = siteName,
                    sourceLabel = sourceLabel,
                    sourceDbId = sourceDbId,
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite,
                    showFavoriteAction = showFavoriteAction,
                    onMoreClick = onMoreClick,
                    onShareClick = onShareClick,
                    myRating = myRating,
                    onRate = onRate,
                    dbId = dbId
                )
            }
        }
    }
}

@Composable
private fun LayoutBlockContent(
    block: LayoutBlock,
    quote: Quote,
    siteName: String?,
    sourceLabel: String?,
    sourceDbId: String?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    showFavoriteAction: Boolean,
    onMoreClick: (() -> Unit)?,
    onShareClick: (() -> Unit)?,
    myRating: Int,
    onRate: ((Int) -> Unit)?,
    dbId: String?
) {
    val pad = if (block.paddingDp > 0) Modifier.padding(vertical = block.paddingDp.dp) else Modifier
    when (block.type) {
        LayoutBlockType.SITE_HEADER -> {
            // Header-bar rule: site name left, quote number right.
            Row(
                modifier = Modifier.fillMaxWidth().then(pad),
                horizontalArrangement = blockArrangement(block.align),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (block.align != "end") {
                    Text(
                        text = siteName ?: sourceLabel ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (block.align == "spread") Spacer(Modifier.weight(1f))
                QuoteNumberBadge(number = quote.number)
            }
        }
        LayoutBlockType.QUOTE_TEXT -> {
            val base = quoteTextStyle(MaterialTheme.typography.bodyMedium)
            val annotated = rememberNickAnnotatedText(translatedBodyText(quote), base)
            Text(
                text = annotated,
                style = base.copy(
                    fontSize = base.fontSize * block.textScale,
                    textAlign = blockTextAlign(block.align)
                ),
                modifier = Modifier.fillMaxWidth().then(pad)
            )
        }
        LayoutBlockType.AUTHOR -> {
            val author = authorFor(quote)
            if (author.isNotEmpty()) {
                Text(
                    text = block.prefix + author,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * block.textScale,
                        textAlign = blockTextAlign(block.align)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().then(pad)
                )
            }
        }
        LayoutBlockType.RATING -> {
            Row(
                modifier = Modifier.fillMaxWidth().then(pad),
                horizontalArrangement = blockArrangement(block.align),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingBadge(quote.rating)
                VoteThumbButtons(myRating = myRating, onRate = onRate)
            }
        }
        LayoutBlockType.META -> {
            if (sourceLabel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().then(pad),
                    horizontalArrangement = blockArrangement(block.align)
                ) {
                    SourceChip(label = sourceLabel, dbId = sourceDbId)
                }
            }
        }
        LayoutBlockType.ACTIONS -> {
            val show = block.show.ifEmpty {
                listOf("share", "copy", "favorite", "tts")
            }
            Row(
                modifier = Modifier.fillMaxWidth().then(pad),
                horizontalArrangement = blockArrangement(block.align),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoreFavoriteButtons(
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite,
                    showFavoriteAction = showFavoriteAction && "favorite" in show,
                    onMoreClick = onMoreClick,
                    onShareClick = if ("share" in show) onShareClick else null,
                    voiceDbId = dbId ?: sourceDbId,
                    voiceNumber = quote.number,
                    explainQuote = quote
                )
            }
        }
        LayoutBlockType.DIVIDER -> HorizontalDivider(modifier = pad)
        LayoutBlockType.SPACER -> Spacer(
            Modifier.height(block.paddingDp.coerceIn(4, 64).dp).then(pad)
        )
    }
}

/** Human label for a block type, used by the picker's mini schematic. */
fun blockLabel(type: LayoutBlockType): String = when (type) {
    LayoutBlockType.SITE_HEADER -> "Header"
    LayoutBlockType.QUOTE_TEXT -> "Quote text"
    LayoutBlockType.AUTHOR -> "Author"
    LayoutBlockType.RATING -> "Rating"
    LayoutBlockType.META -> "Details"
    LayoutBlockType.ACTIONS -> "Actions"
    LayoutBlockType.DIVIDER -> "―"
    LayoutBlockType.SPACER -> "⋮"
}
