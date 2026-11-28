package com.quotebrowser.app.tests

import com.quotebrowser.app.ui.applyKidSafeMask
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * v1.90: Kid-safe mask (ui/KidSafe.kt) — first letter kept, the rest
 * asterisks, output length always equals input length so annotated span
 * offsets stay valid.
 */
class KidSafeMaskTest {

    @Test fun emptyStaysEmpty() {
        assertEquals("", applyKidSafeMask(""))
    }

    @Test fun basicMask() {
        assertEquals("f***", applyKidSafeMask("fuck"))
    }

    @Test fun lengthAlwaysPreserved() {
        val text = "fuck this bullshit, damn"
        assertEquals(text.length, applyKidSafeMask(text).length)
    }

    @Test fun firstLetterCasePreserved() {
        assertEquals("F***", applyKidSafeMask("Fuck"))
        assertEquals("B*******", applyKidSafeMask("Bullshit"))
    }

    @Test fun unlistedWordsUntouched() {
        // "hell" is not on the kid-safe list; ordinary words never mask.
        assertEquals("hell and hello", applyKidSafeMask("hell and hello"))
    }

    @Test fun multipleWordsMasked() {
        assertEquals("s*** and d***", applyKidSafeMask("shit and damn"))
    }

    @Test fun punctuationUntouched() {
        assertEquals("f***!", applyKidSafeMask("fuck!"))
    }

    @Test fun cleanTextUnchanged() {
        val text = "a perfectly fine quote about ducks"
        assertEquals(text, applyKidSafeMask(text))
    }
}
