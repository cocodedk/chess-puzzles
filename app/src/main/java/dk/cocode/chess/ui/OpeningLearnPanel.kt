package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.viewmodel.OpeningUiState

/** Learn mode: the idea, branch chips (when there is more than one line), ◀ ▶ stepping and the move list. */
@Composable
internal fun OpeningLearnPanel(
    state: OpeningUiState,
    opening: Opening,
    onLine: (Int) -> Unit,
    onStep: (Int) -> Unit,
    stacked: Boolean,
) {
    Text(opening.idea, modifier = TextInset, textAlign = TextAlign.Center)
    if (state.lineNames.size > 1) {
        Spacer(Modifier.height(8.dp))
        Lineup(stacked) { item ->
            state.lineNames.forEachIndexed { index, name ->
                FilterChip(
                    selected = index == state.line,
                    onClick = { onLine(index) },
                    label = { Text(name, item, textAlign = TextAlign.Center) },
                    modifier = item,
                    colors = selectedChipColors(),
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
            onClick = { onStep(-1) }, enabled = state.ply > 0,
            modifier = Modifier.semantics { contentDescription = "Previous move" },
        ) { Text("◀") }
        IconButton(
            onClick = { onStep(1) }, enabled = state.ply < state.moveSan.size,
            modifier = Modifier.semantics { contentDescription = "Next move" },
        ) { Text("▶") }
    }
    Spacer(Modifier.height(8.dp))
    Text(moveListText(state.moveSan, state.ply), modifier = TextInset, textAlign = TextAlign.Center)
}

/** "1.e4 e5 2.Nf3 …" with the current ply's move bold; only White's moves carry the move number. */
internal fun moveListText(sans: List<String>, currentPly: Int): AnnotatedString = buildAnnotatedString {
    sans.forEachIndexed { i, san ->
        if (i > 0) append(' ')
        if (i % 2 == 0) append("${i / 2 + 1}.")
        val bold = i + 1 == currentPly
        if (bold) pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(san)
        if (bold) pop()
    }
}
