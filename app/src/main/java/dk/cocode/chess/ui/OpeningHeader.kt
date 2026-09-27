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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState

/** Back arrow, the opening's name/ECO/clean count, and the Learn | Practise switch. */
@Composable
internal fun OpeningHeader(
    state: OpeningUiState,
    opening: Opening,
    onBack: () -> Unit,
    onMode: (OpeningMode) -> Unit,
    stacked: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back" }) { Text("←") }
        Text(opening.name, style = MaterialTheme.typography.titleLarge)
    }
    val clean = state.cleanRuns[opening.id] ?: 0
    Text(opening.eco + if (clean > 0) "  ×$clean clean" else "", style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(8.dp))
    ChipRow(stacked, OpeningMode.entries, selected = { it == state.mode }, onSelect = onMode, label = ::modeLabel)
}

internal fun modeLabel(mode: OpeningMode): String = when (mode) {
    OpeningMode.LEARN -> "Learn"
    OpeningMode.PRACTISE -> "Practise"
}
