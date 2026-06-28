package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.fixtures.intentOf
import dk.cocode.chess.core.fixtures.solveFully
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.model.SubmitResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PuzzleSessionTest {
    @Test fun startAppliesSetupMove() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        assertEquals(PieceColor.WHITE, session.playerColor)
        assertEquals(PieceColor.WHITE, session.state.sideToMove)
        assertEquals(PuzzleStatus.IN_PROGRESS, session.state.status)
        assertEquals("e8d7", session.state.lastMove.uci)
        assertEquals(2, session.state.totalPlayerMoves)
        assertEquals(0, session.state.playerMovesDone)
        assertEquals('k', session.state.board.pieceAt(Square.of("d7"))?.fenChar)
        assertNull(session.state.board.pieceAt(Square.of("e8")))
    }

    @Test fun startPlayerBlack() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_1_BLACK)
        assertEquals(PieceColor.BLACK, session.playerColor)
        assertEquals(PieceColor.BLACK, session.state.sideToMove)
    }

    @Test fun fullMateInTwoSolves() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val first = session.submitMove(intentOf("a2e6"))
        assertTrue(first is SubmitResult.Continues)
        assertTrue(session.state.isCheck)
        assertEquals(1, session.state.playerMovesDone)
        assertEquals("d7d8", session.applyOpponentReply().uci)
        val second = session.submitMove(intentOf("f7f8"))
        assertTrue(second is SubmitResult.Solved)
        assertEquals(PuzzleStatus.SOLVED, session.state.status)
        assertEquals(2, session.state.playerMovesDone)
        assertTrue(session.state.isCheck)
    }

    @Test fun wrongMoveFails() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val result = session.submitMove(intentOf("h2h3"))
        assertTrue(result is SubmitResult.Wrong)
        assertEquals("a2e6", (result as SubmitResult.Wrong).expected.uci)
        assertEquals(PuzzleStatus.FAILED, session.state.status)
    }

    @Test fun illegalMoveLeavesStateUnchanged() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val result = session.submitMove(MoveIntent(Square.of("a2"), Square.of("a5")))
        assertTrue(result is SubmitResult.Illegal)
        assertEquals(PuzzleStatus.IN_PROGRESS, session.state.status)
    }

    @Test fun mateInOneAcceptsListedAndAlternateMate() {
        val listed = PuzzleSession.start(Fixtures.MATE_IN_1_MULTI)
        assertTrue(listed.submitMove(intentOf("b7g7")) is SubmitResult.Solved)
        val alternate = PuzzleSession.start(Fixtures.MATE_IN_1_MULTI)
        assertTrue(alternate.submitMove(intentOf("b7h7")) is SubmitResult.Solved)
    }

    @Test fun mateInOneRejectsNonMate() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_1_MULTI)
        assertTrue(session.submitMove(intentOf("b7b2")) is SubmitResult.Wrong)
        assertEquals(PuzzleStatus.FAILED, session.state.status)
    }

    @Test fun promotionCorrectWrongAndMissing() {
        val correct = PuzzleSession.start(Fixtures.PROMOTION)
        assertTrue(correct.submitMove(intentOf("e7e8q")) is SubmitResult.Solved)
        assertEquals('Q', correct.state.board.pieceAt(Square.of("e8"))?.fenChar)

        val wrong = PuzzleSession.start(Fixtures.PROMOTION)
        assertTrue(wrong.submitMove(intentOf("e7e8n")) is SubmitResult.Wrong)

        val missing = PuzzleSession.start(Fixtures.PROMOTION)
        val result = missing.submitMove(MoveIntent(Square.of("e7"), Square.of("e8")))
        assertTrue(result is SubmitResult.Illegal)
        assertEquals(PuzzleStatus.IN_PROGRESS, missing.state.status)
    }

    @Test fun submitAfterFinishedIsIllegal() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_1_MULTI)
        session.submitMove(intentOf("b7g7"))
        assertTrue(session.submitMove(intentOf("g6g7")) is SubmitResult.Illegal)
        assertEquals(PuzzleStatus.SOLVED, session.state.status)
    }

    @Test fun legalDestinationsEmptyWhenFinished() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_1_MULTI)
        assertFalse(session.legalDestinations(Square.of("b7")).isEmpty())
        session.submitMove(intentOf("b7g7"))
        assertTrue(session.legalDestinations(Square.of("g7")).isEmpty())
    }

    @Test fun requiresPromotionDelegates() {
        val session = PuzzleSession.start(Fixtures.PROMOTION)
        assertTrue(session.requiresPromotion(Square.of("e7"), Square.of("e8")))
    }

    @Test fun hintRevealsNextMove() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        val hint = session.hint()
        assertEquals(Square.of("a2"), hint.from)
        assertEquals(Square.of("e6"), hint.to)
    }

    @Test fun resetRestoresStart() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        session.submitMove(intentOf("a2e6"))
        val state = session.reset()
        assertEquals(PuzzleStatus.IN_PROGRESS, state.status)
        assertEquals(0, state.playerMovesDone)
        assertEquals('k', state.board.pieceAt(Square.of("d7"))?.fenChar)
        assertNull(state.board.pieceAt(Square.of("e6")))
        assertEquals("e8d7", state.lastMove.uci)
    }

    @Test fun currentFenReflectsPosition() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        assertTrue(session.currentFen().startsWith("q5nr")) // position after the setup move e8d7
    }

    @Test fun applyOpponentReplyWithoutPendingThrows() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        assertThrows(IllegalStateException::class.java) { session.applyOpponentReply() }
    }

    @Test fun fixtureSolutionLinesAreValid() {
        solveFully(PuzzleSession.start(Fixtures.MATE_IN_2))
        solveFully(PuzzleSession.start(Fixtures.MATE_IN_1_MULTI))
        solveFully(PuzzleSession.start(Fixtures.PROMOTION))
        solveFully(PuzzleSession.start(Fixtures.MATE_IN_1_BLACK))
    }
}
