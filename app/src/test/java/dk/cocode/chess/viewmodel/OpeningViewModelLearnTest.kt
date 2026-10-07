package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** The list, opening/closing an opening, and Learn's line/ply navigation. */
@OptIn(ExperimentalCoroutinesApi::class)
class OpeningViewModelLearnTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val whiteOpening = testWhiteOpening()

    private fun vm(progress: FakeOpeningProgressRepository = FakeOpeningProgressRepository()) =
        OpeningViewModel(listOf(whiteOpening), progress, this, ZERO_RANDOM)

    @Test fun openEntersLearnModeAtTheStart() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        with(viewModel.state.value) {
            assertEquals(whiteOpening.id, openingId)
            assertEquals(OpeningMode.LEARN, mode)
            assertEquals(0, ply)
            assertFalse(flipped)
            assertEquals(listOf("Main line"), lineNames)
        }
    }

    @Test fun backReturnsToTheList() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        viewModel.onBack()
        assertNull(viewModel.state.value.openingId)
    }

    @Test fun stepAdvancesAndClampsAtBothEnds() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        repeat(10) { viewModel.onStep(1) }
        assertEquals(4, viewModel.state.value.ply) // 4 plies in the line
        repeat(10) { viewModel.onStep(-1) }
        assertEquals(0, viewModel.state.value.ply)
    }

    @Test fun lineSwitchResetsPly() = runTest(dispatcher) {
        val branching = whiteOpening.copy(lines = listOf("e4 e5 Nf3 Nc6", "e4 c5 Nf3 d6"))
        val viewModel = OpeningViewModel(listOf(branching), FakeOpeningProgressRepository(), this@OpeningViewModelLearnTest, ZERO_RANDOM)
        viewModel.onOpen(branching.id)
        viewModel.onStep(1)
        viewModel.onLine(1)
        with(viewModel.state.value) {
            assertEquals(1, line)
            assertEquals(0, ply)
        }
    }

    @Test fun learnModeIgnoresBoardTaps() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        viewModel.onSquareTapped(Square.of("e2"))
        assertNull(viewModel.state.value.selected)
    }

    @Test fun cleanRunsFlowReachesTheState() = runTest(dispatcher) {
        val progress = FakeOpeningProgressRepository()
        progress.recordCleanRun(whiteOpening.id)
        val viewModel = vm(progress)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.cleanRuns[whiteOpening.id])
    }
}
