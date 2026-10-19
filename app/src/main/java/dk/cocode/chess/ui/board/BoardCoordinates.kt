package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dk.cocode.chess.core.model.Square

/** The coordinates are inlaid: walnut-dark on maple squares, pale holly on walnut ones, each outlined in the other's. */
internal val LABEL_ON_MAPLE = Color(0xFF3E2415)
internal val LABEL_ON_WALNUT = Color(0xFFFFF6E4)

private const val LABEL_SIZE = .2f
private const val LABEL_OUTLINE = LABEL_SIZE * .16f   // drawn centred on the glyph's edge, so half shows

/**
 * The files along the bottom row and the ranks up the left-hand column, written on the squares
 * themselves: each rank in its square's top-left corner, each file in its bottom-right.
 */
internal fun DrawScope.drawCoordinates(squarePx: Float, flipped: Boolean, textMeasurer: TextMeasurer) {
    val inset = squarePx * .06f
    val edge = if (flipped) 7 else 0   // both the left-hand file and the bottom rank
    for (i in 0..7) {
        val rankSquare = Square(edge, i)
        val rank = measureLabel((i + 1).toString(), rankSquare, squarePx, textMeasurer)
        drawLabel(rank, rankSquare, BoardGeometry.squareTopLeft(rankSquare, squarePx, flipped) + Offset(inset, 0f), squarePx)
        val fileSquare = Square(i, edge)
        val file = measureLabel(('a' + i).toString(), fileSquare, squarePx, textMeasurer)
        val bottomRight = BoardGeometry.squareTopLeft(fileSquare, squarePx, flipped) + Offset(squarePx, squarePx)
        drawLabel(file, fileSquare, bottomRight - Offset(file.size.width + inset, file.size.height.toFloat()), squarePx)
    }
}

/**
 * Writes [label] on [on] over an outline in the other ink's colour, [LABEL_OUTLINE] of a square thick. The
 * ink alone only reads against bare wood: a last-move, hint or selection wash lightens walnut to a
 * mid-tone neither ink clears 4.5:1 on (pale holly on a selected walnut square is 2.4:1), and the pale hint and
 * selection rings run right under the glyph. So the glyph is set against its outline, whatever lies beneath.
 */
private fun DrawScope.drawLabel(label: TextLayoutResult, on: Square, topLeft: Offset, squarePx: Float) {
    val outline = if (BoardGeometry.isLight(on)) LABEL_ON_WALNUT else LABEL_ON_MAPLE
    drawText(label, color = outline, topLeft = topLeft, drawStyle = Stroke(squarePx * LABEL_OUTLINE, join = StrokeJoin.Round))
    drawText(label, topLeft = topLeft, drawStyle = Fill)   // explicit: a null style would keep the stroke just set
}

private fun DrawScope.measureLabel(text: String, on: Square, squarePx: Float, textMeasurer: TextMeasurer): TextLayoutResult =
    textMeasurer.measure(
        text,
        TextStyle(
            color = if (BoardGeometry.isLight(on)) LABEL_ON_MAPLE else LABEL_ON_WALNUT,
            fontSize = (squarePx * LABEL_SIZE).toSp(),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
        ),
    )
