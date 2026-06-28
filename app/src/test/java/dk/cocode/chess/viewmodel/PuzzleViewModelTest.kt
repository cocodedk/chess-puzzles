package dk.cocode.chess.viewmodel

import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(progress: FakeProgressRepository = FakeProgressRepository(), index: Int = 0) =
        PuzzleViewModel(testPuzzleRepository(), progress, index)

    private fun sq(name: String) = Square.of(name)

    @Test fun loadsSavedProgress() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(7, 3, 9)))
        advanceUntilIdle()
        with(viewModel.state.value) {
            assertEquals(7, solvedCount)
            assertEquals(3, currentStreak)
            assertEquals(9, bestStreak)
        }
    }

    @Test fun initialStateIsFirstPuzzle() = runTest(dispatcher) {
        val state = vm().state.value
        assertEquals(800, state.rating)
        assertFalse(state.flipped)
        assertTrue(state.promptText.startsWith("White"))
        assertNotNull(state.lastMove)
        assertEquals(PuzzleStatus.IN_PROGRESS, state.status)
    }

    @Test fun solvingMateInOne() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        viewModel.onSquareTapped(sq("b7"))
        assertEquals(sq("b7"), viewModel.state.value.selected)
        assertTrue(viewModel.state.value.legalTargets.contains(sq("g7")))
        viewModel.onSquareTapped(sq("g7"))
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.SOLVED, status)
            assertEquals(Feedback.SOLVED, feedback)
            assertEquals(1, solvedCount)
            assertEquals(1, currentStreak)
            assertEquals(1, bestStreak)
        }
        advanceUntilIdle()
        assertEquals(Progress(1, 1, 1), progress.saves.last())
    }

    @Test fun wrongMoveFailsAndResetsStreak() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(5, 4, 6))
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onSquareTapped(sq("b7"))
        viewModel.onSquareTapped(sq("b2"))
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.FAILED, status)
            assertEquals(Feedback.WRONG, feedback)
            assertEquals(0, currentStreak)
        }
        advanceUntilIdle()
        assertEquals(0, progress.saves.last().currentStreak)
    }

    @Test fun mateInTwoAutoPlaysOpponentReply() = runTest(dispatcher) {
        val viewModel = vm(index = 1)
        viewModel.onSquareTapped(sq("a2"))
        viewModel.onSquareTapped(sq("e6"))
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.IN_PROGRESS, status)
            assertEquals(Feedback.CORRECT, feedback)
            assertEquals(sq("d7"), lastMove?.from)
            assertEquals(sq("d8"), lastMove?.to)
        }
        viewModel.onSquareTapped(sq("f7"))
        viewModel.onSquareTapped(sq("f8"))
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun promotionFlow() = runTest(dispatcher) {
        val viewModel = vm(index = 2)
        viewModel.onSquareTapped(sq("e7"))
        viewModel.onSquareTapped(sq("e8"))
        assertNotNull(viewModel.state.value.pendingPromotion)
        viewModel.onPromotionChosen(PieceType.QUEEN)
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun promotionCancelAndNoPending() = runTest(dispatcher) {
        val viewModel = vm(index = 2)
        viewModel.onPromotionChosen(PieceType.QUEEN)
        assertEquals(PuzzleStatus.IN_PROGRESS, viewModel.state.value.status)
        viewModel.onSquareTapped(sq("e7"))
        viewModel.onSquareTapped(sq("e8"))
        viewModel.onPromotionCancelled()
        assertNull(viewModel.state.value.pendingPromotion)
        assertNull(viewModel.state.value.selected)
    }

    @Test fun hintHighlightsNextMoveAndIgnoredWhenSolved() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onHint()
        with(viewModel.state.value) {
            assertEquals(sq("b7"), hint?.from)
            assertEquals(sq("g7"), hint?.to)
            assertEquals(sq("b7"), selected)
        }
        viewModel.onSquareTapped(sq("g7")) // hint pre-selected b7, so tapping the target solves
        viewModel.onHint() // ignored once solved
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun resetRestoresPuzzle() = runTest(dispatcher) {
        val viewModel = vm(index = 1)
        viewModel.onSquareTapped(sq("a2"))
        viewModel.onSquareTapped(sq("e6"))
        viewModel.onReset()
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.IN_PROGRESS, status)
            assertEquals(Feedback.NONE, feedback)
            assertNull(selected)
        }
    }

    @Test fun blackPlayerFlipsBoard() = runTest(dispatcher) {
        val viewModel = vm(index = 3)
        assertTrue(viewModel.state.value.flipped)
        assertTrue(viewModel.state.value.promptText.startsWith("Black"))
    }

    @Test fun nextAdvancesAndWraps() = runTest(dispatcher) {
        val viewModel = vm(index = 3) // last puzzle
        viewModel.onNext()
        assertEquals(800, viewModel.state.value.rating) // wrapped to the first puzzle
        viewModel.onNext()
        assertEquals(1500, viewModel.state.value.rating) // second puzzle
    }
}
