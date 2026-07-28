package dk.cocode.chess.data

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure day-streak rules on [Progress]: how a solve extends the run and when the display lapses. */
class ProgressTest {
    @Test fun firstSolveOnTheDefaultDayStillStartsTheDailyRun() {
        assertEquals(1, Progress().solvedOn(0).dayStreak)
    }

    @Test fun aSecondSolveTheSameDayKeepsTheRun() {
        assertEquals(3, Progress(dayStreak = 3, lastSolvedDay = 100).solvedOn(100).dayStreak)
    }

    @Test fun aClockThatMovedBackwardsKeepsTheRunAndNeverRewindsTheMarker() {
        // Westward travel: the run survives AND lastSolvedDay stays at 100, so the corrected clock
        // neither lapses the run retroactively nor double-counts day 100.
        assertEquals(Progress(1, 1, 1, 0, 3, 100), Progress(dayStreak = 3, lastSolvedDay = 100).solvedOn(99))
    }

    @Test fun displayedDayStreakLapsesAfterAMissedDay() {
        val progress = Progress(dayStreak = 3, lastSolvedDay = 100)
        assertEquals(3, progress.dayStreakAsOf(100)) // extended today
        assertEquals(3, progress.dayStreakAsOf(101)) // yesterday — still alive
        assertEquals(0, progress.dayStreakAsOf(102)) // missed a day
    }
}
