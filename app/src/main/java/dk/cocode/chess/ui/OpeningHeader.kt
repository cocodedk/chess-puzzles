package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState
import dk.cocode.chess.viewmodel.Texts
import dk.cocode.chess.viewmodel.ecoLine

/** Back arrow, the opening's name/ECO/clean count, and the Learn | Practise switch. */
@Composable
internal fun OpeningHeader(
    state: OpeningUiState,
    opening: Opening,
    onBack: () -> Unit,
    onMode: (OpeningMode) -> Unit,
    stacked: Boolean,
) {
    val texts = rememberTexts()
    val back = stringResource(R.string.opening_back)
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = back }) {
            Text("←", Modifier.clearAndSetSemantics {}) // "Back", not "left arrow"
        }
        Text(opening.name, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
    }
    val clean = state.cleanRuns[opening.id] ?: 0
    Text(texts.ecoLine(opening.eco, clean), style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(8.dp))
    ChipRow(
        stacked, OpeningMode.entries, selected = { it == state.mode }, onSelect = onMode,
        label = { texts.modeLabel(it) },
    )
}

internal fun Texts.modeLabel(mode: OpeningMode): String = string(
    when (mode) {
        OpeningMode.LEARN -> R.string.mode_learn
        OpeningMode.PRACTISE -> R.string.mode_practise
    },
)
