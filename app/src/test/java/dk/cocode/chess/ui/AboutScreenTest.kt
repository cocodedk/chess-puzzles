package dk.cocode.chess.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dk.cocode.chess.renderToBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AboutScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(onBack: () -> Unit = {}) = composeRule.setContent { AboutScreen(onBack = onBack) }

    private fun opened(label: String): Intent? {
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        return shadowOf(composeRule.activity).nextStartedActivity
    }

    @Test fun showsTheNameVersionAndWhatTheAppDoes() {
        show()
        composeRule.onNodeWithText("Chess Puzzles").assertExists()
        composeRule.onNodeWithText("Version ", substring = true).assertExists()
        composeRule.onNodeWithText("Chess Puzzles helps you practise chess tactics.", substring = true).assertExists()
        composeRule.renderToBitmap() // draws the branded mark and the whole page headlessly without crashing
    }

    @Test fun theFourPrivacyPromisesAreShown() {
        show()
        listOf(
            "Chess Puzzles does not collect, send or share any personal data.",
            "It never asks you for a permission, and it cannot use the internet.",
            "No account, no ads, no analytics and no crash reporting.",
            "Your progress is saved on your phone. The app itself never sends it anywhere.",
        ).forEach { composeRule.onNodeWithText(it).assertExists() }
    }

    @Test fun sectionTitlesAreHeadingsInTheStandardsOrder() {
        show()
        val headings = composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .fetchSemanticsNodes()
            .map { node -> node.config.getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text } }
        assertEquals(
            listOf("Chess Puzzles", "What the app does", "Privacy", "Links", "Credits and licenses", "Made by Cocode"),
            headings,
        )
    }

    @Test fun theSupportSlotShowsNothing() {
        show()
        composeRule.onAllNodes(SemanticsMatcher("a Support title") { node ->
            node.config.getOrNull(SemanticsProperties.Text)?.any { it.text.contains("Support", ignoreCase = true) } == true
        }).assertCountEquals(0)
    }

    @Test fun everyButtonOpensItsPage() {
        show()
        mapOf(
            "See the latest version" to "https://f-droid.org/packages/dk.cocode.chess/",
            "Read the privacy policy" to "https://chess.cocode.dk/privacy/",
            "Open the website" to "https://chess.cocode.dk/",
            "See the source code on GitHub" to "https://github.com/cocodedk/chess-puzzles",
            "Report a problem on GitHub" to "https://github.com/cocodedk/chess-puzzles/issues",
            "Made by Cocode (cocode.dk)" to "https://cocode.dk",
            "Babak Bandpey on LinkedIn" to "https://linkedin.com/in/babakbandpey",
        ).forEach { (label, url) ->
            val launched = opened(label)
            assertEquals(label, Intent.ACTION_VIEW, launched?.action)
            assertEquals(label, url, launched?.dataString)
        }
    }

    @Test fun backReturnsToTheScreenBefore() {
        var backs = 0
        show { backs++ }
        composeRule.onNodeWithText("Back").performScrollTo().performClick()
        assertTrue(backs == 1)
    }
}
