package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R

/**
 * "Type a move" opens a field for a move written as text, "e2e4" or "Nf3", which the keyboard's voice typing can
 * dictate as well as its keys; the same button reads "Close move field" while it is open. Play, or the keyboard's
 * action key, hands the text to [onMove] and empties the field. The field takes English letters and numbers only
 * (see [englishOnly]) and says so. Nothing is saved: it starts shut, and is [enabled] only while the puzzle is in progress.
 */
@Composable
internal fun TypeMove(enabled: Boolean, onMove: (String) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val play = { if (text.isNotBlank()) { onMove(text); text = "" } }
    TextButton(onClick = { open = !open }) {
        Text(stringResource(if (open) R.string.puzzle_type_move_close else R.string.puzzle_type_move))
    }
    if (open) {
        LaunchedEffect(Unit) { focus.requestFocus() } // the keyboard comes up with its microphone key at hand
        Row(TextInset.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.Top) {
            OutlinedTextField(
                value = text, onValueChange = { text = englishOnly(it) }, enabled = enabled, singleLine = true,
                label = { Text(stringResource(R.string.puzzle_type_move_label)) },
                supportingText = { Text(stringResource(R.string.puzzle_type_move_help)) },
                // Latin keys, no autocorrect and no capitals: a keyboard that "fixes" Nf3 would change the move.
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { play() }),
                modifier = Modifier.weight(1f).focusRequester(focus),
            )
            Button(
                onClick = { play() }, modifier = Modifier.padding(top = 8.dp), // level with the box, not its help line
                enabled = enabled && text.isNotBlank(), contentPadding = CompactPadding,
            ) {
                Text(stringResource(R.string.puzzle_type_move_play))
            }
        }
    }
}

/** What the field takes: English letters and numbers, and the space, - and = that a move may carry. */
internal fun englishOnly(text: String): String =
    text.filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it in " -=" }
