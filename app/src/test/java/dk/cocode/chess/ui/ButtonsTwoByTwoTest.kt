package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.width
import dk.cocode.chess.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * In the panel of a phone on its side Hint and Restart share a row and Previous and Next the row below, each button a
 * full 48dp touch target; the rows mirror in a right-to-left language and a screen reader still meets the buttons
 * Hint, Restart, Previous, Next.
 */
@RunWith(RobolectricTestRunner::class)
class ButtonsTwoByTwoTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val labels by lazy { // in the test's language
        listOf(R.string.puzzle_hint, R.string.puzzle_reset, R.string.puzzle_previous, R.string.puzzle_next).map { appString(it) }
    }

    private fun button(label: String) = composeRule.onNode(hasText(label) or hasContentDescription(label))

    private fun showTheButtons(): List<DpRect> {
        composeRule.showThePuzzleScreen({ AFTER_A_WRONG_MOVE })
        return labels.map { button(it).getUnclippedBoundsInRoot() }
    }

    /** The four buttons, Hint, Restart, Previous, Next, in two rows, the second below the first, and one cell wide each. */
    private fun assertTwoByTwo(buttons: List<DpRect>) {
        val (hint, restart, previous, next) = buttons
        assertEquals("Hint and Restart share a row", hint.top.value, restart.top.value, 0f)
        assertEquals("Previous and Next share a row", previous.top.value, next.top.value, 0f)
        assertTrue("Previous and Next below Hint and Restart", previous.top >= hint.bottom)
        assertEquals("equal cells", hint.width.value, previous.width.value, 0f)
        assertEquals("equal cells", restart.width.value, next.width.value, 0f)
    }

    @Test @Config(qualifiers = SAMSUNG_ON_ITS_SIDE)
    fun hintAndRestartShareARowAndPreviousAndNextTheRowBelow() {
        val buttons = showTheButtons()
        assertTwoByTwo(buttons)
        val (hint, restart, previous, next) = buttons
        assertTrue("Hint left of Restart", hint.right <= restart.left)
        assertTrue("Previous left of Next", previous.right <= next.left)
    }

    /** The buttons draw 36dp tall but a finger finds each one 48dp tall, so the rows touch. */
    @Test @Config(qualifiers = SAMSUNG_ON_ITS_SIDE)
    fun eachButtonStaysAFullTouchTarget() {
        showTheButtons()
        val touch = labels.map { button(it).fetchSemanticsNode().touchBoundsInRoot }
        touch.forEach { assertEquals(48f, with(composeRule.density) { it.height.toDp() }.value, 0f) }
        assertEquals("the second row where the first ends", touch[0].bottom, touch[2].top, 0f)
    }

    /** The semantics tree, which a screen reader walks, lists the buttons in that order. */
    @Test @Config(qualifiers = SAMSUNG_ON_ITS_SIDE)
    fun aScreenReaderMeetsHintRestartPreviousNext() {
        showTheButtons()
        val oneOfThem = labels.map { hasText(it) or hasContentDescription(it) }.reduce { a, b -> a or b }
        val inOrder = composeRule.onAllNodes(hasClickAction() and oneOfThem)
            .fetchSemanticsNodes().map { node ->
                node.config.getOrNull(SemanticsProperties.Text)?.single()?.text
                    ?: node.config[SemanticsProperties.ContentDescription].single()
            }
        assertEquals(labels, inOrder)
    }

    @Test @Config(qualifiers = "fa-ldrtl-$SAMSUNG_ON_ITS_SIDE")
    fun inPersianTheRowsMirror() {
        val buttons = showTheButtons()
        assertTwoByTwo(buttons)
        val (hint, restart, previous, next) = buttons
        assertTrue("Hint right of Restart", hint.left >= restart.right)
        assertTrue("Previous right of Next", previous.left >= next.right)
    }
}
