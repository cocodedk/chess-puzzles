package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.difficultyOf
import dk.cocode.chess.viewmodel.promptLine

/** Keeps the text rows off the screen edge, which the board is allowed to touch. Reused by the openings panels. */
internal val TextInset = Modifier.padding(horizontal = 16.dp)

/** Buttons drawn lower and slimmer than Material's default; the touch target stays 48dp. Reused by the openings panels. */
internal val CompactButton = Modifier.heightIn(min = 36.dp)
internal val CompactPadding = PaddingValues(horizontal = 16.dp)

/** A polite live region that reads [announcement] aloud when it changes; with none, the line reads as its text. */
internal fun Modifier.announces(announcement: String): Modifier = semantics {
    liveRegion = LiveRegionMode.Polite
    if (announcement.isNotEmpty()) contentDescription = announcement
}

/** The selected-state colors shared by every filter chip in the puzzle and openings screens. */
@Composable
internal fun selectedChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
)

/** The puzzle's number and rating, the stats and the bands; [stacked] lines the bands up in a column. */
@Composable
internal fun PuzzleStats(
    state: PuzzleUiState,
    difficulties: List<Difficulty>,
    onDifficulty: (Difficulty) -> Unit,
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
}

/** What the puzzle asks of the player. */
@Composable
internal fun PuzzlePrompt(state: PuzzleUiState) {
    Text(
        rememberTexts().promptLine(state),
        modifier = TextInset, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium,
    )
}

/** The verdict on the last move and the Hint, Restart, Previous and Next buttons; [stacked] lines them up in a column. */
@Composable
internal fun PuzzleControls(
    state: PuzzleUiState,
    onHint: () -> Unit,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    stacked: Boolean,
) {
    Text(
        rememberTexts().feedbackMessage(state.feedback),
        modifier = TextInset.announces(state.announcement), textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(12.dp))
    Lineup(stacked) { item ->
        val inProgress = state.status == PuzzleStatus.IN_PROGRESS
        OutlinedButton(onHint, item.then(CompactButton), enabled = inProgress, contentPadding = CompactPadding) {
            Text(stringResource(R.string.puzzle_hint))
        }
        OutlinedButton(onReset, item.then(CompactButton), contentPadding = CompactPadding) {
            Text(stringResource(R.string.puzzle_reset))
        }
        // Arrows, not words, so four buttons still fit one row on a small phone at a large font.
        val previous = stringResource(R.string.puzzle_previous)
        val next = stringResource(R.string.puzzle_next)
        OutlinedButton(
            onPrevious,
            item.then(CompactButton).semantics { contentDescription = previous },
            contentPadding = CompactPadding,
        ) { Text("◀", Modifier.clearAndSetSemantics {}) }
        Button(
            onNext,
            item.then(CompactButton).semantics { contentDescription = next },
            contentPadding = CompactPadding,
        ) { Text("▶", Modifier.clearAndSetSemantics {}) }
    }
}

/** The theme toggle and the About link; in a narrow panel About moves under the theme whole, never split. */
@OptIn(ExperimentalLayoutApi::class) // FlowRow is still marked experimental (checked up to foundation 1.11)
@Composable
internal fun PuzzleSettings(themeMode: ThemeMode, onThemeToggle: () -> Unit, onAbout: () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        val texts = rememberTexts()
        val switchTo = texts.themeSwitchLabel(themeMode)
        TextButton(onClick = onThemeToggle, modifier = Modifier.semantics { onClick(label = switchTo, action = null) }) {
            Text(texts.themeLabel(themeMode))
        }
        TextButton(onClick = onAbout) { Text(stringResource(R.string.about)) }
    }
}

/**
 * A row of [content], or when [stacked] a column of it as wide as its widest item, each item stretched to
 * match; the 48dp touch targets already space the column. [content] gets the modifier each item wears.
 * Reused by the openings panels for their own chip rows.
 */
@Composable
internal fun Lineup(stacked: Boolean, content: @Composable (item: Modifier) -> Unit) {
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

/**
 * One [Lineup] of [FilterChip]s, one per item in [items] — [selected] and [onSelect] key off the
 * item itself, [label] renders its text. Shared by the puzzle band chips and the openings' mode and
 * line chips, which otherwise repeated this exact chip wiring three times. A chip whose [spoken] is not
 * null is read as that, not as its label.
 */
@Composable
internal fun <T> ChipRow(
    stacked: Boolean,
    items: List<T>,
    selected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    spoken: (T) -> String? = { null },
) {
    Lineup(stacked) { item ->
        items.forEach { value ->
            FilterChip(
                selected = selected(value),
                onClick = { onSelect(value) },
                label = { Text(label(value), item, textAlign = TextAlign.Center) },
                modifier = item.semantics { spoken(value)?.let { contentDescription = it } },
                colors = selectedChipColors(),
            )
        }
    }
}
