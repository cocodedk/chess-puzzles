package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.PuzzleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleUiStateTest {
    @Test fun defaults() {
        val state = PuzzleUiState()
        assertEquals(8, state.board.size)
        assertEquals("        ", state.board[0])
        assertEquals(false, state.flipped)
        assertNull(state.selected)
        assertTrue(state.legalTargets.isEmpty())
        assertNull(state.lastMove)
        assertNull(state.hint)
        assertNull(state.pendingPromotion)
        assertEquals(PuzzleStatus.IN_PROGRESS, state.status)
        assertEquals(Feedback.NONE, state.feedback)
        assertEquals(0, state.rating)
        assertEquals(0, state.solvedCount)
        assertEquals(0, state.currentStreak)
        assertEquals(0, state.bestStreak)
        assertEquals("", state.promptText)
    }
}
