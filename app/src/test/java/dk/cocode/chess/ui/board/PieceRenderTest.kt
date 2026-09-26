package dk.cocode.chess.ui.board

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dk.cocode.chess.near
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Draws [block] onto a fresh [px]-square bitmap, the way the board's Canvas would. */
private fun renderSquare(px: Int, block: DrawScope.() -> Unit): Bitmap {
    val image = ImageBitmap(px, px)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(px.toFloat(), px.toFloat()), block)
    return image.asAndroidBitmap()
}

@RunWith(RobolectricTestRunner::class)
class PieceRenderTest {
    private val px = 100
    private val types = "pnbrqk"

    private fun piece(code: Char, square: Color, palette: BoardPalette = DayBoardPalette) = renderSquare(px) {
        drawRect(square)
        drawPiece(code, Offset.Zero, px.toFloat(), palette)
    }

    /** The pixels a piece changed on its [square]: its silhouette, shadow and aura included. */
    private fun silhouette(bitmap: Bitmap, square: Color): Set<Int> = (0 until px * px)
        .filterNot { near(bitmap.getPixel(it % px, it / px), square.toArgb(), 12) }
        .toSet()

    @Test fun everyPieceIsDrawnSizeablyInBothColoursOnBothWoods() {
        for (square in listOf(DayBoardPalette.lightSquare, DayBoardPalette.darkSquare)) {
            for (code in types + types.uppercase()) assertTrue(silhouette(piece(code, square), square).size > px * px / 10)
        }
    }

    /**
     * Both colours share one outline per type, so the bare shape is compared once: ivory on black, where
     * the shadow vanishes and no aura blurs the edge. Staunton pieces share their bases by design; the
     * upper half, the head, is what names them.
     */
    @Test fun everyPieceTypeHasItsOwnHead() {
        val heads = types.map { t -> silhouette(piece(t.uppercaseChar(), Color.Black), Color.Black).filter { it / px < px / 2 } }
        for (a in heads.indices) for (b in a + 1 until heads.size) {
            val union = (heads[a] union heads[b]).size
            val shared = (heads[a] intersect heads[b]).size
            // A guard against two types collapsing into one head, not a legibility proof: the closest
            // pair, bishop vs king, differs by ~20% — the cross and the mitre's ball are small but telling.
            assertTrue("${types[a]} vs ${types[b]}: ${(union - shared) * 100 / union}%", union - shared > union / 6)
        }
    }

    /** The widest row of [shape] among [rows]. */
    private fun width(shape: Set<Int>, rows: IntRange) = rows.maxOf { row ->
        shape.filter { it / px == row }.let { r -> if (r.isEmpty()) 0 else r.maxOf { it % px } - r.minOf { it % px } + 1 }
    }

    /**
     * Each letter draws its own piece: the king is the tallest, the pawn the shortest, the rook's flat
     * top the widest, the queen's crown the widest head, and only the knight is lopsided. The bishop
     * is none of these, so a swap of any two letters in the art table breaks one of them.
     */
    @Test fun eachLetterDrawsItsOwnPiece() {
        val shapes = types.associateWith { silhouette(piece(it.uppercaseChar(), Color.Black), Color.Black) }
        val top = shapes.mapValues { (_, s) -> s.minOf { it / px } }   // a smaller row is a taller piece
        val crown = shapes.mapValues { (t, s) -> width(s, top.getValue(t) until top.getValue(t) + 5) }
        val head = shapes.mapValues { (t, s) -> width(s, top.getValue(t) until top.getValue(t) + 15) }
        val lopsided = shapes.mapValues { (_, s) -> s.count { it - it % px + (px - 1 - it % px) !in s } * 100 / s.size }
        fun only(type: Char, of: Map<Char, Int>, wins: (Int, Int) -> Boolean) =
            types.filter { it != type }.all { wins(of.getValue(type), of.getValue(it)) }
        val facts = "top $top, crown $crown, head $head, lopsided % $lopsided"
        assertTrue(facts, only('k', top) { a, b -> a < b })
        assertTrue(facts, only('p', top) { a, b -> a > b })
        assertTrue(facts, only('r', crown) { a, b -> a > b })
        assertTrue(facts, only('q', head) { a, b -> a > b })
        assertTrue(facts, only('n', lopsided) { a, b -> a > b + 3 })   // 7% against at most 1%
    }

    @Test fun ivoryIsPaleAndEbonyIsDarkWithShading() {
        val ivory = piece('Q', Color.Black)
        val ebony = piece('q', Color.White)
        val ivoryCore = Color(ivory.getPixel(50, 70))
        val ebonyCore = Color(ebony.getPixel(50, 70))
        assertTrue(ivoryCore.luminance() > .6f)
        assertTrue(ebonyCore.luminance() < .05f)
        // Shaded, not flat: the body lit from the left is brighter than its right flank.
        assertTrue(Color(ivory.getPixel(38, 78)).luminance() > Color(ivory.getPixel(64, 78)).luminance())
    }

    @Test fun ebonyWearsAWarmAuraThatWidensAtNight() {
        fun auraPixels(palette: BoardPalette): Int {
            val bitmap = piece('r', palette.darkSquare, palette)
            val wood = palette.darkSquare.luminance()
            return (0 until px * px).count { Color(bitmap.getPixel(it % px, it / px)).luminance() > wood + .05f }
        }
        val day = auraPixels(DayBoardPalette)
        assertTrue(day > 0)
        assertTrue(auraPixels(NightBoardPalette) > day)
    }

    @Test fun shapeHelpersTraceTheirOutlines() {
        val round = parsePath(circle(50f, 40f, 10f)).getBounds()
        assertEquals(40f, round.left, .01f)
        assertEquals(60f, round.right, .01f)
        assertEquals(30f, round.top, .01f)
        assertEquals(50f, round.bottom, .01f)
        val upright = parsePath(ellipse(50f, 50f, 4f, 8f, 90f)).getBounds() // rx turned onto the y axis
        assertEquals(8f, upright.height, .05f)
        assertEquals(16f, upright.width, .05f)
    }

    @Test fun marksAreStrokesUnlessTheyHaveNoWidth() {
        assertTrue(Mark("M0 0", Ink.HIGHLIGHT).filled)
        assertFalse(Mark("M0 0", Ink.LINE, 1f).filled)
        assertEquals(setOf('p', 'n', 'b', 'r', 'q', 'k'), PIECE_ART.keys)
        assertEquals(EBONY.eye, EBONY.ink(Ink.EYE))
    }
}
