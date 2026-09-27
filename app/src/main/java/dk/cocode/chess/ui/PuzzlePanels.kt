package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

/** Buttons drawn lower and slimmer than Material's default; the touch target stays 48dp. */
private val CompactButton = Modifier.heightIn(min = 36.dp)
private val CompactPadding = PaddingValues(horizontal = 16.dp)

/** The puzzle's number and rating, the stats and the bands; [stacked] lines the bands up in a column. */
@Composable
internal fun PuzzleStats(
    state: PuzzleUiState,
    difficulties: List<Difficulty>,
    onDifficulty: (Difficulty) -> Unit,
    stacked: Boolean,
) {
    Text("Puzzle ${state.position} of ${state.bandSize}", style = MaterialTheme.typography.titleLarge)
    Text("Rating ${state.rating}", style = MaterialTheme.typography.labelMedium)
    StatsRow(
        dayStreak = state.dayStreak, solved = state.solvedCount, hintFree = state.hintFreeCount,
        streak = state.currentStreak, best = state.bestStreak,
    )
    Spacer(Modifier.height(8.dp))
    Lineup(stacked) { item ->
        val current = difficultyOf(state.rating)
        difficulties.forEach { band ->
            FilterChip(
                selected = band == current,
                onClick = { onDifficulty(band) },
                label = { Text(bandLabel(band), item, textAlign = TextAlign.Center) },
                modifier = item,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

/** What the puzzle asks of the player. */
@Composable
internal fun PuzzlePrompt(state: PuzzleUiState) {
    Text(state.promptText, modifier = TextInset, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
}

/** The verdict on the last move and the Hint, Reset and Next buttons; [stacked] lines them up in a column. */
@Composable
internal fun PuzzleControls(state: PuzzleUiState, onHint: () -> Unit, onReset: () -> Unit, onNext: () -> Unit, stacked: Boolean) {
    Text(feedbackMessage(state.feedback), modifier = TextInset, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Lineup(stacked) { item ->
        val inProgress = state.status == PuzzleStatus.IN_PROGRESS
        OutlinedButton(onHint, item.then(CompactButton), enabled = inProgress, contentPadding = CompactPadding) { Text("Hint") }
        OutlinedButton(onReset, item.then(CompactButton), contentPadding = CompactPadding) { Text("Reset") }
        Button(onNext, item.then(CompactButton), contentPadding = CompactPadding) { Text("Next") }
    }
}

/** The theme toggle and the About link. */
@Composable
internal fun PuzzleSettings(themeMode: ThemeMode, onThemeToggle: () -> Unit, onAbout: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onThemeToggle) { Text(themeLabel(themeMode)) }
        TextButton(onClick = onAbout) { Text(stringResource(R.string.about)) }
    }
}

/**
 * A row of [content], or when [stacked] a column of it as wide as its widest item, each item stretched to
 * match; the 48dp touch targets already space the column. [content] gets the modifier each item wears.
 */
@Composable
private fun Lineup(stacked: Boolean, content: @Composable (item: Modifier) -> Unit) {
    if (stacked) {
        Column(Modifier.width(IntrinsicSize.Max), horizontalAlignment = Alignment.CenterHorizontally) {
            content(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            content(Modifier)
        }
    }
}

internal fun bandLabel(band: Difficulty): String = when (band) {
    Difficulty.EASY -> "Easy"
    Difficulty.MEDIUM -> "Medium"
    Difficulty.HARD -> "Hard"
}
