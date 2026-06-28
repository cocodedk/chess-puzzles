package dk.cocode.chess.ui.board

/** Unicode chess glyphs for FEN piece codes ('P', 'n', ...). */
object PieceGlyph {
    fun glyph(code: Char): String = when (code.lowercaseChar()) {
        'k' -> "♚"
        'q' -> "♛"
        'r' -> "♜"
        'b' -> "♝"
        'n' -> "♞"
        'p' -> "♟"
        else -> ""
    }

    fun isWhite(code: Char): Boolean = code.isUpperCase()
}
