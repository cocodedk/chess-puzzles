package dk.cocode.chess.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.viewmodel.PuzzleUiState

internal val LIGHT_SQUARE = Color(0xFFEEEED2)
internal val DARK_SQUARE = Color(0xFF769656)
internal val SELECTED_TINT = Color(0x6603A9F4)
internal val LAST_MOVE_TINT = Color(0x55FFEB3B)
internal val HINT_TINT = Color(0x553F51B5)
internal val MARKER = Color(0x40000000)

internal fun DrawScope.drawSquares(squarePx: Float, flipped: Boolean) {
    for (file in 0..7) {
        for (rank in 0..7) {
            val square = Square(file, rank)
            drawRect(
                color = if (BoardGeometry.isLight(square)) LIGHT_SQUARE else DARK_SQUARE,
                topLeft = BoardGeometry.squareTopLeft(square, squarePx, flipped),
                size = Size(squarePx, squarePx),
            )
        }
    }
}

internal fun DrawScope.tintSquare(square: Square, color: Color, squarePx: Float, flipped: Boolean) {
    drawRect(
        color = color,
        topLeft = BoardGeometry.squareTopLeft(square, squarePx, flipped),
        size = Size(squarePx, squarePx),
    )
}

internal fun DrawScope.highlightMove(from: Square, to: Square, color: Color, squarePx: Float, flipped: Boolean) {
    tintSquare(from, color, squarePx, flipped)
    tintSquare(to, color, squarePx, flipped)
}

internal fun DrawScope.drawPieces(
    state: PuzzleUiState,
    squarePx: Float,
    flipped: Boolean,
    textMeasurer: TextMeasurer,
) {
    for (rank in 0..7) {
        for (file in 0..7) {
            val code = state.board[rank][file]
            if (code == ' ') continue
            val topLeft = BoardGeometry.squareTopLeft(Square(file, rank), squarePx, flipped)
            val color = if (PieceGlyph.isWhite(code)) Color.White else Color(0xFF101010)
            val layout = textMeasurer.measure(
                text = PieceGlyph.glyph(code),
                style = TextStyle(color = color, fontSize = (squarePx * 0.74f).toSp()),
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    topLeft.x + (squarePx - layout.size.width) / 2f,
                    topLeft.y + (squarePx - layout.size.height) / 2f,
                ),
            )
        }
    }
}

internal fun DrawScope.drawTargets(state: PuzzleUiState, squarePx: Float, flipped: Boolean) {
    for (target in state.legalTargets) {
        val center = BoardGeometry.squareCenter(target, squarePx, flipped)
        if (state.board[target.rank][target.file] == ' ') {
            drawCircle(MARKER, radius = squarePx * 0.16f, center = center)
        } else {
            drawCircle(MARKER, radius = squarePx * 0.45f, center = center, style = Stroke(width = squarePx * 0.07f))
        }
    }
}
