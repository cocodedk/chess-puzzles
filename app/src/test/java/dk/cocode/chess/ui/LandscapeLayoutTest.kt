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

    private fun show() = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = PuzzleUiState(board = List(8) { "        " }, promptText = "White to move"),
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = {}, onPromotionCancel = {},
            )
        }
    }

    private fun bounds(text: String) = composeRule.onNodeWithText(text).getUnclippedBoundsInRoot()

    @Test fun squareBoardSitsBetweenTheNumbersAndTheButtons() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertEquals(screen.height.value, board.height.value, 1f) // as tall as the screen ...
        assertEquals(board.height.value, board.width.value, 1f) // ... square ...
        assertEquals((board.left - screen.left).value, (screen.right - board.right).value, 1f) // ... and in the middle
        val stats = composeRule.onNodeWithContentDescription("Day streak").getUnclippedBoundsInRoot()
        assertTrue(stats.right <= board.left) // the numbers on its left ...
        assertTrue(bounds("Hint").left >= board.right) // ... the buttons on its right ...
        assertTrue(bounds("Next").bottom <= screen.bottom) // ... stacked, and all on screen
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
