package dk.cocode.chess.viewmodel

/** Puzzle difficulty bands, derived from the Lichess rating. */
enum class Difficulty { EASY, MEDIUM, HARD }

/** Maps a puzzle rating to its difficulty band. */
fun difficultyOf(rating: Int): Difficulty = when {
    rating < 1200 -> Difficulty.EASY
    rating < 2000 -> Difficulty.MEDIUM
    else -> Difficulty.HARD
}

/** The puzzle [by] places from [index] in this band (puzzle indices, bundle order), wrapping at either end. */
internal fun List<Int>.stepFrom(index: Int, by: Int): Int = this[(indexOf(index) + by).mod(size)]
