package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isSpecified
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.viewmodel.PuzzleUiState

/** Washes [square] in [color], with an inset [ring] round its edge unless it is unspecified. */
internal fun DrawScope.tintSquare(square: Square, color: Color, squarePx: Float, flipped: Boolean, ring: Color) {
    val topLeft = BoardGeometry.squareTopLeft(square, squarePx, flipped)
    drawRect(color = color, topLeft = topLeft, size = Size(squarePx, squarePx))
    if (ring.isSpecified) {
        val width = squarePx * .06f
        drawRect(
            color = ring,
            topLeft = topLeft + Offset(width / 2f, width / 2f),
            size = Size(squarePx - width, squarePx - width),
            style = Stroke(width),
        )
    }
}

internal fun DrawScope.highlightMove(
    from: Square,
    to: Square,
    color: Color,
    squarePx: Float,
    flipped: Boolean,
    ring: Color = Color.Unspecified,
) {
    tintSquare(from, color, squarePx, flipped, ring)
    tintSquare(to, color, squarePx, flipped, ring)
}

internal fun DrawScope.drawPieces(state: PuzzleUiState, palette: BoardPalette, squarePx: Float, flipped: Boolean) {
    for (rank in 0..7) {
        for (file in 0..7) {
            val code = state.board[rank][file]
            if (code == ' ') continue
            drawPiece(code, BoardGeometry.squareTopLeft(Square(file, rank), squarePx, flipped), squarePx, palette)
        }
    }
}

internal fun DrawScope.drawTargets(state: PuzzleUiState, marker: Color, squarePx: Float, flipped: Boolean) {
    for (target in state.legalTargets) {
        val center = BoardGeometry.squareCenter(target, squarePx, flipped)
        if (state.board[target.rank][target.file] == ' ') {
            drawCircle(marker, radius = squarePx * 0.16f, center = center)
        } else {
            drawCircle(marker, radius = squarePx * 0.45f, center = center, style = Stroke(width = squarePx * 0.07f))
        }
    }
}
