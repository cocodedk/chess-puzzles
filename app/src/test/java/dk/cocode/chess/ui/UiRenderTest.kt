package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.renderToBitmap
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.Highlight
import dk.cocode.chess.viewmodel.PendingPromotion
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UiRenderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val board = listOf(
        "RNBQKBNR", "PPPPPPPP", "        ", "        ",
        "        ", "        ", "pppppppp", "rnbqkbnr",
    )

    private fun show(state: PuzzleUiState, onPromotion: (PieceType) -> Unit = {}) {
        composeRule.setContent {
            PuzzleScreenContent(
                state = state,
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {},
                onPromotion = onPromotion, onPromotionCancel = {},
            )
        }
    }

    private fun rich(flipped: Boolean) = PuzzleUiState(
        board = board, flipped = flipped,
        selected = Square.of("e2"),
        legalTargets = setOf(Square.of("e4"), Square.of("e7")), // empty dot + capture ring
        lastMove = Highlight(Square.of("d2"), Square.of("d4")),
        hint = Highlight(Square.of("g1"), Square.of("f3")),
        status = PuzzleStatus.IN_PROGRESS, feedback = Feedback.CORRECT,
        rating = 1500, solvedCount = 3, currentStreak = 2, bestStreak = 5,
        promptText = "White to move",
    )

    @Test fun rendersRichBoard() {
        show(rich(flipped = false))
        composeRule.renderToBitmap()
        composeRule.onNodeWithText("Hint").assertExists()
    }

    @Test fun rendersRichBoardFlipped() {
        show(rich(flipped = true))
        composeRule.renderToBitmap()
    }

    @Test fun rendersSolvedState() {
        show(PuzzleUiState(board = board, status = PuzzleStatus.SOLVED, feedback = Feedback.SOLVED))
        composeRule.renderToBitmap()
        composeRule.onNodeWithText("Next").assertExists()
    }

    @Test fun promotionDialogSelectsPiece() {
        var chosen: PieceType? = null
        show(
            PuzzleUiState(board = board, pendingPromotion = PendingPromotion(Square.of("e7"), Square.of("e8"))),
            onPromotion = { chosen = it },
        )
        composeRule.renderToBitmap()
        composeRule.onNodeWithText("Promote to").assertExists()
        composeRule.onNodeWithText("♜").performClick() // rook glyph
        assertEquals(PieceType.ROOK, chosen)
    }
}
