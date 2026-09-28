package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.data.openings.Openings
import dk.cocode.chess.core.engine.OpeningTree
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

/**
 * Learn on a phone held upright at a 1.3× font, like the owner's Galaxy A52s: the rows around the
 * board used to squeeze it to about two thirds of the width. It must span the screen, with the
 * ◀ ▶ buttons still in view without scrolling.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h780dp", fontScale = 1.3f)
class OpeningLearnSizeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun learnBoardSpansThePhoneWithItsStepButtonsInView() {
        val italian = Openings.all.first { it.id == "italian" }
        val tree = OpeningTree(italian)
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                OpeningScreenContent(
                    state = OpeningUiState(
                        board = tree.start.toRows(), openingId = italian.id, mode = OpeningMode.LEARN,
                        lineNames = tree.lineNames, line = 0, ply = 0, moveSan = tree.plies(0).map { it.san },
                    ),
                    opening = italian, onBack = {}, onMode = {}, onLine = {}, onStep = {},
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
                    tabs = { Spacer(Modifier.height(48.dp)) }, // the Puzzles | Openings row
                )
            }
        }
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        assertEquals(screen.right.value - screen.left.value, board.right.value - board.left.value, 1f)
        val next = composeRule.onNodeWithContentDescription("Next move").getUnclippedBoundsInRoot()
        assertTrue("Next move at ${next.bottom} of ${screen.bottom}", next.bottom <= screen.bottom)
    }
}
