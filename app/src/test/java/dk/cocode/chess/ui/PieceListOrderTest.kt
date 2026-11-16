package dk.cocode.chess.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeProvider
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.ui.board.BoardGeometry
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// The keys Compose answers its own traversal tests with: the item a screen reader goes to after, and before, one.
private const val NEXT = "android.view.accessibility.extra.EXTRA_DATA_TEST_TRAVERSALBEFORE_VAL"
private const val PREVIOUS = "android.view.accessibility.extra.EXTRA_DATA_TEST_TRAVERSALAFTER_VAL"

private const val WHITE = "White: King e1. Pawn a2."
private const val BLACK = "Black: King e8."
private const val WHITE_FA = "سفید: شاه e1. سرباز a2."
private const val BLACK_FA = "سیاه: شاه e8."

/** The order a screen reader walks the board: White's list, Black's list, then the 64 squares, whichever way it faces. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class PieceListOrderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(state: PuzzleUiState) {
        // Compose only works out the order for a screen reader that is on, so switch one on.
        shadowOf(composeRule.activity.getSystemService(AccessibilityManager::class.java)).apply {
            setEnabled(true)
            setEnabledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
        }
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                PuzzleScreenContent(
                    state = state,
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                    onHint = {}, onReset = {}, onNext = {},
                    onPromotion = {}, onPromotionCancel = {},
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun provider(): AccessibilityNodeProvider =
        (composeRule.onRoot().fetchSemanticsNode().root as ViewRootForTest).view.accessibilityNodeProvider

    private fun described() = composeRule
        .onAllNodes(SemanticsMatcher("has a description") { SemanticsProperties.ContentDescription in it.config })
        .fetchSemanticsNodes().associate { it.id to it.config[SemanticsProperties.ContentDescription].single() }

    private fun idOf(description: String) = described().entries.single { it.value == description }.key

    private fun link(id: Int, key: String): Int? {
        val info = provider().createAccessibilityNodeInfo(id) ?: return null // a covered item is not on the screen reader's list
        provider().addExtraDataToAccessibilityNodeInfo(id, info, key, null)
        return info.extras.getInt(key, -1).takeIf { it != -1 }
    }

    /** What a screen reader says, [count] items in a row from the one described as [first]. */
    private fun readFrom(first: String, count: Int): List<String> {
        val names = described()
        return generateSequence(idOf(first)) { link(it, NEXT) }.take(count).map { names[it] ?: "?" }.toList()
    }

    private fun assertWhiteBlackThenSquares(flipped: Boolean, white: String = WHITE, black: String = BLACK) {
        val board = listOf("    K   ", "P       ", "        ", "        ", "        ", "        ", "        ", "    k   ")
        show(PuzzleUiState(flipped = flipped, board = board))
        val read = readFrom(white, 66)
        assertEquals(listOf(white, black), read.take(2))
        assertEquals(
            BoardGeometry.squaresInReadingOrder(flipped).map { it.toAlgebraic() },
            read.drop(2).map { it.substringBefore(",") },
        )
        assertNotNull("the lists sit in the screen's order, not apart from it", link(idOf(white), PREVIOUS))
    }

    @Test fun whiteThenBlackThenTheSquaresInTheirOrder() = assertWhiteBlackThenSquares(flipped = false)

    @Test fun andTheSameWhenTheBoardIsSeenFromBlacksSide() = assertWhiteBlackThenSquares(flipped = true)

    // Right to left lays the screen out the other way, but the squares still read a8, b8 … from the left.
    @Test @Config(qualifiers = "fa-ldrtl-$PHONE_QUALIFIERS")
    fun andTheSameInPersianWhichIsRightToLeft() = assertWhiteBlackThenSquares(flipped = false, WHITE_FA, BLACK_FA)

    @Test fun aSideWithoutPiecesLeavesNoGapBeforeTheSquares() {
        show(PuzzleUiState(board = listOf("    K   ") + List(7) { "        " }))
        val read = readFrom("White: King e1.", 2)
        assertEquals("White: King e1.", read[0])
        assertEquals("a8", read[1].substringBefore(","))
    }
}
