package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import dk.cocode.chess.core.model.Square
import java.util.Random

/** One fibre of grain across a unit square, running left to right with a gentle wander. */
internal class Streak(val path: Path, width: Float, val color: Color) {
    val stroke = Stroke(width)
}

private val PORE = Color(0xFF3C1E0A)
private val FLECK = Color(0xFFFFF8E6)

/** The two ends of the faint wash across each square. */
internal val SHEEN_LIGHT = FLECK.copy(alpha = 0x0D / 255f)
internal val SHEEN_DARK = PORE.copy(alpha = 0x0F / 255f)
private const val STREAKS_PER_SQUARE = 18

/** The grain of the square numbered [index] (0..63): the same every draw, different on every square. */
internal fun grainFor(index: Int): List<Streak> {
    val random = Random(index * 7919L + 17)
    fun wander() = (random.nextFloat() - .5f) * .08f
    return List(STREAKS_PER_SQUARE) { n ->
        val y = random.nextFloat()
        val path = Path().apply {
            moveTo(-.05f, y)
            cubicTo(.33f, y + wander(), .66f, y + wander(), 1.05f, y + wander())
        }
        // Mostly dark pores of varied weight, with every fourth fibre a pale fleck catching the light.
        val color = if (n % 4 == 3) FLECK.copy(alpha = .06f + random.nextFloat() * .08f)
        else PORE.copy(alpha = .05f + random.nextFloat() * .17f)
        Streak(path, width = .004f + random.nextFloat() * .022f, color = color)
    }
}

private val GRAIN by lazy { List(64) { grainFor(it) } }

/**
 * Maple and walnut squares with visible grain. Light squares run it across and dark squares down,
 * laid like parquet as in the reference; each square keeps its own grain when the board flips.
 */
internal fun DrawScope.drawSquares(palette: BoardPalette, squarePx: Float, flipped: Boolean) {
    for (file in 0..7) {
        for (rank in 0..7) {
            val square = Square(file, rank)
            val index = file * 8 + rank   // not Square.index: the grain stays on its own squares
            val light = BoardGeometry.isLight(square)
            val topLeft = BoardGeometry.squareTopLeft(square, squarePx, flipped)
            drawRect(if (light) palette.lightSquare else palette.darkSquare, topLeft, Size(squarePx, squarePx))
            clipRect(topLeft.x, topLeft.y, topLeft.x + squarePx, topLeft.y + squarePx) {
                translate(topLeft.x, topLeft.y) {
                    scale(squarePx, pivot = Offset.Zero) {
                        rotate(if (light) 0f else 90f, pivot = Offset(.5f, .5f)) {
                            GRAIN[index].forEach { drawPath(it.path, it.color, style = it.stroke) }
                        }
                    }
                }
            }
            drawSheen(topLeft, squarePx, index)
        }
    }
}

/** A faint wash across each square at its own angle, so neighbours catch the light differently. */
private fun DrawScope.drawSheen(topLeft: Offset, squarePx: Float, index: Int) {
    val slant = (index * 37 % 11) / 10f
    drawRect(
        brush = Brush.linearGradient(
            0f to SHEEN_LIGHT,
            1f to SHEEN_DARK,
            start = topLeft + Offset(0f, squarePx * slant),
            end = topLeft + Offset(squarePx, squarePx * (1f - slant)),
        ),
        topLeft = topLeft,
        size = Size(squarePx, squarePx),
    )
}
