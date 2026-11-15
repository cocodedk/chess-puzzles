package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.centerOf
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.testPuzzleRepository
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleViewModel
import dk.cocode.chess.viewmodel.ResourceTexts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val ILLEGAL = "Not a legal move here"

/**
 * After typed text that is no move, the verdict line reads "Not a legal move here". Hint or a tap on a piece then says
 * something else aloud, and the line must not go on saying that to the eye. The real screen on the real view model.
 */
@RunWith(RobolectricTestRunner::class)
class HintVerdictTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showAfterIllegalText() {
        val texts = ResourceTexts(RuntimeEnvironment.getApplication().resources)
        val viewModel = PuzzleViewModel(testPuzzleRepository(), FakeProgressRepository(), texts) { 100 }
        composeRule.setContent {
            ChessTheme(darkTheme = false) { PuzzleScreen(viewModel, themeMode = ThemeMode.SYSTEM, onThemeToggle = {}) }
        }
        composeRule.onNodeWithText("Type a move").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.field().performTextInput("e2e4")
        composeRule.onNodeWithText("Play").performClick()
        composeRule.onNodeWithText(ILLEGAL).assertExists() // the verdict on the typed text, to the eye
    }

    private fun spoken() = composeRule.verdict().fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString(" ")

    private fun seen() = composeRule.verdict().fetchSemanticsNode().config[SemanticsProperties.Text].joinToString("") { it.text }

    private fun assertLineAgreesWith(said: String) {
        assertTrue("said \"${spoken()}\"", spoken().startsWith(said))
        assertEquals("", seen()) // as after a hint on a puzzle with no typed text before it
    }

    private fun hintLeavesNoStaleVerdict() {
        showAfterIllegalText()
        composeRule.onNodeWithText("Hint").performClick()
        assertLineAgreesWith("Hint: queen b7 to g7.")
    }

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun hintOnA384dpPhoneLeavesNoStaleVerdict() = hintLeavesNoStaleVerdict()

    @Test @Config(qualifiers = "w412dp-h915dp", fontScale = 1.3f)
    fun hintOnA412dpPhoneAtALargeFontLeavesNoStaleVerdict() = hintLeavesNoStaleVerdict()

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun aTapOnAPieceLeavesNoStaleVerdict() {
        showAfterIllegalText()
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { click(composeRule.centerOf(Square.of("b7"))) }
        assertLineAgreesWith("Queen b7 selected. Moves: ")
    }
}
