package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.assertBoardSpansTheScreen
import dk.cocode.chess.scrollRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The move field open as the keyboard comes and goes on an upright phone; on its side, see [TypingOnItsSideTest]. */
@RunWith(RobolectricTestRunner::class)
class TypingKeyboardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val keyboard = TestKeyboard()

    private fun show() = composeRule.showTheOpenField({ AFTER_A_WRONG_MOVE }, keyboard)

    /** The field open but the keyboard down (Back hides it): the screen is the one without the field, Hint and Next included. */
    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun withTheKeyboardDownTheOpenFieldHidesNothing() {
        show()
        composeRule.field().assertExists()
        composeRule.assertBoardSpansTheScreen()
        composeRule.onNodeWithText("Hint").assertExists()
        composeRule.onAllNodesWithContentDescription("Next puzzle").assertCountEquals(1)
        composeRule.onNodeWithText("About").assertExists()
    }

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theFieldKeepsItsFocusAndTheVerdictItsVoiceAsTheKeyboardComesAndGoes() =
        composeRule.assertTheFieldKeepsItsFocus(PHONE_KEYBOARD)

    /**
     * Scrolled down to About on a small phone with the field open, the keyboard coming and going leaves the column
     * where it was: the rows that fit while a move is typed must not pull it back to the top.
     */
    @Test @Config(qualifiers = "w360dp-h640dp")
    fun theColumnKeepsItsPlaceAsTheKeyboardComesAndGoes() {
        show()
        composeRule.onNodeWithText("About").performScrollTo()
        val place = composeRule.scrollRange().value()
        assertTrue("scrolled down to $place", place > 0f)

        keyboard.height = PHONE_KEYBOARD
        composeRule.waitForIdle()
        keyboard.height = 0.dp
        composeRule.waitForIdle()
        assertEquals("the column's place", place, composeRule.scrollRange().value(), 0f)
    }
}
