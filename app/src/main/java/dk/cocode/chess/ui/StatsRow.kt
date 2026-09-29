package dk.cocode.chess.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R

/**
 * The counters above the board. Each is an icon plus its number — five labelled stats would not fit
 * on one line on a small phone. A screen reader hears each stat as one item, its label and number.
 *
 * Takes the five numbers rather than the whole [dk.cocode.chess.viewmodel.PuzzleUiState]: that type
 * holds collections Compose cannot prove stable, so passing it would re-run this row on every board
 * tap. With plain Ints the row is skipped until a counter actually changes.
 */
@OptIn(ExperimentalLayoutApi::class) // FlowRow is still marked experimental (checked up to foundation 1.11)
@Composable
fun StatsRow(dayStreak: Int, solved: Int, hintFree: Int, streak: Int, best: Int) {
    // A flow row: in a narrow side panel, or at a large font size, whole stats move to a second line
    // instead of the last ones being squeezed to nothing.
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Stat(R.drawable.ic_stat_day, R.string.stat_day, dayStreak)
        Stat(R.drawable.ic_stat_solved, R.string.stat_solved, solved)
        Stat(R.drawable.ic_stat_hint_free, R.string.stat_hint_free, hintFree)
        Stat(R.drawable.ic_stat_streak, R.string.stat_streak, streak)
        Stat(R.drawable.ic_stat_best, R.string.stat_best, best)
    }
}

@Composable
private fun Stat(@DrawableRes icon: Int, @StringRes label: Int, value: Int) {
    val name = stringResource(label)
    Row(
        // One item for a screen reader: "Day streak, 4".
        Modifier.clearAndSetSemantics { contentDescription = "$name, $value" },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = name, Modifier.size(16.dp))
        Text("$value", style = MaterialTheme.typography.bodyMedium)
    }
}
