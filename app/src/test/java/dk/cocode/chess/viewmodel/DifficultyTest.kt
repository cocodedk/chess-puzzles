package dk.cocode.chess.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class DifficultyTest {
    @Test fun mapsRatingToBand() {
        assertEquals(Difficulty.EASY, difficultyOf(0))
        assertEquals(Difficulty.EASY, difficultyOf(1199))
        assertEquals(Difficulty.MEDIUM, difficultyOf(1200))
        assertEquals(Difficulty.MEDIUM, difficultyOf(1999))
        assertEquals(Difficulty.HARD, difficultyOf(2000))
        assertEquals(Difficulty.HARD, difficultyOf(3100))
    }
}
