package dk.cocode.chess.data

import kotlinx.coroutines.flow.Flow

/** Persisted player progress across sessions. [index] is the puzzle to resume at. */
data class Progress(
    val solvedCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val index: Int = 0,
)

interface ProgressRepository {
    /** Emits the current progress and every subsequent change. */
    val progress: Flow<Progress>

    /** Atomically: solved++, streak++, best = max(best, streak). */
    suspend fun recordSolved()

    /** Atomically resets the current streak to 0. */
    suspend fun recordFailed()

    /** Persists the index of the puzzle to resume at. */
    suspend fun setIndex(index: Int)
}
