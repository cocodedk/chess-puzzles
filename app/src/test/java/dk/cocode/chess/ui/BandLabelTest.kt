package dk.cocode.chess.ui

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.viewmodel.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class BandLabelTest : EnglishTexts() {
    @Test fun eachBandHasItsName() {
        assertEquals(listOf("Easy", "Medium", "Hard"), Difficulty.entries.map { bandLabel(it) })
    }
}
