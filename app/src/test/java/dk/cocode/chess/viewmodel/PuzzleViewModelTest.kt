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

    private fun vm(progress: FakeProgressRepository = FakeProgressRepository()) =
        PuzzleViewModel(testPuzzleRepository(), progress)

    private fun sq(name: String) = Square.of(name)

    @Test fun initialStateIsFirstPuzzle() = runTest(dispatcher) {
        val state = vm().state.value
        assertEquals(800, state.rating)
        assertFalse(state.flipped)
        assertTrue(state.promptText.startsWith("White"))
        assertEquals(PuzzleStatus.IN_PROGRESS, state.status)
    }

    @Test fun loadsSavedProgress() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(7, 3, 9, 0)))
        advanceUntilIdle()
        with(viewModel.state.value) {
            assertEquals(7, solvedCount)
            assertEquals(3, currentStreak)
            assertEquals(9, bestStreak)
        }
    }

    @Test fun resumesAtSavedIndex() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 2))) // promotion puzzle
        advanceUntilIdle()
        assertEquals(1000, viewModel.state.value.rating)
    }

    @Test fun staleSavedIndexFallsBackToFirstPuzzle() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 99))) // out of range
        advanceUntilIdle()
        assertEquals(800, viewModel.state.value.rating)
    }

    @Test fun solvingMateInOnePersistsProgress() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        viewModel.onSquareTapped(sq("b7"))
        viewModel.onSquareTapped(sq("g7"))
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
        assertEquals(Feedback.SOLVED, viewModel.state.value.feedback)
        advanceUntilIdle()
        assertEquals(Progress(1, 1, 1, 0), progress.current())
        assertEquals(1, viewModel.state.value.solvedCount)
    }

    @Test fun resetCannotFarmProgress() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        viewModel.onSquareTapped(sq("b7")); viewModel.onSquareTapped(sq("g7"))
        advanceUntilIdle()
        viewModel.onReset()
        viewModel.onSquareTapped(sq("b7")); viewModel.onSquareTapped(sq("g7"))
        advanceUntilIdle()
        assertEquals(1, progress.current().solvedCount) // solved twice, counted once
    }

    @Test fun wrongMoveLetsYouRetryAndResetsStreak() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(0, 5, 5, 0))
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onSquareTapped(sq("b7"))
        viewModel.onSquareTapped(sq("b2")) // legal but not the mate
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.IN_PROGRESS, status) // not locked — you can try again
            assertEquals(Feedback.WRONG, feedback)
            assertNull(hint) // the answer is not auto-revealed
            assertNull(selected)
        }
        advanceUntilIdle()
        assertEquals(0, progress.current().currentStreak) // an error still breaks the streak
        viewModel.onSquareTapped(sq("b7")); viewModel.onSquareTapped(sq("g7")) // retry succeeds
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun mateInTwoAutoPlaysReply() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 1)))
        advanceUntilIdle()
        viewModel.onSquareTapped(sq("a2"))
        viewModel.onSquareTapped(sq("e6"))
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.IN_PROGRESS, status)
            assertEquals(Feedback.CORRECT, feedback)
            assertEquals(sq("d8"), lastMove?.to)
        }
        viewModel.onSquareTapped(sq("f7"))
        viewModel.onSquareTapped(sq("f8"))
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun promotionFlow() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 2)))
        advanceUntilIdle()
        viewModel.onSquareTapped(sq("e7"))
        viewModel.onSquareTapped(sq("e8"))
        assertNotNull(viewModel.state.value.pendingPromotion)
        viewModel.onPromotionChosen(PieceType.QUEEN)
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun promotionCancelAndNoPending() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 2)))
        advanceUntilIdle()
        viewModel.onPromotionChosen(PieceType.QUEEN) // no pending -> no-op
        assertEquals(PuzzleStatus.IN_PROGRESS, viewModel.state.value.status)
        viewModel.onSquareTapped(sq("e7"))
        viewModel.onSquareTapped(sq("e8"))
        viewModel.onPromotionCancelled()
        assertNull(viewModel.state.value.pendingPromotion)
        assertNull(viewModel.state.value.selected)
    }

    @Test fun hintHighlightsAndIgnoredWhenSolved() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onHint()
        with(viewModel.state.value) {
            assertEquals(sq("b7"), hint?.from)
            assertEquals(sq("g7"), hint?.to)
            assertEquals(sq("b7"), selected)
        }
        viewModel.onSquareTapped(sq("g7")) // hint pre-selected b7
        viewModel.onHint() // ignored once solved
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun resetRestoresPuzzle() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 1)))
        advanceUntilIdle()
        viewModel.onSquareTapped(sq("a2"))
        viewModel.onSquareTapped(sq("e6"))
        viewModel.onReset()
        with(viewModel.state.value) {
            assertEquals(PuzzleStatus.IN_PROGRESS, status)
            assertEquals(Feedback.NONE, feedback)
            assertNull(selected)
        }
    }

    @Test fun nextPersistsIndexAndWraps() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(index = 3)) // last puzzle
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onNext()
        assertEquals(800, viewModel.state.value.rating) // wrapped to the first puzzle
        advanceUntilIdle()
        assertEquals(0, progress.current().index)
        viewModel.onNext()
        assertEquals(1500, viewModel.state.value.rating)
    }

    @Test fun blackPlayerFlipsBoard() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository(Progress(index = 3)))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.flipped)
        assertTrue(viewModel.state.value.promptText.startsWith("Black"))
    }
}
