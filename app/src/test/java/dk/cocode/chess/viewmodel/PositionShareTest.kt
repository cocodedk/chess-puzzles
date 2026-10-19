package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.core.model.Square
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

/** Share position hands the position on the board, as FEN and nothing else, to the sharer. */
@OptIn(ExperimentalCoroutinesApi::class)
class PositionShareTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()
    private val shared = mutableListOf<String>()
    private val sharer = PositionSharer { shared += it }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm() = PuzzleViewModel(testPuzzleRepository(), FakeProgressRepository(), this) { 100 }

    private fun PuzzleViewModel.tap(vararg squares: String) = squares.forEach { onSquareTapped(Square.of(it)) }

    @Test fun sharesTheFenAfterTheOpponentsSetupMove() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onSharePosition(sharer)
        assertEquals(listOf("7k/1Q6/6K1/8/8/8/8/8 w - - 1 2"), shared) // M1: black has just played Kh8
    }

    @Test fun sharesTheBoardAsItIsAfterTheMovesPlayed() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onDifficultySelected(Difficulty.MEDIUM) // M2: setup Kd7, then Be6+ Kd8 Qf8#
        viewModel.tap("a2", "e6")
        viewModel.onSharePosition(sharer)
        assertEquals(listOf("q2k2nr/1pp1nQpp/3pB3/1P2p3/4P3/B1PP1b2/6PP/5K2 w - - 3 19"), shared)
    }

    @Test fun aWrongMoveIsNotOnTheBoardAndTheSolutionIsNotLeaked() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.onDifficultySelected(Difficulty.MEDIUM)
        viewModel.onSharePosition(sharer)
        viewModel.tap("a2", "b1") // legal, but not the solution
        viewModel.onSharePosition(sharer)
        assertEquals(2, shared.size)
        assertEquals(shared[0], shared[1])
        assertEquals("q5nr/1ppknQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 w - - 1 18", shared[0])
    }

    @Test fun sharingChangesNothingOnTheScreen() = runTest(dispatcher) {
        val viewModel = vm()
        advanceUntilIdle()
        viewModel.tap("g6")
        val before = viewModel.state.value
        viewModel.onSharePosition(sharer)
        assertEquals(before, viewModel.state.value)
    }
}
