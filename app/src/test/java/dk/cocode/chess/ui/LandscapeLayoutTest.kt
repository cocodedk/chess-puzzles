package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w800dp-h360dp-land")
class LandscapeLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val empty = PuzzleUiState(board = List(8) { "        " }, promptText = "White to move")

    private fun show(state: PuzzleUiState = empty) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = state,
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = {}, onPromotionCancel = {},
            )
        }
    }

    // A stat's number is only in the unmerged tree: the stat is one item for a screen reader.
    private fun bounds(text: String, unmerged: Boolean = false) =
        composeRule.onNodeWithText(text, useUnmergedTree = unmerged).getUnclippedBoundsInRoot()

    @Test fun squareBoardSitsBetweenTheNumbersAndTheButtons() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertEquals(screen.height.value, board.height.value, 1f) // as tall as the screen ...
        assertEquals(board.height.value, board.width.value, 1f) // ... square ...
        assertEquals((board.left - screen.left).value, (screen.right - board.right).value, 1f) // ... and in the middle
        val stats = composeRule.onNodeWithContentDescription("Day streak", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue(stats.right <= board.left) // the numbers on its left ...
        assertTrue(bounds("Hint").left >= board.right) // ... the buttons on its right ...
        val next = composeRule.onNodeWithContentDescription("Next puzzle").getUnclippedBoundsInRoot()
        assertTrue(next.bottom <= screen.bottom) // ... stacked, and all on screen
    }

    /**
     * A phone on its side at a 1.3× font with long numbers, its side panels about 206dp wide: too narrow
     * for the five stats or for Theme and About side by side, so whole items must move to a new line.
     */
    @Test @Config(qualifiers = "w720dp-h356dp-land", fontScale = 1.3f) // set before the activity starts
    fun aLargeFontWrapsWholeItemsInsteadOfSqueezingThem() {
        show(empty.copy(dayStreak = 1, solvedCount = 1441, hintFreeCount = 1101, currentStreak = 53, bestStreak = 120))
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        val oneLine = bounds("1", unmerged = true).height.value
        for (number in listOf("1441", "1101", "53", "120")) {
            val stat = bounds(number, unmerged = true)
            assertEquals("$number on one line", oneLine, stat.height.value, 0f)
            assertTrue("$number beside, not under, the board", stat.right <= board.left)
        }
        assertEquals(bounds("Theme: Auto").height.value, bounds("About").height.value, 0f) // About on one line
    }

    @Test @Config(qualifiers = "w1000dp-h600dp-land")
    fun panelsAreCentredBesideATallBoard() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val above = bounds("Puzzle 0 of 0").top - screen.top
        val below = screen.bottom - bounds("About").bottom
        assertTrue("above $above", above > 50.dp)
        assertEquals("above $above, below $below", above.value, below.value, 8f)
    }

    @Test @Config(qualifiers = "w640dp-h360dp-land")
    fun aSixteenByNinePhoneKeepsTheBoardLeftOfOnePanel() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertEquals(0f, board.left.value, 0f)
        assertEquals(screen.height.value, board.height.value, 1f)
        assertTrue(bounds("Hint").left >= board.right)
        assertTrue(bounds("Hint").top >= bounds("Medium").bottom) // one panel: the bands above the buttons
    }

    @Test fun theBoardGoesWhereTheRoomBesideItAllows() {
        assertEquals(BoardLayout.BETWEEN_PANELS, boardLayout(800.dp, 360.dp)) // a phone on its side
        assertEquals(BoardLayout.BESIDE_PANEL, boardLayout(640.dp, 360.dp)) // a 16:9 phone on its side
        assertEquals(BoardLayout.STACKED, boardLayout(360.dp, 800.dp)) // a phone upright
        assertEquals(BoardLayout.STACKED, boardLayout(700.dp, 650.dp)) // near-square: no room beside it
    }
}
