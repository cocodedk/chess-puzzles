package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dk.cocode.chess.R
import dk.cocode.chess.assertOnScreen
import dk.cocode.chess.bounds
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A phone on its side with the keyboard down: the side panel stacks the prompt, the verdict, the buttons, Share
 * position and the move field, which used to need 442dp of a 336dp panel, leaving "Type a move" 86dp below the fold.
 */
@RunWith(RobolectricTestRunner::class)
class TypeMoveOnItsSideTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun assertTypeMoveAndTheVerdictAreInView() {
        composeRule.showThePuzzleScreen({ AFTER_A_WRONG_MOVE })
        composeRule.assertOnScreen("the verdict", composeRule.verdict().getUnclippedBoundsInRoot())
        composeRule.assertOnScreen("Type a move", composeRule.bounds(appString(R.string.puzzle_type_move)))
    }

    @Test @Config(qualifiers = SAMSUNG_ON_ITS_SIDE)
    fun onASamsungOnItsSide() = assertTypeMoveAndTheVerdictAreInView()

    @Test @Config(qualifiers = LARGER_PHONE_ON_ITS_SIDE, fontScale = 1.3f)
    fun onALargerPhoneAtALargerFont() = assertTypeMoveAndTheVerdictAreInView()
}
