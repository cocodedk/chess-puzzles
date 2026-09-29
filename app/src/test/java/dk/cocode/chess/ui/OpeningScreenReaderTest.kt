package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import dk.cocode.chess.FakeOpeningProgressRepository
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.board.BOARD_SQUARES_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningFeedback
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState
import dk.cocode.chess.viewmodel.OpeningViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What a screen reader finds in Learn and in Practise: the chips, the live regions and the 64 squares. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class OpeningScreenReaderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val opening = Opening(
        id = "italian", name = "Italian Game", eco = "C50", group = OpeningGroup.WHITE,
        idea = "Aim at f7.", lines = listOf("e4 e5 Nf3 Nc6 Bc4 Bc5", "e4 e5 Nf3 Nc6 Bc4 Nf6 d3 Be7"),
    )
    private val blank = List(8) { "        " }
    private val polite = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

    private fun show(state: OpeningUiState) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            OpeningScreenContent(
                state = state, opening = opening, onBack = {}, onMode = {}, onLine = {}, onStep = {},
                onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
            )
        }
    }

    private fun learn(ply: Int = 0, moves: List<String> = listOf("e4", "e5", "Nf3"), said: String = "") = show(
        OpeningUiState(
            board = blank, openingId = opening.id, lineNames = listOf("3…Bc5", "4.d3"), ply = ply, moveSan = moves,
            announcement = said,
        ),
    )

    private fun practise(feedback: OpeningFeedback = OpeningFeedback.CORRECT, said: String = "") = show(
        OpeningUiState(
            board = blank, openingId = opening.id, mode = OpeningMode.PRACTISE, feedback = feedback, announcement = said,
        ),
    )

    @Test fun lineChipsAreReadAsTheMoveTheyNameAndKeepTheirSelectedState() {
        learn()
        composeRule.onNodeWithContentDescription("Move 3, Black: bishop c5").assertIsSelected()
        composeRule.onNodeWithContentDescription("Move 4, White: pawn d3").assertIsNotSelected()
        composeRule.onNodeWithText("3…Bc5").assertExists() // what a sighted player sees is unchanged
    }

    @Test fun stepButtonsExposeNoArrows() {
        learn()
        listOf("Previous move", "Next move").forEach {
            composeRule.onNodeWithContentDescription(it).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Text))
        }
        listOf("◀", "▶").forEach { composeRule.onAllNodesWithText(it, useUnmergedTree = true).assertCountEquals(0) }
    }

    @Test fun theMoveListIsAPoliteLiveRegionExposingTheAnnouncement() {
        learn(ply = 3, said = "Move 2, White: knight f3.")
        val moves = composeRule.onNode(polite)
        moves.assertContentDescriptionEquals("Move 2, White: knight f3.")
        moves.assertTextEquals("1.e4 e5 2.Nf3") // the visible text is unchanged
    }

    @Test fun theMoveListReadsInWordsWithoutAnAnnouncement() {
        learn(moves = listOf("e4", "e5", "Nf3", "Nc6", "Bc4"))
        composeRule.onNode(polite)
            .assertContentDescriptionEquals("1. pawn e4, pawn e5. 2. knight f3, knight c6. 3. bishop c4.")
    }

    @Test fun anEmptyMoveListIsNotRead() {
        learn(moves = emptyList())
        composeRule.onNode(polite).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test fun theIdeaIsReadAsWritten() {
        learn()
        composeRule.onNodeWithText("Aim at f7.").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test fun theVerdictLineIsAPoliteLiveRegionExposingTheAnnouncement() {
        practise(said = "Book move. You played pawn e4. Black played pawn e5.")
        val verdict = composeRule.onNode(polite)
        verdict.assertContentDescriptionEquals("Book move. You played pawn e4. Black played pawn e5.")
        verdict.assertTextEquals("Book move ✓")
    }

    @Test fun theVerdictLineReadsAsItsTextWithoutAnAnnouncement() {
        practise()
        val verdict = composeRule.onNode(polite)
        verdict.assertTextEquals("Book move ✓")
        verdict.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    private fun assertSquaresClickable(clickable: Boolean) {
        val squares = composeRule.onNodeWithTag(BOARD_SQUARES_TAG).onChildren()
        squares.assertCountEquals(64)
        val hasClick = SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)
        val hasNoClick = SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick)
        listOf(0, 27, 63).forEach { squares[it].assert(if (clickable) hasClick else hasNoClick) }
    }

    @Test fun inLearnTheSquaresAreReadButOfferNoClick() {
        learn()
        assertSquaresClickable(false)
        composeRule.onNodeWithTag(BOARD_SQUARES_TAG).onChildren()[0].assertContentDescriptionEquals("a8, empty")
    }

    @Test fun inPractiseTheSquaresKeepTheirClick() {
        practise(OpeningFeedback.NONE)
        assertSquaresClickable(true)
    }

    @Test fun leavingTheOpeningsScreenForgetsTheLastAnnouncement() {
        val viewModel = OpeningViewModel(listOf(opening), FakeOpeningProgressRepository())
        var visible by mutableStateOf(true)
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                if (visible) OpeningScreen(viewModel, listOf(opening), ThemeMode.SYSTEM, onThemeToggle = {}, tabs = {})
            }
        }
        composeRule.runOnIdle { viewModel.onOpen(opening.id) }
        composeRule.runOnIdle { assertEquals("Italian Game. Learn. Start position.", viewModel.state.value.announcement) }
        composeRule.runOnIdle { visible = false }
        composeRule.runOnIdle { assertEquals("", viewModel.state.value.announcement) }
    }

    @Test fun aCompletedLineTakesNoClickUntilAgain() {
        practise(OpeningFeedback.COMPLETE)
        assertSquaresClickable(false)
    }
}
