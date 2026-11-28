package com.quotebrowser.app.tests

import com.quotebrowser.app.ui.applyIrcLineBreaks
import com.quotebrowser.app.ui.normalizeSlashBreaks
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * v1.90: the line-break normalizers the display pipeline and the length
 * filter share (ui/TtsSpeech.kt, ui/QuoteClean.kt).
 */
class QuoteCleanBreaksTest {

    @Test fun ircBackslashBecomesNewline() {
        assertEquals("turn one\nturn two", applyIrcLineBreaks("turn one \\ turn two"))
    }

    @Test fun ircNoSeparatorUnchanged() {
        assertEquals("plain text", applyIrcLineBreaks("plain text"))
    }

    @Test fun slashBeforeNickBecomesNewline() {
        assertEquals(
            "he said\n<nick> hi",
            normalizeSlashBreaks("he said / <nick> hi")
        )
    }

    @Test fun urlSlashesPreserved() {
        // The tight-join rule never fires after ":", so URLs survive.
        assertEquals(
            "see http://example.com/x",
            normalizeSlashBreaks("see http://example.com/x")
        )
    }

    @Test fun plainSlashTextUnchanged() {
        assertEquals(
            "and/or maybe",
            normalizeSlashBreaks("and/or maybe")
        )
    }
}
