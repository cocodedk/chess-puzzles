package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningUiState
import org.junit.Assert.assertEquals
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
}
