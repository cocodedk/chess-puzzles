package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.near
import dk.cocode.chess.viewmodel.Highlight
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.math.ceil
import kotlin.math.floor

/**
 * The coordinates are written on the squares after the highlights are washed over them, so what lies
 * behind a glyph is the wood plus whatever the last move, the hint and the selection laid on it (and
 * the pale ring round a hint or a selection), not the bare wood [BoardPaletteTest] measures. This draws
 * the real board in every such state and reads the contrast off its pixels.
 */
@RunWith(RobolectricTestRunner::class)
class BoardCoordinateContrastTest {
    // Large enough that the glyphs are several pixels thick, so their anti-aliased edge and the
    // background a couple of pixels beyond it are told apart.
    private val px = 1536
    private val squarePx = BoardGeometry.squareSize(px.toFloat())
    private val frame = BoardGeometry.frameDepth(px.toFloat())
    private val measurer by lazy {
        TextMeasurer(createFontFamilyResolver(RuntimeEnvironment.getApplication()), Density(1f), LayoutDirection.Ltr)
    }

    /** What is laid over a pair of neighbouring labelled squares; [selected] is 0 or 1 for which of the two. */
    private class Laid(val last: Boolean, val hint: Boolean, val selected: Int?) {
        override fun toString() = "last=$last hint=$hint selected=$selected"
    }

    private val everyLaying = listOf(false, true).flatMap { last ->
        listOf(false, true).flatMap { hint -> listOf(null, 0, 1).map { Laid(last, hint, it) } }
    }

    /**
     * Two neighbouring squares, one maple and one walnut, that carry labels: the top-left corner of the
     * board down its left edge (the rank written at the very top, rank 8 unflipped), and the bottom row
     * along d and e (the files).
     */
    private fun pairs(flipped: Boolean) = listOf(
        "top row" to listOf(.5f to .5f, .5f to 1.5f), "bottom row" to listOf(3.5f to 7.5f, 4.5f to 7.5f),
    ).map { (name, cells) -> name to cells.map { (x, y) -> BoardGeometry.squareAt(x, y, 1f, flipped) } }

    private fun render(palette: BoardPalette, flipped: Boolean, pair: List<Square>, laid: Laid): IntArray {
        val state = PuzzleUiState(
            board = List(8) { "        " },
            flipped = flipped,
            selected = laid.selected?.let { pair[it] },
            lastMove = Highlight(pair[0], pair[1]).takeIf { laid.last },
            hint = Highlight(pair[0], pair[1]).takeIf { laid.hint },
        )
        val image = ImageBitmap(px, px)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(px.toFloat(), px.toFloat())) {
            drawBoard(state, palette, measurer)
        }
        return IntArray(px * px).also { image.asAndroidBitmap().getPixels(it, 0, px, 0, 0, px, px) }
    }

    /** WCAG contrast ratio between two opaque colours. */
    private fun contrast(a: Color, b: Color): Float {
        val hi = maxOf(a.luminance(), b.luminance())
        val lo = minOf(a.luminance(), b.luminance())
        return (hi + 0.05f) / (lo + 0.05f)
    }

    /** [of] grown by one pixel in every direction. */
    private fun dilate(of: Array<BooleanArray>): Array<BooleanArray> = Array(of.size) { y ->
        BooleanArray(of[y].size) { x ->
            (-1..1).any { dy -> (-1..1).any { dx -> of.getOrNull(y + dy)?.getOrNull(x + dx) == true } }
        }
    }

    /**
     * The contrast of [ink] against each pixel two pixels out from its glyphs on [square], beyond the
     * glyph's own anti-aliased edge: what a reader sees right behind the stroke, be it wood, wash, ring
     * or the glyph's outline. Null if no ink is there.
     */
    private fun behindTheInk(pixels: IntArray, square: Square, flipped: Boolean, ink: Color): List<Float>? {
        val at = BoardGeometry.squareTopLeft(square, squarePx, flipped)
        val x0 = ceil(frame + at.x).toInt()   // whole pixels inside the square: its edge pixel may be frame or neighbour
        val y0 = ceil(frame + at.y).toInt()
        val size = floor(squarePx).toInt() - 1
        val cell = Array(size) { y -> IntArray(size) { x -> pixels[(y0 + y) * px + x0 + x] } }
        val core = Array(size) { y -> BooleanArray(size) { x -> near(cell[y][x], ink.toArgb(), 24) } }
        if (core.sumOf { row -> row.count { it } } < 20) return null
        val near1 = dilate(core)
        val near2 = dilate(near1)
        return (0 until size).flatMap { y -> (0 until size).filter { x -> near2[y][x] && !near1[y][x] }.map { x -> contrast(ink, Color(cell[y][x])) } }
    }

    private fun ink(square: Square) = if (BoardGeometry.isLight(square)) LABEL_ON_MAPLE else LABEL_ON_WALNUT

    /**
     * Every label on both woods, in both looks and both ways round, under every mix of last move, hint
     * and selection: the ink reaches WCAG's 4.5:1 for small text against what is typically behind it, and
     * 3:1 against the worst of it (the palest grain fleck is the existing tolerance for that). The top row
     * is the pair carrying rank 8 (rank 1 when flipped); the bottom row carries the files.
     */
    @Test fun coordinatesReadUnderEveryHighlight() {
        val failures = mutableListOf<String>()
        for ((look, palette) in listOf("day" to DayBoardPalette, "night" to NightBoardPalette)) {
            for (flipped in listOf(false, true)) {
                for ((where, pair) in pairs(flipped)) {
                    for (laid in everyLaying) {
                        val pixels = render(palette, flipped, pair, laid)
                        for (square in pair) {
                            val found = behindTheInk(pixels, square, flipped, ink(square))
                            val name = "$look ${if (flipped) "flipped" else "unflipped"} $where $square ($laid)"
                            if (found == null) { failures += "$name: no ink drawn"; continue }
                            val median = found.sorted()[found.size / 2]
                            val worst = found.min()
                            if (median < 4.5f || worst < 3f) failures += "$name: typical %.2f, worst %.2f".format(median, worst)
                        }
                    }
                }
            }
        }
        assertTrue("${failures.size} unreadable:\n" + failures.joinToString("\n"), failures.isEmpty())
    }
}
