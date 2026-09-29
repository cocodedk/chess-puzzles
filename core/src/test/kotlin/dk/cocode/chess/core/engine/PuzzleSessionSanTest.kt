package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.fixtures.intentOf
import dk.cocode.chess.core.model.SubmitResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PuzzleSessionSanTest {
    @Test fun setupMoveHasSan() {
        assertEquals("Kd7", PuzzleSession.start(Fixtures.MATE_IN_2).setupSan)
        assertEquals("Kh8", PuzzleSession.start(Fixtures.MATE_IN_1_MULTI).setupSan)
    }

    @Test fun resultsCarryThePlayersMoveAndTheReply() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val first = session.submitMove(intentOf("a2e6")) as SubmitResult.Continues
        assertEquals("Be6+", first.san)
        assertEquals("Kd8", first.replySan)
        session.applyOpponentReply()
        assertEquals("Qf8#", (session.submitMove(intentOf("f7f8")) as SubmitResult.Solved).san)
    }

    @Test fun wrongMoveHasSanAndIsNotApplied() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val fen = session.currentFen()
        assertEquals("Bb1", (session.submitMove(intentOf("a2b1")) as SubmitResult.Wrong).san)
        assertEquals(fen, session.currentFen())
    }

    @Test fun promotionMoveHasSan() {
        val session = PuzzleSession.start(Fixtures.PROMOTION)
        assertEquals("e8=Q", (session.submitMove(intentOf("e7e8q")) as SubmitResult.Solved).san)
    }
}
