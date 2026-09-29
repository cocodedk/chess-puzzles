package dk.cocode.chess.core.model

enum class PieceColor {
    WHITE,
    BLACK,
    ;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

/** [letter] is the lowercase FEN letter; SAN and white's FEN use it in uppercase. */
enum class PieceType(val letter: Char) {
    PAWN('p'),
    KNIGHT('n'),
    BISHOP('b'),
    ROOK('r'),
    QUEEN('q'),
    KING('k'),
    ;

    companion object {
        /** The type a FEN or SAN letter names, either case; null for any other character. */
        fun fromLetter(letter: Char): PieceType? = entries.firstOrNull { it.letter == letter.lowercaseChar() }
    }
}

data class Piece(val color: PieceColor, val type: PieceType) {
    /** FEN letter: uppercase for white, lowercase for black (e.g. a white queen -> 'Q'). */
    val fenChar: Char
        get() = if (color == PieceColor.WHITE) type.letter.uppercaseChar() else type.letter
}
