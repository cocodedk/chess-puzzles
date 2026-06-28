package dk.cocode.chess.core.model

/**
 * A single Lichess-style puzzle. [fen] is the position BEFORE the opponent's setup move; [uciMoves]
 * starts with that setup move and then alternates player/opponent moves.
 */
data class Puzzle(
    val id: String,
    val fen: String,
    val uciMoves: List<String>,
    val rating: Int,
    val themes: List<String> = emptyList(),
) {
    init {
        require(id.isNotBlank()) { "Puzzle id is blank" }
        require(fen.isNotBlank()) { "Puzzle fen is blank" }
        require(uciMoves.isNotEmpty()) { "Puzzle has no moves" }
    }

    /** The opponent's first move, applied automatically before the player solves. */
    val setupMoveUci: String get() = uciMoves.first()

    /** Player move, opponent reply, player move, ... starting with the player's first move. */
    val solutionUciMoves: List<String> get() = uciMoves.drop(1)

    /** Number of moves the player must find. */
    val playerMoveCount: Int get() = (solutionUciMoves.size + 1) / 2

    fun hasTheme(theme: String): Boolean = themes.any { it.equals(theme, ignoreCase = true) }
}
