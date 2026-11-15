package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.core.data.openings.Openings
import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The Puzzles | Openings row on a phone held upright: it sat 8dp higher on the openings list than on the puzzle screen,
 * so it jumped as the player switched tab. It must keep its height on every screen of both tabs.
 */
@RunWith(RobolectricTestRunner::class)
class ModeTabsPositionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val italian = Openings.all.first { it.id == "italian" }
    private val tree = OpeningTree(italian)

    private fun opening(mode: OpeningMode) = OpeningUiState(
        board = tree.start.toRows(), openingId = italian.id, mode = mode,
        lineNames = tree.lineNames, line = 0, ply = 0, moveSan = tree.plies(0).map { it.san },
    )

    @Composable
    private fun Screen(index: Int) {
        val puzzles: @Composable () -> Unit = { ModeTabs(AppTab.PUZZLES) {} }
        val openings: @Composable () -> Unit = { ModeTabs(AppTab.OPENINGS) {} }
        when (index) {
            0 -> PuzzleScreenContent(
                PuzzleUiState(), onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onHint = {}, onReset = {},
                onNext = {}, onPromotion = {}, onPromotionCancel = {}, tabs = puzzles,
            )
            1 -> OpeningListScreen(Openings.all, emptyMap(), onOpen = {}, tabs = openings)
            else -> OpeningScreenContent(
                opening(if (index == 2) OpeningMode.LEARN else OpeningMode.PRACTISE), italian, onBack = {},
                onMode = {}, onLine = {}, onStep = {}, onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onAgain = {}, tabs = openings,
            )
        }
    }

    /** The top of the Puzzles tab, in dp, on each of the puzzle screen, the openings list, and Learn and Practise. */
    private fun tabTops(): List<Float> {
        var shown by mutableStateOf(0)
        composeRule.setContent { ChessTheme(darkTheme = false) { Screen(shown) } }
        return (0..3).map {
            shown = it
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Puzzles").getUnclippedBoundsInRoot().top.value
        }
    }

    private fun assertOneHeight() {
        val tops = tabTops()
        assertEquals("tab row top on the puzzle screen, the list, Learn, Practise: $tops", 0f, tops.max() - tops.min(), 0.5f)
    }

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun tabRowKeepsItsHeightOnAnA52s() = assertOneHeight()

    @Test @Config(qualifiers = "w412dp-h915dp", fontScale = 1.3f)
    fun tabRowKeepsItsHeightOnATallPhoneAtALargeFont() = assertOneHeight()
}
