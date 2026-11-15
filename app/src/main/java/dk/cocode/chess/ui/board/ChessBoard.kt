package dk.cocode.chess.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.ui.rememberTexts
import dk.cocode.chess.viewmodel.BoardState
import dk.cocode.chess.viewmodel.pieceList

const val BOARD_TEST_TAG = "chessBoard"

/**
 * The interactive framed 8x8 board: draws frame, squares, highlights and pieces, and reports tap/drag
 * squares. A screen reader first reads the drawing's description, the whole [pieceList], then sees the
 * 64 [BoardSquares] over the drawing.
 */
@Composable
fun ChessBoard(
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.aspectRatio(1f)) {
        BoardCanvas(state, onSquareTap, onDragStart, onDragEnd)
        BoardSquares(state, onSquareTap, Modifier.matchParentSize())
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
    val texts = rememberTexts()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .testTag(BOARD_TEST_TAG)
            // The drawing, described for a screen reader: where every piece stands, read in one go before
            // the 64 squares. Sighted users never see it, so it needs no setting. Its click does nothing:
            // without one, a TalkBack double-tap on it would become a finger tap on the centre square.
            .semantics {
                contentDescription = texts.pieceList(state)
                onClick { true }
            }
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
