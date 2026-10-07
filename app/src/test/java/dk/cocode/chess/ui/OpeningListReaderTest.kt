package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What a screen reader finds on the openings list and in the header of an open opening. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class OpeningListReaderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val opening = Opening("italian", "Italian Game", "C50", OpeningGroup.WHITE, "Aim at f7.", listOf("e4 e5 Nf3"))
    private val blank = List(8) { "        " }

    private fun showList(cleanRuns: Map<String, Int>, onOpen: (String) -> Unit = {}) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            OpeningListScreen(
                openings = listOf(
                    opening,
                    Opening("e4", "E4 One", "T01", OpeningGroup.BLACK_VS_E4, "Idea.", listOf("e4 e5")),
                    Opening("d4", "D4 One", "T02", OpeningGroup.BLACK_VS_D4, "Idea.", listOf("d4 d5")),
                ),
                cleanRuns = cleanRuns, onOpen = onOpen,
            )
        }
    }

    private fun showHeader(cleanRuns: Map<String, Int> = emptyMap()) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            OpeningScreenContent(
                state = OpeningUiState(board = blank, openingId = opening.id, cleanRuns = cleanRuns),
                opening = opening, onBack = {}, onMode = {}, onLine = {}, onStep = {},
                onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
            )
        }
    }

    @Test fun groupTitlesAreHeadings() {
        showList(emptyMap())
        listOf("As White", "As Black against 1.e4", "As Black against 1.d4").forEach {
            composeRule.onNodeWithText(it).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        }
    }

    @Test fun eachRowIsOneItemWithItsCodeAndCleanRuns() {
        showList(mapOf("italian" to 3, "e4" to 1))
        composeRule.onNodeWithContentDescription("Italian Game, Opening code C50, 3 runs without mistakes").assertExists()
        composeRule.onNodeWithContentDescription("E4 One, Opening code T01, 1 run without mistakes").assertExists()
        composeRule.onNodeWithContentDescription("D4 One, Opening code T02").assertExists()
        listOf("3 runs without mistakes", "1 run without mistakes", "Italian Game", "Opening code C50").forEach { composeRule.onAllNodesWithText(it).assertCountEquals(0) }
    }

    @Test fun rowsReadWithoutACountWhenThereAreNoCounts() {
        showList(emptyMap())
        composeRule.onNodeWithContentDescription("Italian Game, Opening code C50").assertExists()
        composeRule.onAllNodesWithText("3 runs without mistakes", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test fun clickingARowOpensItByTouchAndByAccessibilityAction() {
        val opened = mutableListOf<String>()
        showList(emptyMap(), opened::add)
        composeRule.onNodeWithContentDescription("Italian Game, Opening code C50").performClick()
        composeRule.onNodeWithContentDescription("D4 One, Opening code T02").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf("italian", "d4"), opened)
    }

    @Test fun backExposesNoArrowAndTheNameIsAHeading() {
        showHeader()
        composeRule.onNodeWithContentDescription("Back").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Text))
        composeRule.onAllNodesWithText("←", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onNodeWithText("Italian Game").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test fun theCleanRunLineSaysHowManyCleanRuns() {
        showHeader(mapOf("italian" to 3))
        composeRule.onNodeWithText("Opening code C50, 3 runs without mistakes").assertExists()
    }

    @Test fun oneCleanRunIsSingular() {
        showHeader(mapOf("italian" to 1))
        composeRule.onNodeWithText("Opening code C50, 1 run without mistakes").assertExists()
    }

    @Test fun theCodeReadsAloneWithoutCleanRuns() {
        showHeader()
        composeRule.onNodeWithText("Opening code C50").assertTextEquals("Opening code C50")
    }
}
