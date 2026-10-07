package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Square
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** The sentences a screen reader hears in Practise, for a White opening and for a Black one. */
@OptIn(ExperimentalCoroutinesApi::class)
class OpeningPractiseAnnouncementTest : OpeningAnnouncementFixture() {
    @Test fun choosingPractiseAsWhiteSaysWhatToPlay() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        assertEquals("Practise. Play White's moves from memory.", viewModel.said())
    }

    @Test fun choosingPractiseAsBlackSaysWhitesFirstMove() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(black)
        assertEquals("Practise. Play Black's moves from memory. White played pawn e4.", viewModel.said())
    }

    @Test fun againSaysTheSameAsChoosingPractise() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("d2", "d4")
        viewModel.onAgain()
        assertEquals("Practise. Play White's moves from memory.", viewModel.said())
        viewModel.practise(black)
        viewModel.tap("e7", "e6")
        viewModel.onAgain()
        assertEquals("Practise. Play Black's moves from memory. White played pawn e4.", viewModel.said())
    }

    @Test fun aBookMoveThatTheBookAnswersAsWhite() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("e2", "e4")
        assertEquals("Book move. You played pawn e4. Black played pawn e5.", viewModel.said())
    }

    @Test fun aBookMoveThatTheBookAnswersAsBlack() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(black)
        viewModel.tap("e7", "e5")
        assertEquals("Book move. You played pawn e5. White played knight f3.", viewModel.said())
    }

    @Test fun aMoveThatIsNotTheBooksAsWhite() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("d2", "d4")
        assertEquals("You played pawn d4. Not the book move — the book plays pawn e4.", viewModel.said())
    }

    @Test fun aMoveThatIsNotTheBooksAsBlack() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(black)
        viewModel.tap("d7", "d5")
        assertEquals("You played pawn d5. Not the book move — the book plays pawn e5.", viewModel.said())
    }

    @Test fun theMoveThatEndsTheLineAfterTheBooksReplyAsWhite() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("e2", "e4", "g1", "f3")
        assertEquals("Book move. You played knight f3. Black played knight c6. Line complete.", viewModel.said())
    }

    @Test fun theMoveThatEndsTheLineAfterAMistake() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("d2", "d4", "e2", "e4", "g1", "f3")
        assertEquals(
            "Book move. You played knight f3. Black played knight c6. Line complete. Try again with no mistakes for a clean run.",
            viewModel.said(),
        )
    }

    @Test fun theMoveThatEndsTheLineWithNoReply() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(short)
        viewModel.tap("e2", "e4", "g1", "f3")
        assertEquals("Book move. You played knight f3. Line complete.", viewModel.said())
        viewModel.onAgain()
        viewModel.tap("d2", "d4", "e2", "e4", "g1", "f3")
        assertEquals("Book move. You played knight f3. Line complete. Try again with no mistakes for a clean run.", viewModel.said())
    }

    @Test fun theMoveThatEndsTheLineAsBlack() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(black)
        viewModel.tap("e7", "e5", "b8", "c6")
        assertEquals("Book move. You played knight c6. Line complete.", viewModel.said())
    }

    @Test fun selectingAPieceListsItsMoves() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("g1")
        assertEquals("Knight g1 selected. Moves: f3, h3.", viewModel.said())
        viewModel.onDragStart(Square.of("e2"))
        assertEquals("Pawn e2 selected. Moves: e3, e4.", viewModel.said())
    }

    @Test fun aTapThatSelectsNothingSaysSo() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("e5")
        assertEquals("No moves from e5.", viewModel.said())
        viewModel.onDragStart(Square.of("a8"))
        assertEquals("No moves from a8.", viewModel.said())
    }

    @Test fun tappingTheSelectionAgainClearsIt() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.tap("g1", "g1")
        assertEquals("Selection cleared.", viewModel.said())
    }

    @Test fun droppingADragOnAnIllegalSquareClearsTheSelection() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.onDragStart(Square.of("g1"))
        viewModel.onDragEnd(Square.of("a1"))
        assertEquals("Selection cleared.", viewModel.said())
    }

    @Test fun droppingADragOnALegalSquareMoves() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.practise(white)
        viewModel.onDragStart(Square.of("e2"))
        viewModel.onDragEnd(Square.of("e4"))
        assertEquals("Book move. You played pawn e4. Black played pawn e5.", viewModel.said())
    }
}
