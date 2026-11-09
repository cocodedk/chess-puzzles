package dk.cocode.chess.viewmodel

import dk.cocode.chess.EnglishTexts
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.annotation.Config

/** The whole position in one line, in the order a blind tester asked for: king, rook, queen, bishop, knight, pawn. */
class PieceListTest : EnglishTexts() {
    // Row 0 is rank 1.
    private fun list(vararg rows: String) = pieceList(PuzzleUiState(board = rows.toList()))

    @Test fun theStartingPositionReadsAsTheTesterWroteIt() = assertEquals(
        "White: King e1. Rook a1, h1. Queen d1. Bishop c1, f1. Knight b1, g1. Pawn a2, b2, c2, d2, e2, f2, g2, h2. " +
            "Black: King e8. Rook a8, h8. Queen d8. Bishop c8, f8. Knight b8, g8. Pawn a7, b7, c7, d7, e7, f7, g7, h7.",
        list("RNBQKBNR", "PPPPPPPP", "        ", "        ", "        ", "        ", "pppppppp", "rnbqkbnr"),
    )

    @Test fun squaresGoFileByFileThenUpAndMissingKindsAreLeftOut() = assertEquals(
        "White: King g1. Rook a1, a5, h1. Knight e4. Black: King g8. Queen d8.",
        list("R     KR", "        ", "        ", "    N   ", "R       ", "        ", "        ", "   q  k "),
    )

    @Test fun anEmptyBoardSaysNothing() = assertEquals("", pieceList(PuzzleUiState()))

    private val kingsAndAPawn = arrayOf("    K   ", "P       ", "        ", "        ", "        ", "        ", "        ", "    k   ")

    @Test @Config(qualifiers = "zh")
    fun chinesePunctuatesItsOwnWay() = assertEquals("白方：王e1。 兵a2。 黑方：王e8。", list(*kingsAndAPawn))

    @Test @Config(qualifiers = "es")
    fun spanishNamesTheSideAsPieces() = assertEquals("Piezas blancas: Rey e1. Peón a2. Piezas negras: Rey e8.", list(*kingsAndAPawn))

    @Test fun theFlippedBoardReadsTheSame() {
        val board = listOf("R     KR", "        ", "        ", "    N   ", "R       ", "        ", "        ", "   q  k ")
        assertEquals(pieceList(PuzzleUiState(board = board)), pieceList(PuzzleUiState(board = board, flipped = true)))
    }
}
