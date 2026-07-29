package dk.cocode.chess.data

import kotlinx.coroutines.flow.Flow

/**
 * Persisted player progress across sessions. [index] is the puzzle to resume at; [dayStreak] is
 * the run of consecutive local calendar days ([lastSolvedDay], epoch-based) with a solve;
 * [hintFreeCount] is the subset of [solvedCount] solved without the hint ever being shown.
 */
data class Progress(
    val solvedCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val index: Int = 0,
    val dayStreak: Int = 0,
    val lastSolvedDay: Long = 0,
    val hintFreeCount: Int = 0,
)

/** Pure update for one solve on [epochDay]: counters, best, the daily run, and the [hintFree] tally. */
fun Progress.solvedOn(epochDay: Long, hintFree: Boolean): Progress {
    val streak = currentStreak + 1
    val days = when {
        epochDay <= lastSolvedDay -> maxOf(dayStreak, 1) // same day — or a clock that moved backwards
        epochDay == lastSolvedDay + 1 -> dayStreak + 1
        else -> 1
    }
    return copy(
        solvedCount = solvedCount + 1, currentStreak = streak, bestStreak = maxOf(bestStreak, streak),
        dayStreak = days, lastSolvedDay = maxOf(lastSolvedDay, epochDay), // the marker only advances
        hintFreeCount = hintFreeCount + if (hintFree) 1 else 0,
    )
}

/** The day streak to display on [today]: a run not extended today or yesterday has lapsed. */
fun Progress.dayStreakAsOf(today: Long): Int = if (today - lastSolvedDay > 1) 0 else dayStreak

interface ProgressRepository {
    /** Emits the current progress and every subsequent change. */
    val progress: Flow<Progress>

    /** Atomically applies [solvedOn] for a solve on the local [epochDay]. */
    suspend fun recordSolved(epochDay: Long, hintFree: Boolean)

    /** Atomically resets the current streak to 0. */
    suspend fun recordFailed()

    /** Persists the index of the puzzle to resume at. */
    suspend fun setIndex(index: Int)
}
