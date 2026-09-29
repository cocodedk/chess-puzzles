package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChessEngineSanTest {
    private fun san(fen: String, uci: String): String = ChessEngine().apply { loadFen(fen) }.san(uci)

    @Test fun quietMoves() {
        assertEquals("e4", san(Fixtures.START_FEN, "e2e4"))
        assertEquals("Nf3", san(Fixtures.START_FEN, "g1f3"))
    }

    @Test fun capture() {
        assertEquals("exd5", san("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2", "e4d5"))
    }

    @Test fun check() {
        assertEquals("Ra8+", san("4k3/8/8/8/8/8/R7/4K3 w - - 0 1", "a2a8"))
    }

    @Test fun mate() {
        assertEquals("Ra8#", san("6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1", "a1a8"))
    }

    @Test fun castlingBothWays() {
        val fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        assertEquals("O-O", san(fen, "e1g1"))
        assertEquals("O-O-O", san(fen, "e1c1"))
    }

    @Test fun promotion() {
        assertEquals("a8=Q", san("8/P7/8/8/8/2k5/8/6K1 w - - 0 1", "a7a8q"))
    }

    @Test fun doesNotPlayTheMove() {
        val engine = ChessEngine().apply { loadFen(Fixtures.START_FEN) }
        engine.san("e2e4")
        assertEquals(Fixtures.START_FEN.substringBefore(" - "), engine.fen().substringBefore(" - "))
    }
}
