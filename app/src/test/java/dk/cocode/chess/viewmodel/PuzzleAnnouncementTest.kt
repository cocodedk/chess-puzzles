package dk.cocode.chess.viewmodel

import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.Progress
import dk.cocode.chess.testPuzzleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** The exact sentences a screen reader hears, one per event. Test puzzles: M1, PR, BK (easy), M2 (medium). */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleAnnouncementTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(progress: Progress = Progress()) =
        PuzzleViewModel(testPuzzleRepository(), FakeProgressRepository(progress)) { 100 }

    private fun PuzzleViewModel.said() = state.value.announcement

    private fun PuzzleViewModel.tap(vararg squares: String) = squares.forEach { onSquareTapped(Square.of(it)) }

    private val mateInOne = "Puzzle 1 of 3, rating 800. Black played king h8. White to move — checkmate in 1."

    @Test fun nothingIsSaidBeforeProgressIsRead() = runTest(dispatcher) {
        assertEquals("", vm().said())
    }

    @Test fun firstPuzzleIsAnnouncedOnceProgressIsRead() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        assertEquals(mateInOne, viewModel.said())
    }

    @Test fun restoredPuzzleIsAnnounced() = runTest(dispatcher) {
        val viewModel = vm(Progress(index = 3)) // BK
        advanceUntilIdle()
        assertEquals("Puzzle 3 of 3, rating 900. White played king h1. Black to move — checkmate in 1.", viewModel.said())
    }

    @Test fun nextPreviousAndBandChipAnnounceThePuzzle() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onNext()
        assertEquals("Puzzle 2 of 3, rating 1000. Black played pawn a6. White to move — find the best move.", viewModel.said())
        viewModel.onPrevious()
        assertEquals(mateInOne, viewModel.said())
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        assertEquals(
            "Puzzle 1 of 1, rating 1500. Black played king d7. White to move — checkmate in 2.",
            viewModel.said(),
        )
    }

    @Test fun selectingAPieceListsItsMoves() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.tap("g6")
        assertEquals("King g6 selected. Moves: f5, g5, h5, f6, h6, f7.", viewModel.said())
    }

    @Test fun startingADragSelects() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onDragStart(Square.of("g6"))
        assertEquals("King g6 selected. Moves: f5, g5, h5, f6, h6, f7.", viewModel.said())
    }

    @Test fun aTapThatSelectsNothingSaysSo() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.tap("e5")
        assertEquals("No moves from e5.", viewModel.said())
        viewModel.tap("h8") // the opponent's piece
        assertEquals("No moves from h8.", viewModel.said())
        viewModel.onDifficultySelected(Difficulty.MEDIUM) // M2, where White's e4 pawn is blocked by e5
        viewModel.tap("e4") // a piece with no moves
        assertEquals("No moves from e4.", viewModel.said())
    }

    @Test fun tappingTheSelectionAgainClearsIt() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.tap("g6", "g6")
        assertEquals("Selection cleared.", viewModel.said())
    }

    @Test fun droppingADragOnAnIllegalSquareClearsTheSelection() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onDragStart(Square.of("g6"))
        viewModel.onDragEnd(Square.of("a1"))
        assertEquals("Selection cleared.", viewModel.said())
    }

    @Test fun aCorrectMoveThatTheOpponentAnswers() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        viewModel.tap("a2", "e6")
        assertEquals("Correct. You played bishop e6, check. Black played king d8.", viewModel.said())
    }

    @Test fun aWrongMove() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        viewModel.tap("a2", "b1")
        assertEquals("You played bishop b1. Not the best move — try again.", viewModel.said())
    }

    @Test fun theSolvingMove() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.tap("b7", "g7")
        assertEquals("Solved. You played queen g7, checkmate.", viewModel.said())
    }

    @Test fun aHint() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onHint()
        assertEquals("Hint: queen b7 to g7.", viewModel.said())
    }

    @Test fun reset() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.tap("b7", "g7")
        viewModel.onReset()
        assertEquals("Puzzle reset. White to move — checkmate in 1.", viewModel.said())
    }

    @Test fun aPromotionIsAnnouncedWhenThePieceIsChosen() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onNext()
        viewModel.tap("e7", "e8")
        assertEquals("Pawn e7 selected. Moves: e8.", viewModel.said()) // the chooser says nothing new
        viewModel.onPromotionChosen(PieceType.ROOK)
        assertEquals("You played pawn e8, promotes to rook. Not the best move — try again.", viewModel.said())
        viewModel.tap("e7", "e8")
        viewModel.onPromotionChosen(PieceType.QUEEN)
        assertEquals("Solved. You played pawn e8, promotes to queen.", viewModel.said())
    }

    @Test fun cancellingThePromotionChooserClearsTheSelection() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onNext()
        viewModel.tap("e7", "e8")
        viewModel.onPromotionCancelled()
        assertEquals("Selection cleared.", viewModel.said())
    }
}
