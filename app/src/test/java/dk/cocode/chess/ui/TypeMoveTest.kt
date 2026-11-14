package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.board
import dk.cocode.chess.bounds
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Type a move field: shut until asked for, then a field and Play that hand the text over and empty themselves. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class TypeMoveTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val typed = mutableListOf<String>()

    private companion object {
        const val HELP = "English letters and numbers in any writing system, like e2e4 or Nf3 (K, Q, R, B, N for the pieces, no letter for a pawn)."
    }

    @Composable
    private fun Screen(status: PuzzleStatus = PuzzleStatus.IN_PROGRESS) = ChessTheme(darkTheme = false) {
        PuzzleScreenContent(
            state = PuzzleUiState(status = status),
            onSquareTap = {}, onDragStart = {}, onDragEnd = {},
            onHint = {}, onReset = {}, onNext = {},
            onPromotion = {}, onPromotionCancel = {},
            onMoveTyped = { typed += it },
        )
    }

    private fun show(status: PuzzleStatus = PuzzleStatus.IN_PROGRESS) = composeRule.setContent { Screen(status) }

    private fun open() = composeRule.onNodeWithText("Type a move").performClick()

    @Test fun staysShutUntilAskedFor() {
        show()
        composeRule.onNodeWithText("Play").assertDoesNotExist()
        open()
        composeRule.field().assertIsEnabled()
    }

    @Test fun playHandsTheTextOverAndEmptiesTheField() {
        show()
        open()
        composeRule.onNodeWithText("Play").assertIsNotEnabled() // nothing typed yet
        composeRule.field().performTextInput("Nf3")
        composeRule.onNodeWithText("Play").assertIsEnabled().performClick()
        assertEquals(listOf("Nf3"), typed)
        composeRule.field().assertTextEquals("Your move", HELP, "")
    }

    @Test fun takesEnglishLettersAndAnyScriptsNumbersAndSaysSo() {
        show()
        open()
        composeRule.onNodeWithText(HELP, substring = true).assertExists()
        composeRule.field().performTextInput("Sf3 \u0436\u4E2D\u00E9-=+#.") // Cyrillic, Chinese and accented letters, and marks, are dropped
        composeRule.field().assertTextEquals("Your move", HELP, "Sf3 -=")
    }

    @Test fun theSameButtonClosesTheFieldAgain() {
        show()
        open()
        composeRule.onNodeWithText("Type a move").assertDoesNotExist()
        composeRule.onNodeWithText("Close move field").performClick()
        composeRule.onNodeWithText("Close move field").assertDoesNotExist()
        composeRule.onNodeWithText("Type a move").assertExists()
        composeRule.field().assertDoesNotExist()
    }

    /** Closing the field keeps what was typed for when it opens again. */
    @Test fun closingTheFieldKeepsTheTextForWhenItOpensAgain() {
        show()
        open()
        composeRule.field().performTextInput("Nf3")
        composeRule.onNodeWithText("Close move field").performClick()
        open()
        composeRule.field().assertTextEquals("Your move", HELP, "Nf3")
    }

    /**
     * A move half typed on a Galaxy A52s held upright is still there once the screen is made again on its side, as a
     * rotation does, though the field is then in a panel of its own, its caret still at the end, where the next key
     * goes; playing it still empties the field.
     */
    @Test @Config(qualifiers = "w853dp-h853dp")
    fun aHalfTypedMoveSurvivesARotation() {
        var size = DpSize(384.dp, 758.dp)
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent { Box(Modifier.size(size)) { Screen() } }
        open()
        assertTrue("upright: Hint under the board", composeRule.bounds("Hint").top >= composeRule.board().bottom)
        composeRule.field().performTextInput("Nf")
        size = DpSize(853.dp, 336.dp)
        restoration.emulateSavedInstanceStateRestore()
        assertTrue("on its side: Hint beside the board", composeRule.bounds("Hint").left >= composeRule.board().right)
        composeRule.field().performTextInput("3")
        composeRule.field().assertTextEquals("Your move", HELP, "Nf3")
        composeRule.onNodeWithText("Play").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf("Nf3"), typed)
        composeRule.field().assertTextEquals("Your move", HELP, "")
    }

    /** A letter the field drops, typed in the middle of the move, leaves the caret where it was: before the f. */
    @Test fun aDroppedLetterLeavesTheCaretWhereItWas() {
        show()
        open()
        composeRule.field().performTextInput("Nf3")
        composeRule.field().performSemanticsAction(SemanticsActions.SetSelection) { it(1, 1, false) }
        composeRule.field().performTextInput("\u0436") // Cyrillic
        composeRule.field().performTextInput("x")
        composeRule.field().assertTextEquals("Your move", HELP, "Nxf3")
    }

    /** What the keyboard commits together with its action key, in one turn and before a redraw, is what is played. */
    @Test fun theActionKeyPlaysTheTextJustCommittedWithIt() {
        show()
        open()
        composeRule.field().performTextInput("Nf")
        val field = composeRule.field().fetchSemanticsNode()
        composeRule.runOnUiThread {
            field.config[SemanticsActions.SetText].action?.invoke(AnnotatedString("Nf3"))
            field.config[SemanticsActions.OnImeAction].action?.invoke()
        }
        assertEquals(listOf("Nf3"), typed)
    }

    @Test fun theKeyboardsActionKeyPlaysToo() {
        show()
        open()
        composeRule.field().performTextInput("e2e4")
        composeRule.field().performImeAction()
        assertEquals(listOf("e2e4"), typed)
    }

    @Test fun theActionKeyWithNothingTypedPlaysNothing() {
        show()
        open()
        composeRule.field().performImeAction()
        composeRule.field().performTextInput("   ")
        composeRule.field().performImeAction()
        assertEquals(emptyList<String>(), typed)
    }

    @Test fun isOffOnceThePuzzleIsSolved() {
        show(PuzzleStatus.SOLVED)
        open()
        composeRule.onNodeWithText("Your move").assertIsNotEnabled() // a disabled field takes no text
        composeRule.onNodeWithText("Play").assertIsNotEnabled()
    }

    @Test @Config(qualifiers = "w800dp-h360dp-land")
    fun canBeReachedAndUsedOnAPhoneOnItsSide() {
        show()
        composeRule.onNodeWithText("Type a move").performScrollTo().assertIsDisplayed().performClick()
        composeRule.field().performScrollTo().assertIsDisplayed().performTextInput("e2e4")
        composeRule.onNodeWithText("Play").performScrollTo().performClick()
        assertEquals(listOf("e2e4"), typed)
    }
}
