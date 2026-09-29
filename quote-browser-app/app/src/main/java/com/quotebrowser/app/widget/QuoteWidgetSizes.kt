package com.quotebrowser.app.widget

import com.quotebrowser.app.R

/**
 * Compact 2x2 widget variant. Same behavior as the standard widget —
 * tap for a new quote, tap the text to open it — in a smaller footprint.
 */
class QuoteWidgetSmall : QuoteWidgetProvider() {
    override val layoutRes: Int = R.layout.widget_quote_small
}

/**
 * Large 4x4 widget variant. Shows long quotes with room to spare
 * (up to 20 lines); tap for a new quote, tap the text to open it.
 */
class QuoteWidgetLarge : QuoteWidgetProvider() {
    override val layoutRes: Int = R.layout.widget_quote_large
}
