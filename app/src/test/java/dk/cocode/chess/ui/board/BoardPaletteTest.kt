package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dk.cocode.chess.WOOD_TOLERANCE
import dk.cocode.chess.near
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class) // the grain's fibres are Android paths
class BoardPaletteTest {
    private val palettes = listOf(DayBoardPalette, NightBoardPalette)

    /** Render tests find a look by its wood within [WOOD_TOLERANCE], so no two woods may pass for each other. */
    @Test fun noTwoWoodsPassForEachOther() {
        val woods = palettes.flatMap { listOf(it.lightSquare, it.darkSquare) }
        for (a in woods.indices) for (b in a + 1 until woods.size) {
            assertFalse("${woods[a]} vs ${woods[b]}", near(woods[a].toArgb(), woods[b].toArgb(), WOOD_TOLERANCE))
        }
    }

    @Test fun nightBoardIsDimmerThanDay() {
        assertTrue(NightBoardPalette.lightSquare.luminance() < DayBoardPalette.lightSquare.luminance())
        assertTrue(NightBoardPalette.darkSquare.luminance() < DayBoardPalette.darkSquare.luminance())
    }

    @Test fun squaresStillReadAsACheckerAtNight() {
        palettes.forEach { assertTrue(contrast(it.lightSquare, it.darkSquare) > 2.5f) }
    }

    /**
     * Every colour the board really paints on its light and on its dark squares, at a phone's square
     * size: grain, crossing fibres and sheen included, so no shade of the wood is left out.
     */
    private fun paintedWood(palette: BoardPalette): Pair<Set<Color>, Set<Color>> {
        val px = 1024
        val squarePx = px / 8f
        val image = ImageBitmap(px, px)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(px.toFloat(), px.toFloat())) {
            drawSquares(palette, squarePx, flipped = false)
        }
        val pixels = IntArray(px * px).also { image.asAndroidBitmap().getPixels(it, 0, px, 0, 0, px, px) }
        val (light, dark) = pixels.indices.partition { i ->
            BoardGeometry.isLight(BoardGeometry.squareAt((i % px).toFloat(), (i / px).toFloat(), squarePx, false))
        }
        return light.map { pixels[it] }.toSet().map { Color(it) }.toSet() to dark.map { pixels[it] }.toSet().map { Color(it) }.toSet()
    }

    /**
     * Each colour clears the WCAG 3:1 bar for non-text on every shade of the grained wood, through
     * whatever carries its edge there. On the wood it is worst against, that is ivory's dark rim on
     * maple and ebony's aura on walnut. On the other wood it is ivory's lit body and ebony's own dark
     * body. A carved piece's shaded flank is darker by design: ivory's dips to about 2:1 on walnut,
     * and only its lit side is held to the bar there.
     */
    @Test fun everyPieceColourClearsThreeToOneOnTheGrainedWood() {
        palettes.forEach { p ->
            val (maple, walnut) = paintedWood(p)
            fun worst(on: Set<Color>, edge: (Color) -> Float) = on.minOf(edge)
            val found = mapOf(
                "ivory rim on maple" to worst(maple) { contrast(IVORY.outline, it) },
                "ebony body on maple" to worst(maple) { contrast(EBONY.shading[1].second, it) },
                "ebony aura on walnut" to worst(walnut) { contrast(EBONY.outline, p.ebonyHalo.compositeOver(it)) },
                "lit ivory on walnut" to worst(walnut) { contrast(IVORY.shading[1].second, it) },
            )
            found.forEach { (edge, ratio) -> assertTrue("$edge: $ratio in $found", ratio > 3f) }
        }
    }

    /** CIE ΔE between two opaque colours: about 10 and up reads as a different colour at a glance. */
    private fun deltaE(a: Color, b: Color): Float {
        val (p, q) = a.convert(ColorSpaces.CieLab) to b.convert(ColorSpaces.CieLab)
        return sqrt((p.red - q.red) * (p.red - q.red) + (p.green - q.green) * (p.green - q.green) +
            (p.blue - q.blue) * (p.blue - q.blue))
    }

    /**
     * Every highlight and the legal-move dot stand out on both woods in both looks. Brightness alone
     * undersells them: the amber last move is barely lighter than maple, yet plainly a different
     * colour. So this measures the colour difference, brightness and hue together.
     */
    @Test fun everyHighlightAndMarkerStandsOutOnEveryWood() {
        palettes.forEach { p ->
            for (mark in listOf(p.selectedTint, p.lastMoveTint, p.hintTint, p.marker)) {
                for (wood in listOf(p.lightSquare, p.darkSquare)) {
                    val difference = deltaE(mark.compositeOver(wood), wood)
                    assertTrue("$mark on $wood: ΔE $difference", difference >= 12f)
                }
            }
        }
    }

    /** The rim and the aura carry the pieces' edges on the wood, so neither may thin to a hairline. */
    @Test fun rimAndAuraStayThickEnoughToCarryTheEdge() {
        assertTrue(OUTLINE_WIDTH >= 1.5f)
        palettes.forEach { assertTrue(it.ebonyHaloWidth >= 4f) }
    }

    @Test fun ebonyAuraIsStrongerAtNight() {
        assertTrue(NightBoardPalette.ebonyHaloWidth > DayBoardPalette.ebonyHaloWidth)
        assertTrue(NightBoardPalette.ebonyHalo.alpha > DayBoardPalette.ebonyHalo.alpha)
    }

    /** The hint must not pass for the last move or the selection: a different hue, not just a shade. */
    @Test fun hintLooksUnlikeTheOtherHighlights() {
        palettes.forEach { p ->
            assertTrue(p.hintTint.green > p.hintTint.red)
            assertTrue(p.lastMoveTint.red > p.lastMoveTint.green)
            assertTrue(p.selectedTint.red > p.selectedTint.green)
        }
    }

    /** WCAG contrast ratio between two opaque colors. */
    private fun contrast(a: Color, b: Color): Float {
        val hi = maxOf(a.luminance(), b.luminance())
        val lo = minOf(a.luminance(), b.luminance())
        return (hi + 0.05f) / (lo + 0.05f)
    }

}
