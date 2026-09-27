package dk.cocode.chess.core.model

/** Which side an opening is studied from, and (for Black) what White typically opens with. */
enum class OpeningGroup(val side: PieceColor) {
    WHITE(PieceColor.WHITE),
    BLACK_VS_E4(PieceColor.BLACK),
    BLACK_VS_D4(PieceColor.BLACK),
}

/**
 * A named opening to learn and drill. [lines] are SAN strings from the start position that must
 * branch only on the opponent's replies, so the player always has exactly one book move at each of
 * their own turns (enforced by [dk.cocode.chess.core.engine.OpeningTree]).
 */
data class Opening(
    val id: String,
    val name: String,
    val eco: String,
    val group: OpeningGroup,
    val idea: String,
    val lines: List<String>,
) {
    val side: PieceColor get() = group.side
}
