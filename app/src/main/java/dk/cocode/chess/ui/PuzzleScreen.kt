package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.board.PromotionDialog
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.PuzzleViewModel

@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    tabs: @Composable () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAbout by rememberSaveable { mutableStateOf(false) }
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
    } else {
        PuzzleScreenContent(
            state = state,
            themeMode = themeMode,
            onThemeToggle = onThemeToggle,
            onSquareTap = viewModel::onSquareTapped,
            onDragStart = viewModel::onDragStart,
            onDragEnd = viewModel::onDragEnd,
            onHint = viewModel::onHint,
            onReset = viewModel::onReset,
            onNext = viewModel::onNext,
            onPromotion = viewModel::onPromotionChosen,
            onPromotionCancel = viewModel::onPromotionCancelled,
            onAbout = { showAbout = true },
            onDifficulty = viewModel::onDifficultySelected,
            difficulties = viewModel.availableDifficulties,
            tabs = tabs,
        )
    }
}

@Composable
fun PuzzleScreenContent(
    state: PuzzleUiState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    onHint: () -> Unit,
    onReset: () -> Unit,
    onNext: () -> Unit,
    onPromotion: (PieceType) -> Unit,
    onPromotionCancel: () -> Unit,
    onAbout: () -> Unit = {},
    onDifficulty: (Difficulty) -> Unit = {},
    difficulties: List<Difficulty> = Difficulty.entries,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeToggle: () -> Unit = {},
    tabs: @Composable () -> Unit = {},
) {
    // `stacked` is true only when each slot has a side panel to itself. Then the settings sit under the
    // stats and the prompt above the buttons, balancing the two panels; when both slots share one
    // column the rows read top to bottom: stats, prompt, (board), buttons, settings.
    BoardScaffold(
        state = state,
        onSquareTap = onSquareTap, onDragStart = onDragStart, onDragEnd = onDragEnd,
        tabs = tabs,
        header = { stacked ->
            PuzzleStats(state, difficulties, onDifficulty, stacked)
            Spacer(Modifier.height(8.dp))
            if (stacked) PuzzleSettings(themeMode, onThemeToggle, onAbout) else PuzzlePrompt(state)
        },
        body = { stacked ->
            if (stacked) {
                PuzzlePrompt(state)
                Spacer(Modifier.height(16.dp))
                PuzzleControls(state, onHint, onReset, onNext, stacked)
            } else {
                PuzzleControls(state, onHint, onReset, onNext, stacked)
                PuzzleSettings(themeMode, onThemeToggle, onAbout)
            }
        },
    )
    state.pendingPromotion?.let {
        PromotionDialog(onSelect = onPromotion, onDismiss = onPromotionCancel)
    }
}

internal fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "Theme: Auto"
    ThemeMode.LIGHT -> "Theme: Light"
    ThemeMode.DARK -> "Theme: Dark"
}

internal fun feedbackMessage(feedback: Feedback): String = when (feedback) {
    Feedback.NONE -> ""
    Feedback.CORRECT -> "Correct — keep going"
    Feedback.SOLVED -> "Solved ✓"
    Feedback.WRONG -> "Not the best move — try again"
}
