package dk.cocode.chess.viewmodel

import androidx.lifecycle.SavedStateHandle
import dk.cocode.chess.FakeOpeningProgressRepository
import dk.cocode.chess.ZERO_RANDOM
import dk.cocode.chess.testWhiteOpening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** Survives process death: the open opening and its mode are recorded in a [SavedStateHandle], and
 * a fresh [OpeningViewModel] built with a handle that still holds them reopens straight into place. */
@OptIn(ExperimentalCoroutinesApi::class)
class OpeningViewModelSavedStateTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val whiteOpening = testWhiteOpening()

    private fun vm(handle: SavedStateHandle) =
        OpeningViewModel(listOf(whiteOpening), FakeOpeningProgressRepository(), ZERO_RANDOM, handle)

    @Test fun aSavedOpeningAndModeReopenStraightIntoThatPractise() = runTest(dispatcher) {
        val handle = SavedStateHandle(
            mapOf(OPENING_ID_KEY to whiteOpening.id, OPENING_MODE_KEY to OpeningMode.PRACTISE.name),
        )
        val viewModel = vm(handle)
        with(viewModel.state.value) {
            assertEquals(whiteOpening.id, openingId)
            assertEquals(OpeningMode.PRACTISE, mode)
        }
    }

    @Test fun withNoSavedOpeningConstructionLeavesTheListShowing() = runTest(dispatcher) {
        val viewModel = vm(SavedStateHandle())
        assertNull(viewModel.state.value.openingId)
    }

    @Test fun openRecordsTheOpeningAndLearnMode() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val viewModel = vm(handle)
        viewModel.onOpen(whiteOpening.id)
        assertEquals(whiteOpening.id, handle.get<String>(OPENING_ID_KEY))
        assertEquals(OpeningMode.LEARN.name, handle.get<String>(OPENING_MODE_KEY))
    }

    @Test fun modeChangeUpdatesTheRecordedMode() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val viewModel = vm(handle)
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        assertEquals(OpeningMode.PRACTISE.name, handle.get<String>(OPENING_MODE_KEY))
    }

    @Test fun backClearsTheSavedOpeningId() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val viewModel = vm(handle)
        viewModel.onOpen(whiteOpening.id)
        viewModel.onBack()
        assertNull(handle.get<String>(OPENING_ID_KEY))
    }
}
