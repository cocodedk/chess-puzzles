package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.viewmodel.OpeningFeedback
import dk.cocode.chess.viewmodel.OpeningUiState

/** Practise mode: the drill prompt, the verdict on the last move, and an Again button. */
@Composable
internal fun OpeningPractisePanel(state: OpeningUiState, opening: Opening, onAgain: () -> Unit) {
    val prompt =
        if (opening.side == PieceColor.WHITE) "Play White's moves from memory." else "Play Black's moves from memory."
    Text(prompt, modifier = TextInset, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text(practiseFeedback(state.feedback, state.clean), modifier = TextInset, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = onAgain, modifier = CompactButton, contentPadding = CompactPadding) { Text("Again") }
}

internal fun practiseFeedback(feedback: OpeningFeedback, clean: Boolean): String = when (feedback) {
    OpeningFeedback.NONE -> ""
    OpeningFeedback.CORRECT -> "Book move ✓"
    OpeningFeedback.WRONG -> "Not the book move — play the highlighted one"
    OpeningFeedback.COMPLETE -> if (clean) "Line complete ✓" else "Line complete — try it clean"
}
