package dk.cocode.chess

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.board.DayBoardPalette
import dk.cocode.chess.ui.board.NightBoardPalette
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun awaitText(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun launchesRendersAndHandlesInput() {
        awaitText("Hint") // real app launched and rendered once the first DataStore read lands
        composeRule.renderToBitmap() // draws the real first puzzle
        composeRule.onNodeWithText("Hint").performClick() // enabled while in progress
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { click(center) }
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.onNodeWithText("Restart").performClick()
        composeRule.onNodeWithContentDescription("Next puzzle").performClick()
        composeRule.renderToBitmap()
    }

    @Test
    fun themeToggleCyclesThroughTheRealStack() {
        awaitText("Use light theme")
        composeRule.onNodeWithText("Use light theme").performClick()
        awaitText("Use dark theme")
        composeRule.onNodeWithText("Use dark theme").performClick()
        awaitText("Match phone theme")
        val bitmap = composeRule.renderToBitmap()
        assertTrue(bitmap.showsWood(NightBoardPalette.darkSquare, DayBoardPalette.darkSquare)) // night board really drawn
        // Cycle back to SYSTEM: the DataStore singleton outlives this test in the Robolectric JVM.
        composeRule.onNodeWithText("Match phone theme").performClick()
        awaitText("Use light theme")
    }

    @Test
    fun tabsSwitchBetweenPuzzlesAndOpenings() {
        awaitText("Hint")
        composeRule.onNodeWithText("Openings").performClick()
        awaitText("As White")
        composeRule.onNodeWithText("Puzzles").performClick()
        awaitText("Hint")
    }

    @Test
    fun theBackButtonLeavesAnOpeningForTheList() {
        awaitText("Hint")
        composeRule.onNodeWithText("Openings").performClick()
        composeRule.onNodeWithText("Italian Game", useUnmergedTree = true).performClick()
        awaitText("Learn")
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitText("As White") // back on the list, not out of the app
        assertFalse(composeRule.activity.isFinishing)
    }
}
