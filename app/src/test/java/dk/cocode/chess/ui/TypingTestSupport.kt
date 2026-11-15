package dk.cocode.chess.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.Goal
import dk.cocode.chess.viewmodel.Prompt
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import org.junit.Assert.assertEquals
import org.robolectric.RuntimeEnvironment

/** 439dp less Robolectric's 56dp of system bars: the 384 x 383dp a Galaxy A52s's keyboard leaves. */
internal const val KEYBOARD_UP = "w384dp-h439dp"

/** The keyboard's height there: the 758dp of `A52S_QUALIFIERS` less the 383dp it leaves. */
internal val PHONE_KEYBOARD = 375.dp

/** 384dp less the 48dp Robolectric keeps for system bars on a phone's side: a Galaxy A52s on its side, 853 x 336dp. */
internal const val ON_ITS_SIDE = "w853dp-h384dp-land"

/** 384dp less those 48dp: a Samsung on its side, 814 x 336dp, room for a panel either side of the board. */
internal const val SAMSUNG_ON_ITS_SIDE = "w814dp-h384dp-land"

/** 412dp less those 48dp: a larger phone on its side, 915 x 364dp. */
internal const val LARGER_PHONE_ON_ITS_SIDE = "w915dp-h412dp-land"

/** 198dp less those 48dp: the 853 x 150dp its keyboard leaves there. */
internal const val KEYBOARD_UP_ON_ITS_SIDE = "w853dp-h198dp-land"

/** The keyboard's height there: the 336dp of [ON_ITS_SIDE] less the 150dp it leaves. */
internal val SIDE_KEYBOARD = 186.dp

/** 360dp less those 48dp: a 16:9 phone on its side, 640 x 312dp, room for one panel beside the board. */
internal const val SIXTEEN_NINE_ON_ITS_SIDE = "w640dp-h360dp-land"

/** 198dp less those 48dp: the same 150dp strip, the 640 x 150dp its keyboard leaves there. */
internal const val SIXTEEN_NINE_KEYBOARD_UP = "w640dp-h198dp-land"

/** The keyboard's height there: the 312dp of [SIXTEEN_NINE_ON_ITS_SIDE] less the 150dp it leaves. */
internal val SIXTEEN_NINE_KEYBOARD = 162.dp

/** A 16:9 phone held upright: 640dp less Robolectric's 56dp of system bars, 360 x 584dp. */
internal const val SIXTEEN_NINE_UPRIGHT = "w360dp-h640dp"

/** The keyboard's height there, 270dp of its 584dp. */
internal val SIXTEEN_NINE_UPRIGHT_KEYBOARD = 270.dp

/** The puzzle screen after a move that is not the best: its verdict says so. */
internal val AFTER_A_WRONG_MOVE =
    PuzzleUiState(prompt = Prompt(PieceColor.WHITE, Goal.BEST_MOVE, 1), feedback = Feedback.WRONG)

/** After an illegal move: the verdict says so, and a screen reader hears which move. */
internal val AFTER_AN_ILLEGAL_MOVE =
    AFTER_A_WRONG_MOVE.copy(feedback = Feedback.ILLEGAL, announcement = "Not a legal move: e2e5.")

/** A keyboard [height] tall, which a test raises and lowers in one step: itself the insets the screen reads. */
internal class TestKeyboard(height: Dp = 0.dp) : WindowInsets {
    var height by mutableStateOf(height)
    override fun getBottom(density: Density) = with(density) { height.roundToPx() }
    override fun getTop(density: Density) = 0
    override fun getLeft(density: Density, layoutDirection: LayoutDirection) = 0
    override fun getRight(density: Density, layoutDirection: LayoutDirection) = 0
}

/**
 * The puzzle screen, its tabs and Theme menu included, padded with [insets]; [state] and the [keyboard] are read as
 * they change.
 */
internal fun ComposeContentTestRule.showThePuzzleScreen(
    state: () -> PuzzleUiState,
    insets: WindowInsets = WindowInsets(0),
    keyboard: WindowInsets = WindowInsets(0),
) {
    setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = state(),
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {}, onPromotion = {}, onPromotionCancel = {},
                themes = PuzzleTheme.entries,
                tabs = { ModeTabs(selected = AppTab.PUZZLES, onSelect = {}) },
                insets = insets, keyboard = keyboard,
            )
        }
    }
}

/**
 * The puzzle screen with the move field opened, as its button's double-tap would, under the [keyboard], read as it
 * changes; the test's window is the screen the keyboard leaves.
 */
internal fun ComposeContentTestRule.showTheOpenField(state: () -> PuzzleUiState, keyboard: WindowInsets) {
    showThePuzzleScreen(state, keyboard = keyboard)
    openTheField()
}

/**
 * The puzzle screen with the move field opened under a keyboard that pads the screen, as on a phone: the test's window
 * is the screen with the keyboard down. Returns the keyboard, down, for the test to raise.
 */
internal fun ComposeContentTestRule.showTheOpenFieldUnderAKeyboard(state: () -> PuzzleUiState): TestKeyboard {
    val keyboard = TestKeyboard()
    showThePuzzleScreen(state, insets = keyboard, keyboard = keyboard)
    openTheField()
    return keyboard
}

private fun ComposeContentTestRule.openTheField() {
    onNodeWithText(appString(R.string.puzzle_type_move)).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

/** The app's string [id] in the test's language. */
internal fun appString(id: Int): String = RuntimeEnvironment.getApplication().getString(id)

/** The move field. */
internal fun SemanticsNodeInteractionsProvider.field() = onNode(hasSetTextAction())

/** The verdict on the last move: the one live region on the puzzle screen. */
internal fun SemanticsNodeInteractionsProvider.verdict() =
    onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))

/** Fails unless everything but the board, the verdict and the move field waits for the keyboard to go (in English). */
internal fun SemanticsNodeInteractionsProvider.assertOnlyTheFieldStays() {
    listOf("Hint", "Restart", "Puzzles", "Openings", "Puzzle 0 of 0 · Rating 0", "Use light theme", "About", "Share position")
        .forEach { onAllNodesWithText(it).assertCountEquals(0) }
    onAllNodesWithText("to move", substring = true).assertCountEquals(0) // the prompt
    listOf("Previous puzzle", "Next puzzle", "Day streak", "Level, Easy", "Theme, All").forEach {
        onAllNodesWithContentDescription(it, useUnmergedTree = true).assertCountEquals(0)
    }
}

/**
 * The open field as a keyboard [height] tall comes and goes, padding the screen: the field keeps its
 * focus, which keeps the keyboard up, and the verdict stays the one live region a screen reader announces, still
 * saying what became of the typed move; Hint goes with the keyboard up, and comes back with it down, as does the field's
 * help line where typing beside the board hid it.
 */
internal fun ComposeContentTestRule.assertTheFieldKeepsItsFocus(height: Dp) {
    var state by mutableStateOf(AFTER_A_WRONG_MOVE)
    val keyboard = showTheOpenFieldUnderAKeyboard { state }
    val field = field()
    field.assertIsFocused()
    val fieldId = field.fetchSemanticsNode().id
    val verdictId = verdict().fetchSemanticsNode().id
    onNodeWithText("Hint").assertExists()

    keyboard.height = height
    waitForIdle()
    onAllNodesWithText("Hint").assertCountEquals(0)
    field.assertIsFocused()
    assertEquals("the same field", fieldId, field.fetchSemanticsNode().id)
    state = AFTER_AN_ILLEGAL_MOVE
    waitForIdle()
    assertEquals("the same live region", verdictId, verdict().fetchSemanticsNode().id)
    verdict().assertContentDescriptionEquals(AFTER_AN_ILLEGAL_MOVE.announcement)

    keyboard.height = 0.dp
    waitForIdle()
    onNodeWithText("Hint").assertExists()
    field.assertIsFocused()
    assertEquals("the same field", fieldId, field.fetchSemanticsNode().id)
    assertEquals("the same live region", verdictId, verdict().fetchSemanticsNode().id)
    onNodeWithText(appString(R.string.puzzle_type_move_help)).assertExists()
}
