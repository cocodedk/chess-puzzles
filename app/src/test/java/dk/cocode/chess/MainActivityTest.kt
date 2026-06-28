package dk.cocode.chess

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesRendersAndHandlesInput() {
        composeRule.onNodeWithText("Hint").assertExists() // real app launched and rendered
        composeRule.renderToBitmap() // draws the real first puzzle
        composeRule.onNodeWithText("Hint").performClick() // enabled while in progress
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { click(center) }
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.onNodeWithText("Reset").performClick()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.renderToBitmap()
    }
}
