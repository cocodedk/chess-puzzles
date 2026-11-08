package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** A move written as text goes the way a tap would take it: right, wrong, solved, asked which piece, or refused aloud. */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleViewModelTypedMoveTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(index: Int = 0) = PuzzleViewModel(testPuzzleRepository(), FakeProgressRepository(Progress(index = index)), this)

    @Test fun theWinningMoveInEitherNotationSolvesThePuzzle() = runTest(dispatcher) {
        listOf("Qg7", "b7g7", " B7 G7 ").forEach { text ->
            val viewModel = vm()
            viewModel.onMoveTyped(text)
            assertEquals(text, PuzzleStatus.SOLVED, viewModel.state.value.status)
            assertEquals(Feedback.SOLVED, viewModel.state.value.feedback)
        }
    }

    @Test fun aLegalMoveThatIsNotTheAnswerIsWrongAndPlayContinues() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onMoveTyped("Qb2")
        assertEquals(Feedback.WRONG, viewModel.state.value.feedback)
        assertEquals(PuzzleStatus.IN_PROGRESS, viewModel.state.value.status)
        viewModel.onMoveTyped("Qg7")
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun textThatIsNoLegalMoveChangesNothingAndSaysSo() = runTest(dispatcher) {
        val viewModel = vm()
        val before = viewModel.state.value
        viewModel.onSquareTapped(Square.of("b7"))
        viewModel.onMoveTyped("  e2e4 ")
        with(viewModel.state.value) {
            assertEquals(Feedback.ILLEGAL, feedback)
            assertEquals("e2e4 is not a legal move here.", announcement)
            assertEquals(PuzzleStatus.IN_PROGRESS, status)
            assertEquals(before.board, board)
            assertNull(selected)
        }
        viewModel.onMoveTyped("hello")
        assertEquals("hello is not a legal move here.", viewModel.state.value.announcement)
    }

    @Test fun aPromotionWithNoPieceAsksWhichAndOneWithAPieceJustPlays() = runTest(dispatcher) {
        val asked = vm(index = 2).also { advanceUntilIdle() }
        asked.onMoveTyped("e8")
        assertNotNull(asked.state.value.pendingPromotion)
        asked.onPromotionChosen(PieceType.QUEEN)
        assertEquals(PuzzleStatus.SOLVED, asked.state.value.status)

        val named = vm(index = 2).also { advanceUntilIdle() }
        named.onMoveTyped("e8=Q")
        assertNull(named.state.value.pendingPromotion)
        assertEquals(PuzzleStatus.SOLVED, named.state.value.status)
    }

    @Test fun nothingIsReadOnceTheLastPuzzleIsSolved() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onMoveTyped("Qg7")
        val solved = viewModel.state.value
        viewModel.onMoveTyped("e2e4")
        assertEquals(solved, viewModel.state.value)
    }
}
