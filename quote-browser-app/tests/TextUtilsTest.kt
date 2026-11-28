package com.quotebrowser.app.tests

import com.quotebrowser.app.data.LengthMode
import com.quotebrowser.app.ui.lengthFilterOk
import com.quotebrowser.app.ui.wordCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** v1.90: word counting + the quote-length filter (ui/TextUtils.kt). */
class TextUtilsTest {

    @Test fun wordCountEmpty() {
        assertEquals(0, wordCount(""))
        assertEquals(0, wordCount("   \n\t  "))
    }

    @Test fun wordCountBasic() {
        assertEquals(1, wordCount("one"))
        assertEquals(3, wordCount("one two three"))
    }

    @Test fun wordCountWhitespaceRuns() {
        assertEquals(2, wordCount("  spaced   out  "))
        assertEquals(3, wordCount("a\nb\tc"))
    }

    private fun ok(
        text: String,
        mode: LengthMode,
        customMin: Int? = null,
        customMax: Int? = null,
        longer: Int = 10,
        shorter: Int = 3,
        normalize: Boolean = true
    ) = lengthFilterOk(text, mode, customMin, customMax, longer, shorter, normalize)

    @Test fun allModeAlwaysPasses() {
        assertTrue(ok("", LengthMode.ALL))
        assertTrue(ok("any number of words at all here", LengthMode.ALL))
    }

    @Test fun longerMode() {
        assertFalse(ok("one two three", LengthMode.LONGER, longer = 5))
        assertTrue(ok("one two three four five", LengthMode.LONGER, longer = 5))
    }

    @Test fun shorterMode() {
        assertTrue(ok("one two", LengthMode.SHORTER, shorter = 3))
        assertFalse(ok("one two three four", LengthMode.SHORTER, shorter = 3))
    }

    @Test fun customModeBounds() {
        assertFalse(ok("one", LengthMode.CUSTOM, customMin = 2, customMax = 4))
        assertTrue(ok("one two three", LengthMode.CUSTOM, customMin = 2, customMax = 4))
        assertFalse(ok("one two three four five", LengthMode.CUSTOM, customMin = 2, customMax = 4))
    }

    @Test fun customModeOpenBounds() {
        assertTrue(ok("one", LengthMode.CUSTOM, customMin = null, customMax = 4))
        assertTrue(ok("one two three four five six", LengthMode.CUSTOM, customMin = 2, customMax = null))
    }

    @Test fun oneLineMode() {
        assertTrue(ok("a single line", LengthMode.ONE_LINE))
        assertFalse(ok("two\nlines", LengthMode.ONE_LINE))
    }

    @Test fun oneLineModeIrcSeparatorCountsAsBreak() {
        // Raw text has no \n, but the display pipeline turns the IRC
        // backslash separator into a line break, so ONE_LINE must fail it.
        assertFalse(ok("turn one \\ turn two", LengthMode.ONE_LINE))
    }
}
