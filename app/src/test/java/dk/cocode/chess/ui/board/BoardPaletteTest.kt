package dk.cocode.chess.ui.board

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardPaletteTest {
    @Test fun nightRimIsThickerAndBrighterThanDay() {
        // The rim is what separates a neon glyph from a square close to it in luminance.
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

    /**
     * The neon set buys its separation from chroma as much as luminance, so it cannot hold the 3:1
     * non-text bar a near-black set could. What it must hold is a floor on EVERY piece/square pair
     * — the worst is magenta on the day light square at 2.07:1 — and enough distance between the
     * two sides that they stay tellable apart at a glance.
     */
    @Test fun everyPieceSquarePairStaysSeparable() {
        for (palette in listOf(DayBoardPalette, NightBoardPalette)) {
            for (piece in listOf(PIECE_WHITE, PIECE_DARK)) {
                assertTrue(contrast(piece, palette.lightSquare) > 2f)
                assertTrue(contrast(piece, palette.darkSquare) > 2f)
            }
        }
        assertTrue(contrast(PIECE_WHITE, PIECE_DARK) > 2f) // the two sides, from each other
    }

    /** WCAG contrast ratio between two opaque colors. */
    private fun contrast(a: Color, b: Color): Float {
        val hi = maxOf(a.luminance(), b.luminance())
        val lo = minOf(a.luminance(), b.luminance())
        return (hi + 0.05f) / (lo + 0.05f)
    }
}
