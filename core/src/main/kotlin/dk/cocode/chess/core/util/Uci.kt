package dk.cocode.chess.core.util

import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square

/** Helpers for UCI move strings such as "e2e4" and "e7e8q". */
object Uci {
    fun toMoveStep(uci: String): MoveStep {
        val u = uci.lowercase()
        require(u.length == 4 || u.length == 5) { "Bad UCI move: $uci" }
        val promotion = if (u.length == 5) charToType(u[4]) else null
        return MoveStep(Square.of(u.substring(0, 2)), Square.of(u.substring(2, 4)), promotion, u)
    }

    fun charToType(c: Char): PieceType = when (c.lowercaseChar()) {
        'q' -> PieceType.QUEEN
        'r' -> PieceType.ROOK
        'b' -> PieceType.BISHOP
        'n' -> PieceType.KNIGHT
        else -> throw IllegalArgumentException("Bad promotion char: $c")
    }

    fun typeToChar(type: PieceType): Char = when (type) {
        PieceType.QUEEN -> 'q'
        PieceType.ROOK -> 'r'
        PieceType.BISHOP -> 'b'
        PieceType.KNIGHT -> 'n'
        else -> throw IllegalArgumentException("Not a promotion piece: $type")
    }
}

/** The UCI string for this intent, e.g. "e7e8q". */
fun MoveIntent.toUci(): String =
    from.toAlgebraic() + to.toAlgebraic() + (promotion?.let { Uci.typeToChar(it) } ?: "")
