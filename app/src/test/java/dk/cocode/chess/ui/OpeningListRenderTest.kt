package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.theme.ChessTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class OpeningListRenderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // One per group, so all three headings fit on screen without scrolling the LazyColumn.
    private val openings = listOf(
        Opening("w", "White One", "T00", OpeningGroup.WHITE, "Idea.", listOf("e4")),
        Opening("e4", "E4 One", "T01", OpeningGroup.BLACK_VS_E4, "Idea.", listOf("e4 e5")),
        Opening("d4", "D4 One", "T02", OpeningGroup.BLACK_VS_D4, "Idea.", listOf("d4 d5")),
    )

    @Test fun listsEveryGroupAndOpensOnTap() {
        var opened: String? = null
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                OpeningListScreen(
                    openings = openings, cleanRuns = mapOf("w" to 3),
                    onOpen = { opened = it }, themeMode = ThemeMode.SYSTEM,
                )
            }
        }
        composeRule.onNodeWithText("As White").assertExists()
        composeRule.onNodeWithText("As Black against 1.e4").assertExists()
        composeRule.onNodeWithText("As Black against 1.d4").assertExists()
        // A row is one item for a screen reader, so its texts are only in the unmerged tree.
        composeRule.onNodeWithText("3 runs without mistakes", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("White One", useUnmergedTree = true).performClick()
        assertEquals("w", opened)
    }
}
