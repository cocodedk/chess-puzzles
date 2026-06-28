package dk.cocode.chess.core.model

/**
 * A board square in neutral coordinates: [file] 0..7 maps to files a..h, and [rank] 0..7 maps to
 * ranks 1..8 (rank 0 is White's first rank).
 */
data class Square(val file: Int, val rank: Int) {
    init {
        require(file in 0..7 && rank in 0..7) { "Square off board: file=$file rank=$rank" }
    }

    /** Index 0..63 with a1 == 0 and h8 == 63. */
    val index: Int get() = rank * 8 + file

    /** Algebraic name such as "e2". */
    fun toAlgebraic(): String = "${'a' + file}${'1' + rank}"

    companion object {
        fun of(algebraic: String): Square {
            require(algebraic.length == 2) { "Bad square: $algebraic" }
            return Square(algebraic[0].lowercaseChar() - 'a', algebraic[1] - '1')
        }

        fun of(file: Int, rank: Int): Square = Square(file, rank)
    }
}
