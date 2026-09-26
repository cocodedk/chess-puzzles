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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.viewmodel.PuzzleUiState

const val BOARD_TEST_TAG = "chessBoard"

/** The interactive framed 8x8 board: draws frame, squares, highlights and pieces, and reports tap/drag squares. */
@Composable
fun ChessBoard(
    state: PuzzleUiState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer(cacheSize = 16)   // the frame's 16 coordinate labels
    val palette = LocalBoardPalette.current
    val flipped = state.flipped
    var dragTarget by remember { mutableStateOf(Square(0, 0)) }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .testTag(BOARD_TEST_TAG)
            .pointerInput(flipped) {
                detectTapGestures { offset ->
                    onSquareTap(BoardGeometry.squareOnBoard(offset.x, offset.y, size.width.toFloat(), flipped))
                }
            }
            .pointerInput(flipped) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(BoardGeometry.squareOnBoard(offset.x, offset.y, size.width.toFloat(), flipped))
                    },
                    onDrag = { change, _ ->
                        val at = change.position
                        dragTarget = BoardGeometry.squareOnBoard(at.x, at.y, size.width.toFloat(), flipped)
                    },
                    onDragEnd = { onDragEnd(dragTarget) },
                )
            },
    ) {
        val frame = BoardGeometry.frameDepth(size.width)
        val squarePx = BoardGeometry.squareSize(size.width)
        drawFrame(frame, squarePx, flipped, textMeasurer)
        translate(frame, frame) {
            drawSquares(palette, squarePx, flipped)
            state.lastMove?.let { highlightMove(it.from, it.to, palette.lastMoveTint, squarePx, flipped) }
            state.hint?.let { highlightMove(it.from, it.to, palette.hintTint, squarePx, flipped, palette.hintRing) }
            state.selected?.let { tintSquare(it, palette.selectedTint, squarePx, flipped, palette.selectedRing) }
            drawPieces(state, palette, squarePx, flipped)
            drawTargets(state, palette.marker, squarePx, flipped)
        }
    }
}
