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

const val BOARD_SQUARES_TAG = "chessBoardSquares"

/**
 * The 64 squares a screen reader sees over the drawn board: one item per square, in reading order,
 * each covering the square it names and saying what [squareDescription] says. They draw nothing and
 * take no touches; a double-tap runs [onSquareTap], as a finger tap on the square does — unless the
 * state is not [BoardState.tappable], when the items are read but offer no click.
 */
@Composable
internal fun BoardSquares(state: BoardState, onSquareTap: (Square) -> Unit, modifier: Modifier = Modifier) {
    val flipped = state.flipped
    val tappable = state.tappable
    val order = remember(flipped) { BoardGeometry.squaresInReadingOrder(flipped) }
    Layout(
        content = {
            order.forEach { square ->
                Box(
                    Modifier.semantics {
                        contentDescription = squareDescription(state, square)
                        if (tappable) onClick { onSquareTap(square); true }
                    },
                )
            }
        },
        modifier = modifier.testTag(BOARD_SQUARES_TAG),
    ) { measurables, constraints ->
        // Reading order runs row by row from the top, so item i sits in column i % 8 and row i / 8.
        val edges = BoardGeometry.squareEdgesPx(constraints.maxWidth.toFloat())
        val placeables = measurables.mapIndexed { i, measurable ->
            val col = i % 8
            val row = i / 8
            measurable.measure(Constraints.fixed(edges[col + 1] - edges[col], edges[row + 1] - edges[row]))
        }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { i, placeable -> placeable.place(edges[i % 8], edges[i / 8]) }
        }
    }
}
