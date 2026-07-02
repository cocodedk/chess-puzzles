package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.Puzzle
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testPuzzleRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class PromptTextTest {
    private fun puzzle(vararg themes: String) =
        Puzzle("id", "fen", listOf("e2e4", "e7e5"), 1000, themes.toList())

    @Test fun mateGoalIsDetectedFromTheSolutionNotTags() {
        assertEquals("checkmate in 1", goalText(endsInMate = true, remainingMoves = 1, puzzle = puzzle()))
        assertEquals("checkmate in 3", goalText(endsInMate = true, remainingMoves = 3, puzzle = puzzle("crushing")))
    }

    @Test fun materialAndDefenseGoalsComeFromTags() {
        assertEquals("win material", goalText(false, 2, puzzle("crushing")))
        assertEquals("gain the upper hand", goalText(false, 2, puzzle("advantage", "endgame")))
        assertEquals("find the best defense", goalText(false, 1, puzzle("defensiveMove")))
        assertEquals("save the game", goalText(false, 1, puzzle("equality")))
    }

    @Test fun defenseWinsWhenItCoOccursWithAttackTags() {
        assertEquals("find the best defense", goalText(false, 2, puzzle("defensiveMove", "crushing")))
    }

    @Test fun motifTagsAloneFallBackToBestMove() {
        assertEquals("find the best move", goalText(false, 2, puzzle("fork", "pin")))
        assertEquals("find the best move", goalText(false, 1, puzzle()))
    }

    @Test fun mateCountdownUpdatesAsTheSolutionProgresses() {
        val session = PuzzleSession.start(testPuzzleRepository().getById("M2")!!)
        assertEquals("White to move — checkmate in 2", session.prompt())
        session.submitMove(MoveIntent(Square.of("a2"), Square.of("e6"), null))
        session.applyOpponentReply()
        assertEquals("White to move — checkmate in 1", session.prompt())
    }

    @Test fun blackToMoveIsAnnounced() {
        val session = PuzzleSession.start(testPuzzleRepository().getById("BK")!!)
        assertEquals("Black to move — checkmate in 1", session.prompt())
    }
}
