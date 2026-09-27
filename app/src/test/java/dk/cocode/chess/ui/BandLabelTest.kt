package dk.cocode.chess.ui

import dk.cocode.chess.viewmodel.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class BandLabelTest {
    @Test fun eachBandHasItsName() {
        assertEquals(listOf("Easy", "Medium", "Hard"), Difficulty.entries.map(::bandLabel))
    }
}
