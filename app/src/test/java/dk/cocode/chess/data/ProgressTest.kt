package dk.cocode.chess.data

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure solve rules on [Progress]: the daily run, when the display lapses, and the hint-free tally. */
class ProgressTest {
    @Test fun firstSolveOnTheDefaultDayStillStartsTheDailyRun() {
        assertEquals(1, Progress().solvedOn(0, hintFree = true).dayStreak)
    }

    @Test fun aSecondSolveTheSameDayKeepsTheRun() {
        assertEquals(3, Progress(dayStreak = 3, lastSolvedDay = 100).solvedOn(100, hintFree = true).dayStreak)
    }

    @Test fun aClockThatMovedBackwardsKeepsTheRunAndNeverRewindsTheMarker() {
        // Westward travel: the run survives AND lastSolvedDay stays at 100, so the corrected clock
        // neither lapses the run retroactively nor double-counts day 100.
        val progress = Progress(dayStreak = 3, lastSolvedDay = 100)
        assertEquals(Progress(1, 1, 1, 0, 3, 100), progress.solvedOn(99, hintFree = false))
    }

    @Test fun onlyHintFreeSolvesRaiseTheHintFreeTally() {
        val progress = Progress().solvedOn(5, hintFree = true).solvedOn(5, hintFree = false)
        assertEquals(2, progress.solvedCount)
        assertEquals(1, progress.hintFreeCount)
    }

    @Test fun displayedDayStreakLapsesAfterAMissedDay() {
        val progress = Progress(dayStreak = 3, lastSolvedDay = 100)
        assertEquals(3, progress.dayStreakAsOf(100)) // extended today
        assertEquals(3, progress.dayStreakAsOf(101)) // yesterday — still alive
        assertEquals(0, progress.dayStreakAsOf(102)) // missed a day
    }
}
