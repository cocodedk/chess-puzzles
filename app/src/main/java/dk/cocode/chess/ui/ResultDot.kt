package dk.cocode.chess.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.viewmodel.PuzzleMark

/**
 * The puzzle's latest result, before its title: a green dot with a tick when solved, a red one with a cross when
 * got wrong, an empty ring when not solved yet. The mark reads without colour, the dot grows with the phone's text
 * size, and it stays silent: the title's heading says the result in words ([PuzzleMark.word]).
 */
@Composable
internal fun ResultDot(mark: PuzzleMark) {
    val colors = MaterialTheme.colorScheme
    val dot = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleMedium.fontSize.toDp() })
    when (mark) {
        PuzzleMark.UNSOLVED -> Box(dot.border(2.dp, colors.outline, CircleShape))
        PuzzleMark.SOLVED -> MarkedDot(R.drawable.ic_stat_solved, colors.tertiary, colors.onTertiary, dot)
        PuzzleMark.WRONG -> MarkedDot(R.drawable.ic_mark_wrong, colors.error, colors.onError, dot)
    }
}

@Composable
private fun MarkedDot(@DrawableRes icon: Int, fill: Color, ink: Color, modifier: Modifier = Modifier) =
    Icon(painterResource(icon), null, modifier.background(fill, CircleShape).padding(2.dp), ink)
