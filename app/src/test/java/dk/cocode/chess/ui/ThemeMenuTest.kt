package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import dk.cocode.chess.FakeProgressRepository
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.themedPuzzleRepository
import dk.cocode.chess.ui.board.BOARD_TEST_TAG
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
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

/** The theme button under the bands: what a screen reader says of it, its size and place, and its list. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class ThemeMenuTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val themes = listOf(PuzzleTheme.FORK, PuzzleTheme.DISCOVERED_ATTACK, PuzzleTheme.ENDGAME)

    private fun show(state: PuzzleUiState = PuzzleUiState(), shown: List<PuzzleTheme> = themes, onTheme: (PuzzleTheme?) -> Unit = {}) =
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                PuzzleScreenContent(
                    state = state, themes = shown, onTheme = onTheme,
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                    onHint = {}, onReset = {}, onNext = {},
                    onPromotion = {}, onPromotionCancel = {},
                )
            }
        }

    @Test fun theButtonReadsTheThemeInUseAndIsAButtonThatShowsIt() {
        show()
        val button = composeRule.onNodeWithContentDescription("Theme, All")
        button.assertTextEquals("Theme: All ▾")
        button.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
        composeRule.onAllNodesWithContentDescription("Theme, Fork").assertCountEquals(0)
    }

    @Test fun aChosenThemeIsReadByItsName() {
        show(PuzzleUiState(theme = PuzzleTheme.DISCOVERED_ATTACK))
        composeRule.onNodeWithContentDescription("Theme, Discovered attack").assertTextEquals("Theme: Discovered attack ▾")
    }

    /** The drawn button is slimmer (like Hint and Restart); what a finger can hit is Compose's 48dp minimum. */
    private fun touchTarget(description: String) = with(composeRule.density) {
        composeRule.onNodeWithContentDescription(description).fetchSemanticsNode().touchBoundsInRoot.let {
            it.width.toDp() to it.height.toDp()
        }
    }

    @Test fun theTouchTargetIsAtLeast48dp() {
        show()
        val (width, height) = touchTarget("Theme, All")
        assertTrue("$width x $height", width >= 48.dp && height >= 48.dp)
    }

    @Test fun withNoThemesThereIsNoButton() {
        show(shown = emptyList())
        composeRule.onAllNodesWithContentDescription("Theme, All").assertCountEquals(0)
    }

    @Test fun aTapListsAllAndEachThemeMarkingTheOneInUse() {
        show(PuzzleUiState(theme = PuzzleTheme.FORK))
        composeRule.onNodeWithText("Endgame").assertDoesNotExist() // closed until tapped
        composeRule.onNodeWithContentDescription("Theme, Fork").performClick()
        composeRule.onNodeWithText("Fork").assertIsSelected()
        listOf("All", "Discovered attack", "Endgame").forEach { composeRule.onNodeWithText(it).assertIsNotSelected() }
    }

    @Test fun choosingAThemeSaysWhichAndClosesTheList() {
        val chosen = mutableListOf<PuzzleTheme?>()
        show(onTheme = chosen::add)
        composeRule.onNodeWithContentDescription("Theme, All").performClick()
        composeRule.onNodeWithText("Discovered attack").performClick()
        composeRule.waitForIdle()
        assertEquals(listOf<PuzzleTheme?>(PuzzleTheme.DISCOVERED_ATTACK), chosen)
        composeRule.onNodeWithText("Endgame").assertDoesNotExist()
    }

    @Test fun choosingAllSaysNoTheme() {
        val chosen = mutableListOf<PuzzleTheme?>(PuzzleTheme.ENDGAME)
        show(PuzzleUiState(theme = PuzzleTheme.ENDGAME), onTheme = chosen::add)
        composeRule.onNodeWithContentDescription("Theme, Endgame").performClick()
        composeRule.onNodeWithText("All").performClick()
        assertEquals(listOf(PuzzleTheme.ENDGAME, null), chosen)
    }

    @Test fun thePuzzleScreenPassesTheChoiceToTheViewModelAndTheLiveRegionAnnouncesIt() {
        val texts = ResourceTexts(RuntimeEnvironment.getApplication().resources)
        val viewModel = PuzzleViewModel(themedPuzzleRepository(), FakeProgressRepository(), texts) { 100 }
        composeRule.setContent {
            ChessTheme(darkTheme = false) { PuzzleScreen(viewModel, themeMode = ThemeMode.SYSTEM, onThemeToggle = {}) }
        }
        composeRule.onNodeWithText("Puzzle 1 of 4").assertExists()
        composeRule.onNodeWithContentDescription("Theme, All").performClick()
        composeRule.onNodeWithText("Fork").performClick()
        composeRule.onNodeWithText("Puzzle 1 of 3").assertExists()
        composeRule.onNodeWithContentDescription("Theme, Fork").assertExists()
        val said = composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        assertTrue(said.fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString(" ").startsWith("Theme: Fork. 3 puzzles. "))
    }

    /** The longest name at a 1.3× font on a phone upright: the button is whole, on screen and above the board. */
    @Test @Config(qualifiers = "w360dp-h800dp", fontScale = 1.3f)
    fun aLargeFontKeepsTheButtonOnScreenAboveTheBoard() {
        show(PuzzleUiState(theme = PuzzleTheme.DISCOVERED_ATTACK))
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot()
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        val button = composeRule.onNodeWithContentDescription("Theme, Discovered attack").getUnclippedBoundsInRoot()
        assertTrue(button.left >= screen.left && button.right <= screen.right)
        assertTrue(button.bottom <= board.top)
        assertTrue(touchTarget("Theme, Discovered attack").second >= 48.dp)
    }

    /** A phone on its side at 1.3×: the button sits in the panel beside the board, wrapping rather than overflowing. */
    @Test @Config(qualifiers = "w720dp-h356dp-land", fontScale = 1.3f)
    fun aLargeFontKeepsTheButtonInTheSidePanelBesideTheBoard() {
        show(PuzzleUiState(theme = PuzzleTheme.DISCOVERED_ATTACK))
        val board = composeRule.onNodeWithTag(BOARD_TEST_TAG).getUnclippedBoundsInRoot()
        val button = composeRule.onNodeWithContentDescription("Theme, Discovered attack").getUnclippedBoundsInRoot()
        assertTrue("left of the board", button.right <= board.left)
        assertTrue("whole", button.left >= 0.dp)
        assertTrue(touchTarget("Theme, Discovered attack").second >= 48.dp)
    }
}
