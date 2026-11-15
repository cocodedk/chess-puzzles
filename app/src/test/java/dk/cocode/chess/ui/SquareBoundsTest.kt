package dk.cocode.chess.ui

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import dk.cocode.chess.A52S_QUALIFIERS
import dk.cocode.chess.core.data.openings.Openings
import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.ui.board.BOARD_SQUARES_TAG
import dk.cocode.chess.ui.board.BoardGeometry
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * TalkBack outlines the item it focuses, and Compose widens an item smaller than 48dp to 48dp for that, so on a
 * board whose squares are under 48dp (46 on a 384dp phone) the outlines of neighbouring squares overlapped and sat
 * off their squares. Each of the 64 items must be exactly the rectangle it names, both as the semantics tree gives
 * it and as the accessibility service, which is what TalkBack asks, reads it.
 */
@RunWith(RobolectricTestRunner::class)
class SquareBoundsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showPuzzle(flipped: Boolean = false) = composeRule.setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = PuzzleUiState(flipped = flipped),
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = {}, onPromotionCancel = {},
            )
        }
    }

    private fun showOpening(mode: OpeningMode) {
        val italian = Openings.all.first { it.id == "italian" }
        val tree = OpeningTree(italian)
        composeRule.setContent {
            ChessTheme(darkTheme = false) {
                OpeningScreenContent(
                    state = OpeningUiState(
                        board = tree.start.toRows(), openingId = italian.id, mode = mode,
                        lineNames = tree.lineNames, line = 0, ply = 0, moveSan = tree.plies(0).map { it.san },
                    ),
                    opening = italian, onBack = {}, onMode = {}, onLine = {}, onStep = {},
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {}, onAgain = {},
                )
            }
        }
    }

    /** The Compose view, which answers the accessibility service's questions about every item in it. */
    private fun View.composeView(): View =
        if (javaClass.name.endsWith("AndroidComposeView")) this
        else (this as ViewGroup).let { group -> (0 until group.childCount).firstNotNullOf { group.getChildAt(it).composeView() } }

    private fun android.graphics.Rect.toRect() = Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

    private fun assertEverySquareIsItsDrawnRectangle() {
        val squares = composeRule.onNodeWithTag(BOARD_SQUARES_TAG)
        val board = squares.fetchSemanticsNode()
        val edges = BoardGeometry.squareEdgesPx(board.size.width.toFloat())
        val squarePx = edges[1] - edges[0]
        assertTrue("squares of ${squarePx}px are under 48dp", squarePx < 48 * composeRule.density.density)
        val reader = composeRule.activity.findViewById<View>(android.R.id.content).composeView()
        val onScreen = IntArray(2).also(reader::getLocationOnScreen)
        val items = squares.onChildren()
        items.assertCountEquals(64)
        for (i in 0 until 64) {
            val left = board.positionInRoot.x + edges[i % 8]
            val top = board.positionInRoot.y + edges[i / 8]
            val drawn = Rect(left, top, left + edges[i % 8 + 1] - edges[i % 8], top + edges[i / 8 + 1] - edges[i / 8])
            val node = items[i].fetchSemanticsNode()
            assertEquals("square $i in the semantics tree", drawn, node.touchBoundsInRoot)
            val outline = android.graphics.Rect()
            reader.accessibilityNodeProvider.createAccessibilityNodeInfo(node.id)!!.getBoundsInScreen(outline)
            outline.offset(-onScreen[0], -onScreen[1])
            assertEquals("square $i as TalkBack outlines it", drawn, outline.toRect())
        }
    }

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun squaresUnder48dpAreEachExactlyTheirDrawnRectangle() {
        showPuzzle()
        assertEverySquareIsItsDrawnRectangle()
    }

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theyAreOnABoardSeenFromBlacksSideToo() {
        showPuzzle(flipped = true)
        assertEverySquareIsItsDrawnRectangle()
    }

    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theyAreOnTheOpeningsPractiseBoard() {
        showOpening(OpeningMode.PRACTISE)
        assertEverySquareIsItsDrawnRectangle()
    }

    /** In Learn the squares are read but offer no click, and were never widened: this holds, and must go on holding. */
    @Test @Config(qualifiers = A52S_QUALIFIERS)
    fun theyAreOnTheOpeningsLearnBoard() {
        showOpening(OpeningMode.LEARN)
        assertEverySquareIsItsDrawnRectangle()
    }

    @Test @Config(qualifiers = "w800dp-h360dp-land")
    fun theyAreOnAPhoneHeldSideways() {
        showPuzzle()
        assertEverySquareIsItsDrawnRectangle()
    }
}
