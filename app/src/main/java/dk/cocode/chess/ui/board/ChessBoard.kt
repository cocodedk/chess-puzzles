package dk.cocode.chess.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.viewmodel.BoardState

const val BOARD_TEST_TAG = "chessBoard"

/**
 * The interactive framed 8x8 board: draws frame, squares, highlights and pieces, and reports tap/drag
 * squares. A screen reader reads [BoardPieceLists] (White's list, then Black's) and then the 64
 * [BoardSquares], all laid over the drawing.
 */
@Composable
fun ChessBoard(
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A traversal group keeps the lists and squares together, so the order set on them is the order read.
    // A screen reader sorts a group's squares by the group's own direction, so it is left to right in every
    // language: in Persian it would otherwise read each rank from h to a.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier.aspectRatio(1f).semantics { isTraversalGroup = true }) {
            BoardCanvas(state, onSquareTap, onDragStart, onDragEnd)
            BoardPieceLists(state, Modifier.matchParentSize())
            BoardSquares(state, onSquareTap, Modifier.matchParentSize())
        }
    }
}

@Composable
private fun BoardCanvas(
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer(cacheSize = 16)   // the 16 coordinate labels
    val palette = LocalBoardPalette.current
    val flipped = state.flipped
    var dragTarget by remember { mutableStateOf(Square(0, 0)) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
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
        drawBoard(state, palette, textMeasurer)
    }
}
