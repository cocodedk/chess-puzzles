package dk.cocode.chess.core.model

/**
 * Immutable 8x8 snapshot. [squares] is indexed `[rank][file]`, where rank 0 is rank 1 and file 0
 * is the a-file.
 */
data class BoardView(val squares: List<List<Piece?>>) {
    init {
        require(squares.size == 8 && squares.all { it.size == 8 }) { "BoardView must be 8x8" }
    }

    fun pieceAt(square: Square): Piece? = squares[square.rank][square.file]

    /** 8 rows of FEN chars for rendering; ' ' marks an empty square. Row 0 is rank 1. */
    fun toRows(): List<String> =
        squares.map { row -> String(CharArray(8) { file -> row[file]?.fenChar ?: ' ' }) }
}
