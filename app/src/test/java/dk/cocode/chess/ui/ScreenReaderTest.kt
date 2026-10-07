package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.width
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.ui.board.BOARD_SQUARES_TAG
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.board.BoardGeometry
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What a screen reader finds on the puzzle screen: the 64 squares, the verdict line, the heading and stats. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class ScreenReaderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(state: PuzzleUiState, onSquareTap: (Square) -> Unit = {}) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = state,
                onSquareTap = onSquareTap, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = {}, onPromotionCancel = {},
            )
        }
    }

    private val files = 'a'..'h'

    private fun assertSquares(names: List<String>, flipped: Boolean) {
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        val boardSize = board.width.value
        val frame = BoardGeometry.frameDepth(boardSize)
        val squareSize = BoardGeometry.squareSize(boardSize)
        val items = composeRule.onNodeWithTag(BOARD_SQUARES_TAG).onChildren()
        items.assertCountEquals(64)
        names.forEachIndexed { i, name ->
            items[i].assertContentDescriptionEquals("$name, empty")
            val bounds = items[i].getUnclippedBoundsInRoot()
            assertTrue("$name inside the board", bounds.left >= board.left && bounds.right <= board.right)
            assertTrue("$name inside the board", bounds.top >= board.top && bounds.bottom <= board.bottom)
            val centre = BoardGeometry.squareCenter(Square.of(name), squareSize, flipped)
            assertEquals("$name centre x", board.left.value + frame + centre.x, (bounds.left.value + bounds.right.value) / 2, 1f)
            assertEquals("$name centre y", board.top.value + frame + centre.y, (bounds.top.value + bounds.bottom.value) / 2, 1f)
        }
    }

    @Test fun squaresAreReadFromWhitesSideTopRowFirst() {
        show(PuzzleUiState())
        assertSquares(('8' downTo '1').flatMap { rank -> files.map { "$it$rank" } }, flipped = false)
    }

    @Test fun squaresAreReadFromBlacksSideTopRowFirst() {
        show(PuzzleUiState(flipped = true))
        val names = ('1'..'8').flatMap { rank -> files.reversed().map { "$it$rank" } }
        assertEquals(listOf("h1", "g1"), names.take(2))
        assertEquals("a8", names.last())
        assertSquares(names, flipped = true)
    }

    @Test fun accessibilityClickOnASquareTapsIt() {
        val tapped = mutableListOf<Square>()
        show(PuzzleUiState(board = listOf("RNBQKBNR", "PPPPPPPP") + List(6) { "        " }), tapped::add)
        composeRule.onNodeWithContentDescription("e2, white pawn").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf(Square.of("e2")), tapped)
    }

    @Test fun verdictLineIsAPoliteLiveRegionExposingTheAnnouncement() {
        show(PuzzleUiState(feedback = Feedback.CORRECT, announcement = "Correct. You played knight f3."))
        val verdict = composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        verdict.assertContentDescriptionEquals("Correct. You played knight f3.")
        verdict.assertTextEquals("Correct — keep going") // the visible text is unchanged
    }

    @Test fun verdictLineReadsAsItsTextWithoutAnAnnouncement() {
        show(PuzzleUiState(feedback = Feedback.CORRECT))
        val verdict = composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        verdict.assertTextEquals("Correct — keep going")
        verdict.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test fun titleIsAHeading() {
        show(PuzzleUiState(position = 4, bandSize = 812))
        composeRule.onNodeWithText("Puzzle 4 of 812").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test fun eachStatIsOneItemWithItsLabelAndNumber() {
        show(PuzzleUiState(dayStreak = 4, solvedCount = 1441, hintFreeCount = 1101, currentStreak = 53, bestStreak = 120))
        listOf(
            "Day streak, 4", "Solved, 1441", "Solved without a hint, 1101", "Current streak, 53", "Best streak, 120",
        ).forEach { composeRule.onAllNodesWithContentDescription(it).assertCountEquals(1) }
    }

    @Test fun theThemeButtonOffersTheThemeATapSwitchesTo() {
        show(PuzzleUiState())
        val label = composeRule.onNodeWithText("Theme: Match phone").fetchSemanticsNode()
            .config[SemanticsActions.OnClick].label
        assertEquals("Switch to the light theme", label)
        composeRule.onNodeWithText("Theme: Match phone").assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
    }

    @Test fun previousAndNextButtonsExposeNoArrows() {
        show(PuzzleUiState())
        listOf("Previous puzzle", "Next puzzle").forEach {
            composeRule.onNodeWithContentDescription(it).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Text))
        }
        listOf("◀", "▶").forEach { composeRule.onAllNodesWithText(it, useUnmergedTree = true).assertCountEquals(0) }
    }
}
