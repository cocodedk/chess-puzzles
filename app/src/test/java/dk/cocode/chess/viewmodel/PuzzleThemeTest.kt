package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.Progress
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Choosing puzzles by theme, on [themedPuzzleRepository]: fork is puzzles 0, 2, 3 of Easy, 4, 6 of Medium and 7 of Hard,
 * endgame 0, 1 | 5 | 8. Puzzles 0, 2 and 3 are solved by b7 to g7.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleThemeTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(progress: FakeProgressRepository = FakeProgressRepository()) =
        PuzzleViewModel(themedPuzzleRepository(), progress, this) { 100 }

    /** The rating, the place and the size of the list: what the screen's title and rating say. */
    private fun PuzzleViewModel.shown() = state.value.let { Triple(it.rating, it.position, it.bandSize) }

    private fun PuzzleViewModel.tap(vararg squares: String) = squares.forEach { onSquareTapped(Square.of(it)) }

    @Test fun onlyTheThemesEveryBandHasAreOfferedAndAllIsTheStart() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        assertEquals(listOf(PuzzleTheme.FORK, PuzzleTheme.ENDGAME), viewModel.availableThemes)
        assertNull(viewModel.state.value.theme)
        assertEquals(Triple(800, 1, 4), viewModel.shown())
    }

    @Test fun aThemeShowsOnlyItsPuzzlesAndSaysSo() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        assertEquals(PuzzleTheme.FORK, viewModel.state.value.theme)
        assertEquals(Triple(800, 1, 3), viewModel.shown()) // fork is 3 of the 4 easy puzzles
        assertEquals(
            "Theme: Fork. 3 puzzles. Puzzle 1 of 3, rating 800. Black played king h8. White to move — checkmate in 1.",
            viewModel.state.value.announcement,
        )
    }

    @Test fun allIsSaidWithTheBandsCountAndOnePuzzleIsNotPlural() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        viewModel.onThemeSelected(null)
        assertNull(viewModel.state.value.theme)
        assertEquals(Triple(800, 1, 4), viewModel.shown())
        assertEquals(
            "Theme: All. 4 puzzles. Puzzle 1 of 4, rating 800. Black played king h8. White to move — checkmate in 1.",
            viewModel.state.value.announcement,
        )
        viewModel.onDifficultySelected(Difficulty.HARD)
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        assertEquals(Triple(2100, 1, 1), viewModel.shown())
        assertEquals(
            "Theme: Fork. 1 puzzle. Puzzle 1 of 1, rating 2100. Black played king d7. White to move — checkmate in 2.",
            viewModel.state.value.announcement,
        )
    }

    @Test fun aThemeStartsAtTheFirstPuzzleNotSolvedYetAndAtTheFirstWhenAllAre() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.tap("b7", "g7") // solve puzzle 0 with All showing
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        assertEquals(Triple(1000, 2, 3), viewModel.shown()) // puzzle 2, second of the forks
        viewModel.tap("b7", "g7")
        viewModel.onNext()
        viewModel.tap("b7", "g7") // puzzle 3: every easy fork is now solved
        viewModel.onThemeSelected(null)
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        assertEquals(Triple(800, 1, 3), viewModel.shown())
    }

    @Test fun aBandChipUnderAThemeStartsAtThatBandsFirstPuzzleNotSolvedYet() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.tap("b7", "g7")
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        assertEquals(Triple(1500, 1, 2), viewModel.shown())
        viewModel.onDifficultySelected(Difficulty.HARD)
        assertEquals(Triple(2100, 1, 1), viewModel.shown())
        viewModel.onDifficultySelected(Difficulty.EASY)
        assertEquals(Triple(1000, 2, 3), viewModel.shown()) // puzzle 0 is solved
        assertEquals(PuzzleTheme.FORK, viewModel.state.value.theme)
    }

    @Test fun nextAndPreviousStayInsideTheThemesPuzzles() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        viewModel.onNext()
        assertEquals(Triple(1000, 2, 3), viewModel.shown()) // puzzle 1 is skipped: it is no fork
        viewModel.onNext()
        assertEquals(Triple(1100, 3, 3), viewModel.shown())
        viewModel.onNext()
        assertEquals(Triple(800, 1, 3), viewModel.shown())
        viewModel.onPrevious()
        assertEquals(Triple(1100, 3, 3), viewModel.shown())
    }

    @Test fun theSavedPlaceIsNotWrittenWhileAThemeIsOnAndIsWrittenAgainByAll() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(index = 1))
        val viewModel = vm(progress)
        advanceUntilIdle()
        assertEquals(Triple(900, 2, 4), viewModel.shown()) // resumed on puzzle 1, as without a theme
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        viewModel.onNext()
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        viewModel.onPrevious()
        advanceUntilIdle()
        assertEquals(Triple(1700, 2, 2), viewModel.shown())
        assertEquals(1, progress.current().index) // none of that moved it
        viewModel.onThemeSelected(null)
        advanceUntilIdle()
        assertEquals(Triple(1700, 3, 3), viewModel.shown()) // the puzzle stays; it is now the third of Medium
        assertEquals(1, progress.current().index)
        viewModel.onNext()
        advanceUntilIdle()
        assertEquals(4, progress.current().index) // plain browsing saves the place as before
    }

    @Test fun solvedCountsAndStreaksStayGlobal() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(solvedCount = 40, currentStreak = 2, bestStreak = 9))
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        viewModel.tap("b7", "g7")
        advanceUntilIdle()
        assertEquals(41, viewModel.state.value.solvedCount)
        assertEquals(3, viewModel.state.value.currentStreak)
        assertEquals(41, progress.current().solvedCount)
        assertEquals(3, viewModel.state.value.bandSize) // the title counts within the theme, the stats do not
    }

    @Test fun leavingAFailedPuzzleForAThemeStillBreaksTheStreak() = runTest(dispatcher) {
        val progress = FakeProgressRepository(Progress(currentStreak = 5, bestStreak = 5, index = 1))
        val viewModel = vm(progress)
        advanceUntilIdle()
        viewModel.tap("b2", "a2") // a wrong move on puzzle 1
        viewModel.onThemeSelected(PuzzleTheme.FORK) // leaves it for puzzle 0
        advanceUntilIdle()
        assertEquals(0, progress.current().currentStreak)
    }

    @Test fun allKeepsThePuzzleAndWhatTheMoveSoFarWas() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.tap("b7")
        viewModel.onThemeSelected(PuzzleTheme.FORK) // puzzle 0 is the first fork: it stays, selected piece and all
        assertEquals(Square.of("b7"), viewModel.state.value.selected)
        viewModel.onThemeSelected(null)
        assertEquals(Square.of("b7"), viewModel.state.value.selected)
        assertEquals(Triple(800, 1, 4), viewModel.shown())
    }

    @Test fun aRestartKeepsTheTheme() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onThemeSelected(PuzzleTheme.ENDGAME)
        viewModel.onNext()
        viewModel.onReset()
        assertEquals(PuzzleTheme.ENDGAME, viewModel.state.value.theme)
        assertEquals(Triple(900, 2, 2), viewModel.shown())
    }

    @Test fun aThemeNotOfferedOrAlreadyChosenChangesNothing() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        val before = viewModel.state.value
        viewModel.onThemeSelected(PuzzleTheme.PIN) // no puzzle has it
        viewModel.onThemeSelected(null) // All is already showing
        assertEquals(before, viewModel.state.value)
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        val forks = viewModel.state.value
        viewModel.onThemeSelected(PuzzleTheme.FORK)
        assertEquals(forks, viewModel.state.value)
    }
}
