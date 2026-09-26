package dk.cocode.chess.ui.board

import dk.cocode.chess.core.model.Square
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardGeometryTest {
    @Test fun topLeftNotFlipped() {
        val a1 = BoardGeometry.squareTopLeft(Square.of("a1"), 10f, false)
        assertEquals(0f, a1.x, 0f)
        assertEquals(70f, a1.y, 0f)
        val h8 = BoardGeometry.squareTopLeft(Square.of("h8"), 10f, false)
        assertEquals(70f, h8.x, 0f)
        assertEquals(0f, h8.y, 0f)
    }

    @Test fun topLeftFlipped() {
        val a1 = BoardGeometry.squareTopLeft(Square.of("a1"), 10f, true)
        assertEquals(70f, a1.x, 0f)
        assertEquals(0f, a1.y, 0f)
    }

    @Test fun center() {
        val c = BoardGeometry.squareCenter(Square.of("a1"), 10f, false)
        assertEquals(5f, c.x, 0f)
        assertEquals(75f, c.y, 0f)
    }

    @Test fun squareAtRoundTrip() {
        assertEquals(Square.of("a1"), BoardGeometry.squareAt(5f, 75f, 10f, false))
        assertEquals(Square.of("h8"), BoardGeometry.squareAt(75f, 5f, 10f, false))
        assertEquals(Square.of("a1"), BoardGeometry.squareAt(75f, 5f, 10f, true))
    }

    @Test fun squareAtClampsOutOfBounds() {
        assertEquals(Square.of("a1"), BoardGeometry.squareAt(-50f, 999f, 10f, false))
        assertEquals(Square.of("h8"), BoardGeometry.squareAt(999f, -50f, 10f, false))
    }

    @Test fun frameLeavesEightSquaresBetweenItsSides() {
        val board = 1000f
        val frame = BoardGeometry.frameDepth(board)
        assertEquals(58f, frame, 1e-3f)
        assertEquals(board, 2 * frame + 8 * BoardGeometry.squareSize(board), 1e-3f)
    }

    @Test fun touchesOnAFramedBoardFindTheSquareUnderThem() {
        val board = 1000f
        val frame = BoardGeometry.frameDepth(board)
        val square = BoardGeometry.squareSize(board)
        // Near a1's right edge: a frame-blind board/8 split would already call it b1.
        assertEquals(Square.of("a1"), BoardGeometry.squareOnBoard(frame + square - 2f, frame + 7.5f * square, board, false))
        assertEquals(Square.of("b2"), BoardGeometry.squareOnBoard(frame + 1.5f * square, frame + 6.5f * square, board, false))
        assertEquals(Square.of("g7"), BoardGeometry.squareOnBoard(frame + 1.5f * square, frame + 6.5f * square, board, true))
        // A touch on the frame itself clamps to the nearest edge square.
        assertEquals(Square.of("a8"), BoardGeometry.squareOnBoard(1f, 1f, board, false))
        assertEquals(Square.of("a8"), BoardGeometry.squareOnBoard(board - 1f, board - 1f, board, true))
    }

    @Test fun lightAndDark() {
        assertEquals(false, BoardGeometry.isLight(Square.of("a1")))
        assertEquals(true, BoardGeometry.isLight(Square.of("b1")))
        assertEquals(true, BoardGeometry.isLight(Square.of("a2")))
    }
}
