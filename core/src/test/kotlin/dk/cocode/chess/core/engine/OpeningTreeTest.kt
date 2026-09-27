package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.OpeningFixtures
import dk.cocode.chess.core.model.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class OpeningTreeTest {
    @Test fun mergesLinesAndKeepsEachLinesPlies() {
        val tree = OpeningTree(OpeningFixtures.ITALIAN)
        assertEquals(8, tree.plies(0).size)
        assertEquals(8, tree.plies(1).size)
        assertEquals("Nf6", tree.plies(0).last().san)
        assertEquals("Be7", tree.plies(1).last().san)
    }

    @Test fun lineNamesForBlackBranchPoint() {
        // Italian's two lines share the first 5 plies and diverge on Black's 3rd move.
        val tree = OpeningTree(OpeningFixtures.ITALIAN)
        assertEquals(listOf("3…Bc5", "3…Nf6"), tree.lineNames)
    }

    @Test fun lineNamesForWhiteBranchPoint() {
        // Sicilian demo's two lines diverge on White's 2nd move.
        val tree = OpeningTree(OpeningFixtures.SICILIAN_DEMO)
        assertEquals(listOf("2.Nf3", "2.c3"), tree.lineNames)
    }

    @Test fun singleLineIsNamedMainLine() {
        val tree = OpeningTree(OpeningFixtures.SINGLE_LINE)
        assertEquals(listOf("Main line"), tree.lineNames)
    }

    @Test fun pliesCarryTheBoardAfterEachMove() {
        val tree = OpeningTree(OpeningFixtures.ITALIAN)
        val plies = tree.plies(0)
        val afterE4 = plies[0].board
        assertEquals('P', afterE4.pieceAt(Square.of("e4"))?.fenChar)
        assertNull(afterE4.pieceAt(Square.of("e2")))
        val afterNf6 = plies.last().board
        assertEquals('n', afterNf6.pieceAt(Square.of("f6"))?.fenChar)
        assertNull(afterNf6.pieceAt(Square.of("b8")))
    }

    @Test fun startIsTheInitialPosition() {
        val tree = OpeningTree(OpeningFixtures.ITALIAN)
        assertEquals('P', tree.start.pieceAt(Square.of("e2"))?.fenChar)
        assertEquals('p', tree.start.pieceAt(Square.of("e7"))?.fenChar)
    }

    @Test fun rejectsABranchOnThePlayersOwnMove() {
        assertThrows(IllegalArgumentException::class.java) {
            OpeningTree(OpeningFixtures.INVALID_PLAYER_BRANCH)
        }
    }

    @Test fun rejectsAnEmptyLine() {
        assertThrows(IllegalArgumentException::class.java) {
            OpeningTree(OpeningFixtures.INVALID_EMPTY_LINE)
        }
    }
}
