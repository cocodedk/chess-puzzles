package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Square
import org.junit.Assert.assertEquals
import org.junit.Test

/** [resolveTap]'s four branches: select-none, clear, move, and select-other. */
class BoardTapsTest {
    private val e2 = Square(4, 1)
    private val e4 = Square(4, 3)
    private val d2 = Square(3, 1)

    @Test fun selectsWhenNothingIsSelected() {
        assertEquals(Tap.Select(e2), resolveTap(selected = null, targets = emptySet(), square = e2))
    }

    @Test fun clearsWhenTheSelectionIsTappedAgain() {
        assertEquals(Tap.Clear, resolveTap(selected = e2, targets = setOf(e4), square = e2))
    }

    @Test fun movesWhenTheTappedSquareIsALegalTarget() {
        assertEquals(Tap.Move(e2, e4), resolveTap(selected = e2, targets = setOf(e4), square = e4))
    }

    @Test fun selectsAnotherSquareWhenItIsNotALegalTarget() {
        assertEquals(Tap.Select(d2), resolveTap(selected = e2, targets = setOf(e4), square = d2))
    }
}
