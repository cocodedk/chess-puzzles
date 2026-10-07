package dk.cocode.chess.viewmodel

import androidx.lifecycle.SavedStateHandle
import dk.cocode.chess.FakeOpeningProgressRepository
import dk.cocode.chess.ZERO_RANDOM
import dk.cocode.chess.core.model.Square
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** The sentences a screen reader hears on the list and in Learn, and after a restart. */
@OptIn(ExperimentalCoroutinesApi::class)
class OpeningAnnouncementTest : OpeningAnnouncementFixture() {
    @Test fun openingAnOpeningFromTheListSaysItsNameAndTheStart() = runTest(dispatcher) {
        val viewModel = vm()
        assertEquals("", viewModel.said())
        viewModel.onOpen(white.id)
        assertEquals("Test Opening white-test. Learn. Start position.", viewModel.said())
    }

    @Test fun steppingSaysTheMoveNowOnTheBoard() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(white.id)
        val forward = List(4) { viewModel.onStep(1); viewModel.said() }
        assertEquals(
            listOf(
                "Move 1, White: pawn e4.", "Move 1, Black: pawn e5.",
                "Move 2, White: knight f3.", "Move 2, Black: knight c6.",
            ),
            forward,
        )
        viewModel.onStep(-1)
        assertEquals("Move 2, White: knight f3.", viewModel.said())
        viewModel.onStep(-1)
        viewModel.onStep(-1)
        assertEquals("Move 1, White: pawn e4.", viewModel.said())
        viewModel.onStep(-1)
        assertEquals("Start position.", viewModel.said())
    }

    @Test fun steppingInABlackOpeningNamesTheSideOfEachMove() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(black.id)
        viewModel.onStep(1)
        assertEquals("Move 1, White: pawn e4.", viewModel.said())
        viewModel.onStep(1)
        assertEquals("Move 1, Black: pawn e5.", viewModel.said())
    }

    @Test fun choosingALineChipSaysItsMoveAndTheStart() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(whiteBranching.id)
        viewModel.onStep(3)
        viewModel.onLine(1)
        assertEquals("Line: move 3, Black: knight f6. Start position.", viewModel.said())
        viewModel.onLine(0)
        assertEquals("Line: move 3, Black: bishop c5. Start position.", viewModel.said())
    }

    @Test fun aLineChipOfABlackOpeningNamesWhitesMove() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(blackBranching.id)
        viewModel.onLine(0)
        assertEquals("Line: move 2, White: knight f3. Start position.", viewModel.said())
        viewModel.onLine(1)
        assertEquals("Line: move 2, White: pawn c3. Start position.", viewModel.said())
    }

    @Test fun choosingLearnSaysTheStartOrTheMoveItWasLeftAt() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(white.id)
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onMode(OpeningMode.LEARN)
        assertEquals("Learn. Start position.", viewModel.said())
        repeat(3) { viewModel.onStep(1) }
        viewModel.onMode(OpeningMode.PRACTISE)
        viewModel.onMode(OpeningMode.LEARN)
        assertEquals("Learn. Move 2, White: knight f3.", viewModel.said())
    }

    @Test fun learnIgnoresTapsAndDragsAfterPractise() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.onMode(OpeningMode.LEARN)
        val before = viewModel.state.value
        viewModel.tap("e2", "e4")
        viewModel.onDragStart(Square.of("g1"))
        viewModel.onDragEnd(Square.of("f3"))
        assertEquals(before, viewModel.state.value)
    }

    @Test fun leavingTheScreenForgetsWhatWasSaid() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(white.id)
        viewModel.onScreenLeft()
        assertEquals("", viewModel.said())
        assertEquals(white.id, viewModel.state.value.openingId)
    }

    @Test fun goingBackToTheListSaysNothingNew() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("e2", "e4")
        viewModel.onBack()
        assertEquals("", viewModel.said())
    }

    @Test fun aRestoredLearnScreenAnnouncesNothing() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf(OPENING_ID_KEY to white.id, OPENING_MODE_KEY to OpeningMode.LEARN.name))
        val viewModel = vm(handle)
        assertEquals(white.id, viewModel.state.value.openingId)
        assertEquals("", viewModel.said())
    }

    @Test fun aRestoredPractiseScreenAnnouncesNothing() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf(OPENING_ID_KEY to black.id, OPENING_MODE_KEY to OpeningMode.PRACTISE.name))
        val viewModel = vm(handle)
        assertEquals(OpeningMode.PRACTISE, viewModel.state.value.mode)
        assertEquals("", viewModel.said())
    }

    @Test fun cleanRunsArriveWithoutDisturbingTheAnnouncement() = runTest(dispatcher) {
        val progress = FakeOpeningProgressRepository()
        val viewModel = OpeningViewModel(listOf(white), progress, this@OpeningAnnouncementTest, ZERO_RANDOM)
        viewModel.onOpen(white.id)
        progress.recordCleanRun(white.id)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.state.value.cleanRuns[white.id])
        assertEquals("Test Opening white-test. Learn. Start position.", viewModel.said())
    }

    @Test fun squaresAreTappableOnlyInPractiseUntilTheLineIsComplete() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.onOpen(white.id)
        assertEquals(false, viewModel.state.value.tappable)
        viewModel.onMode(OpeningMode.PRACTISE)
        assertEquals(true, viewModel.state.value.tappable)
        viewModel.tap("e2", "e4", "g1", "f3")
        assertEquals(false, viewModel.state.value.tappable)
        viewModel.onAgain()
        assertEquals(true, viewModel.state.value.tappable)
    }
}
