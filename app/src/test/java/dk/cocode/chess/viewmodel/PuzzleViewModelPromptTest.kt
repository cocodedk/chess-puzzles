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

/** The goal line at the ViewModel layer: countdown on progress, stable count across retries. */
@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleViewModelPromptTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun mateInTwoVm() =
        PuzzleViewModel(testPuzzleRepository(), FakeProgressRepository(Progress(index = 1)))

    @Test fun promptCountsDownAfterTheOpponentReplies() = runTest(dispatcher) {
        val viewModel = mateInTwoVm()
        advanceUntilIdle()
        assertEquals("White to move — checkmate in 2", viewModel.state.value.promptText)
        viewModel.onSquareTapped(Square.of("a2"))
        viewModel.onSquareTapped(Square.of("e6"))
        assertEquals("White to move — checkmate in 1", viewModel.state.value.promptText)
    }

    @Test fun wrongMoveMidPuzzleKeepsTheRemainingCount() = runTest(dispatcher) {
        val viewModel = mateInTwoVm()
        advanceUntilIdle()
        viewModel.onSquareTapped(Square.of("a2"))
        viewModel.onSquareTapped(Square.of("e6"))
        viewModel.onSquareTapped(Square.of("h2"))
        viewModel.onSquareTapped(Square.of("h3")) // legal but not the mating move
        with(viewModel.state.value) {
            assertEquals(Feedback.WRONG, feedback)
            assertEquals("White to move — checkmate in 1", promptText)
        }
    }
}
