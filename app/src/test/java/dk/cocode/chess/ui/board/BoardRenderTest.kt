package dk.cocode.chess.ui.board

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.near
import dk.cocode.chess.renderToBitmap
import dk.cocode.chess.viewmodel.Highlight
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = PHONE_QUALIFIERS)
class BoardRenderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val empty = PuzzleUiState(board = List(8) { "        " })
    private val taps = mutableListOf<Square>()
    private val drags = mutableListOf<Square>()

    /** Hosts the board alone at the top-left of the screen and draws it. */
    private fun show(state: PuzzleUiState = empty, palette: BoardPalette = DayBoardPalette): Bitmap {
        composeRule.setContent {
            CompositionLocalProvider(LocalBoardPalette provides palette) {
                ChessBoard(
                    state = state,
                    onSquareTap = { taps += it },
                    onDragStart = { drags += it },
                    onDragEnd = { drags += it },
                    modifier = Modifier.size(320.dp),
                )
            }
        }
        return composeRule.renderToBitmap()
    }

    private fun boardPx(): Float = composeRule.onNodeWithTag(BOARD_TEST_TAG).fetchSemanticsNode().size.width.toFloat()

    /** Centre of [square] in the board's own pixels, frame included. */
    private fun centerOf(square: Square, flipped: Boolean = false): Offset {
        val frame = BoardGeometry.frameDepth(boardPx())
        return BoardGeometry.squareCenter(square, BoardGeometry.squareSize(boardPx()), flipped) + Offset(frame, frame)
    }

    @Test fun tapsLandOnTheSquareUnderTheFingerInsideTheFrame() {
        show(empty.copy(flipped = true))
        val a1 = centerOf(Square.of("a1"), flipped = true)
        val g2 = centerOf(Square.of("g2"), flipped = true)
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { click(a1) }
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { click(g2) }
        assertEquals(listOf(Square.of("a1"), Square.of("g2")), taps)
    }

    @Test fun dragsReportTheirFirstAndLastSquares() {
        show()
        val from = centerOf(Square.of("e2"))
        val to = centerOf(Square.of("e4"))
        composeRule.onNodeWithTag(BOARD_TEST_TAG).performTouchInput { swipe(from, to) }
        assertEquals(listOf(Square.of("e2"), Square.of("e4")), drags)
    }

    @Test fun boardSitsInAMahoganyRimWithBrassCorners() {
        val bitmap = show()
        val board = boardPx()
        val frame = BoardGeometry.frameDepth(board)
        val lacquer = Color(bitmap.getPixel((frame * .3f).toInt(), (board / 2 + 7).toInt()))
        assertTrue(lacquer.red > lacquer.green * 1.5f && lacquer.red < .45f) // deep red-brown mahogany
        // A bracket's arm runs 3 to 16 units deep in its 64-unit box, filling the mahogany outside the bands.
        val k = frame * (1 - BAND_REACH) / 16f
        for ((x, y) in listOf(30 * k to 9.5f * k, board - 30 * k to board - 9.5f * k)) {
            val brass = Color(bitmap.getPixel(x.toInt(), y.toInt()))
            assertTrue(brass.red > .6f && brass.green > .45f && brass.blue < brass.green) // a gold plate
        }
    }

    /** How many pixels in the quarter of [square] at [corner] (its fractions across and down) are [ink]. */
    private fun inkIn(bitmap: Bitmap, square: String, ink: Color, corner: Offset, flipped: Boolean = false): Int {
        val squarePx = BoardGeometry.squareSize(boardPx())
        val from = centerOf(Square.of(square), flipped) + (corner - Offset(.5f, .5f)) * squarePx
        val xs = from.x.toInt() until (from.x + squarePx / 2).toInt()
        val ys = from.y.toInt() until (from.y + squarePx / 2).toInt()
        return xs.sumOf { x -> ys.count { y -> near(bitmap.getPixel(x, y), ink.toArgb(), 40) } }
    }

    private val topLeft = Offset.Zero
    private val bottomRight = Offset(.5f, .5f)

    @Test fun filesAndRanksAreWrittenOnTheEdgeSquares() {
        val bitmap = show()
        assertTrue(inkIn(bitmap, "e1", LABEL_ON_WALNUT, bottomRight) > 0) // e1's file, pale on walnut
        assertTrue(inkIn(bitmap, "d1", LABEL_ON_MAPLE, bottomRight) > 0) // d1's file, dark on maple
        assertTrue(inkIn(bitmap, "a4", LABEL_ON_MAPLE, topLeft) > 0) // the fourth rank
        assertEquals(0, inkIn(bitmap, "e1", LABEL_ON_WALNUT, topLeft)) // only the a-file carries ranks
        assertEquals(0, inkIn(bitmap, "e2", LABEL_ON_MAPLE, bottomRight)) // only the first rank carries files
    }

    @Test fun flippedBoardWritesItsCoordinatesOnBlacksEdges() {
        val bitmap = show(empty.copy(flipped = true))
        assertTrue(inkIn(bitmap, "e8", LABEL_ON_MAPLE, bottomRight, flipped = true) > 0) // e8 is at the bottom now
        assertTrue(inkIn(bitmap, "h5", LABEL_ON_MAPLE, topLeft, flipped = true) > 0) // h is the left-hand file
        assertEquals(0, inkIn(bitmap, "e1", LABEL_ON_WALNUT, bottomRight, flipped = true)) // e1 is at the top
    }

    @Test fun mapleHasVisibleGrainByDay() = assertGrain(DayBoardPalette)

    @Test fun walnutAndMapleKeepTheirGrainAtNight() = assertGrain(NightBoardPalette)

    private fun assertGrain(palette: BoardPalette) {
        val bitmap = show(palette = palette)
        val r = (BoardGeometry.squareSize(boardPx()) * .4f).toInt()
        for (square in listOf(Square.of("d5"), Square.of("d4"))) {
            val wood = if (BoardGeometry.isLight(square)) palette.lightSquare else palette.darkSquare
            val c = centerOf(square)
            val pixels = (-r until r).flatMap { dx -> (-r until r).map { dy -> bitmap.getPixel(c.x.toInt() + dx, c.y.toInt() + dy) } }
            assertTrue(pixels.distinct().size > 40) // fibres, not a flat fill
            assertTrue(pixels.count { near(it, wood.toArgb(), 12) } > pixels.size / 2) // still reads as that wood
        }
    }

    @Test fun highlightsAndMarkersStillShowOnAFlippedNightBoard() {
        val state = PuzzleUiState(
            board = listOf("RNBQKBNR", "PPPPPPPP", "        ", "        ", "        ", "        ", "pppppppp", "rnbqkbnr"),
            flipped = true,
            selected = Square.of("e2"),
            legalTargets = setOf(Square.of("e4"), Square.of("d7")),
            lastMove = Highlight(Square.of("d2"), Square.of("d4")),
            hint = Highlight(Square.of("g1"), Square.of("f3")),
        )
        val bitmap = show(state, NightBoardPalette)
        fun at(square: String) = centerOf(Square.of(square), flipped = true).let { Color(bitmap.getPixel(it.x.toInt(), it.y.toInt())) }
        val hint = at("f3")
        assertTrue(hint.green > hint.red) // the felt-green hint, where the board is flipped to
        val dot = at("e4")
        val wood = NightBoardPalette.lightSquare
        assertTrue(dot.red < wood.red - .1f) // the legal-move dot darkens the empty target
    }
}
