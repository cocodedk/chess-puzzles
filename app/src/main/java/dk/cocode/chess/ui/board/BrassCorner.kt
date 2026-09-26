package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform

/** The reference's bracket for the top-left corner, in its 64x64 box; the others are its mirror images. */
private val PLATE by lazy { parsePath("M3 3H55a6.5 6.5 0 0 1 0 13H25a9 9 0 0 0-9 9V55a6.5 6.5 0 0 1-13 0Z") }
private val BEVEL by lazy { parsePath("M6 6.5H54M6.5 6V54") }
private val CHASE by lazy { parsePath("M22 19c2-1.4 4-2 6-2") }

/** Screw heads: centre, radius, and the slot across each head. */
private val SCREWS by lazy {
    listOf(
        Triple(Offset(9.6f, 9.6f), 3.6f, parsePath("M7.4 11.8l4.4-4.4")),
        Triple(Offset(47f, 9.6f), 2.6f, parsePath("M45.4 9.6h3.2")),
        Triple(Offset(9.6f, 47f), 2.6f, parsePath("M9.6 45.4v3.2")),
    )
}

private val BRASS = arrayOf(
    0f to Color(0xFFFBE6AE), .35f to Color(0xFFD9B56A), .6f to Color(0xFFA98237), .85f to Color(0xFFE2C47E),
    1f to Color(0xFF8D6A2F),
)
private val SCREW = arrayOf(0f to Color(0xFFFFF2C9), .5f to Color(0xFFC9A45C), 1f to Color(0xFF6E5125))
private val ENGRAVING = Color(0xFF4D3713)

/** Brass brackets [sizePx] square over the four corners of the board. */
internal fun DrawScope.drawBrassCorners(sizePx: Float) {
    val k = sizePx / 64f
    for ((x, y) in listOf(0f to 0f, size.width to 0f, 0f to size.height, size.width to size.height)) {
        withTransform({
            translate(x, y)
            scale(if (x == 0f) k else -k, if (y == 0f) k else -k, pivot = Offset.Zero)
        }) { drawBracket() }
    }
}

private fun DrawScope.drawBracket() {
    translate(top = 1.5f) { drawPath(PLATE, Color(0x66000000)) }
    drawPath(PLATE, Brush.linearGradient(*BRASS, start = Offset(3f, 3f), end = Offset(61.5f, 61.5f)))
    drawPath(PLATE, ENGRAVING, style = Stroke(1f))
    drawPath(BEVEL, Color(0xA6FFF8DC), style = Stroke(1f))
    drawPath(CHASE, ENGRAVING.copy(alpha = 0x99 / 255f), style = Stroke(1f))
    for ((center, radius, slot) in SCREWS) {
        val light = Offset(center.x - radius * .24f, center.y - radius * .3f)
        drawCircle(Brush.radialGradient(*SCREW, center = light, radius = radius * 1.4f), radius, center)
        drawCircle(ENGRAVING, radius, center, style = Stroke(.7f))
        drawPath(slot, ENGRAVING, style = Stroke(.8f))
    }
}
