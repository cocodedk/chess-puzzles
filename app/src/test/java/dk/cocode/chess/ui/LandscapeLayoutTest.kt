package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.Goal
import dk.cocode.chess.viewmodel.Prompt
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

    private val empty = PuzzleUiState(board = List(8) { "        " }, prompt = Prompt(PieceColor.WHITE, Goal.BEST_MOVE, 1))

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
        assertEquals(bounds("Use light theme").height.value, bounds("About").height.value, 0f) // About on one line
    }

    /** Each theme button text, the longest ("Match phone theme") included, stays on one line in that same panel. */
    @Test @Config(qualifiers = "w720dp-h356dp-land", fontScale = 1.3f)
    fun everyThemeButtonTextFitsOneLineAtALargeFont() {
        var mode by mutableStateOf(ThemeMode.SYSTEM)
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                PuzzleScreenContent(
                    state = empty, themeMode = mode,
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                    onHint = {}, onReset = {}, onNext = {},
                    onPromotion = {}, onPromotionCancel = {},
                )
            }
        }
        mapOf(
            ThemeMode.SYSTEM to "Use light theme", ThemeMode.LIGHT to "Use dark theme", ThemeMode.DARK to "Match phone theme",
        ).forEach { (themeMode, text) ->
            mode = themeMode
            composeRule.waitForIdle()
            assertEquals(text, bounds("About").height.value, bounds(text).height.value, 0f)
        }
    }

    @Test @Config(qualifiers = "w1000dp-h600dp-land")
    fun panelsAreCentredBesideATallBoard() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val above = bounds("Puzzle 0 of 0 · Rating 0").top - screen.top
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
        val level = composeRule.onNodeWithContentDescription("Level, Easy").getUnclippedBoundsInRoot()
        assertTrue(bounds("Hint").top >= level.bottom) // one panel: the Level menu above the buttons
    }

    /** The whole board in view, unscrolled, and square. */
    private fun assertTheWholeBoardFits() {
        show()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertTrue("board to ${board.bottom} of ${screen.right} x ${screen.bottom}", board.bottom <= screen.bottom)
        assertEquals(board.width.value, board.height.value, 1f)
    }

    /** A near-square screen, wider than tall but with no room beside the board. */
    @Test @Config(qualifiers = "w700dp-h650dp")
    fun aNearSquareScreenFitsTheWholeBoard() = assertTheWholeBoardFits()

    /** A foldable's inner screen held upright, 673 x 785dp: taller than wide, but not by a phone's margin. */
    @Test @Config(qualifiers = "w673dp-h849dp")
    fun aFoldablesInnerScreenUprightFitsTheWholeBoard() = assertTheWholeBoardFits()

    @Test fun theBoardGoesWhereTheRoomBesideItAllows() {
        assertEquals(BoardLayout.BETWEEN_PANELS, boardLayout(800.dp, 360.dp)) // a phone on its side
        assertEquals(BoardLayout.BESIDE_PANEL, boardLayout(640.dp, 360.dp)) // a 16:9 phone on its side
        assertEquals(BoardLayout.UPRIGHT, boardLayout(360.dp, 800.dp)) // a phone upright
        assertEquals(BoardLayout.STACKED, boardLayout(700.dp, 650.dp)) // near-square: no room beside it
    }

    /**
     * Only a screen 1.3 times taller than wide, an upright phone, gives every board screen a full-width board
     * with its rows scrolling; elsewhere only Learn asks for that.
     */
    @Test fun theUprightLayoutIsOnlyForAnUprightPhone() {
        assertEquals(BoardLayout.STACKED, boardLayout(1000.dp, 1299.dp))
        assertEquals(BoardLayout.UPRIGHT, boardLayout(1000.dp, 1300.dp))
        assertEquals(BoardLayout.UPRIGHT, boardLayout(360.dp, 584.dp)) // a small 16:9 phone, less its system bars
        assertEquals(BoardLayout.STACKED, boardLayout(673.dp, 785.dp)) // a foldable's inner screen held upright
    }
}
