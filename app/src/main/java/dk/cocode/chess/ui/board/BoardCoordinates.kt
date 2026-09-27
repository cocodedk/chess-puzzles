package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dk.cocode.chess.core.model.Square

/** The coordinates are inlaid: walnut-dark on maple squares, pale holly on walnut ones. */
internal val LABEL_ON_MAPLE = Color(0xFF3E2415)
internal val LABEL_ON_WALNUT = Color(0xFFFFF6E4)

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
        drawText(rank, topLeft = BoardGeometry.squareTopLeft(rankSquare, squarePx, flipped) + Offset(inset, 0f))
        val fileSquare = Square(i, edge)
        val file = measureLabel(('a' + i).toString(), fileSquare, squarePx, textMeasurer)
        val bottomRight = BoardGeometry.squareTopLeft(fileSquare, squarePx, flipped) + Offset(squarePx, squarePx)
        drawText(file, topLeft = bottomRight - Offset(file.size.width + inset, file.size.height.toFloat()))
    }
}

private fun DrawScope.measureLabel(text: String, on: Square, squarePx: Float, textMeasurer: TextMeasurer): TextLayoutResult =
    textMeasurer.measure(
        text,
        TextStyle(
            color = if (BoardGeometry.isLight(on)) LABEL_ON_MAPLE else LABEL_ON_WALNUT,
            fontSize = (squarePx * .2f).toSp(),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
        ),
    )
