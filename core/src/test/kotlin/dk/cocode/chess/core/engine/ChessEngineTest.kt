package dk.cocode.chess.core.engine

import dk.cocode.chess.core.fixtures.Fixtures
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChessEngineTest {
    private fun startEngine(): ChessEngine = ChessEngine().apply { loadFen(Fixtures.START_FEN) }

    @Test fun fenSideAndCheck() {
        val engine = startEngine()
        assertTrue(engine.fen().startsWith("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq"))
        assertEquals(PieceColor.WHITE, engine.sideToMove())
        assertFalse(engine.isCheck())
        assertEquals("RNBQKBNR", engine.boardView().toRows()[0])
    }

    @Test fun legalDestinations() {
        val engine = startEngine()
        assertEquals(
            setOf(Square.of("e3"), Square.of("e4")),
            engine.legalDestinations(Square.of("e2")).toSet(),
        )
        assertEquals(
            setOf(Square.of("f3"), Square.of("h3")),
            engine.legalDestinations(Square.of("g1")).toSet(),
        )
        assertTrue(engine.legalDestinations(Square.of("e4")).isEmpty())
    }

    @Test fun isLegal() {
        val engine = startEngine()
        assertTrue(engine.isLegal(MoveIntent(Square.of("e2"), Square.of("e4"))))
        assertFalse(engine.isLegal(MoveIntent(Square.of("e2"), Square.of("e5"))))
    }

    @Test fun applyUciAdvancesSide() {
        val engine = startEngine()
        assertTrue(engine.applyUci("e2e4"))
        assertEquals(PieceColor.BLACK, engine.sideToMove())
    }

    @Test fun promotionDetectionAndType() {
        val engine = ChessEngine().apply { loadFen(Fixtures.PROMOTION.fen); applyUci("a7a6") }
        assertTrue(engine.requiresPromotion(Square.of("e7"), Square.of("e8")))
        assertFalse(engine.requiresPromotion(Square.of("g1"), Square.of("g2")))
        assertTrue(engine.isLegal(MoveIntent(Square.of("e7"), Square.of("e8"), PieceType.QUEEN)))
        assertFalse(engine.isLegal(MoveIntent(Square.of("e7"), Square.of("e8"), null)))
    }

    @Test fun wouldBeMate() {
        val engine = ChessEngine().apply { loadFen(Fixtures.MATE_IN_1_MULTI.fen); applyUci("g8h8") }
        assertTrue(engine.wouldBeMate("b7g7"))
        assertTrue(engine.wouldBeMate("b7h7"))
        assertFalse(engine.wouldBeMate("b7b2"))
        assertEquals(PieceColor.WHITE, engine.sideToMove()) // probing left the board unchanged
    }

    @Test fun sanLineToUciNormalMoves() {
        assertEquals(listOf("e2e4", "d7d5", "e4d5"), ChessEngine.sanLineToUci("e4 d5 exd5"))
    }

    @Test fun sanLineToUciCastling() {
        assertEquals(
            listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5", "e1g1"),
            ChessEngine.sanLineToUci("e4 e5 Nf3 Nc6 Bc4 Bc5 O-O"),
        )
    }

    @Test fun sanLineToUciPromotion() {
        // Black ignores the advancing pawn each move (captures are never mandatory), so it walks
        // to b7 and then captures-and-promotes on a8.
        val uci = ChessEngine.sanLineToUci("a4 Nf6 a5 Ng8 a6 Nf6 axb7 Ng8 bxa8=Q")
        assertEquals("b7a8q", uci.last())
    }

    @Test fun sanLineToUciBlankLineIsEmpty() {
        assertTrue(ChessEngine.sanLineToUci("   ").isEmpty())
    }

    @Test fun sanLineToUciIllegalMoveThrows() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            ChessEngine.sanLineToUci("e4 e5 Nf9")
        }
        assertTrue(error.message!!.contains("Nf9"))
    }
}
