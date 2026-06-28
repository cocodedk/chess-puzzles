package dk.cocode.chess.viewmodel

import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testPuzzleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleViewModelGestureTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm() = PuzzleViewModel(testPuzzleRepository(), FakeProgressRepository())

    private fun sq(name: String) = Square.of(name)

    @Test fun tappingSelectedSquareDeselects() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onSquareTapped(sq("b7"))
        assertEquals(sq("b7"), viewModel.state.value.selected)
        viewModel.onSquareTapped(sq("b7"))
        assertNull(viewModel.state.value.selected)
    }

    @Test fun tappingAnotherPieceReselects() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onSquareTapped(sq("b7"))
        viewModel.onSquareTapped(sq("g6"))
        assertEquals(sq("g6"), viewModel.state.value.selected)
    }

    @Test fun tappingEmptySquareSelectsNothing() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onSquareTapped(sq("a1"))
        assertNull(viewModel.state.value.selected)
        assertTrue(viewModel.state.value.legalTargets.isEmpty())
    }

    @Test fun tapsIgnoredAfterSolved() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onSquareTapped(sq("b7"))
        viewModel.onSquareTapped(sq("g7"))
        viewModel.onSquareTapped(sq("g6"))
        assertNull(viewModel.state.value.selected)
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun dragToLegalTargetMoves() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onDragStart(sq("b7"))
        assertEquals(sq("b7"), viewModel.state.value.selected)
        viewModel.onDragEnd(sq("g7"))
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }

    @Test fun dragToNonTargetSnapsBack() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onDragStart(sq("b7"))
        viewModel.onDragEnd(sq("a1"))
        assertNull(viewModel.state.value.selected)
        assertEquals(PuzzleStatus.IN_PROGRESS, viewModel.state.value.status)
    }

    @Test fun dragIgnoredAfterSolvedAndDragEndWithoutSelection() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onSquareTapped(sq("b7"))
        viewModel.onSquareTapped(sq("g7"))
        viewModel.onDragStart(sq("b7"))
        assertNull(viewModel.state.value.selected)
        viewModel.onDragEnd(sq("g7"))
        assertEquals(PuzzleStatus.SOLVED, viewModel.state.value.status)
    }
}
