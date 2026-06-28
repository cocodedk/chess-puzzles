package dk.cocode.chess.core.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as CbPiece
import com.github.bhlangonijr.chesslib.PieceType as CbPieceType
import com.github.bhlangonijr.chesslib.Side as CbSide
import com.github.bhlangonijr.chesslib.Square as CbSquare
import dk.cocode.chess.core.model.BoardView
import dk.cocode.chess.core.model.Piece
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square

/** The single boundary between the neutral model and the chesslib types. */
internal object BoardMapper {
    fun color(side: CbSide): PieceColor =
        if (side == CbSide.WHITE) PieceColor.WHITE else PieceColor.BLACK

    fun type(type: CbPieceType): PieceType? = when (type) {
        CbPieceType.PAWN -> PieceType.PAWN
        CbPieceType.KNIGHT -> PieceType.KNIGHT
        CbPieceType.BISHOP -> PieceType.BISHOP
        CbPieceType.ROOK -> PieceType.ROOK
        CbPieceType.QUEEN -> PieceType.QUEEN
        CbPieceType.KING -> PieceType.KING
        else -> null
    }

    fun piece(piece: CbPiece): Piece? =
        if (piece == CbPiece.NONE) null else Piece(color(piece.pieceSide), type(piece.pieceType)!!)

    fun toCbSquare(square: Square): CbSquare = CbSquare.fromValue(square.toAlgebraic().uppercase())

    fun fromCbSquare(square: CbSquare): Square = Square.of(square.name.lowercase())

    fun toBoardView(board: Board): BoardView {
        val grid = ArrayList<List<Piece?>>(8)
        for (rank in 0..7) {
            val row = ArrayList<Piece?>(8)
            for (file in 0..7) row.add(piece(board.getPiece(toCbSquare(Square(file, rank)))))
            grid.add(row)
        }
        return BoardView(grid)
    }
}
