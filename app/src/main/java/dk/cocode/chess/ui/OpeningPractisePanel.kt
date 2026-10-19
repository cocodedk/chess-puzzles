package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.viewmodel.OpeningFeedback
import dk.cocode.chess.viewmodel.OpeningUiState
import dk.cocode.chess.viewmodel.Texts
import dk.cocode.chess.viewmodel.practisePrompt

/** Practise mode: the drill prompt, the verdict on the last move, and a Start again button. */
@Composable
internal fun OpeningPractisePanel(state: OpeningUiState, opening: Opening, onAgain: () -> Unit) {
    val texts = rememberTexts()
    Text(texts.practisePrompt(opening.side), modifier = TextInset, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text(
        texts.practiseFeedback(state.feedback, state.clean, state.bookSan),
        modifier = TextInset.announces(state.announcement), textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = onAgain, modifier = CompactButton, contentPadding = CompactPadding) { Text(stringResource(R.string.practise_again)) }
}

/** [bookSan] names the wrong move's book reply ("Nf3"); a fallback covers the case (never expected
 * in practice) where WRONG feedback arrives without one. */
internal fun Texts.practiseFeedback(
    feedback: OpeningFeedback,
    clean: Boolean,
    bookSan: String?,
): String = when (feedback) {
    OpeningFeedback.NONE -> ""
    OpeningFeedback.CORRECT -> string(R.string.practise_book_move)
    OpeningFeedback.WRONG -> bookSan?.let { string(R.string.practise_not_book, it) }
        ?: string(R.string.practise_not_book_fallback)
    OpeningFeedback.COMPLETE -> string(if (clean) R.string.practise_complete_clean else R.string.practise_complete)
}
