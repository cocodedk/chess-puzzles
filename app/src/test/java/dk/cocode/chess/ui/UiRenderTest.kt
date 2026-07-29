package dk.cocode.chess.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.width
import dk.cocode.chess.PHONE_QUALIFIERS
import dk.cocode.chess.containsColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.renderToBitmap
import dk.cocode.chess.ui.board.DayBoardPalette
import dk.cocode.chess.ui.board.NightBoardPalette
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.ui.theme.DarkColors
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.Highlight
import dk.cocode.chess.viewmodel.PendingPromotion
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
class UiRenderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val board = listOf(
        "RNBQKBNR", "PPPPPPPP", "        ", "        ",
        "        ", "        ", "pppppppp", "rnbqkbnr",
    )

    private fun show(
        state: PuzzleUiState,
        onPromotion: (PieceType) -> Unit = {},
        darkTheme: Boolean = false,
        themeMode: ThemeMode = ThemeMode.SYSTEM,
        onThemeToggle: () -> Unit = {},
    ) {
        composeRule.setContent {
            ChessTheme(darkTheme = darkTheme) {
                PuzzleScreenContent(
                    state = state,
                    onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                    onHint = {}, onReset = {}, onNext = {},
                    onPromotion = onPromotion, onPromotionCancel = {},
                    themeMode = themeMode, onThemeToggle = onThemeToggle,
                )
            }
        }
    }

    private fun rich(flipped: Boolean) = PuzzleUiState(
        board = board, flipped = flipped,
        selected = Square.of("e2"),
        legalTargets = setOf(Square.of("e4"), Square.of("e7")), // empty dot + capture ring
        lastMove = Highlight(Square.of("d2"), Square.of("d4")),
        hint = Highlight(Square.of("g1"), Square.of("f3")),
        status = PuzzleStatus.IN_PROGRESS, feedback = Feedback.CORRECT,
        rating = 1500, solvedCount = 3, hintFreeCount = 2, currentStreak = 6, bestStreak = 5, dayStreak = 4,
        promptText = "White to move",
    )

    @Test fun rendersRichBoard() {
        show(rich(flipped = false))
        val bitmap = composeRule.renderToBitmap()
        assertTrue(bitmap.containsColor(DayBoardPalette.darkSquare.toArgb())) // day board drawn
        composeRule.onNodeWithText("Hint").assertExists()
        composeRule.onNodeWithContentDescription("Day streak").assertExists() // stats row icons
        composeRule.onNodeWithText("2").assertExists() // ... each with its number: hint-free solves
    }

    @Test fun statsRowKeepsFiveIconsOnOneLine() {
        show(rich(flipped = false))
        composeRule.renderToBitmap()
        val screen = composeRule.onRoot().getUnclippedBoundsInRoot().width
        val stats = listOf("Day streak", "Solved", "Solved without a hint", "Current streak", "Best streak")
            .map { composeRule.onNodeWithContentDescription(it).getUnclippedBoundsInRoot() }
        stats.forEach {
            assertEquals(stats[0].top, it.top) // all five share one row ...
            assertTrue(it.right <= screen) // ... and none is pushed off a 360dp screen
        }
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

    @Test fun rendersDarkThemeAndTogglesIt() {
        var toggled = false
        show(rich(flipped = false), darkTheme = true, themeMode = ThemeMode.DARK, onThemeToggle = { toggled = true })
        val bitmap = composeRule.renderToBitmap()
        assertEquals(DarkColors.background.toArgb(), bitmap.getPixel(1, 1)) // dark scheme applied
        assertTrue(bitmap.containsColor(NightBoardPalette.darkSquare.toArgb())) // night board drawn
        composeRule.onNodeWithText("Theme: Dark").performClick()
        assertTrue(toggled)
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
