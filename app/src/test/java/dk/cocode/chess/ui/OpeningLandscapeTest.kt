package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import dk.cocode.chess.assertBoardSpansTheScreen
import dk.cocode.chess.core.data.openings.Openings
import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w800dp-h360dp-land")
class OpeningLandscapeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val opening = Opening(
        id = "land-test", name = "Land Test", eco = "T98", group = OpeningGroup.WHITE,
        idea = "Idea.", lines = listOf("e4 e5"),
    )

    @Test fun boardSitsBetweenThePanelsOnItsSide() {
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                OpeningScreenContent(
                    state = OpeningUiState(board = List(8) { "        " }, openingId = opening.id, moveSan = listOf("e4", "e5")),
                    opening = opening, onBack = {}, onMode = {}, onLine = {}, onStep = {},
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
                )
            }
        }
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertEquals(screen.height.value, board.height.value, 1f)
        assertEquals(board.height.value, board.width.value, 1f)
    }

    private fun showItalian(mode: OpeningMode) {
        val italian = Openings.all.first { it.id == "italian" }
        val tree = OpeningTree(italian)
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                OpeningScreenContent(
                    state = OpeningUiState(
                        board = tree.start.toRows(), openingId = italian.id, mode = mode,
                        lineNames = tree.lineNames, line = 0, ply = 0, moveSan = tree.plies(0).map { it.san },
                    ),
                    opening = italian, onBack = {}, onMode = {}, onLine = {}, onStep = {},
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
                    tabs = { Spacer(Modifier.height(48.dp)) }, // the Puzzles | Openings row
                )
            }
        }
    }

    /** A near-square screen: Learn's many rows would squeeze the board, so they scroll under a full-width one. */
    @Test @Config(qualifiers = "w700dp-h650dp")
    fun learnOnANearSquareScreenScrollsUnderAFullWidthBoard() {
        showItalian(OpeningMode.LEARN)
        composeRule.assertBoardSpansTheScreen()
    }

    /** The same screen in Practise, whose few rows leave room: the whole board in view, unscrolled. */
    @Test @Config(qualifiers = "w700dp-h650dp")
    fun practiseOnANearSquareScreenFitsTheWholeBoard() {
        showItalian(OpeningMode.PRACTISE)
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertTrue("board to ${board.bottom} of ${screen.bottom}", board.bottom <= screen.bottom)
        assertEquals(board.width.value, board.height.value, 1f)
    }
}
