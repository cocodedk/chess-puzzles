package dk.cocode.chess.core.model

enum class PieceColor {
    WHITE,
    BLACK,
    ;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType {
    PAWN,
    KNIGHT,
    BISHOP,
    ROOK,
    QUEEN,
    KING,
}

data class Piece(val color: PieceColor, val type: PieceType) {
    /** FEN letter: uppercase for white, lowercase for black (e.g. a white queen -> 'Q'). */
    val fenChar: Char
        get() {
            val lower = when (type) {
                PieceType.PAWN -> 'p'
                PieceType.KNIGHT -> 'n'
                PieceType.BISHOP -> 'b'
                PieceType.ROOK -> 'r'
                PieceType.QUEEN -> 'q'
                PieceType.KING -> 'k'
            }
            return if (color == PieceColor.WHITE) lower.uppercaseChar() else lower
        }
}
