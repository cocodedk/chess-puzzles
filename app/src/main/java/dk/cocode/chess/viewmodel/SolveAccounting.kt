package dk.cocode.chess.viewmodel

/**
 * One-shot per-puzzle, per-local-day accounting: each puzzle records at most one fail and one
 * solve per [day], and a puzzle already solved that day cannot break the streak. A later day clears
 * the flags so every day can earn again; a clock that moves backwards never re-opens a spent day.
 */
internal class SolveAccounting {
    private var day = 0L
    private val failed = mutableSetOf<Int>()
    private val solved = mutableSetOf<Int>()

    /** True when a fail on [index] should be recorded: its first mistake, and not solved today. */
    fun countFail(day: Long, index: Int): Boolean {
        refresh(day)
        return index !in solved && failed.add(index)
    }

    /** True when a solve on [index] should be recorded: its first solve today. */
    fun countSolve(day: Long, index: Int): Boolean {
        refresh(day)
        return solved.add(index)
    }

    private fun refresh(day: Long) {
        if (day <= this.day) return
        this.day = day
        failed.clear()
        solved.clear()
    }
}
