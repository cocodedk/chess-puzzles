package dk.cocode.chess.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import dk.cocode.chess.R
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.difficultyOf
import dk.cocode.chess.viewmodel.themeName

/**
 * The puzzle's number and rating on one line, the stats, then the Level menu and, when there are [themes]
 * to pick, the Theme menu after it; [stacked] lines the two menus up in a column.
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
    val title = stringResource(R.string.puzzle_title, state.position, state.bandSize)
    val rating = stringResource(R.string.puzzle_rating, state.rating)
    // One line, wrapping as a whole: the title, then the rating smaller and fainter. Heard as one heading.
    Text(
        buildAnnotatedString {
            append(title)
            withStyle(
                MaterialTheme.typography.bodyMedium.toSpanStyle().copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            ) { append(" · $rating") }
        },
        TextInset.semantics { heading(); contentDescription = "$title, $rating" },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
    StatsRow(
        dayStreak = state.dayStreak, solved = state.solvedCount, hintFree = state.hintFreeCount,
        streak = state.currentStreak, best = state.bestStreak,
    )
    val texts = rememberTexts()
    // In a row the Theme menu comes last, so it takes the room the Level menu leaves and wraps a long name.
    Lineup(stacked) { item ->
        MenuChip(
            shown = R.string.puzzle_level_button, spoken = R.string.puzzle_level_button_description,
            current = difficultyOf(state.rating), choices = difficulties, name = texts::bandLabel,
            onSelect = onDifficulty, item = item,
        )
        if (themes.isNotEmpty()) {
            MenuChip(
                shown = R.string.puzzle_theme_button, spoken = R.string.puzzle_theme_button_description,
                current = state.theme, choices = listOf(null) + themes, name = texts::themeName,
                onSelect = onTheme, item = item,
            )
        }
    }
}

/**
 * A chip showing the choice in use, "Theme: Fork ▾", which opens a list of [choices] with that one marked
 * selected. A screen reader hears [spoken], "Theme, Fork". Every such chip has one shape and height; it
 * keeps the 48dp touch target and wraps its text, so a long name fits a narrow room at a large font.
 * [item] is what the [Lineup] gives each of its items.
 */
@Composable
private fun <T> MenuChip(
    @StringRes shown: Int,
    @StringRes spoken: Int,
    current: T,
    choices: List<T>,
    name: (T) -> String,
    onSelect: (T) -> Unit,
    item: Modifier,
) {
    val texts = rememberTexts()
    var open by remember { mutableStateOf(false) }
    val currentName = name(current)
    Box {
        AssistChip(
            onClick = { open = true },
            label = { Text("${texts.string(shown, currentName)} ▾", item, textAlign = TextAlign.Center) },
            modifier = item.semantics { contentDescription = texts.string(spoken, currentName) },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(name(choice)) },
                    onClick = { open = false; onSelect(choice) },
                    modifier = Modifier.semantics { selected = choice == current },
                )
            }
        }
    }
}
