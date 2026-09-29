package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.fixtures.intentOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PuzzleSessionSanTest {
    @Test fun setupMoveHasSan() {
        assertEquals("Kd7", PuzzleSession.start(Fixtures.MATE_IN_2).setupSan)
        assertEquals("Kh8", PuzzleSession.start(Fixtures.MATE_IN_1_MULTI).setupSan)
    }

    @Test fun sanTracksPlayerMoveAndReply() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        assertEquals("Kd7", session.lastSan)
        session.submitMove(intentOf("a2e6"))
        assertEquals("Be6+", session.lastSan)
        session.applyOpponentReply()
        assertEquals("Kd8", session.lastSan)
        session.submitMove(intentOf("f7f8"))
        assertEquals("Qf8#", session.lastSan)
    }

    @Test fun wrongMoveHasSanAndIsNotApplied() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val fen = session.currentFen()
        session.submitMove(intentOf("a2b1"))
        assertEquals("Bb1", session.lastSan)
        assertEquals(fen, session.currentFen())
    }

    @Test fun promotionMoveHasSan() {
        val session = PuzzleSession.start(Fixtures.PROMOTION)
        session.submitMove(intentOf("e7e8q"))
        assertEquals("e8=Q", session.lastSan)
    }

    @Test fun resetRestoresSetupSan() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        session.submitMove(intentOf("a2e6"))
        session.reset()
        assertEquals("Kd7", session.lastSan)
    }
}
