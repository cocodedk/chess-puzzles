package dk.cocode.chess.core.model

/** A move the player is attempting (UI -> engine). [promotion] is set only for pawn promotions. */
data class MoveIntent(val from: Square, val to: Square, val promotion: PieceType? = null)

/** A move that has been applied (engine -> UI). [uci] is normalized lowercase, e.g. "e7e8q". */
data class MoveStep(
    val from: Square,
    val to: Square,
    val promotion: PieceType?,
    val uci: String,
)
