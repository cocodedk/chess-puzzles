package dk.cocode.chess.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dk.cocode.chess.renderToBitmap
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AboutScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersAndDownloadOpensReleaseUrl() {
        composeRule.setContent { AboutScreen(onBack = {}) }
        composeRule.onNodeWithText("Chess Puzzles").assertExists()
        composeRule.onNodeWithText("Source on GitHub").assertExists()
        composeRule.renderToBitmap() // draws the branded mark + links headlessly without crashing

        composeRule.onNodeWithText("Download the latest APK").performScrollTo().performClick()
        val launched: Intent? = shadowOf(composeRule.activity).peekNextStartedActivity()
        assertEquals(Intent.ACTION_VIEW, launched?.action)
        assertEquals(
            "https://github.com/cocodedk/chess-puzzles/releases/latest/download/ChessPuzzles.apk",
            launched?.dataString,
        )
    }
}
