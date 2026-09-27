package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.renderToBitmap
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningFeedback
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class OpeningScreenRenderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val opening = Opening(
        id = "render-test", name = "Render Test", eco = "T99", group = OpeningGroup.WHITE,
        idea = "Test the render.", lines = listOf("e4 e5 Nf3 Nc6", "e4 c5 Nf3 d6"),
    )
    private val blank = List(8) { "        " }

    private fun show(state: OpeningUiState, onBack: () -> Unit = {}) {
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                OpeningScreenContent(
                    state = state, opening = opening, onBack = onBack, onMode = {}, onLine = {}, onStep = {},
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
                )
            }
        }
    }

    @Test fun learnShowsTheIdeaBranchesAndMoveList() {
        show(
            OpeningUiState(
                board = blank, openingId = opening.id, mode = OpeningMode.LEARN,
                lineNames = listOf("Main line", "3…d6"), line = 0, ply = 1,
                moveSan = listOf("e4", "e5", "Nf3", "Nc6"),
            ),
        )
        composeRule.renderToBitmap()
        composeRule.onNodeWithText(opening.idea).assertExists()
        composeRule.onNodeWithText("Main line").assertExists()
        composeRule.onNodeWithContentDescription("Previous move").assertExists()
        composeRule.onNodeWithContentDescription("Next move").assertExists()
    }

    @Test fun practiseShowsThePromptFeedbackAndAgain() {
        show(
            OpeningUiState(
                board = blank, openingId = opening.id, mode = OpeningMode.PRACTISE,
                feedback = OpeningFeedback.CORRECT, clean = true,
            ),
        )
        composeRule.renderToBitmap()
        composeRule.onNodeWithText("Play White's moves from memory.").assertExists()
        composeRule.onNodeWithText("Again").assertExists()
    }

    @Test fun backButtonFires() {
        var back = false
        show(OpeningUiState(board = blank, openingId = opening.id), onBack = { back = true })
        composeRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(back)
    }
}
