package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.OpeningFixtures
import dk.cocode.chess.core.fixtures.intentOf
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class OpeningDrillTest {
    private fun italianDrill(seed: Int = 1) =
        OpeningDrill(OpeningTree(OpeningFixtures.ITALIAN), PieceColor.WHITE, Random(seed))

    @Test fun whiteSideStartsAtTheInitialPositionWithNoLastMove() {
        val drill = italianDrill()
        assertEquals(drill.board, OpeningTree(OpeningFixtures.ITALIAN).start)
        assertNull(drill.lastMove)
        assertTrue(drill.clean)
        assertFalse(drill.complete)
        assertEquals("e2e4", drill.bookMove().uci)
    }

    @Test fun blackSideAutoPlaysWhitesFirstMove() {
        val tree = OpeningTree(OpeningFixtures.SICILIAN_DEMO)
        val drill = OpeningDrill(tree, PieceColor.BLACK, Random(1))
        assertEquals("e2e4", drill.lastMove?.uci)
        assertEquals("c7c5", drill.bookMove().uci)
    }

    @Test fun correctMoveIsAppliedAndOpponentReplies() {
        val drill = italianDrill()
        val result = drill.submit(intentOf("e2e4"))
        check(result is DrillResult.Correct)
        assertEquals("e2e4", result.playerMove.uci)
        assertEquals("e7e5", result.reply.uci)
        assertEquals("e7e5", drill.lastMove?.uci)
        assertTrue(drill.clean)
    }

    @Test fun wrongMoveIsNotAppliedAndMarksNotClean() {
        val drill = italianDrill()
        val startBoard = drill.board
        val result = drill.submit(intentOf("d2d4"))
        check(result is DrillResult.Wrong)
        assertEquals("e2e4", result.book.uci)
        assertEquals(startBoard, drill.board)
        assertNull(drill.lastMove)
        assertFalse(drill.clean)

        // Playing the book move afterwards succeeds, but the run stays marked as not clean.
        val recovered = drill.submit(intentOf("e2e4"))
        assertTrue(recovered is DrillResult.Correct)
        assertFalse(drill.clean)
    }

    @Test fun illegalMoveChangesNothing() {
        val drill = italianDrill()
        val result = drill.submit(intentOf("a2a5"))
        assertEquals(DrillResult.Illegal, result)
        assertNull(drill.lastMove)
        assertTrue(drill.clean)
    }

    @Test fun completesWithAReplyWhenTheLineEndsOnTheOpponentsMove() {
        val drill = italianDrill()
        var last: DrillResult = drill.submit(intentOf(drill.bookMove().uci))
        while (!drill.complete) {
            last = drill.submit(intentOf(drill.bookMove().uci))
        }
        check(last is DrillResult.Complete)
        assertTrue(last.reply != null)
        assertTrue(last.clean)
    }

    @Test fun completesWithoutAReplyWhenTheLineEndsOnThePlayersMove() {
        val drill = OpeningDrill(OpeningTree(OpeningFixtures.SINGLE_LINE), PieceColor.WHITE, Random(1))
        drill.submit(intentOf("e2e4"))
        val result = drill.submit(intentOf("g1f3"))
        check(result is DrillResult.Complete)
        assertNull(result.reply)
        assertTrue(result.clean)
        assertTrue(drill.complete)
    }

    @Test fun randomBranchChoiceIsDeterministicPerSeedAndVariesAcrossSeeds() {
        val tree = OpeningTree(OpeningFixtures.SICILIAN_DEMO)
        fun replyFor(seed: Int): String {
            val drill = OpeningDrill(tree, PieceColor.BLACK, Random(seed))
            val result = drill.submit(intentOf("c7c5"))
            check(result is DrillResult.Correct)
            return result.reply.uci
        }
        assertEquals(replyFor(1), replyFor(1))
        val seen = (1..30).map { replyFor(it) }.toSet()
        assertEquals(setOf("g1f3", "c2c3"), seen)
    }

    @Test fun restartResetsPositionAndCleanFlag() {
        val drill = italianDrill()
        drill.submit(intentOf("d2d4")) // wrong -> not clean
        drill.restart()
        assertTrue(drill.clean)
        assertNull(drill.lastMove)
        assertFalse(drill.complete)
        assertEquals("e2e4", drill.bookMove().uci)
    }

    @Test fun legalDestinationsDelegatesAndEmptiesWhenComplete() {
        val drill = OpeningDrill(OpeningTree(OpeningFixtures.SINGLE_LINE), PieceColor.WHITE, Random(1))
        assertTrue(drill.legalDestinations(Square.of("e2")).containsAll(listOf(Square.of("e3"), Square.of("e4"))))
        drill.submit(intentOf("e2e4"))
        drill.submit(intentOf("g1f3"))
        assertTrue(drill.legalDestinations(Square.of("f3")).isEmpty())
    }

    @Test fun bookMoveAndSubmitAfterCompletion() {
        val drill = OpeningDrill(OpeningTree(OpeningFixtures.SINGLE_LINE), PieceColor.WHITE, Random(1))
        drill.submit(intentOf("e2e4"))
        drill.submit(intentOf("g1f3"))
        assertThrows(IllegalStateException::class.java) { drill.bookMove() }
        assertEquals(DrillResult.Illegal, drill.submit(intentOf("g1f3")))
    }
}
