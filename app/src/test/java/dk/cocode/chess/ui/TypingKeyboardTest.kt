package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.assertBoardSpansTheScreen
import dk.cocode.chess.scrollRange
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.viewmodel.Feedback
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

/** The move field open as the keyboard comes and goes, and on a phone on its side, where nothing changes. */
@RunWith(RobolectricTestRunner::class)
class TypingKeyboardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var state by mutableStateOf(
        PuzzleUiState(prompt = Prompt(PieceColor.WHITE, Goal.BEST_MOVE, 1), feedback = Feedback.WRONG),
    )
    private var keyboardHeight by mutableStateOf(0.dp)

    private fun show() = composeRule.showTheOpenField({ state }, { keyboardHeight })

    private fun verdict() = composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))

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

    /**
     * As the keyboard comes and goes the field keeps its focus, which keeps the keyboard up, and the verdict stays
     * the one live region a screen reader announces, still saying what became of the typed move.
     */
    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theFieldKeepsItsFocusAndTheVerdictItsVoiceAsTheKeyboardComesAndGoes() {
        show()
        val field = composeRule.field()
        field.assertIsFocused()
        val fieldId = field.fetchSemanticsNode().id
        val verdictId = verdict().fetchSemanticsNode().id

        keyboardHeight = PHONE_KEYBOARD
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Hint").assertCountEquals(0)
        field.assertIsFocused()
        assertEquals("the same field", fieldId, field.fetchSemanticsNode().id)
        state = state.copy(feedback = Feedback.ILLEGAL, announcement = "Not a legal move: e2e5.")
        composeRule.waitForIdle()
        assertEquals("the same live region", verdictId, verdict().fetchSemanticsNode().id)
        verdict().assertContentDescriptionEquals("Not a legal move: e2e5.")

        keyboardHeight = 0.dp
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Hint").assertExists()
        field.assertIsFocused()
        assertEquals("the same field", fieldId, field.fetchSemanticsNode().id)
        assertEquals("the same live region", verdictId, verdict().fetchSemanticsNode().id)
    }

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

        keyboardHeight = PHONE_KEYBOARD
        composeRule.waitForIdle()
        keyboardHeight = 0.dp
        composeRule.waitForIdle()
        assertEquals("the column's place", place, composeRule.scrollRange().value(), 0f)
    }

    /** A phone on its side, 800 x 352dp, under a keyboard that leaves it a strip: its panels stay beside the board. */
    @Test @Config(qualifiers = "w800dp-h200dp-land")
    fun onItsSideAPhoneKeepsItsPanelsWhileTyping() {
        keyboardHeight = 200.dp
        show()
        composeRule.onNodeWithText("Hint").assertExists()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertEquals("as tall as the screen", screen.height.value, board.height.value, 1f)
    }
}
