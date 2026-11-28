package com.quotebrowser.app.tests

import com.quotebrowser.app.ui.applyCleanMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.90: Clean mode substitution (ui/CleanMode.kt) — display-only censor
 * that swaps profanity for milder words. The pick is deterministic per
 * (text, word, occurrence), so membership + stability are the contract.
 */
class CleanModeTest {

    @Test fun emptyStaysEmpty() {
        assertEquals("", applyCleanMode(""))
    }

    @Test fun cleanTextUnchanged() {
        assertEquals("a perfectly fine quote", applyCleanMode("a perfectly fine quote"))
    }

    @Test fun substringOfLongerWordNotReplaced() {
        // "hell" inside "hello" has no word boundary; "fuck" inside
        // "fuckery" neither — neither may be substituted.
        assertEquals("hello there", applyCleanMode("hello there"))
        assertEquals("fuckery", applyCleanMode("fuckery"))
    }

    @Test fun fuckVariants() {
        assertTrue(applyCleanMode("fuck") in listOf("fudge", "frick", "flip"))
    }

    @Test fun capitalizedFollowsCase() {
        assertTrue(applyCleanMode("Fuck") in listOf("Fudge", "Frick", "Flip"))
    }

    @Test fun allCapsFollowsCase() {
        assertTrue(applyCleanMode("FUCK") in listOf("FUDGE", "FRICK", "FLIP"))
    }

    @Test fun punctuationPreserved() {
        val out = applyCleanMode("fuck!")
        assertTrue(out.endsWith("!"))
        assertTrue(out.dropLast(1) in listOf("fudge", "frick", "flip"))
    }

    @Test fun longestKeyWins() {
        // "fucking" must use its own variants, not "fuck" + "ing".
        assertTrue(
            applyCleanMode("fucking") in
                listOf("fudging", "fricking", "flipping")
        )
    }

    @Test fun shitVariants() {
        assertTrue(applyCleanMode("shit") in listOf("shoot", "shucks", "crud"))
    }

    @Test fun bullshitVariants() {
        assertTrue(
            applyCleanMode("bullshit") in
                listOf("baloney", "hogwash", "nonsense")
        )
    }

    @Test fun damnVariants() {
        assertTrue(applyCleanMode("damn") in listOf("darn", "dang"))
    }

    @Test fun deterministicAcrossCalls() {
        val text = "this shit is fucked, damn it"
        assertEquals(applyCleanMode(text), applyCleanMode(text))
    }

    @Test fun sentenceFullyCleaned() {
        val out = applyCleanMode("fuck this shit")
        assertTrue(!out.contains("fuck"))
        assertTrue(!out.contains("shit"))
        assertEquals(3, out.split(" ").size)
    }

    @Test fun profanityFreeWordsAroundSubstitutionUntouched() {
        val out = applyCleanMode("well fuck me")
        assertTrue(out.startsWith("well "))
        assertTrue(out.endsWith(" me"))
    }
}
