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
import dk.cocode.chess.viewmodel.PuzzleUiState

/**
 * The counters above the board. Each is an icon plus its number — five labelled stats would not fit
 * on one line on a small phone. The icon's content description is what a screen reader announces.
 */
@Composable
fun StatsRow(state: PuzzleUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Stat(R.drawable.ic_stat_day, R.string.stat_day, state.dayStreak)
        Stat(R.drawable.ic_stat_solved, R.string.stat_solved, state.solvedCount)
        Stat(R.drawable.ic_stat_hint_free, R.string.stat_hint_free, state.hintFreeCount)
        Stat(R.drawable.ic_stat_streak, R.string.stat_streak, state.currentStreak)
        Stat(R.drawable.ic_stat_best, R.string.stat_best, state.bestStreak)
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
