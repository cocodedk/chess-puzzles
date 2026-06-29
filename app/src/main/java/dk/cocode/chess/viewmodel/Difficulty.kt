package dk.cocode.chess.viewmodel

/** Puzzle difficulty bands, derived from the Lichess rating. */
enum class Difficulty { EASY, MEDIUM, HARD }

/** Maps a puzzle rating to its difficulty band. */
fun difficultyOf(rating: Int): Difficulty = when {
    rating < 1200 -> Difficulty.EASY
    rating < 2000 -> Difficulty.MEDIUM
    else -> Difficulty.HARD
}
