package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.height
import dk.cocode.chess.PHONE_QUALIFIERS
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

/** The Share position button: labelled for a screen reader, a 48dp target, and in reach at every size. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class SharePositionButtonTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var shares = 0

    private fun show() = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = PuzzleUiState(),
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = {}, onPromotionCancel = {},
                onSharePosition = { shares++ },
            )
        }
    }

    private fun button() = composeRule.onNodeWithText("Share position")

    /** In view (after scrolling its panel, where the layout scrolls), on one line like About, and a 48dp touch target. */
    private fun assertReachable(scrolls: Boolean) {
        if (scrolls) button().performScrollTo()
        button().assertIsDisplayed()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val bounds = button().getUnclippedBoundsInRoot()
        assertTrue("inside the screen", bounds.left >= screen.left && bounds.right <= screen.right)
        val density = composeRule.activity.resources.displayMetrics.density
        val target = button().fetchSemanticsNode().touchBoundsInRoot
        assertTrue("touch target ${target.height.div(density)}dp high", target.height.div(density) >= 47.5f)
        assertTrue("touch target ${target.width.div(density)}dp wide", target.width.div(density) >= 47.5f)
        assertEquals(composeRule.onNodeWithText("About").getUnclippedBoundsInRoot().height.value, bounds.height.value, 0f)
    }

    @Test fun isAButtonThatSaysWhatItDoesAndSharesOnATap() {
        show()
        button().assertIsEnabled().assertHasClickAction()
        assertEquals(Role.Button, button().fetchSemanticsNode().config[SemanticsProperties.Role])
        button().performClick()
        assertEquals(1, shares)
    }

    @Test fun fitsAnUprightPhone() {
        show()
        assertReachable(scrolls = false)
    }

    @Test @Config(qualifiers = "w360dp-h640dp", fontScale = 1.3f)
    fun fitsAnUprightPhoneAtALargeFont() {
        show()
        assertReachable(scrolls = false)
    }

    @Test @Config(qualifiers = "w800dp-h360dp-land")
    fun fitsAPhoneOnItsSide() {
        show()
        assertReachable(scrolls = false)
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertTrue("on the buttons' side of the board", button().getUnclippedBoundsInRoot().left >= board.right)
    }

    @Test @Config(qualifiers = "w720dp-h356dp-land", fontScale = 1.3f)
    fun fitsAPhoneOnItsSideAtALargeFont() {
        show()
        assertReachable(scrolls = true)
    }
}
