package com.quotebrowser.app.tests

import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.ShareOptions
import com.quotebrowser.app.ui.formatQuoteForSharing
import com.quotebrowser.app.ui.stripUsernames
import org.junit.Assert.assertEquals
import org.junit.Test

/** v1.90: username stripping + share-text formatting (ui/ShareUtils.kt). */
class ShareUtilsTest {

    @Test fun stripsIrcAngleNick() {
        assertEquals("hello all", stripUsernames("<someone> hello all"))
    }

    @Test fun stripsColonNick() {
        assertEquals("hey there", stripUsernames("bloodninja: hey there"))
    }

    @Test fun stripsPipePairNick() {
        assertEquals("joined line", stripUsernames("nick1 | nick2: joined line"))
    }

    @Test fun stripsPerLine() {
        val input = "<alpha> first\n<beta> second"
        assertEquals("first\nsecond", stripUsernames(input))
    }

    @Test fun plainTextUntouched() {
        assertEquals("Say hello: friend", stripUsernames("Say hello: friend"))
        assertEquals("no prefix here", stripUsernames("no prefix here"))
    }

    private fun quote(
        number: Int = 7,
        rating: Int? = 42,
        text: String = "the quote body",
        url: String = "https://example.com/q/7"
    ) = Quote(number = number, rating = rating, text = text, url = url)

    @Test fun defaultShareIsBareText() {
        assertEquals("the quote body", formatQuoteForSharing(quote()))
    }

    @Test fun numberAndRatingHeader() {
        val out = formatQuoteForSharing(
            quote(),
            ShareOptions(includeNumber = true, includeRating = true)
        )
        assertEquals("#7 (rating 42)\n\nthe quote body", out)
    }

    @Test fun unratedLabel() {
        val out = formatQuoteForSharing(
            quote(rating = null),
            ShareOptions(includeNumber = true, includeRating = true)
        )
        assertEquals("#7 (rating unrated)\n\nthe quote body", out)
    }

    @Test fun ratingOnlyHeader() {
        val out = formatQuoteForSharing(
            quote(),
            ShareOptions(includeRating = true)
        )
        assertEquals("Rating: 42\n\nthe quote body", out)
    }

    @Test fun urlAppended() {
        val out = formatQuoteForSharing(
            quote(),
            ShareOptions(includeUrl = true)
        )
        assertEquals("the quote body\n\nhttps://example.com/q/7", out)
    }

    @Test fun categoriesAppended() {
        val out = formatQuoteForSharing(
            quote(),
            ShareOptions(includeCategories = true),
            categories = listOf("funny", "classic")
        )
        assertEquals("the quote body\n\nCategories: funny, classic", out)
    }

    @Test fun stripUsernamesOption() {
        val out = formatQuoteForSharing(
            quote(text = "<nick> hello"),
            ShareOptions(stripUsernames = true)
        )
        assertEquals("hello", out)
    }

    @Test fun spaceOutLinesOption() {
        val out = formatQuoteForSharing(
            quote(text = "one\n\ntwo\nthree"),
            ShareOptions(spaceOutLines = true)
        )
        assertEquals("one\n\ntwo\n\nthree", out)
    }
}
