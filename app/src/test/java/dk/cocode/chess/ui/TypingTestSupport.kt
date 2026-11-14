package dk.cocode.chess.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState

/** 439dp less Robolectric's 56dp of system bars: the 384 x 383dp a Galaxy A52s's keyboard leaves. */
internal const val KEYBOARD_UP = "w384dp-h439dp"

/** The keyboard's height there: the 758dp of `A52S_QUALIFIERS` less the 383dp it leaves. */
internal val PHONE_KEYBOARD = 375.dp

/**
 * The puzzle screen, its tabs and Theme menu included, with the move field opened as its button's double-tap would;
 * [state] and [keyboardHeight] are read as they change.
 */
internal fun ComposeContentTestRule.showTheOpenField(state: () -> PuzzleUiState, keyboardHeight: () -> Dp) {
    setContent {
        ChessTheme(darkTheme = false) {
            PuzzleScreenContent(
                state = state(),
                onSquareTap = {}, onDragStart = {}, onDragEnd = {},
                onHint = {}, onReset = {}, onNext = {}, onPromotion = {}, onPromotionCancel = {},
                themes = PuzzleTheme.entries,
                tabs = { ModeTabs(selected = AppTab.PUZZLES, onSelect = {}) },
                keyboardHeight = keyboardHeight,
            )
        }
    }
    onNodeWithText("Type a move").performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

/** The move field. */
internal fun SemanticsNodeInteractionsProvider.field() = onNode(hasSetTextAction())
