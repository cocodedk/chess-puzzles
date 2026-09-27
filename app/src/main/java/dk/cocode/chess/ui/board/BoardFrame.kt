package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

private val MAHOGANY = arrayOf(0f to Color(0xFF5C2416), .55f to Color(0xFF3B150D), 1f to Color(0xFF220A05))
private val LACQUER_SHEEN = arrayOf(
    0f to Color(0x24FFFFFF), .26f to Color.Transparent, .72f to Color.Transparent, 1f to Color(0x0DFFFFFF),
)

/** How far the bands around the squares reach into the frame, as a fraction of its depth; the brass corners fill the rest. */
internal const val BAND_REACH = .36f

/** The bands around the squares, outermost first: dark bronze, a brass fillet, then a black reveal. */
private val BORDER_BANDS = listOf(BAND_REACH to Color(0xFF4A3418), .3f to Color(0xFFB8914A), .12f to Color(0xFF190704))

/**
 * The lacquered mahogany rim around a board [frame] px deep with [squarePx] squares: the bands that seat
 * the squares, and brass brackets on the corners filling the mahogany outside the bands.
 */
internal fun DrawScope.drawFrame(frame: Float, squarePx: Float) {
    val corner = CornerRadius(frame * .32f)
    drawRoundRect(
        brush = Brush.radialGradient(*MAHOGANY, center = Offset(size.width * .3f, size.height * .1f), radius = size.width * 1.2f),
        cornerRadius = corner,
    )
    drawRoundRect(brush = Brush.verticalGradient(*LACQUER_SHEEN), cornerRadius = corner)
    for ((reach, color) in BORDER_BANDS) {
        val grow = frame * reach
        drawRect(color, Offset(frame - grow, frame - grow), Size(squarePx * 8 + 2 * grow, squarePx * 8 + 2 * grow))
    }
    drawBrassCorners(frame * (1 - BAND_REACH))
}
