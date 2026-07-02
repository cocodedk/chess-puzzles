package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.board.ChessBoard
import dk.cocode.chess.ui.board.PromotionDialog
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.PuzzleViewModel
import dk.cocode.chess.viewmodel.difficultyOf

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
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Puzzle ${state.rating}", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Solved ${state.solvedCount}")
                Text("Streak ${state.currentStreak}")
                Text("Best ${state.bestStreak}")
            }
            Spacer(Modifier.height(8.dp))
            DifficultyRow(available = difficulties, current = difficultyOf(state.rating), onSelect = onDifficulty)
            Spacer(Modifier.height(8.dp))
            Text(state.promptText, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ChessBoard(
                state = state,
                onSquareTap = onSquareTap,
                onDragStart = onDragStart,
                onDragEnd = onDragEnd,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(feedbackMessage(state.feedback))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onHint, enabled = state.status == PuzzleStatus.IN_PROGRESS) {
                    Text("Hint")
                }
                OutlinedButton(onClick = onReset) { Text("Reset") }
                Button(onClick = onNext) { Text("Next") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onThemeToggle) { Text(themeLabel(themeMode)) }
                TextButton(onClick = onAbout) { Text(stringResource(R.string.about)) }
            }
        }
    }
    state.pendingPromotion?.let {
        PromotionDialog(onSelect = onPromotion, onDismiss = onPromotionCancel)
    }
}

@Composable
private fun DifficultyRow(available: List<Difficulty>, current: Difficulty, onSelect: (Difficulty) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        available.forEach { DifficultyChip(it, current, onSelect) }
    }
}

@Composable
private fun DifficultyChip(value: Difficulty, current: Difficulty, onSelect: (Difficulty) -> Unit) {
    val label = when (value) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HARD -> "Hard"
    }
    if (value == current) Button(onClick = { onSelect(value) }) { Text(label) }
    else OutlinedButton(onClick = { onSelect(value) }) { Text(label) }
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
