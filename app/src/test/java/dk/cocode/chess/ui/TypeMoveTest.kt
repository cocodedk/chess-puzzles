package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
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

    private fun show(status: PuzzleStatus = PuzzleStatus.IN_PROGRESS) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = PuzzleUiState(status = status),
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = {}, onPromotionCancel = {},
                onMoveTyped = { typed += it },
            )
        }
    }

    private fun open() = composeRule.onNodeWithText("Type a move").performClick()

    private fun field() = composeRule.onNode(hasSetTextAction())

    @Test fun staysShutUntilAskedFor() {
        show()
        composeRule.onNodeWithText("Play").assertDoesNotExist()
        open()
        field().assertIsEnabled()
    }

    @Test fun playHandsTheTextOverAndEmptiesTheField() {
        show()
        open()
        composeRule.onNodeWithText("Play").assertIsNotEnabled() // nothing typed yet
        field().performTextInput("Nf3")
        composeRule.onNodeWithText("Play").assertIsEnabled().performClick()
        assertEquals(listOf("Nf3"), typed)
        field().assertTextEquals("Your move, like e2e4 or Nf3", "")
    }

    @Test fun theKeyboardsActionKeyPlaysToo() {
        show()
        open()
        field().performTextInput("e2e4")
        field().performImeAction()
        assertEquals(listOf("e2e4"), typed)
    }

    @Test fun theActionKeyWithNothingTypedPlaysNothing() {
        show()
        open()
        field().performImeAction()
        field().performTextInput("   ")
        field().performImeAction()
        assertEquals(emptyList<String>(), typed)
    }

    @Test fun isOffOnceThePuzzleIsSolved() {
        show(PuzzleStatus.SOLVED)
        open()
        composeRule.onNodeWithText("Your move, like e2e4 or Nf3").assertIsNotEnabled() // a disabled field takes no text
        composeRule.onNodeWithText("Play").assertIsNotEnabled()
    }

    @Test @Config(qualifiers = "w800dp-h360dp-land")
    fun canBeReachedAndUsedOnAPhoneOnItsSide() {
        show()
        composeRule.onNodeWithText("Type a move").performScrollTo().assertIsDisplayed().performClick()
        field().performScrollTo().assertIsDisplayed().performTextInput("e2e4")
        composeRule.onNodeWithText("Play").performScrollTo().performClick()
        assertEquals(listOf("e2e4"), typed)
    }
}
