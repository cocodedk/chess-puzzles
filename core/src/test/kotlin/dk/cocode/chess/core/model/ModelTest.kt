package dk.cocode.chess.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModelTest {
    @Test fun squareRoundTrip() {
        assertEquals(Square(4, 1), Square.of("e2"))
        assertEquals("e2", Square(4, 1).toAlgebraic())
        assertEquals("h8", Square.of("h8").toAlgebraic())
        assertEquals(0, Square.of("a1").index)
        assertEquals(63, Square.of("h8").index)
        assertEquals(Square(2, 3), Square.of(2, 3))
    }

    @Test fun squareInvalidThrows() {
        assertThrows(IllegalArgumentException::class.java) { Square(8, 0) }
        assertThrows(IllegalArgumentException::class.java) { Square(-1, 0) }
        assertThrows(IllegalArgumentException::class.java) { Square(0, -1) }
        assertThrows(IllegalArgumentException::class.java) { Square.of("e9") }
        assertThrows(IllegalArgumentException::class.java) { Square.of("e22") }
    }

    @Test fun pieceFenChars() {
        assertEquals('P', Piece(PieceColor.WHITE, PieceType.PAWN).fenChar)
        assertEquals('N', Piece(PieceColor.WHITE, PieceType.KNIGHT).fenChar)
        assertEquals('B', Piece(PieceColor.WHITE, PieceType.BISHOP).fenChar)
        assertEquals('R', Piece(PieceColor.WHITE, PieceType.ROOK).fenChar)
        assertEquals('Q', Piece(PieceColor.WHITE, PieceType.QUEEN).fenChar)
        assertEquals('K', Piece(PieceColor.WHITE, PieceType.KING).fenChar)
        assertEquals('p', Piece(PieceColor.BLACK, PieceType.PAWN).fenChar)
        assertEquals('n', Piece(PieceColor.BLACK, PieceType.KNIGHT).fenChar)
    }

    @Test fun colorOpposite() {
        assertEquals(PieceColor.BLACK, PieceColor.WHITE.opposite())
        assertEquals(PieceColor.WHITE, PieceColor.BLACK.opposite())
    }

    @Test fun boardViewPieceAtAndRows() {
        val empty: List<List<Piece?>> = List(8) { List<Piece?>(8) { null } }
        val bv = BoardView(empty)
        assertNull(bv.pieceAt(Square.of("a1")))
        assertEquals(List(8) { "        " }, bv.toRows())

        val grid = empty.mapIndexed { r, row ->
            row.mapIndexed { f, _ ->
                if (r == 0 && f == 4) Piece(PieceColor.WHITE, PieceType.KING) else null
            }
        }
        val placed = BoardView(grid)
        assertEquals(Piece(PieceColor.WHITE, PieceType.KING), placed.pieceAt(Square.of("e1")))
        assertEquals("    K   ", placed.toRows()[0])
    }

    @Test fun boardViewRequires8x8() {
        assertThrows(IllegalArgumentException::class.java) { BoardView(emptyList()) }
        assertThrows(IllegalArgumentException::class.java) {
            BoardView(List(8) { List<Piece?>(7) { null } })
        }
    }

    @Test fun puzzleDerivedAccessors() {
        val p = Puzzle("id", "fen", listOf("e8d7", "a2e6", "d7d8", "f7f8"), 1500, listOf("mate"))
        assertEquals("e8d7", p.setupMoveUci)
        assertEquals(listOf("a2e6", "d7d8", "f7f8"), p.solutionUciMoves)
        assertEquals(2, p.playerMoveCount)
        assertTrue(p.hasTheme("MATE"))
        assertFalse(p.hasTheme("fork"))
        assertEquals(p, p.copy())
        val (id, fen) = p
        assertEquals("id", id)
        assertEquals("fen", fen)
    }

    @Test fun puzzleRequires() {
        assertThrows(IllegalArgumentException::class.java) { Puzzle("", "fen", listOf("e2e4"), 1) }
        assertThrows(IllegalArgumentException::class.java) { Puzzle("id", "", listOf("e2e4"), 1) }
        assertThrows(IllegalArgumentException::class.java) { Puzzle("id", "fen", emptyList(), 1) }
    }
}
