package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.ui.board.BOARD_SQUARES_TAG
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val ARROWS = listOf("◀", "▶", "←")

/**
 * Persian is right to left, and Compose then lays everything out right to left. The text around the board
 * follows that, but the board's squares must not: a screen reader walks each rank from its a-file end, and
 * a1 sits bottom-left. The back and forward arrows turn round to point the way the row reads.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fa-ldrtl-$PHONE_QUALIFIERS")
class PersianRtlTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun theScreenIsPersianAndRightToLeftButTheSquaresReadLeftToRight() {
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                PuzzleScreenContent(
                    state = PuzzleUiState(),
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                    onHint = {}, onReset = {}, onNext = {},
                    onPromotion = {}, onPromotionCancel = {},
                )
            }
        }
        assertEquals("fa", composeRule.activity.resources.configuration.locales[0].language)
        val hint = composeRule.onNodeWithText("راهنمایی").fetchSemanticsNode() // the Hint button, in Persian
        assertEquals(LayoutDirection.Rtl, hint.layoutInfo.layoutDirection)
        // The title line is heard puzzle first, then rating ("Puzzle 0 of 0, Rating 0"), as a reader reads right to left.
        composeRule.onNodeWithText("معمای ۰ از ۰ · ریتینگ ۰").assertContentDescriptionEquals("معمای ۰ از ۰, ریتینگ ۰")
        val squares = composeRule.onNodeWithTag(BOARD_SQUARES_TAG)
        assertEquals(LayoutDirection.Ltr, squares.fetchSemanticsNode().layoutInfo.layoutDirection) // a screen reader sorts by this
        val a1 = squares.onChildren()[56] // reading order: a8 … h8, then down to a1 … h1
        a1.assertContentDescriptionEquals("a1, خالی") // "empty"
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        val square = a1.getUnclippedBoundsInRoot()
        val frameSlack = (board.right - board.left).value * .03f // a1 sits just inside the board's frame
        assertEquals("a1 sits on the left edge", board.left.value, square.left.value, frameSlack)
        assertEquals("a1 sits on the bottom edge", board.bottom.value, square.bottom.value, frameSlack)
    }

    @Test fun theArrowsPointTheWayTheRowReads() {
        var rightToLeft = emptyList<String>()
        var leftToRight = emptyList<String>()
        composeRule.setContent {
            rightToLeft = ARROWS.map { directional(it) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                leftToRight = ARROWS.map { directional(it) }
            }
        }
        composeRule.waitForIdle()
        assertEquals(listOf("▶", "◀", "→"), rightToLeft)
        assertEquals(ARROWS, leftToRight)
    }
}
