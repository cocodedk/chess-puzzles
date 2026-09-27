package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.OpeningDrill
import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testWhiteOpening
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class OpeningPractiseTest {
    private fun practise() =
        OpeningPractise(OpeningDrill(OpeningTree(testWhiteOpening()), PieceColor.WHITE, Random(0)))

    private fun sq(name: String) = Square.of(name)

    @Test fun selectFindsLegalTargets() {
        val p = practise()
        p.select(sq("e2"))
        assertEquals(sq("e2"), p.selected)
        assertTrue(sq("e4") in p.legalTargets)
    }

    @Test fun selectingAPieceWithNoLegalMovesClears() {
        val p = practise()
        p.select(sq("a8")) // Black's rook, but it's White's move
        assertNull(p.selected)
        assertTrue(p.legalTargets.isEmpty())
    }

    @Test fun clearResetsSelectionAndHint() {
        val p = practise()
        p.select(sq("e2"))
        p.clear()
        assertNull(p.selected)
        assertTrue(p.legalTargets.isEmpty())
        assertNull(p.hint)
    }

    @Test fun correctMoveAppliesTheReplyAndStaysClean() {
        val p = practise()
        val recorded = p.submit(sq("e2"), sq("e4"))
        assertFalse(recorded)
        assertEquals(OpeningFeedback.CORRECT, p.feedback)
        assertTrue(p.clean)
        assertEquals(sq("e7"), p.lastMove?.from) // the auto-played reply, e7e5
        assertNull(p.bookSan)
    }

    @Test fun wrongMoveShowsTheHintAndBreaksClean() {
        val p = practise()
        val recorded = p.submit(sq("d2"), sq("d4"))
        assertFalse(recorded)
        assertEquals(OpeningFeedback.WRONG, p.feedback)
        assertFalse(p.clean)
        assertEquals(sq("e2"), p.hint?.from)
        assertEquals(sq("e4"), p.hint?.to)
        assertEquals("e4", p.bookSan)
    }

    @Test fun aCorrectMoveClearsAPreviousWrongsBookSan() {
        val p = practise()
        p.submit(sq("d2"), sq("d4")) // wrong -> bookSan set to "e4"
        p.submit(sq("e2"), sq("e4")) // now correct
        assertNull(p.bookSan)
    }

    @Test fun illegalMoveIsIgnored() {
        val p = practise()
        val recorded = p.submit(sq("e2"), sq("e5")) // not a legal pawn move
        assertFalse(recorded)
        assertEquals(OpeningFeedback.NONE, p.feedback)
        assertTrue(p.clean)
    }

    @Test fun completingCleanIsRecordedExactlyOnce() {
        val p = practise()
        p.submit(sq("e2"), sq("e4"))
        val first = p.submit(sq("g1"), sq("f3"))
        assertTrue(first)
        assertEquals(OpeningFeedback.COMPLETE, p.feedback)
        assertTrue(p.complete)
        // The drill refuses any further submit, so this can never fire "true" a second time.
        val again = p.submit(sq("g1"), sq("f3"))
        assertFalse(again)
    }

    @Test fun completingUncleanDoesNotRecord() {
        val p = practise()
        p.submit(sq("d2"), sq("d4")) // wrong -> unclean
        p.submit(sq("e2"), sq("e4")) // now correct
        val recorded = p.submit(sq("g1"), sq("f3"))
        assertFalse(recorded)
        assertEquals(OpeningFeedback.COMPLETE, p.feedback)
        assertFalse(p.clean)
    }

    @Test fun restartResetsEverything() {
        val p = practise()
        p.submit(sq("d2"), sq("d4")) // unclean
        p.restart()
        assertTrue(p.clean)
        assertEquals(OpeningFeedback.NONE, p.feedback)
        assertNull(p.selected)
        assertFalse(p.complete)
        assertNull(p.bookSan)
    }
}
