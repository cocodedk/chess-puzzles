package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser

/** One colour of the set: the body's light-to-shadow gradient, its outline, and the detail inks. */
internal class PieceSet(
    val shading: Array<Pair<Float, Color>>,
    val outline: Color,
    val highlight: Color,
    val line: Color,
    val eye: Color,
) {
    fun ink(ink: Ink): Color = when (ink) {
        Ink.HIGHLIGHT -> highlight
        Ink.LINE -> line
        Ink.EYE -> eye
    }
}

internal val IVORY = PieceSet(
    shading = arrayOf(0f to Color(0xFFFFFDF8), .36f to Color(0xFFF7F0E1), .74f to Color(0xFFE3D3B3), 1f to Color(0xFFBDA27A)),
    // Dark enough that the rim or the body clears 3:1 on every shade of the grained maple:
    // wherever the rim's contrast falls, the body's rises, and they cross above 3:1.
    outline = Color(0xFF4A3C27),
    highlight = Color(0xD9FFFFFF),
    line = Color(0x8C6B5638),
    eye = Color(0xFF4D3B26),
)

internal val EBONY = PieceSet(
    shading = arrayOf(0f to Color(0xFF5D5249), .28f to Color(0xFF2F2A25), .7f to Color(0xFF1E1B18), 1f to Color(0xFF0B0A09)),
    outline = Color(0xFF050404),
    highlight = Color(0x42FFEED6),
    line = Color(0x33FFEED6),
    eye = Color(0x8CFFEED6),
)

internal fun pieceSet(code: Char): PieceSet = if (code.isUpperCase()) IVORY else EBONY

internal fun parsePath(data: String): Path = PathParser().parsePathString(data).toPath()

/** A piece's art parsed once: its body parts with their bounds, the whole silhouette, and the marks. */
private class PiecePaths(art: PieceArt) {
    val body: List<Pair<Path, Rect>> = art.body.map { parsePath(it).let { path -> path to path.getBounds() } }
    val silhouette = Path().apply { body.forEach { addPath(it.first) } }
    val marks: List<Pair<Mark, Path>> = art.marks.map { it to parsePath(it.path) }
}

private val PIECE_PATHS by lazy { PIECE_ART.mapValues { PiecePaths(it.value) } }

private val SHADOW = Color(0x47000000)
/** The rim around every part of a piece, in its 100-unit box: it carries ivory's edge on maple. */
internal const val OUTLINE_WIDTH = 1.5f
private val OUTLINE = Stroke(width = OUTLINE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)

/**
 * Draws the piece for FEN letter [code] filling the [size]-px square at [topLeft]: a drop shadow, a
 * warm aura behind ebony, then each shaded, outlined body part back to front and its carved marks.
 */
internal fun DrawScope.drawPiece(code: Char, topLeft: Offset, size: Float, palette: BoardPalette) {
    val set = pieceSet(code)
    val paths = PIECE_PATHS.getValue(code.lowercaseChar())
    translate(topLeft.x, topLeft.y) {
        scale(size / 100f, pivot = Offset.Zero) {
            translate(top = 3f) { drawPath(paths.silhouette, SHADOW) }
            if (set === EBONY) {
                val aura = Stroke(width = palette.ebonyHaloWidth, join = StrokeJoin.Round)
                drawPath(paths.silhouette, palette.ebonyHalo, style = aura)
            }
            for ((part, bounds) in paths.body) {
                // The reference shades each part across its own box, lit from the upper left.
                val end = Offset(bounds.right, bounds.top + bounds.height * .22f)
                drawPath(part, Brush.linearGradient(*set.shading, start = bounds.topLeft, end = end))
                drawPath(part, set.outline, style = OUTLINE)
            }
            for ((mark, path) in paths.marks) {
                val style = if (mark.filled) Fill else Stroke(mark.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawPath(path, set.ink(mark.ink), style = style)
            }
        }
    }
}
