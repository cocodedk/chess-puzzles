package dk.cocode.chess.ui.board

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PieceGlyphTest {
    @Test fun glyphs() {
        assertEquals("♚", PieceGlyph.glyph('K'))
        assertEquals("♛", PieceGlyph.glyph('q'))
        assertEquals("♜", PieceGlyph.glyph('R'))
        assertEquals("♝", PieceGlyph.glyph('b'))
        assertEquals("♞", PieceGlyph.glyph('N'))
        assertEquals("♟", PieceGlyph.glyph('p'))
        assertEquals("", PieceGlyph.glyph(' '))
    }

    @Test fun whiteDetection() {
        assertTrue(PieceGlyph.isWhite('K'))
        assertFalse(PieceGlyph.isWhite('k'))
    }
}
