package dk.cocode.chess.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R

/**
 * The counters above the board. Each is an icon plus its number — five labelled stats would not fit
 * on one line on a small phone. The icon's content description is what a screen reader announces.
 *
 * Takes the five numbers rather than the whole [dk.cocode.chess.viewmodel.PuzzleUiState]: that type
 * holds collections Compose cannot prove stable, so passing it would re-run this row on every board
 * tap. With plain Ints the row is skipped until a counter actually changes.
 */
@Composable
fun StatsRow(dayStreak: Int, solved: Int, hintFree: Int, streak: Int, best: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Stat(R.drawable.ic_stat_day, R.string.stat_day, dayStreak)
        Stat(R.drawable.ic_stat_solved, R.string.stat_solved, solved)
        Stat(R.drawable.ic_stat_hint_free, R.string.stat_hint_free, hintFree)
        Stat(R.drawable.ic_stat_streak, R.string.stat_streak, streak)
        Stat(R.drawable.ic_stat_best, R.string.stat_best, best)
    }
}

@Composable
private fun Stat(@DrawableRes icon: Int, @StringRes label: Int, value: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = stringResource(label), Modifier.size(16.dp))
        Text("$value", style = MaterialTheme.typography.bodyMedium)
    }
}
