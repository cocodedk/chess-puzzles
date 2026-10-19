package dk.cocode.chess.util

import android.app.Activity
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AndroidPositionSharerTest {
    private val fen = "7k/1Q6/6K1/8/8/8/8/8 w - - 1 2"

    private fun assertShareSheetCarries(chooser: Intent) {
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals("Share position", chooser.getStringExtra(Intent.EXTRA_TITLE))
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals(fen, send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test fun anActivityOpensTheShareSheetWithThePositionAsPlainText() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        AndroidPositionSharer(activity).share(fen)
        val chooser = shadowOf(activity).nextStartedActivity
        assertShareSheetCarries(chooser)
        assertFalse(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test fun anythingElseStartsTheShareSheetInATaskOfItsOwn() {
        val app = RuntimeEnvironment.getApplication()
        AndroidPositionSharer(app).share(fen)
        val chooser = shadowOf(app).nextStartedActivity
        assertShareSheetCarries(chooser)
        assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }
}
