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

/** One-time solve/fail accounting: each puzzle earns one solve and one streak-break at most. */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleCountingTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(progress: FakeProgressRepository, today: () -> Long = { 100 }) =
        PuzzleViewModel(testPuzzleRepository(), progress, today)

    @Test fun aFailedPuzzleBreaksTheStreakOnceAndStillEarnsItsSolve() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(index = 3)) // BK, last in the easy band [0,2,3]
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onSquareTapped(Square.of("b2")); viewModel.onSquareTapped(Square.of("a2")) // wrong
        viewModel.onNext() // wraps to M1 (index 0)
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // solve M1
        viewModel.onNext() // PR (index 2)
        viewModel.onNext() // back to BK (index 3)
        viewModel.onSquareTapped(Square.of("b2")); viewModel.onSquareTapped(Square.of("a2")) // wrong again
        viewModel.onSquareTapped(Square.of("b2")); viewModel.onSquareTapped(Square.of("g2")) // solve BK
        advanceUntilIdle()
        // BK broke the streak only on its first wrong move, and its later solve still counted.
        assertEquals(Progress(2, 2, 2, 3, 1, 100), progress.current())
    }

    @Test fun replayOfASolvedPuzzleCannotBreakTheStreak() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // solve M1
        viewModel.onReset()
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("b2")) // wrong replay
        advanceUntilIdle()
        assertEquals(Progress(1, 1, 1, 0, 1, 100), progress.current()) // no penalty: the solve already counted
    }

    @Test fun dayStreakGrowsPerDayAndLapsesWhenDisplayedStale() = runTest(dispatcher) {
        var day = 100L
        val progress = FakeProgressRepository()
        val viewModel = vm(progress) { day }
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // solve M1
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.dayStreak)
        day = 101 // next day: solve the promotion puzzle
        viewModel.onNext()
        viewModel.onSquareTapped(Square.of("e7")); viewModel.onSquareTapped(Square.of("e8"))
        viewModel.onPromotionChosen(PieceType.QUEEN)
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.dayStreak)
        day = 103 // two days later without a solve: the displayed run has lapsed
        viewModel.onReset()
        assertEquals(0, viewModel.state.value.dayStreak)
        assertEquals(2, progress.current().dayStreak) // the persisted run only restarts on the next solve
    }

    @Test fun aNewDayLetsASolvedPuzzleEarnAgain() = runTest(dispatcher) {
        var day = 100L
        val progress = FakeProgressRepository()
        val viewModel = vm(progress) { day }
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // solve M1
        day = 101 // the same ViewModel survives to the next day (app left in the background)
        viewModel.onReset()
        viewModel.onSquareTapped(Square.of("b7")); viewModel.onSquareTapped(Square.of("g7")) // replay M1
        advanceUntilIdle()
        assertEquals(Progress(2, 2, 2, 0, 2, 101), progress.current()) // the replay extends the day streak
    }
}
