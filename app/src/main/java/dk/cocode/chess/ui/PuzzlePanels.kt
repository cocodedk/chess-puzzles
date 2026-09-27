package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.difficultyOf

/** Keeps the text rows off the screen edge, which the board is allowed to touch. */
private val TextInset = Modifier.padding(horizontal = 16.dp)

/** The puzzle's number and rating, the stats, the bands and the goal: above the board, or beside it when wide. */
@Composable
internal fun PuzzleHeader(state: PuzzleUiState, difficulties: List<Difficulty>, onDifficulty: (Difficulty) -> Unit) {
    Text("Puzzle ${state.position} of ${state.bandSize}", style = MaterialTheme.typography.titleLarge)
    Text("Rating ${state.rating}", style = MaterialTheme.typography.labelMedium)
    StatsRow(
        dayStreak = state.dayStreak, solved = state.solvedCount, hintFree = state.hintFreeCount,
        streak = state.currentStreak, best = state.bestStreak,
    )
    Spacer(Modifier.height(8.dp))
    DifficultyRow(available = difficulties, current = difficultyOf(state.rating), onSelect = onDifficulty)
    Spacer(Modifier.height(8.dp))
    Text(
        state.promptText,
        modifier = TextInset,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
    )
}

/** The verdict on the last move and the buttons: below the board, or beside it under the header when wide. */
@Composable
internal fun PuzzleControls(
    state: PuzzleUiState,
    onHint: () -> Unit,
    onReset: () -> Unit,
    onNext: () -> Unit,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    onAbout: () -> Unit,
) {
    Text(feedbackMessage(state.feedback), modifier = TextInset)
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
