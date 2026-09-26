package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Square

private val MAHOGANY = arrayOf(0f to Color(0xFF5C2416), .55f to Color(0xFF3B150D), 1f to Color(0xFF220A05))
private val LACQUER_SHEEN = arrayOf(
    0f to Color(0x24FFFFFF), .26f to Color.Transparent, .72f to Color.Transparent, 1f to Color(0x0DFFFFFF),
)
private val BRASS_HAIRLINE = Color(0x80C9A45C)

/** The bands around the squares, outermost first: dark bronze, a brass fillet, then a black reveal. */
private val BORDER_BANDS = listOf(5f to Color(0xFF4A3418), 4f to Color(0xFFB8914A), 2f to Color(0xFF190704))

private val COORDINATE = Color(0xFFD9B76C)
private const val FAR_SIDE_ALPHA = .55f

/**
 * The lacquered mahogany frame around a board [frame] px deep with [squarePx] squares: a brass hairline,
 * the bands that seat the squares, the files and ranks in the frame, and brass brackets on the corners.
 * The near-side coordinates (bottom and left) are bright; the far side repeats them faded.
 */
internal fun DrawScope.drawFrame(frame: Float, squarePx: Float, flipped: Boolean, textMeasurer: TextMeasurer) {
    val corner = CornerRadius(frame * .32f)
    drawRoundRect(
        brush = Brush.radialGradient(*MAHOGANY, center = Offset(size.width * .3f, size.height * .1f), radius = size.width * 1.2f),
        cornerRadius = corner,
    )
    drawRoundRect(brush = Brush.verticalGradient(*LACQUER_SHEEN), cornerRadius = corner)
    val inset = frame * .36f
    drawRect(
        color = BRASS_HAIRLINE,
        topLeft = Offset(inset, inset),
        size = Size(size.width - 2 * inset, size.height - 2 * inset),
        style = Stroke(1.dp.toPx()),
    )
    for ((reach, color) in BORDER_BANDS) {
        val grow = reach.dp.toPx()
        drawRect(color, Offset(frame - grow, frame - grow), Size(squarePx * 8 + 2 * grow, squarePx * 8 + 2 * grow))
    }
    drawCoordinates(frame, squarePx, flipped, textMeasurer)
    drawBrassCorners(frame * 1.9f)
}

private fun DrawScope.drawCoordinates(frame: Float, squarePx: Float, flipped: Boolean, textMeasurer: TextMeasurer) {
    val style = TextStyle(
        color = COORDINATE,
        fontSize = (frame * .5f).toSp(),
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Serif,
        shadow = Shadow(color = Color(0xBF000000), offset = Offset(0f, -1.dp.toPx())),
    )
    val near = size.width - frame / 2f
    val far = frame / 2f
    for (i in 0..7) {
        // Square(i, i) sits on file i and rank i, so BoardGeometry places both labels, flip included.
        val at = BoardGeometry.squareCenter(Square(i, i), squarePx, flipped) + Offset(frame, frame)
        val file = textMeasurer.measure(('a' + i).toString(), style)
        val rank = textMeasurer.measure((i + 1).toString(), style)
        drawLabel(file, Offset(at.x, near), 1f)
        drawLabel(file, Offset(at.x, far), FAR_SIDE_ALPHA)
        drawLabel(rank, Offset(far, at.y), 1f)
        drawLabel(rank, Offset(near, at.y), FAR_SIDE_ALPHA)
    }
}

private fun DrawScope.drawLabel(label: TextLayoutResult, center: Offset, alpha: Float) {
    val topLeft = Offset(center.x - label.size.width / 2f, center.y - label.size.height / 2f)
    drawText(textLayoutResult = label, topLeft = topLeft, alpha = alpha)
}
