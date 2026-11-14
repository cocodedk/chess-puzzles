package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import dk.cocode.chess.R
import dk.cocode.chess.assertOnScreen
import dk.cocode.chess.board
import dk.cocode.chess.bounds
import dk.cocode.chess.scrollRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A move typed on a phone on its side, in the 150dp strip its keyboard leaves: the whole board as tall as the strip,
 * beside one panel of the verdict, Close move field, and the field and Play, the field without its help line, which
 * would not fit; whichever of the two panel layouts the phone has.
 */
@RunWith(RobolectricTestRunner::class)
class TypingOnItsSideTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * The board as tall as the strip at the left; beside it the verdict, Close, the field and Play, unscrolled, or with
     * the panel scrolled [overflow] to the focused field and the verdict's top that far above the strip.
     */
    private fun assertTheBoardTheVerdictAndTheFieldFit(keyboard: Dp, overflow: Dp = 0.dp) {
        composeRule.showTheOpenField({ AFTER_AN_ILLEGAL_MOVE }, TestKeyboard(keyboard))
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.board()
        composeRule.assertOnScreen("the board", board)
        assertEquals("as tall as the strip", screen.height.value, board.height.value, 1f)
        assertEquals("square", board.height.value, board.width.value, 1f)
        assertEquals("at the left", screen.left.value, board.left.value, 0f)
        val field = composeRule.field().getUnclippedBoundsInRoot()
        assertTrue("the field beside the board", field.left >= board.right)
        val verdict = composeRule.verdict().getUnclippedBoundsInRoot()
        assertTrue("the verdict at $verdict on $screen", verdict.top >= screen.top - overflow && verdict.bottom <= screen.bottom)
        listOf(
            "Close move field" to composeRule.bounds(appString(R.string.puzzle_type_move_close)),
            "the field" to field,
            "Play" to composeRule.bounds(appString(R.string.puzzle_type_move_play)),
        ).forEach { (name, rect) -> composeRule.assertOnScreen(name, rect) }
        assertEquals("the overflow", overflow.value, composeRule.scrollRange().maxValue(), 0.5f)
        composeRule.onAllNodesWithText(appString(R.string.puzzle_type_move_help)).assertCountEquals(0)
    }

    /** A Galaxy A52s on its side, room for a panel either side of the board: a 150dp board beside a 703dp panel. */
    @Test @Config(qualifiers = KEYBOARD_UP_ON_ITS_SIDE)
    fun betweenPanelsOnlyTheBoardTheVerdictAndTheFieldStay() {
        assertTheBoardTheVerdictAndTheFieldFit(SIDE_KEYBOARD)
        composeRule.assertOnlyTheFieldStays()
    }

    /** A 16:9 phone on its side, room for one panel: a 150dp board beside a 490dp panel. */
    @Test @Config(qualifiers = SIXTEEN_NINE_KEYBOARD_UP)
    fun besideOnePanelOnlyTheBoardTheVerdictAndTheFieldStay() {
        assertTheBoardTheVerdictAndTheFieldFit(SIXTEEN_NINE_KEYBOARD)
        composeRule.assertOnlyTheFieldStays()
    }

    /** Spanish, its Close button and help the longest: a row each, and no help, whatever the language. */
    @Test @Config(qualifiers = "es-$SIXTEEN_NINE_KEYBOARD_UP")
    fun inSpanishTheyFitToo() = assertTheBoardTheVerdictAndTheFieldFit(SIXTEEN_NINE_KEYBOARD)

    /**
     * At a 1.3× font the verdict, Close and the field take 152dp, 2dp more than the strip: the panel scrolls 2dp to the
     * focused field, Close, the field and Play stay whole, and the verdict's top 2dp stand above the strip.
     */
    @Test @Config(qualifiers = KEYBOARD_UP_ON_ITS_SIDE, fontScale = 1.3f)
    fun atALargerFontTheVerdictsTopScrollsAboveTheStrip() =
        assertTheBoardTheVerdictAndTheFieldFit(SIDE_KEYBOARD, overflow = 2.dp)

    @Test @Config(qualifiers = ON_ITS_SIDE)
    fun betweenPanelsTheFieldKeepsItsFocusAsTheKeyboardComesAndGoes() = composeRule.assertTheFieldKeepsItsFocus(SIDE_KEYBOARD)

    @Test @Config(qualifiers = SIXTEEN_NINE_ON_ITS_SIDE)
    fun besideOnePanelTheFieldKeepsItsFocusAsTheKeyboardComesAndGoes() =
        composeRule.assertTheFieldKeepsItsFocus(SIXTEEN_NINE_KEYBOARD)
}
