package dk.cocode.chess.viewmodel

import dk.cocode.chess.FakeOpeningProgressRepository
import dk.cocode.chess.ZERO_RANDOM
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testWhiteOpening
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

/** Board-tap and drag resolution in Practise mode: select, reselect, clear, move. */
@OptIn(ExperimentalCoroutinesApi::class)
class OpeningViewModelGestureTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val whiteOpening = testWhiteOpening()

    private fun sq(name: String) = Square.of(name)

    private fun practising(): OpeningViewModel {
        val viewModel = OpeningViewModel(listOf(whiteOpening), FakeOpeningProgressRepository(), ZERO_RANDOM)
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        return viewModel
    }

    @Test fun tapSelectsThenReselectsThenClears() = runTest(dispatcher) {
        val viewModel = practising()
        viewModel.onSquareTapped(sq("e2"))
        assertEquals(sq("e2"), viewModel.state.value.selected)
        viewModel.onSquareTapped(sq("d2")) // a different square: reselect
        assertEquals(sq("d2"), viewModel.state.value.selected)
        viewModel.onSquareTapped(sq("d2")) // the same square again: clear
        assertNull(viewModel.state.value.selected)
    }

    @Test fun dragSelectsAndMoves() = runTest(dispatcher) {
        val viewModel = practising()
        viewModel.onDragStart(sq("e2"))
        // Shown while the finger is still down, as on the puzzle screen.
        assertEquals(sq("e2"), viewModel.state.value.selected)
        assertEquals(setOf(sq("e3"), sq("e4")), viewModel.state.value.legalTargets)
        viewModel.onDragEnd(sq("e4"))
        assertEquals(OpeningFeedback.CORRECT, viewModel.state.value.feedback)
    }

    @Test fun dragToAnIllegalTargetClearsTheSelection() = runTest(dispatcher) {
        val viewModel = practising()
        viewModel.onDragStart(sq("e2"))
        viewModel.onDragEnd(sq("a8"))
        assertNull(viewModel.state.value.selected)
    }

    @Test fun dragEndWithNothingSelectedIsANoOp() = runTest(dispatcher) {
        val viewModel = practising()
        viewModel.onDragEnd(sq("e4"))
        assertNull(viewModel.state.value.selected)
    }

    @Test fun dragAfterCompletionChangesNothingAndRecordsNoExtraCleanRun() = runTest(dispatcher) {
        val progress = FakeOpeningProgressRepository()
        val viewModel = OpeningViewModel(listOf(whiteOpening), progress, ZERO_RANDOM)
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onDragStart(sq("e2")); viewModel.onDragEnd(sq("e4"))
        viewModel.onDragStart(sq("g1")); viewModel.onDragEnd(sq("f3")) // completes the line
        advanceUntilIdle()
        assertEquals(listOf(whiteOpening.id), progress.recordedIds)
        val stateAfterComplete = viewModel.state.value

        viewModel.onDragStart(sq("f3"))
        viewModel.onDragEnd(sq("d4"))
        advanceUntilIdle()

        assertEquals(stateAfterComplete, viewModel.state.value)
        assertEquals(listOf(whiteOpening.id), progress.recordedIds)
    }
}
