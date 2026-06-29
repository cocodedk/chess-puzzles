package dk.cocode.chess.viewmodel

import dk.cocode.chess.FakeProgressRepository
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

/** Difficulty-band navigation: jumping between bands and advancing within the current band. */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleNavigationTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(progress: FakeProgressRepository = FakeProgressRepository()) =
        PuzzleViewModel(testPuzzleRepository(), progress)

    @Test fun nextStaysWithinDifficultyAndWraps() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(index = 3)) // BK, last in the easy band [0,2,3]
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onNext()
        assertEquals(800, viewModel.state.value.rating) // wrapped to the first easy puzzle (index 0)
        advanceUntilIdle()
        assertEquals(0, progress.current().index)
        viewModel.onNext()
        assertEquals(1000, viewModel.state.value.rating) // next easy puzzle (index 2); skips the 1500 medium
    }

    @Test fun difficultyPickerJumpsToBand() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        advanceUntilIdle()
        assertEquals(800, viewModel.state.value.rating) // starts in the easy band
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        assertEquals(1500, viewModel.state.value.rating)
        advanceUntilIdle()
        assertEquals(1, progress.current().index)
        viewModel.onDifficultySelected(Difficulty.HARD)
        assertEquals(2100, viewModel.state.value.rating)
        viewModel.onDifficultySelected(Difficulty.EASY)
        assertEquals(800, viewModel.state.value.rating)
    }

    @Test fun selectingTheCurrentBandKeepsYourPlace() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository())
        advanceUntilIdle()
        viewModel.onNext() // advance within the easy band: index 0 -> index 2 (rating 1000)
        assertEquals(1000, viewModel.state.value.rating)
        viewModel.onDifficultySelected(Difficulty.EASY) // already easy -> no-op, not bounced to index 0
        assertEquals(1000, viewModel.state.value.rating)
    }

    @Test fun nextOnASoloBandPuzzleStaysPut() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository())
        advanceUntilIdle()
        viewModel.onDifficultySelected(Difficulty.HARD) // index 4, the only hard puzzle
        viewModel.onNext() // band size 1 -> no-op; must not wipe/reload the position
        assertEquals(2100, viewModel.state.value.rating)
    }

    @Test fun revisitingASolvedPuzzleDoesNotRecount() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // solve M1 (index 0)
        advanceUntilIdle()
        assertEquals(1, progress.current().solvedCount)
        viewModel.onDifficultySelected(Difficulty.MEDIUM) // leave the easy band
        viewModel.onDifficultySelected(Difficulty.EASY) // jump back to M1 (index 0)
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // solve it again
        advanceUntilIdle()
        assertEquals(1, progress.current().solvedCount) // still 1 — already counted, not re-earned
    }

    @Test fun availableDifficultiesAreThePopulatedBands() {
        assertEquals(listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD), vm().availableDifficulties)
    }
}
