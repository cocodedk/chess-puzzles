package dk.cocode.chess.ui.board

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.viewmodel.BoardState
import dk.cocode.chess.viewmodel.squareDescription
import kotlin.math.roundToInt

const val BOARD_SQUARES_TAG = "chessBoardSquares"

/**
 * The 64 squares a screen reader sees over the drawn board: one item per square, in reading order,
 * each covering the square it names and saying what [squareDescription] says. They draw nothing and
 * take no touches; a double-tap runs [onSquareTap], as a finger tap on the square does.
 */
@Composable
internal fun BoardSquares(state: BoardState, onSquareTap: (Square) -> Unit, modifier: Modifier = Modifier) {
    val flipped = state.flipped
    val order = remember(flipped) { BoardGeometry.squaresInReadingOrder(flipped) }
    Layout(
        content = {
            order.forEach { square ->
                Box(
                    Modifier.semantics {
                        contentDescription = squareDescription(state, square)
                        onClick { onSquareTap(square); true }
                    },
                )
            }
        },
        modifier = modifier.testTag(BOARD_SQUARES_TAG),
    ) { measurables, constraints ->
        val boardPx = constraints.maxWidth.toFloat()
        val frame = BoardGeometry.frameDepth(boardPx)
        val squarePx = BoardGeometry.squareSize(boardPx)
        val side = squarePx.roundToInt()
        val placeables = measurables.map { it.measure(Constraints.fixed(side, side)) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { i, placeable ->
                val at = BoardGeometry.squareTopLeft(order[i], squarePx, flipped)
                placeable.place((frame + at.x).roundToInt(), (frame + at.y).roundToInt())
            }
        }
    }
}
