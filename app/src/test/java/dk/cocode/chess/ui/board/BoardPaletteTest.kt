package dk.cocode.chess.ui.board

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardPaletteTest {
    @Test fun nightRimIsThickerAndBrighterThanDay() {
        // A black fill is ~1.5:1 against the night dark square, so the rim carries the piece.
        assertTrue(NightBoardPalette.pieceOutlineWidth > DayBoardPalette.pieceOutlineWidth)
        assertNotEquals(DayBoardPalette.darkPieceOutline, NightBoardPalette.darkPieceOutline)
    }

    @Test fun onlyNightHasAHaloBehindDarkPieces() {
        assertTrue(DayBoardPalette.darkPieceHaloWidth == 0f) // day pieces need no aura
        assertTrue(NightBoardPalette.darkPieceHaloWidth > 0f)
        assertTrue(NightBoardPalette.darkPieceHalo.alpha > 0f)
    }

    @Test fun nightBoardIsDimmerThanDay() {
        assertTrue(NightBoardPalette.lightSquare.luminance() < DayBoardPalette.lightSquare.luminance())
        assertTrue(NightBoardPalette.darkSquare.luminance() < DayBoardPalette.darkSquare.luminance())
    }

    private fun androidx.compose.ui.graphics.Color.luminance(): Float =
        0.2126f * red + 0.7152f * green + 0.0722f * blue
}
