package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.assertBoardSpansTheScreen
import dk.cocode.chess.assertWithinScreenWidth
import dk.cocode.chess.centerOf
import dk.cocode.chess.scrollRange
import dk.cocode.chess.textLayout
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.Goal
import dk.cocode.chess.viewmodel.Prompt
import dk.cocode.chess.viewmodel.PuzzleTheme
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
 * A puzzle on a phone held upright: the rows around the board used to take their height first and
 * squeeze it, to about half the width at a 1.3× font. It must span the screen, the rows below scrolling.
 */
@RunWith(RobolectricTestRunner::class)
class PuzzlePortraitSizeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val drags = mutableListOf<Square>()

    private fun show() = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = PuzzleUiState(
                    prompt = Prompt(PieceColor.WHITE, Goal.BEST_MOVE, 1), rating = 1523, position = 412, bandSize = 1500,
                    solvedCount = 1441, hintFreeCount = 1101, currentStreak = 53, bestStreak = 120, dayStreak = 12,
                ),
                onSquareTap = {}, onDragStart = { drags += it }, onDragEnd = { drags += it },
                onHint = {}, onReset = {}, onNext = {}, onPromotion = {}, onPromotionCancel = {},
                themes = PuzzleTheme.entries,
                tabs = { Spacer(Modifier.height(48.dp)) }, // the Puzzles | Openings row
            )
        }
    }

    private fun boardBounds() = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()

    /** A tall phone at a 1.3× font: the board spans it, and Hint sits under the board, in view. */
    @Test @Config(qualifiers = "w412dp-h915dp", fontScale = 1.3f)
    fun boardSpansThePhoneWithTheHintButtonInView() {
        show()
        composeRule.assertBoardSpansTheScreen()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = boardBounds()
        val hint = composeRule.onNodeWithText("Hint").getUnclippedBoundsInRoot()
        assertTrue("Hint at ${hint.top} under the board at ${board.bottom}", hint.top >= board.bottom)
        assertTrue("Hint at ${hint.bottom} of ${screen.bottom}", hint.bottom <= screen.bottom)
    }

    /**
     * 384 x 758dp at font 1.0: every row fits, down to About, unscrolled, and Hint stays 44dp or more under the
     * board, clear of a tap meant for its bottom rank.
     */
    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theWholeScreenFitsWithoutScrolling() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        assertEquals("the screen's usable height", 758f, (screen.bottom - screen.top).value, 0f)
        val gap = composeRule.onNodeWithText("Hint").getUnclippedBoundsInRoot().top - boardBounds().bottom
        val about = composeRule.onNodeWithText("About").getUnclippedBoundsInRoot()
        val overflow = with(composeRule.density) { composeRule.scrollRange().maxValue().toDp() }
        val said = "Hint $gap under the board, About at ${about.bottom} of ${screen.bottom}, overflow $overflow"
        assertTrue(said, gap >= 44.dp)
        assertTrue(said, about.bottom <= screen.bottom)
        assertEquals(said, 0f, overflow.value, 0f)
    }

    private fun titleLine() = composeRule.onNodeWithText("Puzzle 412 of 1500 · Rating 1523")

    private fun titleLayout() = titleLine().textLayout()

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theTitleAndRatingShareOneLine() {
        show()
        assertEquals(1, titleLayout().lineCount)
    }

    /** At a 2× font, Android's largest, on a small phone the title line wraps onto two lines rather than clip. */
    @Test @Config(qualifiers = "w360dp-h640dp", fontScale = 2f)
    fun theTitleLineWrapsRatherThanClipsAtALargeFont() {
        show()
        val line = titleLine().getUnclippedBoundsInRoot()
        composeRule.assertWithinScreenWidth(line)
        val layout = titleLayout()
        assertFalse("the title is cut off", layout.hasVisualOverflow)
        assertEquals("the title's lines", 2, layout.lineCount)
        val stats = composeRule.onNodeWithContentDescription("Day streak", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("the title, down to ${line.bottom}, runs into the stats at ${stats.top}", line.bottom <= stats.top)
    }

    /**
     * On a small phone the column overflows and scrolls, yet a piece dragged up the board moves the piece,
     * not the column; the same swipe just above the board does scroll it, so the scroll is live.
     */
    @Test @Config(qualifiers = "w360dp-h640dp")
    fun aDragUpTheBoardMovesThePieceNotTheColumn() {
        show()
        val range = composeRule.scrollRange()
        assertTrue("the column overflows by ${range.maxValue()}", range.maxValue() > 0f)

        val top = boardBounds().top
        val e4 = composeRule.centerOf(Square.of("e4"))
        val e8 = composeRule.centerOf(Square.of("e8"))
        // Slow, so the drag starts (passes the touch slop) still inside e4, as a finger's many small moves do.
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { swipe(e4, e8, 1000) }
        composeRule.waitForIdle()
        assertEquals("the board stayed put", top.value, boardBounds().top.value, 0f)
        assertEquals(listOf(Square.of("e4"), Square.of("e8")), drags)

        val aboveBoard = with(composeRule.density) { top.toPx() } - 4f
        composeRule.onRoot().performTouchInput { swipe(Offset(centerX, aboveBoard), Offset(centerX, 4f)) }
        composeRule.waitForIdle()
        assertTrue("the column scrolled: ${boardBounds().top} above $top", boardBounds().top < top)
    }
}
