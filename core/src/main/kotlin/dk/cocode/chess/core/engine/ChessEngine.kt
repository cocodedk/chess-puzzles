package dk.cocode.chess.core.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as CbPiece
import com.github.bhlangonijr.chesslib.move.Move as CbMove
import com.github.bhlangonijr.chesslib.move.MoveConversionException
import com.github.bhlangonijr.chesslib.move.MoveList
import dk.cocode.chess.core.model.BoardView
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.util.toUci

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

    companion object {
        // chesslib reports an illegal/unparseable move as "... [<san token>] on <fen>".
        private val BAD_MOVE_TOKEN = Regex("\\[(.*?)]")

        private fun promotionType(move: CbMove): PieceType? =
            if (move.promotion == CbPiece.NONE) null else BoardMapper.type(move.promotion.pieceType)

        /**
         * Parses a space-separated SAN line (e.g. "e4 e5 Nf3 Nc6 Bc4 Bc5 O-O") from the standard
         * start position into lowercase UCI moves ("e2e4", "e1g1", "e7e8q"). A blank [san] parses
         * to an empty list. [MoveList.loadFromSan] resets its (thread-local, chesslib-internal)
         * board to the standard start position before replaying the line, so this is safe to call
         * repeatedly regardless of what any previous call left the board in.
         */
        fun sanLineToUci(san: String): List<String> {
            val trimmed = san.trim()
            if (trimmed.isEmpty()) return emptyList()
            val moves = MoveList()
            try {
                moves.loadFromSan(trimmed)
            } catch (e: MoveConversionException) {
                val badMove = BAD_MOVE_TOKEN.find(e.message.orEmpty())?.groupValues?.get(1) ?: trimmed
                throw IllegalArgumentException("Bad move '$badMove' in line \"$san\"", e)
            }
            return moves.map { it.toUciString() }
        }

        private fun CbMove.toUciString(): String =
            MoveIntent(BoardMapper.fromCbSquare(from), BoardMapper.fromCbSquare(to), promotionType(this)).toUci()
    }
}
