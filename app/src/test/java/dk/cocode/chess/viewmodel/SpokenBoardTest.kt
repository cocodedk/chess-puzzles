package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.core.model.Square
import org.junit.Assert.assertEquals
import org.junit.Test

class SpokenBoardTest : EnglishTexts() {
    private fun sq(name: String) = Square.of(name)

    // Row 0 is rank 1: a white knight on e4 and g1, a black pawn on d5, a white king on e1, a black queen on d8.
    private val board = listOf(
        "    K N ", "        ", "        ", "    N   ",
        "   p    ", "        ", "        ", "   q    ",
    )
    private val state = PuzzleUiState(board = board)

    @Test fun emptySquare() = assertEquals("a3, empty", squareDescription(state, sq("a3")))

    @Test fun pieceWithoutStates() {
        assertEquals("e1, white king", squareDescription(state, sq("e1")))
        assertEquals("d8, black queen", squareDescription(state, sq("d8")))
    }

    @Test fun lastMoveOnEitherEnd() {
        val moved = state.copy(lastMove = Highlight(sq("e2"), sq("e4")))
        assertEquals("e4, white knight, last move", squareDescription(moved, sq("e4")))
        assertEquals("e2, empty, last move", squareDescription(moved, sq("e2")))
    }

    @Test fun legalMoveOnEmptyAndOccupiedSquares() {
        val selected = state.copy(selected = sq("g1"), legalTargets = setOf(sq("f3"), sq("d5")))
        assertEquals("f3, empty, legal move", squareDescription(selected, sq("f3")))
        assertEquals("d5, black pawn, legal move", squareDescription(selected, sq("d5")))
    }

    @Test fun selectedAndHint() {
        val hinted = state.copy(selected = sq("g1"), hint = Highlight(sq("g1"), sq("f3")))
        assertEquals("g1, white knight, selected, hint", squareDescription(hinted, sq("g1")))
        assertEquals("f3, empty, hint", squareDescription(hinted, sq("f3")))
    }

    @Test fun statesComeInOrder() {
        val all = state.copy(
            selected = sq("e4"), legalTargets = setOf(sq("e4")),
            lastMove = Highlight(sq("e4"), sq("e5")), hint = Highlight(sq("e3"), sq("e4")),
        )
        assertEquals("e4, white knight, selected, legal move, last move, hint", squareDescription(all, sq("e4")))
    }

    @Test fun everyPieceHasAName() {
        val pieces = state.copy(board = List(8) { "KQRBNPkq" })
        assertEquals(
            listOf("king", "queen", "rook", "bishop", "knight", "pawn"),
            "abcdef".map { squareDescription(pieces, sq("${it}1")).substringAfter("white ") },
        )
    }

    @Test fun selectionSentenceListsTargetsFromA1UpTheBoard() {
        val targets = setOf(sq("h3"), sq("f3"))
        assertEquals("Knight g1 selected. Moves: f3, h3.", selectionSentence(state, sq("g1"), targets))
        assertEquals("Pawn d5 selected. Moves: a1, d4.", selectionSentence(state, sq("d5"), setOf(sq("d4"), sq("a1"))))
    }

    @Test fun hintSentenceNamesThePieceLowercase() {
        assertEquals("Hint: knight g1 to f3.", hintSentence(state, Highlight(sq("g1"), sq("f3"))))
    }
}
