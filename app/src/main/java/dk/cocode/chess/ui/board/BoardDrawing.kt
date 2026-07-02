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

internal val PIECE_WHITE = Color.White
internal val PIECE_DARK = Color(0xFF101010)
internal val PIECE_WHITE_OUTLINE = Color(0xFF2B2B2B)

internal fun DrawScope.drawSquares(palette: BoardPalette, squarePx: Float, flipped: Boolean) {
    for (file in 0..7) {
        for (rank in 0..7) {
            val square = Square(file, rank)
            drawRect(
                color = if (BoardGeometry.isLight(square)) palette.lightSquare else palette.darkSquare,
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
    palette: BoardPalette,
    squarePx: Float,
    flipped: Boolean,
    textMeasurer: TextMeasurer,
) {
    val fontSize = (squarePx * 0.74f).toSp()
    val outlineWidth = squarePx * palette.pieceOutlineWidth
    for (rank in 0..7) {
        for (file in 0..7) {
            val code = state.board[rank][file]
            if (code == ' ') continue
            val topLeft = BoardGeometry.squareTopLeft(Square(file, rank), squarePx, flipped)
            val white = PieceGlyph.isWhite(code)
            val glyph = PieceGlyph.glyph(code)
            // Outline first, fill on top: a contrasting rim so white pieces read on light
            // squares and dark pieces read on dark squares.
            val outline = textMeasurer.measure(
                text = glyph,
                style = TextStyle(
                    color = if (white) PIECE_WHITE_OUTLINE else palette.darkPieceOutline,
                    fontSize = fontSize,
                    drawStyle = Stroke(width = outlineWidth),
                ),
            )
            val fill = textMeasurer.measure(
                text = glyph,
                style = TextStyle(color = if (white) PIECE_WHITE else PIECE_DARK, fontSize = fontSize),
            )
            val offset = Offset(
                topLeft.x + (squarePx - fill.size.width) / 2f,
                topLeft.y + (squarePx - fill.size.height) / 2f,
            )
            drawText(textLayoutResult = outline, topLeft = offset)
            drawText(textLayoutResult = fill, topLeft = offset)
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
