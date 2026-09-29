package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import dk.cocode.chess.core.model.Square
import kotlin.math.roundToInt

/**
 * Maps between board squares and pixel positions on an 8x8 board whose squares are [squarePx] wide.
 * When [flipped] is true the board is shown from Black's side (a8 at bottom-left).
 */
object BoardGeometry {
    /** How deep the frame around the squares is, as a fraction of the framed board's width. */
    private const val FRAME_FRACTION = 0.02f

    fun frameDepth(boardPx: Float): Float = boardPx * FRAME_FRACTION

    /** Square size on a framed board [boardPx] wide. */
    fun squareSize(boardPx: Float): Float = (boardPx - 2 * frameDepth(boardPx)) / 8f

    /** The square under pixel ([x], [y]) of the framed board [boardPx] wide; touches on the frame clamp inward. */
    fun squareOnBoard(x: Float, y: Float, boardPx: Float, flipped: Boolean): Square {
        val frame = frameDepth(boardPx)
        return squareAt(x - frame, y - frame, squareSize(boardPx), flipped)
    }

    /**
     * The 9 whole-pixel lines that bound the columns (and the rows) on a framed board [boardPx] wide,
     * each rounded once, so squares laid between neighbouring lines tile the board with no gap.
     */
    fun squareEdgesPx(boardPx: Float): List<Int> {
        val frame = frameDepth(boardPx)
        val square = squareSize(boardPx)
        return (0..8).map { (frame + it * square).roundToInt() }
    }

    fun squareTopLeft(square: Square, squarePx: Float, flipped: Boolean): Offset {
        val col = if (flipped) 7 - square.file else square.file
        val rowFromTop = if (flipped) square.rank else 7 - square.rank
        return Offset(col * squarePx, rowFromTop * squarePx)
    }

    fun squareCenter(square: Square, squarePx: Float, flipped: Boolean): Offset {
        val topLeft = squareTopLeft(square, squarePx, flipped)
        return Offset(topLeft.x + squarePx / 2f, topLeft.y + squarePx / 2f)
    }

    /** The square under pixel ([x], [y]), clamped to the board edges. */
    fun squareAt(x: Float, y: Float, squarePx: Float, flipped: Boolean): Square {
        val col = (x / squarePx).toInt().coerceIn(0, 7)
        val rowFromTop = (y / squarePx).toInt().coerceIn(0, 7)
        val file = if (flipped) 7 - col else col
        val rank = if (flipped) rowFromTop else 7 - rowFromTop
        return Square(file, rank)
    }

    /** All 64 squares as they are shown, the top row left to right and then the next row down. */
    fun squaresInReadingOrder(flipped: Boolean): List<Square> =
        (0..63).map { squareAt((it % 8) + .5f, (it / 8) + .5f, 1f, flipped) }

    /** Light squares are the ones where file+rank is odd (a1 is dark). */
    fun isLight(square: Square): Boolean = (square.file + square.rank) % 2 == 1
}
