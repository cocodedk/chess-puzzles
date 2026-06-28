package dk.cocode.chess.core.util

import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UciTest {
    @Test fun toMoveStepPlain() {
        val step = Uci.toMoveStep("a2e6")
        assertEquals(Square.of("a2"), step.from)
        assertEquals(Square.of("e6"), step.to)
        assertNull(step.promotion)
        assertEquals("a2e6", step.uci)
    }

    @Test fun toMoveStepPromotionUppercase() {
        val step = Uci.toMoveStep("E7E8Q")
        assertEquals(Square.of("e7"), step.from)
        assertEquals(Square.of("e8"), step.to)
        assertEquals(PieceType.QUEEN, step.promotion)
        assertEquals("e7e8q", step.uci)
    }

    @Test fun toMoveStepBadLengthThrows() {
        assertThrows(IllegalArgumentException::class.java) { Uci.toMoveStep("e2e") }
    }

    @Test fun charToType() {
        assertEquals(PieceType.QUEEN, Uci.charToType('q'))
        assertEquals(PieceType.ROOK, Uci.charToType('R'))
        assertEquals(PieceType.BISHOP, Uci.charToType('b'))
        assertEquals(PieceType.KNIGHT, Uci.charToType('n'))
        assertThrows(IllegalArgumentException::class.java) { Uci.charToType('x') }
    }

    @Test fun typeToChar() {
        assertEquals('q', Uci.typeToChar(PieceType.QUEEN))
        assertEquals('r', Uci.typeToChar(PieceType.ROOK))
        assertEquals('b', Uci.typeToChar(PieceType.BISHOP))
        assertEquals('n', Uci.typeToChar(PieceType.KNIGHT))
        assertThrows(IllegalArgumentException::class.java) { Uci.typeToChar(PieceType.KING) }
        assertThrows(IllegalArgumentException::class.java) { Uci.typeToChar(PieceType.PAWN) }
    }

    @Test fun moveIntentToUci() {
        assertEquals("a2e6", MoveIntent(Square.of("a2"), Square.of("e6")).toUci())
        assertEquals(
            "e7e8q",
            MoveIntent(Square.of("e7"), Square.of("e8"), PieceType.QUEEN).toUci(),
        )
    }
}
