package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import org.junit.Assert.assertEquals
import org.junit.Test

class SpokenMovesTest : EnglishTexts() {
    @Test fun everyRowOfTheTable() {
        mapOf(
            "e4" to "pawn e4",
            "exd5" to "e pawn takes d5",
            "e8=Q" to "pawn e8, promotes to queen",
            "exd8=N+" to "e pawn takes d8, promotes to knight, check",
            "Nf3" to "knight f3",
            "Nxe5" to "knight takes e5",
            "Nbd7" to "knight b d7",
            "R1e2" to "rook 1 e2",
            "Qh4xe1" to "queen h4 takes e1",
            "O-O" to "kingside castling",
            "O-O-O" to "queenside castling",
            "Qxf7#" to "queen takes f7, checkmate",
            "Bb5+" to "bishop b5, check",
        ).forEach { (san, spoken) -> assertEquals(san, spoken, spokenMove(san)) }
    }

    @Test fun castlingCanGiveCheck() {
        assertEquals("queenside castling, check", spokenMove("O-O-O+"))
    }

    @Test fun everyPieceHasAName() {
        assertEquals(
            listOf("king", "queen", "rook", "bishop", "knight"),
            "KQRBN".map { spokenMove("${it}a1").substringBefore(' ') },
        )
    }
}
