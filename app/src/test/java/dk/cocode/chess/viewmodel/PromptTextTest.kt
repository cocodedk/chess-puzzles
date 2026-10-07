package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Puzzle
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testPuzzleRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class PromptTextTest : EnglishTexts() {
    private fun puzzle(vararg themes: String) =
        Puzzle("id", "fen", listOf("e2e4", "e7e5"), 1000, themes.toList())

    private fun said(goal: Goal, movesLeft: Int = 1) = promptText(Prompt(PieceColor.WHITE, goal, movesLeft))

    @Test fun mateGoalIsDetectedFromTheSolutionNotTags() {
        assertEquals(Goal.MATE, goalOf(endsInMate = true, puzzle = puzzle()))
        assertEquals(Goal.MATE, goalOf(endsInMate = true, puzzle = puzzle("crushing")))
    }

    @Test fun materialAndDefenseGoalsComeFromTags() {
        assertEquals(Goal.MATERIAL, goalOf(false, puzzle("crushing")))
        assertEquals(Goal.UPPER_HAND, goalOf(false, puzzle("advantage", "endgame")))
        assertEquals(Goal.DEFENSE, goalOf(false, puzzle("defensiveMove")))
        assertEquals(Goal.SAVE, goalOf(false, puzzle("equality")))
    }

    @Test fun defenseWinsWhenItCoOccursWithAttackTags() {
        assertEquals(Goal.DEFENSE, goalOf(false, puzzle("defensiveMove", "crushing")))
    }

    @Test fun motifTagsAloneFallBackToBestMove() {
        assertEquals(Goal.BEST_MOVE, goalOf(false, puzzle("fork", "pin")))
        assertEquals(Goal.BEST_MOVE, goalOf(false, puzzle()))
    }

    @Test fun everyGoalIsPutIntoWords() {
        assertEquals("White to move — checkmate in 3", said(Goal.MATE, movesLeft = 3))
        assertEquals("White to move — find the best defense", said(Goal.DEFENSE))
        assertEquals("White to move — save the game", said(Goal.SAVE))
        assertEquals("White to move — win material", said(Goal.MATERIAL))
        assertEquals("White to move — gain the upper hand", said(Goal.UPPER_HAND))
        assertEquals("White to move — find the best move", said(Goal.BEST_MOVE))
    }

    @Test fun mateCountdownUpdatesAsTheSolutionProgresses() {
        val session = PuzzleSession.start(testPuzzleRepository().getById("M2")!!)
        assertEquals(Prompt(PieceColor.WHITE, Goal.MATE, 2), session.prompt())
        assertEquals("White to move — checkmate in 2", promptText(session.prompt()))
        session.submitMove(MoveIntent(Square.of("a2"), Square.of("e6"), null))
        session.applyOpponentReply()
        assertEquals("White to move — checkmate in 1", promptText(session.prompt()))
    }

    @Test fun blackToMoveIsAnnounced() {
        val session = PuzzleSession.start(testPuzzleRepository().getById("BK")!!)
        assertEquals("Black to move — checkmate in 1", promptText(session.prompt()))
    }

    @Test fun theLineUnderTheTitleIsThePromptOrSolved() {
        val prompt = Prompt(PieceColor.BLACK, Goal.BEST_MOVE, 1)
        assertEquals("", promptLine(PuzzleUiState()))
        assertEquals("Black to move — find the best move", promptLine(PuzzleUiState(prompt = prompt)))
        assertEquals("Solved!", promptLine(PuzzleUiState(prompt = prompt, status = PuzzleStatus.SOLVED)))
    }
}
