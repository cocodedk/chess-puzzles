package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R

/**
 * "Type a move" opens a field for a move written as text, "e2e4" or "Nf3", which the keyboard's voice typing can
 * dictate as well as its keys. Play, or the keyboard's action key, hands the text to [onMove] and empties the field.
 * Nothing is saved: the field starts shut, and is [enabled] only while the puzzle is in progress.
 */
@Composable
internal fun TypeMove(enabled: Boolean, onMove: (String) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val play = { if (text.isNotBlank()) { onMove(text); text = "" } }
    TextButton(onClick = { open = !open }) { Text(stringResource(R.string.puzzle_type_move)) }
    if (open) {
        LaunchedEffect(Unit) { focus.requestFocus() } // the keyboard comes up with its microphone key at hand
        Row(TextInset.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
            OutlinedTextField(
                value = text, onValueChange = { text = it }, enabled = enabled, singleLine = true,
                label = { Text(stringResource(R.string.puzzle_type_move_label)) },
                // No autocorrect and no capitals: a keyboard that "fixes" Nf3 would change the move.
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { play() }),
                modifier = Modifier.weight(1f).focusRequester(focus),
            )
            Button(onClick = { play() }, enabled = enabled && text.isNotBlank(), contentPadding = CompactPadding) {
                Text(stringResource(R.string.puzzle_type_move_play))
            }
        }
    }
}
