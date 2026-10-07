package dk.cocode.chess.viewmodel

import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType

/** A piece type in words, e.g. "knight". */
internal fun Texts.spokenPiece(type: PieceType): String = string(
    when (type) {
        PieceType.PAWN -> R.string.spoken_pawn
        PieceType.KNIGHT -> R.string.spoken_knight
        PieceType.BISHOP -> R.string.spoken_bishop
        PieceType.ROOK -> R.string.spoken_rook
        PieceType.QUEEN -> R.string.spoken_queen
        PieceType.KING -> R.string.spoken_king
    },
)

/** "White" or "Black". */
internal fun Texts.side(color: PieceColor): String =
    string(if (color == PieceColor.WHITE) R.string.side_white else R.string.side_black)

/**
 * A move in SAN as words for a screen reader: "Nxe5" is "knight takes e5", "exd8=N+" is "e pawn takes
 * d8, promotes to knight, check". Shared by every screen that speaks a move.
 */
fun Texts.spokenMove(san: String): String {
    val bare = san.trimEnd('+', '#')
    val words = when {
        bare.startsWith("O-O-O") -> string(R.string.spoken_castles_queenside)
        bare.startsWith("O-O") -> string(R.string.spoken_castles_kingside)
        else -> spokenPlay(bare.substringBefore('='), bare.substringAfter('=', ""))
    }
    return when {
        san.endsWith("#") -> string(R.string.spoken_checkmate, words)
        san.endsWith("+") -> string(R.string.spoken_check, words)
        else -> words
    }
}

private fun Texts.spokenPlay(play: String, promotion: String): String {
    val type = sanPiece(play.first())
    val rest = if (type == null) play else play.drop(1)
    val target = rest.takeLast(2)
    val before = rest.dropLast(2)
    val disambiguation = before.removeSuffix("x")
    val takes = before.endsWith("x")
    val words = when {
        type == null && takes -> string(R.string.spoken_pawn_takes, disambiguation, target)
        type == null -> string(R.string.spoken_pawn_move, target)
        else -> spokenPieceMove(spokenPiece(type), disambiguation, takes, target)
    }
    val promoted = promotion.firstOrNull()?.let(::sanPiece) ?: return words
    return string(R.string.spoken_promotes, words, spokenPiece(promoted))
}

/** A piece's move: "knight f3", "knight takes e5", and with [from] (a file, rank or both) "knight b d7". */
private fun Texts.spokenPieceMove(piece: String, from: String, takes: Boolean, target: String): String = when {
    from.isEmpty() && takes -> string(R.string.spoken_piece_takes, piece, target)
    from.isEmpty() -> string(R.string.spoken_piece_move, piece, target)
    takes -> string(R.string.spoken_piece_takes_from, piece, from, target)
    else -> string(R.string.spoken_piece_move_from, piece, from, target)
}

/** A SAN piece letter is uppercase; a lowercase first letter is a pawn's file. */
private fun sanPiece(letter: Char): PieceType? = if (letter.isUpperCase()) PieceType.fromLetter(letter) else null
