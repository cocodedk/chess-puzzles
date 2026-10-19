package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.difficultyOf
import dk.cocode.chess.viewmodel.themeName

/**
 * The puzzle's number and rating, the stats, the bands and, when there are [themes] to pick, the theme button;
 * [stacked] lines the bands up in a column.
 */
@Composable
internal fun PuzzleStats(
    state: PuzzleUiState,
    difficulties: List<Difficulty>,
    onDifficulty: (Difficulty) -> Unit,
    themes: List<PuzzleTheme>,
    onTheme: (PuzzleTheme?) -> Unit,
    stacked: Boolean,
) {
    Text(
        stringResource(R.string.puzzle_title, state.position, state.bandSize),
        Modifier.semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
    )
    Text(stringResource(R.string.puzzle_rating, state.rating), style = MaterialTheme.typography.labelMedium)
    StatsRow(
        dayStreak = state.dayStreak, solved = state.solvedCount, hintFree = state.hintFreeCount,
        streak = state.currentStreak, best = state.bestStreak,
    )
    Spacer(Modifier.height(8.dp))
    val current = difficultyOf(state.rating)
    val texts = rememberTexts()
    ChipRow(
        stacked, difficulties, selected = { it == current }, onSelect = onDifficulty,
        label = { texts.bandLabel(it) },
    )
    ThemeMenu(state.theme, themes, onTheme)
}

/**
 * The theme filter, under the bands (they leave no room beside them): a button showing the theme in use,
 * which opens a list of All and each of [themes]. A screen reader hears "Theme, Fork"; the button keeps
 * the 48dp touch target and wraps its text, so a long name fits a narrow panel at a large font.
 */
@Composable
internal fun ThemeMenu(current: PuzzleTheme?, themes: List<PuzzleTheme>, onSelect: (PuzzleTheme?) -> Unit) {
    if (themes.isEmpty()) return
    val texts = rememberTexts()
    var open by remember { mutableStateOf(false) }
    val name = texts.themeName(current)
    Box {
        OutlinedButton(
            onClick = { open = true },
            modifier = CompactButton.semantics { contentDescription = texts.string(R.string.puzzle_theme_button_description, name) },
            contentPadding = CompactPadding,
        ) { Text("${texts.string(R.string.puzzle_theme_button, name)} ▾", textAlign = TextAlign.Center) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (listOf(null) + themes).forEach { choice ->
                DropdownMenuItem(
                    text = { Text(texts.themeName(choice)) },
                    onClick = { open = false; onSelect(choice) },
                    modifier = Modifier.semantics { selected = choice == current },
                )
            }
        }
    }
}
