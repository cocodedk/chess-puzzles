package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.FakeOpeningProgressRepository
import dk.cocode.chess.ZERO_RANDOM
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testBlackOpening
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Practise mode: correct/wrong/complete moves, clean-run recording, again, and a Black opening. */
@OptIn(ExperimentalCoroutinesApi::class)
class OpeningViewModelPractiseTest : EnglishTexts() {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val whiteOpening = testWhiteOpening()
    private val blackOpening = testBlackOpening()

    private fun sq(name: String) = Square.of(name)

    private fun vm(progress: FakeOpeningProgressRepository = FakeOpeningProgressRepository()) =
        OpeningViewModel(listOf(whiteOpening, blackOpening), progress, this, ZERO_RANDOM)

    @Test fun correctMoveAppliesTheReplyAndStaysClean() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onSquareTapped(sq("e2"))
        viewModel.onSquareTapped(sq("e4"))
        with(viewModel.state.value) {
            assertEquals(OpeningFeedback.CORRECT, feedback)
            assertEquals(sq("e5"), lastMove?.to)
            assertTrue(clean)
            assertNull(bookSan)
        }
    }

    @Test fun wrongMoveHintsTheBookMoveAndBreaksClean() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onSquareTapped(sq("d2"))
        viewModel.onSquareTapped(sq("d4"))
        with(viewModel.state.value) {
            assertEquals(OpeningFeedback.WRONG, feedback)
            assertEquals(sq("e2"), hint?.from)
            assertEquals(sq("e4"), hint?.to)
            assertFalse(clean)
            assertNull(selected)
            assertEquals("e4", bookSan)
        }
    }

    @Test fun completeCleanRecordsTheRunOnce() = runTest(dispatcher) {
        val progress = FakeOpeningProgressRepository()
        val viewModel = vm(progress)
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onSquareTapped(sq("e2")); viewModel.onSquareTapped(sq("e4"))
        viewModel.onSquareTapped(sq("g1")); viewModel.onSquareTapped(sq("f3"))
        assertEquals(OpeningFeedback.COMPLETE, viewModel.state.value.feedback)
        assertTrue(viewModel.state.value.clean)
        advanceUntilIdle()
        assertEquals(listOf(whiteOpening.id), progress.recordedIds)
        // Input is ignored once the line is complete.
        viewModel.onSquareTapped(sq("f3"))
        assertNull(viewModel.state.value.selected)
    }

    @Test fun completeUncleanDoesNotRecord() = runTest(dispatcher) {
        val progress = FakeOpeningProgressRepository()
        val viewModel = vm(progress)
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onSquareTapped(sq("d2")); viewModel.onSquareTapped(sq("d4")) // wrong
        viewModel.onSquareTapped(sq("e2")); viewModel.onSquareTapped(sq("e4")) // now correct
        viewModel.onSquareTapped(sq("g1")); viewModel.onSquareTapped(sq("f3"))
        assertEquals(OpeningFeedback.COMPLETE, viewModel.state.value.feedback)
        assertFalse(viewModel.state.value.clean)
        advanceUntilIdle()
        assertTrue(progress.recordedIds.isEmpty())
    }

    @Test fun againRestartsTheDrill() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onSquareTapped(sq("d2")); viewModel.onSquareTapped(sq("d4")) // wrong -> unclean
        viewModel.onAgain()
        assertTrue(viewModel.state.value.clean)
        assertEquals(OpeningFeedback.NONE, viewModel.state.value.feedback)
    }

    @Test fun blackOpeningPicksTheOpponentsFirstMoveAndFlipsTheBoard() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(blackOpening.id)
        assertTrue(viewModel.state.value.flipped)
        viewModel.onMode(OpeningMode.PRACTISE)
        assertEquals(sq("e4"), viewModel.state.value.lastMove?.to) // White's first move, auto-played
    }

    @Test fun switchingModesTwiceIsANoOp() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteOpening.id)
        viewModel.onMode(OpeningMode.LEARN) // already Learn: no-op
        assertEquals(OpeningMode.LEARN, viewModel.state.value.mode)
    }
}
