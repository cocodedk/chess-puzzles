package dk.cocode.chess.ui.board

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardPaletteTest {
    @Test fun nightRimIsThickerAndBrighterThanDay() {
        // A dark fill is only ~2:1 against the night dark square, so the rim carries the piece.
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
     * The dark squares are tuned around [PIECE_DARK]: both boards keep the ink pieces above the
     * WCAG 3:1 bar for non-text, which is what stops them sinking into the wood. Prose cannot
     * enforce that — dimming the night board any further breaks it.
     */
    @Test fun darkPiecesStayLegibleOnTheSquaresTheySitOn() {
        assertTrue(contrast(PIECE_DARK, DayBoardPalette.darkSquare) > 3f)
        assertTrue(contrast(PIECE_DARK, NightBoardPalette.darkSquare) > 3f)
    }

    /** WCAG contrast ratio between two opaque colors. */
    private fun contrast(a: Color, b: Color): Float {
        val hi = maxOf(a.luminance(), b.luminance())
        val lo = minOf(a.luminance(), b.luminance())
        return (hi + 0.05f) / (lo + 0.05f)
    }
}
