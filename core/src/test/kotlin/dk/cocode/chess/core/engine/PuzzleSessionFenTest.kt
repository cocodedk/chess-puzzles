package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.fixtures.intentOf
import dk.cocode.chess.core.model.Puzzle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The FEN of the position on the board now: what the Share position button hands to another app. */
class PuzzleSessionFenTest {
    private fun puzzle(fen: String, vararg moves: String) = Puzzle("FEN", fen, moves.toList(), 1000)

    @Test fun fenIsTheBoardAfterTheSetupMoveAndNothingLater() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        assertEquals("q5nr/1ppknQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 w - - 1 18", session.currentFen())
    }

    @Test fun fenFollowsTheSideToMoveAndTheMoveNumber() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        session.submitMove(intentOf("a2e6"))
        assertEquals("q5nr/1ppknQpp/3pB3/1P2p3/4P3/B1PP1b2/6PP/5K2 b - - 2 18", session.currentFen()) // black to move
        session.applyOpponentReply()
        assertEquals("q2k2nr/1pp1nQpp/3pB3/1P2p3/4P3/B1PP1b2/6PP/5K2 w - - 3 19", session.currentFen())
    }

    @Test fun aWrongMoveLeavesTheFenAlone() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val before = session.currentFen()
        session.submitMove(intentOf("a2b1"))
        assertEquals(before, session.currentFen())
    }

    @Test fun fenKeepsTheCastlingRightsStillStanding() {
        val session = PuzzleSession.start(puzzle("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 5 20", "a8b8", "h1h2"))
        assertEquals("1r2k2r/8/8/8/8/8/8/R3K2R w KQk - 6 21", session.currentFen()) // black lost queenside
    }

    @Test fun fenNamesTheEnPassantSquareAfterADoublePush() {
        val session = PuzzleSession.start(puzzle("4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 30", "d7d5", "e5d6"))
        assertEquals("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 31", session.currentFen())
    }

    @Test fun resetGoesBackToTheFenAfterTheSetupMove() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val start = session.currentFen()
        session.submitMove(intentOf("a2e6"))
        session.applyOpponentReply()
        session.reset()
        assertEquals(start, session.currentFen())
    }
}
