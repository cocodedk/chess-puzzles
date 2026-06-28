package dk.cocode.chess.core.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as CbPiece
import com.github.bhlangonijr.chesslib.PieceType as CbPieceType
import com.github.bhlangonijr.chesslib.Side as CbSide
import com.github.bhlangonijr.chesslib.Square as CbSquare
import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.model.Piece
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BoardMapperTest {
    @Test fun colorMapping() {
        assertEquals(PieceColor.WHITE, BoardMapper.color(CbSide.WHITE))
        assertEquals(PieceColor.BLACK, BoardMapper.color(CbSide.BLACK))
    }

    @Test fun typeMapping() {
        assertEquals(PieceType.PAWN, BoardMapper.type(CbPieceType.PAWN))
        assertEquals(PieceType.KNIGHT, BoardMapper.type(CbPieceType.KNIGHT))
        assertEquals(PieceType.BISHOP, BoardMapper.type(CbPieceType.BISHOP))
        assertEquals(PieceType.ROOK, BoardMapper.type(CbPieceType.ROOK))
        assertEquals(PieceType.QUEEN, BoardMapper.type(CbPieceType.QUEEN))
        assertEquals(PieceType.KING, BoardMapper.type(CbPieceType.KING))
        assertNull(BoardMapper.type(CbPieceType.NONE))
    }

    @Test fun pieceMapping() {
        assertNull(BoardMapper.piece(CbPiece.NONE))
        assertEquals(
            Piece(PieceColor.WHITE, PieceType.QUEEN),
            BoardMapper.piece(CbPiece.WHITE_QUEEN),
        )
        assertEquals(
            Piece(PieceColor.BLACK, PieceType.KNIGHT),
            BoardMapper.piece(CbPiece.BLACK_KNIGHT),
        )
    }

    @Test fun squareMapping() {
        assertEquals(CbSquare.E2, BoardMapper.toCbSquare(Square.of("e2")))
        assertEquals(Square.of("e2"), BoardMapper.fromCbSquare(CbSquare.E2))
        assertEquals(Square.of("h8"), BoardMapper.fromCbSquare(CbSquare.H8))
    }

    @Test fun boardViewMapping() {
        val board = Board().apply { loadFromFen(Fixtures.START_FEN) }
        val view = BoardMapper.toBoardView(board)
        assertEquals(Piece(PieceColor.WHITE, PieceType.ROOK), view.pieceAt(Square.of("a1")))
        assertEquals(Piece(PieceColor.WHITE, PieceType.KING), view.pieceAt(Square.of("e1")))
        assertEquals(Piece(PieceColor.BLACK, PieceType.KING), view.pieceAt(Square.of("e8")))
        assertNull(view.pieceAt(Square.of("e4")))
        assertEquals("RNBQKBNR", view.toRows()[0])
        assertEquals("rnbqkbnr", view.toRows()[7])
    }
}
