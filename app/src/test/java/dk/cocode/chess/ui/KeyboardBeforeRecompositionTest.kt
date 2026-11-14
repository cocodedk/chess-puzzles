package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The keyboard as a phone brings it and takes it away: in one step, its new height reaching the layout a frame before
 * anything is composed again. Laid out then, the clock stopped, and once composed, the move field, the verdict and the
 * board are the same nodes as before: no layout of another family was composed on the way, which would have taken the
 * field's focus, and the keyboard, with it.
 */
@RunWith(RobolectricTestRunner::class)
class KeyboardBeforeRecompositionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun nodes() = listOf(composeRule.field(), composeRule.verdict(), composeRule.onNodeWithTag(BOARD_TEST_TAG))
        .map { it.fetchSemanticsNode().id }

    @OptIn(ExperimentalComposeUiApi::class) // measureAndLayoutForTest: a layout pass, and nothing composed
    private fun assertTheSameNodesAsAKeyboardJumps(height: Dp) {
        val keyboard = composeRule.showTheOpenFieldUnderAKeyboard { AFTER_A_WRONG_MOVE }
        val nodes = nodes()
        val root = composeRule.onRoot().fetchSemanticsNode().root!!
        composeRule.mainClock.autoAdvance = false
        for (step in listOf(height, 0.dp)) {
            keyboard.height = step
            Snapshot.sendApplyNotifications() // as the new insets reach the layout
            composeRule.runOnUiThread { root.measureAndLayoutForTest() }
            assertEquals("laid out with a $step keyboard, before composing", nodes, nodes())
            composeRule.mainClock.advanceTimeByFrame()
            assertEquals("composed with a $step keyboard", nodes, nodes())
        }
    }

    @Test @Config(qualifiers = ON_ITS_SIDE)
    fun aGalaxyA52sOnItsSide() = assertTheSameNodesAsAKeyboardJumps(SIDE_KEYBOARD)

    @Test @Config(qualifiers = SIXTEEN_NINE_ON_ITS_SIDE)
    fun aSixteenNinePhoneOnItsSide() = assertTheSameNodesAsAKeyboardJumps(SIXTEEN_NINE_KEYBOARD)

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun aGalaxyA52sUpright() = assertTheSameNodesAsAKeyboardJumps(PHONE_KEYBOARD)

    @Test @Config(qualifiers = SIXTEEN_NINE_UPRIGHT)
    fun aSixteenNinePhoneUpright() = assertTheSameNodesAsAKeyboardJumps(SIXTEEN_NINE_UPRIGHT_KEYBOARD)
}
