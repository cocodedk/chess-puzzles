package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.board
import dk.cocode.chess.scrollsUpAndDown
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The system bars and the camera's cutout pad the puzzle screen, upright and on its side: nothing goes under them. */
@RunWith(RobolectricTestRunner::class)
class ScreenInsetsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private companion object {
        val CUTOUT = 32.dp // at the left
        val STATUS_BAR = 24.dp
        val NAVIGATION_BAR = 48.dp // at the right
        val GESTURE_BAR = 16.dp
    }

    @Before fun show() = composeRule.showThePuzzleScreen(
        { AFTER_A_WRONG_MOVE },
        insets = WindowInsets(left = CUTOUT, top = STATUS_BAR, right = NAVIGATION_BAR, bottom = GESTURE_BAR),
    )

    /** The columns that scroll: the panels either side of the board, or the one column above and below it. */
    private fun scrolling() = composeRule.onAllNodes(scrollsUpAndDown())

    /** The board as tall as the room the bars leave, between panels out to the cutout and the navigation bar. */
    @Test @Config(qualifiers = ON_ITS_SIDE)
    fun onItsSide() {
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.board()
        assertEquals("the board's top", STATUS_BAR.value, board.top.value, 0.5f)
        assertEquals("the board's bottom", (screen.bottom - GESTURE_BAR).value, board.bottom.value, 0.5f)
        assertEquals("the left panel's left", CUTOUT.value, scrolling()[0].getUnclippedBoundsInRoot().left.value, 0.5f)
        val right = scrolling()[1].getUnclippedBoundsInRoot().right
        assertEquals("the right panel's right", (screen.right - NAVIGATION_BAR).value, right.value, 0.5f)
    }

    /** The column above and below the board, as wide as it, fills the room the bars leave. */
    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun upright() {
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val column = scrolling()[0].getUnclippedBoundsInRoot()
        assertEquals("the column's top", STATUS_BAR.value, column.top.value, 0.5f)
        assertEquals("the column's bottom", (screen.bottom - GESTURE_BAR).value, column.bottom.value, 0.5f)
        assertEquals("the board's left", CUTOUT.value, composeRule.board().left.value, 0.5f)
        assertEquals("the board's right", (screen.right - NAVIGATION_BAR).value, composeRule.board().right.value, 0.5f)
    }
}
