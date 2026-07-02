package dk.cocode.chess

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.board.NightBoardPalette
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
        composeRule.onNodeWithText("Reset").performClick()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.renderToBitmap()
    }

    @Test
    fun themeToggleCyclesThroughTheRealStack() {
        awaitText("Theme: Auto")
        composeRule.onNodeWithText("Theme: Auto").performClick()
        awaitText("Theme: Light")
        composeRule.onNodeWithText("Theme: Light").performClick()
        awaitText("Theme: Dark")
        val bitmap = composeRule.renderToBitmap()
        assertTrue(bitmap.containsColor(NightBoardPalette.darkSquare.toArgb())) // night board really drawn
        // Cycle back to SYSTEM: the DataStore singleton outlives this test in the Robolectric JVM.
        composeRule.onNodeWithText("Theme: Dark").performClick()
        awaitText("Theme: Auto")
    }
}
