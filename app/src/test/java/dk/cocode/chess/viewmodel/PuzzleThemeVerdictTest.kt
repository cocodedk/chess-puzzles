package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.FakeProgressRepository
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

/**
 * Choosing a theme that keeps the puzzle on screen is an event like a hint or a tap: it outdates the verdict on typed text
 * that is no move, while the theme sentence is said, and leaves the verdict on a real move alone. On [themedPuzzleRepository]
 * puzzle 0 (mate in 1, b7 to g7) is the first fork and puzzle 4 (mate in 2, Be6 first) the first Medium fork.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleThemeVerdictTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm() = PuzzleViewModel(themedPuzzleRepository(), FakeProgressRepository(), this) { 100 }

    @Test fun aThemeThatKeepsThePuzzleLeavesNoVerdictOnTextThatIsNoMove() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onMoveTyped("e2e4")
        assertEquals(Feedback.ILLEGAL, viewModel.state.value.feedback)
        viewModel.onThemeSelected(PuzzleTheme.FORK) // puzzle 0 is the first fork: it stays
        assertEquals(PuzzleTheme.FORK, viewModel.state.value.theme)
        assertEquals(Feedback.NONE, viewModel.state.value.feedback)
    }

    @Test fun aThemeThatKeepsThePuzzleKeepsTheVerdictOnAWrongOrACorrectMove() = runTest(dispatcher) {
        val wrong = vm()
        advanceUntilIdle()
        wrong.onMoveTyped("Qb2")
        wrong.onThemeSelected(PuzzleTheme.FORK)
        assertEquals(Feedback.WRONG, wrong.state.value.feedback)

        val correct = vm()
        advanceUntilIdle()
        correct.onDifficultySelected(Difficulty.MEDIUM)
        correct.onMoveTyped("Be6")
        assertEquals(Feedback.CORRECT, correct.state.value.feedback)
        correct.onThemeSelected(PuzzleTheme.FORK) // puzzle 4 is the first Medium fork: it stays
        assertEquals(PuzzleTheme.FORK, correct.state.value.theme)
        assertEquals(Feedback.CORRECT, correct.state.value.feedback)
    }
}
