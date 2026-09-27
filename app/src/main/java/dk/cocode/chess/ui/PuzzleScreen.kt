package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.board.ChessBoard
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
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            if (boardBesideControls(maxWidth, maxHeight)) {
                Row(Modifier.fillMaxSize()) {
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
                    Column(
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        PuzzleHeader(state, difficulties, onDifficulty)
                        Spacer(Modifier.height(16.dp))
                        PuzzleControls(state, onHint, onReset, onNext, themeMode, onThemeToggle, onAbout)
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    PuzzleHeader(state, difficulties, onDifficulty)
                    Spacer(Modifier.height(8.dp))
                    // The largest square that fits both the width and the height the other rows leave.
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.weight(1f, fill = false))
                    Spacer(Modifier.height(8.dp))
                    PuzzleControls(state, onHint, onReset, onNext, themeMode, onThemeToggle, onAbout)
                }
            }
        }
    }
    state.pendingPromotion?.let {
        PromotionDialog(onSelect = onPromotion, onDismiss = onPromotionCancel)
    }
}

/** Side by side when a board as tall as the screen still leaves the controls room beside it. */
internal fun boardBesideControls(width: Dp, height: Dp): Boolean = width - height >= CONTROLS_MIN_WIDTH

/** Wide enough for the three difficulty chips and the Hint, Reset and Next buttons. */
private val CONTROLS_MIN_WIDTH = 300.dp

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
