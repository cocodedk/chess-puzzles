package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.assertWithinScreenWidth
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Level and Theme menus above the board: one row on a phone held upright, and what Level offers. */
@RunWith(RobolectricTestRunner::class)
class PuzzleMenusTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val medium = PuzzleUiState(rating = 1500)

    private fun show(
        state: PuzzleUiState = medium,
        difficulties: List<Difficulty> = Difficulty.entries,
        onDifficulty: (Difficulty) -> Unit = {},
    ) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = state, difficulties = difficulties, onDifficulty = onDifficulty, themes = PuzzleTheme.entries,
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {}, onPromotion = {}, onPromotionCancel = {},
            )
        }
    }

    private fun bounds(description: String) = composeRule.onNodeWithContentDescription(description).getUnclippedBoundsInRoot()

    /** 384dp wide at font 1.0: both chips one height, side by side. */
    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun levelAndThemeShareOneRow() {
        show()
        val level = bounds("Level, Medium")
        val theme = bounds("Theme, All")
        assertEquals(level.top.value, theme.top.value, 1f)
        assertEquals(level.bottom.value, theme.bottom.value, 1f)
        composeRule.assertWithinScreenWidth(level)
        composeRule.assertWithinScreenWidth(theme)
    }

    /** The longest theme name at a 1.3× font on a small phone: Theme wraps in the room Level leaves, whole. */
    @Test @Config(qualifiers = "w360dp-h640dp", fontScale = 1.3f)
    fun aLongThemeWrapsBesideLevelAtALargeFont() {
        show(medium.copy(theme = PuzzleTheme.DISCOVERED_ATTACK))
        val level = bounds("Level, Medium")
        val theme = bounds("Theme, Discovered attack")
        assertEquals((level.top + level.bottom).value / 2, (theme.top + theme.bottom).value / 2, 1f)
        composeRule.assertWithinScreenWidth(level)
        composeRule.assertWithinScreenWidth(theme)
        assertTrue("$level overlaps $theme", level.right <= theme.left)
        val label = composeRule.onNodeWithText("Theme: Discovered attack ▾", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("$label clipped by $theme", label.top >= theme.top && label.bottom <= theme.bottom)
    }

    @Test @Config(qualifiers = PHONE_QUALIFIERS)
    fun theLevelChipShowsAndSaysTheBandInUseAndIsAButton() {
        show()
        val level = composeRule.onNodeWithContentDescription("Level, Medium")
        level.assertTextEquals("Level: Medium ▾")
        assertEquals(Role.Button, level.fetchSemanticsNode().config[SemanticsProperties.Role])
    }

    @Test @Config(qualifiers = PHONE_QUALIFIERS)
    fun theLevelMenuListsOnlyTheBandsOnOfferMarksTheOneInUseAndPicksHard() {
        val picked = mutableListOf<Difficulty>()
        show(difficulties = listOf(Difficulty.MEDIUM, Difficulty.HARD), onDifficulty = picked::add)
        composeRule.onNodeWithText("Hard").assertDoesNotExist() // closed until tapped
        composeRule.onNodeWithContentDescription("Level, Medium").performClick()
        composeRule.onNodeWithText("Medium").assertIsSelected()
        composeRule.onNodeWithText("Hard").assertIsNotSelected()
        composeRule.onNodeWithText("Easy").assertDoesNotExist()
        composeRule.onNodeWithText("Hard").performClick()
        composeRule.waitForIdle()
        assertEquals(listOf(Difficulty.HARD), picked)
        composeRule.onNodeWithText("Hard").assertDoesNotExist() // the list closed
    }
}
