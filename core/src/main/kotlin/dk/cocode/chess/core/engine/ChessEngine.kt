package dk.cocode.chess.core.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as CbPiece
import com.github.bhlangonijr.chesslib.move.Move as CbMove
import dk.cocode.chess.core.model.BoardView
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square

/** Thin, neutral-facing wrapper around a single mutable chesslib [Board]. */
internal class ChessEngine {
    private val board = Board()

    fun loadFen(fen: String) {
        board.loadFromFen(fen)
    }

    fun fen(): String = board.fen

    fun sideToMove(): PieceColor = BoardMapper.color(board.sideToMove)

    fun boardView(): BoardView = BoardMapper.toBoardView(board)

    fun isCheck(): Boolean = board.isKingAttacked

    fun legalDestinations(from: Square): List<Square> =
        board.legalMoves()
            .filter { BoardMapper.fromCbSquare(it.from) == from }
            .map { BoardMapper.fromCbSquare(it.to) }
            .distinct()

    fun requiresPromotion(from: Square, to: Square): Boolean =
        board.legalMoves().any {
            BoardMapper.fromCbSquare(it.from) == from &&
                BoardMapper.fromCbSquare(it.to) == to &&
                it.promotion != CbPiece.NONE
        }

    fun isLegal(intent: MoveIntent): Boolean =
        board.legalMoves().any {
            BoardMapper.fromCbSquare(it.from) == intent.from &&
                BoardMapper.fromCbSquare(it.to) == intent.to &&
                promotionType(it) == intent.promotion
        }

    fun applyUci(uci: String): Boolean = board.doMove(CbMove(uci, board.sideToMove))

    /** Side-effect-free: would the legal move [uci] deliver checkmate? */
    fun wouldBeMate(uci: String): Boolean {
        board.doMove(CbMove(uci, board.sideToMove))
        val mate = board.isMated
        board.undoMove()
        return mate
    }

    private fun promotionType(move: CbMove): PieceType? =
        if (move.promotion == CbPiece.NONE) null else BoardMapper.type(move.promotion.pieceType)
}
