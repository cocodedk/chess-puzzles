package dk.cocode.chess.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.viewmodel.PuzzleUiState

const val BOARD_TEST_TAG = "chessBoard"

/** The interactive 8x8 board: draws squares, highlights and pieces, and reports tap/drag squares. */
@Composable
fun ChessBoard(
    state: PuzzleUiState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val flipped = state.flipped
    var dragTarget by remember { mutableStateOf(Square(0, 0)) }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .testTag(BOARD_TEST_TAG)
            .pointerInput(flipped) {
                detectTapGestures { offset ->
                    onSquareTap(BoardGeometry.squareAt(offset.x, offset.y, size.width / 8f, flipped))
                }
            }
            .pointerInput(flipped) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(BoardGeometry.squareAt(offset.x, offset.y, size.width / 8f, flipped))
                    },
                    onDrag = { change, _ ->
                        dragTarget = BoardGeometry.squareAt(change.position.x, change.position.y, size.width / 8f, flipped)
                    },
                    onDragEnd = { onDragEnd(dragTarget) },
                )
            },
    ) {
        val squarePx = size.width / 8f
        drawSquares(squarePx, flipped)
        state.lastMove?.let { highlightMove(it.from, it.to, LAST_MOVE_TINT, squarePx, flipped) }
        state.hint?.let { highlightMove(it.from, it.to, HINT_TINT, squarePx, flipped) }
        state.selected?.let { tintSquare(it, SELECTED_TINT, squarePx, flipped) }
        drawPieces(state, squarePx, flipped, textMeasurer)
        drawTargets(state, squarePx, flipped)
    }
}
