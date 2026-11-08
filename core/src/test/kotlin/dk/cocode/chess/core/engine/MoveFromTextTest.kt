package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.fixtures.intentOf
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MoveFromTextTest {
    private fun engineAt(fen: String) = ChessEngine().apply { loadFen(fen) }

    private fun ChessEngine.reads(text: String, uci: String) = assertEquals(intentOf(uci), moveFromText(text), "\"$text\"")

    private fun ChessEngine.reads(text: String, nothing: Nothing?) = assertEquals(nothing, moveFromText(text), "\"$text\"")

    @Test fun coordinatesInAnySpelling() {
        val engine = engineAt(Fixtures.START_FEN)
        listOf("e2e4", "e2-e4", " E2 E4 ", "e2 e4", "E2-E4", "E2 E4.", "e2, e4").forEach { engine.reads(it, "e2e4") }
    }

    @Test fun standardNotationInAnySpelling() {
        val engine = engineAt(Fixtures.START_FEN)
        listOf("e4", "E4", " e 4 ", "E4.").forEach { engine.reads(it, "e2e4") }
        listOf("Nf3", "nf3", "NF3", "Nxf3").forEach { engine.reads(it, "g1f3") }
    }

    @Test fun movesThatAreNotLegalOrNotMoves() {
        val engine = engineAt(Fixtures.START_FEN)
        listOf("e2e5", "Nf6", "e5", "hello", "", "  ").forEach { engine.reads(it, null) }
    }

    @Test fun capturesWithAndWithoutTheX() {
        val engine = engineAt("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2")
        listOf("exd5", "ed5", "e4d5", "EXD5").forEach { engine.reads(it, "e4d5") }
    }

    @Test fun checkAndMateMarksAreIgnored() {
        val engine = engineAt(Fixtures.MATE_IN_1_MULTI.fen).apply { applyUci("g8h8") }
        listOf("Qg7#", "Qg7", "Qxg7#", "qg7", "b7g7").forEach { engine.reads(it, "b7g7") }
        engine.reads("Qh7#", "b7h7")
    }

    @Test fun castlingInAnySpelling() {
        val engine = engineAt("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        listOf("O-O", "0-0", "o-o", "OO", "e1g1").forEach { engine.reads(it, "e1g1") }
        listOf("O-O-O", "0-0-0", "o-o-o", "e1c1").forEach { engine.reads(it, "e1c1") }
    }

    @Test fun twoPiecesThatCanGoThereAreNamedByTheirFile() {
        val engine = engineAt("4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1")
        engine.reads("Nd2", null)
        engine.reads("Nbd2", "b1d2")
        engine.reads("Nfd2", "f1d2")
    }

    @Test fun aBishopAndAPawnTakingOnTheSameSquareAreToldApartByCase() {
        val engine = engineAt("4k3/8/8/8/8/2n5/1P1B4/4K3 w - - 0 1")
        engine.reads("Bxc3", "d2c3")
        engine.reads("bxc3", "b2c3")
        engine.reads("bc3", "b2c3")
        engine.reads("Bc3", "d2c3")
        engine.reads("BC3", null)
    }

    @Test fun aPromotionNamesItsPieceOrLeavesItToBeAsked() {
        val engine = engineAt("8/4P3/8/8/8/2k5/8/6K1 w - - 0 1")
        engine.reads("e8=Q", "e7e8q")
        engine.reads("e8q", "e7e8q")
        engine.reads("e7e8n", "e7e8n")
        val square = MoveIntent(Square.of("e7"), Square.of("e8"))
        assertEquals(square, engine.moveFromText("e8"))
        assertEquals(square, engine.moveFromText("e7e8"))
        assertEquals(MoveIntent(square.from, square.to, PieceType.KNIGHT), engine.moveFromText("e8=N"))
    }

    @Test fun aPuzzleReadsTheMoveOnlyWhileItIsInProgress() {
        val session = PuzzleSession.start(Fixtures.MATE_IN_2)
        assertEquals(intentOf("a2e6"), session.moveFromText("Be6"))
        session.submitMove(intentOf("h2h3"))
        assertEquals(PuzzleStatus.FAILED, session.state.status)
        assertNull(session.moveFromText("Be6"))
    }
}
