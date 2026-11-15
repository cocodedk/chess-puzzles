package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dk.cocode.chess.R
import dk.cocode.chess.textLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Hint and Restart show their whole label, on one line and given all the room it takes, in each language, on three
 * phones on their side: two hold the buttons two by two in a panel, the 640dp wide one in a row.
 */
@RunWith(RobolectricTestRunner::class)
class ButtonLabelsFitTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun assertEveryLabelIsWhole() {
        composeRule.showThePuzzleScreen({ AFTER_A_WRONG_MOVE })
        listOf(R.string.puzzle_hint, R.string.puzzle_reset).map { appString(it) }.forEach { label ->
            val layout = composeRule.onNodeWithText(label, useUnmergedTree = true).textLayout()
            assertEquals("$label on one line", 1, layout.lineCount)
            val room = layout.multiParagraph.width
            val needed = layout.multiParagraph.maxIntrinsicWidth
            assertTrue("$label needs $needed of $room", needed <= room)
        }
    }

    @Test @Config(qualifiers = SAMSUNG_ON_ITS_SIDE)
    fun englishOnASamsung() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = LARGER_PHONE_ON_ITS_SIDE, fontScale = 1.3f)
    fun englishOnALargerPhoneAtALargerFont() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = SIXTEEN_NINE_ON_ITS_SIDE)
    fun englishOnASixteenByNinePhone() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "da-$SAMSUNG_ON_ITS_SIDE")
    fun danishOnASamsung() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "da-$LARGER_PHONE_ON_ITS_SIDE", fontScale = 1.3f)
    fun danishOnALargerPhoneAtALargerFont() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "da-$SIXTEEN_NINE_ON_ITS_SIDE")
    fun danishOnASixteenByNinePhone() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "es-$SAMSUNG_ON_ITS_SIDE")
    fun spanishOnASamsung() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "es-$LARGER_PHONE_ON_ITS_SIDE", fontScale = 1.3f)
    fun spanishOnALargerPhoneAtALargerFont() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "es-$SIXTEEN_NINE_ON_ITS_SIDE")
    fun spanishOnASixteenByNinePhone() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "fa-ldrtl-$SAMSUNG_ON_ITS_SIDE")
    fun persianOnASamsung() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "fa-ldrtl-$LARGER_PHONE_ON_ITS_SIDE", fontScale = 1.3f)
    fun persianOnALargerPhoneAtALargerFont() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "fa-ldrtl-$SIXTEEN_NINE_ON_ITS_SIDE")
    fun persianOnASixteenByNinePhone() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "zh-$SAMSUNG_ON_ITS_SIDE")
    fun chineseOnASamsung() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "zh-$LARGER_PHONE_ON_ITS_SIDE", fontScale = 1.3f)
    fun chineseOnALargerPhoneAtALargerFont() = assertEveryLabelIsWhole()

    @Test @Config(qualifiers = "zh-$SIXTEEN_NINE_ON_ITS_SIDE")
    fun chineseOnASixteenByNinePhone() = assertEveryLabelIsWhole()
}
