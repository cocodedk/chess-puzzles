package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType

/** A piece type in words, e.g. "knight". */
internal fun spokenPiece(type: PieceType): String = type.name.lowercase()

/** "White" or "Black". */
internal fun PieceColor.label(): String = if (this == PieceColor.WHITE) "White" else "Black"

/**
 * A move in SAN as words for a screen reader: "Nxe5" is "knight takes e5", "exd8=N+" is "e pawn takes
 * d8, promotes to knight, check". Shared by every screen that speaks a move.
 */
fun spokenMove(san: String): String {
    val suffix = when {
        san.endsWith("#") -> ", checkmate"
        san.endsWith("+") -> ", check"
        else -> ""
    }
    val bare = san.trimEnd('+', '#')
    val words = when {
        bare.startsWith("O-O-O") -> "castles queenside"
        bare.startsWith("O-O") -> "castles kingside"
        else -> spokenPlay(bare.substringBefore('='), bare.substringAfter('=', ""))
    }
    return words + suffix
}

private fun spokenPlay(play: String, promotion: String): String {
    val type = sanPiece(play.first())
    val rest = if (type == null) play else play.drop(1)
    val target = rest.takeLast(2)
    val before = rest.dropLast(2)
    val disambiguation = before.removeSuffix("x")
    val takes = before.endsWith("x")
    val words = if (type == null) {
        if (takes) "$disambiguation pawn takes $target" else "pawn $target"
    } else {
        listOfNotNull(spokenPiece(type), disambiguation.takeIf { it.isNotEmpty() }, "takes".takeIf { takes }, target)
            .joinToString(" ")
    }
    val promotes = promotion.firstOrNull()?.let(::sanPiece)?.let { ", promotes to ${spokenPiece(it)}" }.orEmpty()
    return words + promotes
}

/** A SAN piece letter is uppercase; a lowercase first letter is a pawn's file. */
private fun sanPiece(letter: Char): PieceType? = if (letter.isUpperCase()) PieceType.fromLetter(letter) else null
