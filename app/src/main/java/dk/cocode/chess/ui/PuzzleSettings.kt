package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.data.ThemeMode

/**
 * The theme toggle, the Share position button when [onShare] is given, and the About link; in a narrow
 * panel each moves to its own line whole, never split.
 */
@OptIn(ExperimentalLayoutApi::class) // FlowRow is still marked experimental (checked up to foundation 1.11)
@Composable
internal fun PuzzleSettings(
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    onAbout: () -> Unit,
    onShare: (() -> Unit)? = null,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        val texts = rememberTexts()
        val inUse = texts.themeLabel(themeMode)
        TextButton(onClick = onThemeToggle, modifier = Modifier.semantics { stateDescription = inUse }) {
            Text(texts.themeSwitchLabel(themeMode))
        }
        onShare?.let { SharePositionButton(it) }
        TextButton(onClick = onAbout) { Text(stringResource(R.string.about)) }
    }
}

/** Opens the share sheet with the position on the board; its text is what a screen reader says, too. */
@Composable
internal fun SharePositionButton(onShare: () -> Unit) {
    TextButton(onClick = onShare) { Text(stringResource(R.string.position_share)) }
}
