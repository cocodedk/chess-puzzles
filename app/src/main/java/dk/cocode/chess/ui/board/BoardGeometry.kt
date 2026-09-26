package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import dk.cocode.chess.core.model.Square

/**
 * Maps between board squares and pixel positions on an 8x8 board whose squares are [squarePx] wide.
 * When [flipped] is true the board is shown from Black's side (a8 at bottom-left).
 */
object BoardGeometry {
    /** How deep the frame around the squares is, as a fraction of the framed board's width. */
    private const val FRAME_FRACTION = 0.058f

    fun frameDepth(boardPx: Float): Float = boardPx * FRAME_FRACTION

    /** Square size on a framed board [boardPx] wide. */
    fun squareSize(boardPx: Float): Float = (boardPx - 2 * frameDepth(boardPx)) / 8f

    /** The square under pixel ([x], [y]) of the framed board [boardPx] wide; touches on the frame clamp inward. */
    fun squareOnBoard(x: Float, y: Float, boardPx: Float, flipped: Boolean): Square {
        val frame = frameDepth(boardPx)
        return squareAt(x - frame, y - frame, squareSize(boardPx), flipped)
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

    /** Light squares are the ones where file+rank is odd (a1 is dark). */
    fun isLight(square: Square): Boolean = (square.file + square.rank) % 2 == 1
}
