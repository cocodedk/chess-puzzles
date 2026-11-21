package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.Progress
import dk.cocode.chess.testPuzzleRepository
import dk.cocode.chess.themedPuzzleRepository
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

/** The dot before the title: each puzzle's latest result, saved by id on every wrong move and every solve. */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleMarkTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(progress: FakeProgressRepository) = PuzzleViewModel(testPuzzleRepository(), progress, this) { 100 }

    private fun PuzzleViewModel.tap(vararg squares: String) = squares.forEach { onSquareTapped(Square.of(it)) }

    @Test fun aPuzzleNeverPlayedIsNotSolvedYet() = runTest(dispatcher) {
        val viewModel = vm(FakeProgressRepository())
        advanceUntilIdle()
        assertEquals(PuzzleMark.UNSOLVED, viewModel.state.value.mark)
    }

    @Test fun theDotFollowsTheLatestResultEvenTwiceTheSameDay() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        val viewModel = vm(progress)
        advanceUntilIdle()
        listOf(
            Triple("b7", "b2", PuzzleMark.WRONG), // legal, not the mate
            Triple("b7", "g7", PuzzleMark.SOLVED), // solved after all
            Triple("b7", "b2", PuzzleMark.WRONG), // a later mistake turns it back
            Triple("b7", "g7", PuzzleMark.SOLVED), // a solve the daily count no longer counts
        ).forEach { (from, to, mark) ->
            if (viewModel.state.value.mark == PuzzleMark.SOLVED) viewModel.onReset()
            viewModel.tap(from, to)
            advanceUntilIdle()
            assertEquals(mark, viewModel.state.value.mark)
        }
        assertEquals(setOf("M1"), progress.current().solvedIds)
        assertEquals(1, progress.current().solvedCount)
    }

    @Test fun theDotSurvivesARestartAndBelongsToItsOwnPuzzle() = runTest(dispatcher) {
        val progress = FakeProgressRepository()
        vm(progress).apply { advanceUntilIdle(); tap("b7", "g7") }
        advanceUntilIdle()
        val restarted = vm(progress)
        advanceUntilIdle()
        assertEquals(PuzzleMark.SOLVED, restarted.state.value.mark)
        assertEquals("Puzzle 1 of 3, rating 800, solved.", restarted.state.value.announcement.substringBefore(" Black"))
        restarted.onNext()
        assertEquals(PuzzleMark.UNSOLVED, restarted.state.value.mark)
    }

    /** The first two Easy forks are puzzles 0 (rating 800) and 2 (rating 1000); one got wrong last time is not solved. */
    @Test fun aThemeStartsAfterThePuzzlesSavedAsSolved() = runTest(dispatcher) {
        val first = themedPuzzleRepository().all()[0].id
        listOf(Progress(solvedIds = setOf(first)) to 1000, Progress(wrongIds = setOf(first)) to 800).forEach { (saved, rating) ->
            val viewModel = PuzzleViewModel(themedPuzzleRepository(), FakeProgressRepository(saved), this@PuzzleMarkTest) { 100 }
            advanceUntilIdle()
            viewModel.onThemeSelected(PuzzleTheme.FORK)
            assertEquals(rating, viewModel.state.value.rating)
        }
    }
}
