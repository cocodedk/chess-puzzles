package dk.cocode.chess.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

/** The words a screen reader reads on the openings screens where no announcement is involved. */
class OpeningSpeechTest {
    @Test fun aMoveListWithAnOddNumberOfMovesEndsOnWhitesMove() {
        assertEquals(
            "1. pawn e4, pawn e5. 2. knight f3, knight c6. 3. bishop c4.",
            spokenMoveList(listOf("e4", "e5", "Nf3", "Nc6", "Bc4")),
        )
    }

    @Test fun aMoveListWithAnEvenNumberOfMovesEndsOnBlacksMove() {
        assertEquals("1. pawn e4, pawn e5. 2. knight f3, knight c6.", spokenMoveList(listOf("e4", "e5", "Nf3", "Nc6")))
    }

    @Test fun anEmptyMoveListIsNotRead() {
        assertEquals("", spokenMoveList(emptyList()))
    }

    @Test fun captureAndCheckAreSpokenInTheMoveList() {
        assertEquals(
            "1. pawn e4, pawn d5. 2. e pawn takes d5, queen takes d5, check.",
            spokenMoveList(listOf("e4", "d5", "exd5", "Qxd5+")),
        )
    }

    @Test fun aChipForWhitesMoveIsReadAsThatMove() {
        assertEquals("Move 4, White: pawn d3", spokenLineName("4.d3"))
    }

    @Test fun aChipForBlacksMoveIsReadAsThatMove() {
        assertEquals("Move 3, Black: bishop c5", spokenLineName("3…Bc5"))
    }

    @Test fun anyOtherLineNameIsReadAsItIs() {
        assertEquals("Main line", spokenLineName("Main line"))
    }

    @Test fun theEcoCodeIsReadWithItsCleanRuns() {
        assertEquals("C50", spokenEco("C50", 0))
        assertEquals("C50, 1 clean run", spokenEco("C50", 1))
        assertEquals("C50, 3 clean runs", spokenEco("C50", 3))
    }
}
