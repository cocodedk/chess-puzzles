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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import java.text.Normalizer

/**
 * "Type a move" opens a field for a move written as text, "e2e4" or "Nf3", which the keyboard's voice typing can
 * dictate as well as its keys; the same button reads "Close move field" while it is open. Play, or the keyboard's
 * action key, hands the text to [onMove] and empties the field. The field takes English letters, and numbers in any
 * script (see [englishOnly]), and says so, unless [showHelp] is false. Nothing is stored: it starts shut and empty
 * whenever the puzzle screen is shown, and keeps whether it is [open] and its [text], caret included, across a
 * rotation, since the screen keeps both (each layout composes the field at its own place, which would lose them
 * here); [onText] hands the screen each change. [text] is read when it is needed, so Play hands over a change the
 * field has not yet been redrawn with. It is [enabled] only while the puzzle is in progress.
 */
@Composable
internal fun TypeMove(
    open: Boolean,
    onToggle: () -> Unit,
    text: () -> TextFieldValue,
    onText: (TextFieldValue) -> Unit,
    enabled: Boolean,
    onMove: (String) -> Unit,
    showHelp: Boolean,
) {
    val focus = remember { FocusRequester() }
    val play = {
        val move = text().text
        if (move.isNotBlank()) { onMove(move); onText(TextFieldValue()) }
    }
    TextButton(onClick = onToggle) {
        Text(stringResource(if (open) R.string.puzzle_type_move_close else R.string.puzzle_type_move))
    }
    if (open) {
        LaunchedEffect(Unit) { focus.requestFocus() } // the keyboard comes up with its microphone key at hand
        Row(TextInset.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.Top) {
            OutlinedTextField(
                value = text(), onValueChange = { onText(englishOnly(it)) }, enabled = enabled, singleLine = true,
                label = { Text(stringResource(R.string.puzzle_type_move_label)) },
                supportingText = if (showHelp) { { Text(stringResource(R.string.puzzle_type_move_help)) } } else null,
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
                enabled = enabled && text().text.isNotBlank(), contentPadding = CompactPadding,
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

/**
 * [value] as the field keeps it ([englishOnly]), its caret or selection between the same characters: a dropped letter
 * does not push it past the next one. Unchanged, it keeps the keyboard's word being composed too. One limit: a caret
 * between a letter and an accent the keyboard sent on its own can land one place off, as the two are normalised
 * together in the whole text but apart in the part before the caret.
 */
internal fun englishOnly(value: TextFieldValue): TextFieldValue {
    val text = englishOnly(value.text)
    if (text == value.text) return value
    fun kept(index: Int) = englishOnly(value.text.take(index)).length.coerceAtMost(text.length)
    return TextFieldValue(text, TextRange(kept(value.selection.start), kept(value.selection.end)))
}

/** The English digit [c] stands for in any script, or the Chinese 一 to 八, else null. */
private fun englishDigit(c: Char): Char? = when {
    c.isDigit() -> '0' + Character.digit(c, 10)
    c in CHINESE_ONE_TO_EIGHT -> '1' + CHINESE_ONE_TO_EIGHT.indexOf(c)
    else -> null
}

private const val CHINESE_ONE_TO_EIGHT = "\u4E00\u4E8C\u4E09\u56DB\u4E94\u516D\u4E03\u516B"
