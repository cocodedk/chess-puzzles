package dk.cocode.chess

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.width
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.board.BoardGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/** Fails unless [rect] lies between the screen's left and right edges. */
fun SemanticsNodeInteractionsProvider.assertWithinScreenWidth(rect: DpRect) {
    val screen = onRoot().getUnclippedBoundsInRoot()
    assertTrue("$rect on $screen", rect.left >= screen.left && rect.right <= screen.right)
}

/** Fails unless the board is as wide as the screen, to within 1dp. */
fun SemanticsNodeInteractionsProvider.assertBoardSpansTheScreen() {
    val screen = onRoot().getUnclippedBoundsInRoot()
    val board = onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
    assertEquals(screen.width.value, board.width.value, 1f)
}

/** The board's width in pixels, frame included. */
fun SemanticsNodeInteractionsProvider.boardPx(): Float =
    onNodeWithTag(BOARD_TEST_TAG).fetchSemanticsNode().size.width.toFloat()

/** Centre of [square] in the board's own pixels, frame included: where a finger touches it. */
fun SemanticsNodeInteractionsProvider.centerOf(square: Square, flipped: Boolean = false): Offset {
    val board = boardPx()
    val frame = BoardGeometry.frameDepth(board)
    return BoardGeometry.squareCenter(square, BoardGeometry.squareSize(board), flipped) + Offset(frame, frame)
}
