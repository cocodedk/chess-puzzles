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
import java.text.Normalizer

/**
 * "Type a move" opens a field for a move written as text, "e2e4" or "Nf3", which the keyboard's voice typing can
 * dictate as well as its keys; the same button reads "Close move field" while it is open. Play, or the keyboard's
 * action key, hands the text to [onMove] and empties the field. The field takes English letters, and numbers in any
 * script (see [englishOnly]), and says so. Nothing is saved: it starts shut, and is [enabled] only while the puzzle is in progress.
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

/**
 * What the field keeps: English letters and the space, - and = a move may carry, and numbers written in any script.
 * Full-width letters and digits (ｅ２), the digits of other scripts (Persian ۲, Arabic ٢) and the Chinese 一 to 八 become
 * the English ones first, so a keyboard that is not Latin can still give the numbers; its letters are dropped.
 */
internal fun englishOnly(text: String): String = buildString {
    for (c in Normalizer.normalize(text, Normalizer.Form.NFKC)) {
        val kept = englishDigit(c) ?: c
        if (kept in 'a'..'z' || kept in 'A'..'Z' || kept in '0'..'9' || kept in " -=") append(kept)
    }
}

/** The English digit [c] stands for in any script, or the Chinese 一 to 八, else null. */
private fun englishDigit(c: Char): Char? = when {
    c.isDigit() -> '0' + Character.digit(c, 10)
    c in CHINESE_ONE_TO_EIGHT -> '1' + CHINESE_ONE_TO_EIGHT.indexOf(c)
    else -> null
}

private const val CHINESE_ONE_TO_EIGHT = "\u4E00\u4E8C\u4E09\u56DB\u4E94\u516D\u4E03\u516B"
