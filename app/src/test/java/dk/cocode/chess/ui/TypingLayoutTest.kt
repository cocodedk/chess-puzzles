package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import dk.cocode.chess.assertBoardSpansTheScreen
import dk.cocode.chess.assertWithinScreenWidth
import dk.cocode.chess.scrollRange
import dk.cocode.chess.textLayout
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.Goal
import dk.cocode.chess.viewmodel.Prompt
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A move typed on a phone held upright, in the 384 x 383dp its keyboard leaves: room for the whole board only if the
 * verdict and the move field are all that stay with it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = KEYBOARD_UP)
class TypingLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val state = PuzzleUiState(prompt = Prompt(PieceColor.WHITE, Goal.BEST_MOVE, 1), feedback = Feedback.WRONG)

    private fun show() = composeRule.showTheOpenField({ state }, { PHONE_KEYBOARD })

    private fun bounds(text: String) = composeRule.onNodeWithText(text, substring = true).getUnclippedBoundsInRoot()

    private fun board() = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()

    private fun assertOnScreen(name: String, rect: DpRect) {
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        assertTrue("$name at $rect on $screen", rect.top >= screen.top && rect.bottom <= screen.bottom)
        composeRule.assertWithinScreenWidth(rect)
    }

    private fun helpLayout() = composeRule.onNodeWithText("English letters", substring = true, useUnmergedTree = true).textLayout()

    /** The whole board, square, centred and unscrolled, above the field and Play. */
    private fun assertTheBoardAndTheFieldFit(): DpRect {
        val board = board()
        assertOnScreen("the board", board)
        assertEquals("square", board.width.value, board.height.value, 1f)
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        assertEquals("centred", (board.left - screen.left).value, (screen.right - board.right).value, 1f)
        assertOnScreen("the field", composeRule.field().getUnclippedBoundsInRoot())
        assertOnScreen("Play", bounds("Play"))
        assertEquals("the overflow", 0f, composeRule.scrollRange().maxValue(), 0f)
        return board
    }

    @Test fun theWholeBoardStaysInViewAboveTheFieldAndItsVerdict() {
        show()
        assertEquals("the height the keyboard leaves", 383f, composeRule.onRoot().getUnclippedBoundsInRoot().height.value, 0f)
        val board = assertTheBoardAndTheFieldFit()
        // 383dp less the verdict line (24) and the 8dp above it, Close move field (48) and the field with its
        // three-line help (116).
        assertTrue("the board is ${board.width} wide", board.width >= 187.dp)
        assertOnScreen("the verdict", bounds("Not the best move — try again"))
        assertOnScreen("the field's help", bounds("English letters and numbers"))
        assertOnScreen("Close move field", bounds("Close move field"))
        // Everything else waits for the keyboard to go.
        listOf("Hint", "Restart", "Openings", "Puzzle 0 of 0 · Rating 0", "Use light theme", "About", "Share position")
            .forEach { composeRule.onAllNodesWithText(it).assertCountEquals(0) }
        listOf("Previous puzzle", "Next puzzle", "Day streak", "Level, Easy", "Theme, All").forEach {
            composeRule.onAllNodesWithContentDescription(it, useUnmergedTree = true).assertCountEquals(0)
        }
    }

    /** Close move field, the keyboard still on its way down: the screen it had before the field opened. */
    @Test fun closingTheFieldWithTheKeyboardUpBringsTheScreenBack() {
        show()
        composeRule.onNodeWithText("Close move field").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Hint").assertExists()
        composeRule.assertBoardSpansTheScreen()
    }

    /** At a 1.3× font the field's rows grow, and the board gives way to them: 135dp here. */
    @Test @Config(fontScale = 1.3f)
    fun aLargerFontStillFitsTheWholeBoardAndTheField() {
        show()
        val board = assertTheBoardAndTheFieldFit()
        assertTrue("the board is ${board.width} wide", board.width >= 135.dp && board.width < 187.dp)
    }

    /**
     * At a 2× font, Android's largest, the verdict, Close and the field outgrow the 383dp: the board shrinks to nothing
     * rather than spill over them, and the column scrolls to the focused field, its whole help and Play in view, the
     * verdict's last line still showing above them: a live region scrolled wholly out of view may go unannounced.
     */
    @Test @Config(fontScale = 2f)
    fun theLargestFontStillShowsTheWholeField() {
        show()
        assertEquals("the board's size", 0f, board().width.value, 0f)
        assertOnScreen("the field", composeRule.field().getUnclippedBoundsInRoot())
        assertOnScreen("Play", bounds("Play"))
        assertFalse("the help is cut off", helpLayout().hasVisualOverflow)
        val verdict = bounds("Not the best move — try again")
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        assertTrue("the verdict at $verdict on $screen", verdict.bottom > screen.top && verdict.top < screen.bottom)
    }
}
