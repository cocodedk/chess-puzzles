package dk.cocode.chess.viewmodel

import androidx.annotation.StringRes
import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square

/** The board's FEN letter at [square], or ' ' when it is empty. */
private fun BoardState.codeAt(square: Square): Char = board[square.rank][square.file]

/** The piece on the occupied [square] in words with no colour, e.g. "knight". */
private fun Texts.pieceWordAt(state: BoardState, square: Square): String =
    spokenPiece(PieceType.fromLetter(state.codeAt(square))!!)

/**
 * What a screen reader says for one board square: "e4, white knight, last move". The square, its
 * contents (or "empty"), then any of selected, legal move, last move and hint, in that order.
 */
fun Texts.squareDescription(state: BoardState, square: Square): String {
    val code = state.codeAt(square)
    val contents = when {
        code == ' ' -> string(R.string.spoken_empty)
        code.isUpperCase() -> string(R.string.spoken_white_piece, pieceWordAt(state, square))
        else -> string(R.string.spoken_black_piece, pieceWordAt(state, square))
    }
    val states = listOfNotNull(
        stateWord(square == state.selected, R.string.spoken_selected),
        stateWord(square in state.legalTargets, R.string.spoken_legal_move),
        stateWord(state.lastMove.touches(square), R.string.spoken_last_move),
        stateWord(state.hint.touches(square), R.string.spoken_hint),
    )
    return (listOf(square.toAlgebraic(), contents) + states).joinToString(", ")
}

private fun Texts.stateWord(applies: Boolean, @StringRes id: Int): String? = if (applies) string(id) else null

private fun Highlight?.touches(square: Square) = this != null && (square == from || square == to)

/** "Knight g1 selected. Moves: f3, h3." — the targets in the order a1, b1 … h8. */
internal fun Texts.selectionSentence(state: BoardState, square: Square, targets: Set<Square>): String = string(
    R.string.sentence_selected,
    pieceWordAt(state, square).replaceFirstChar(Char::uppercase),
    square.toAlgebraic(),
    targets.sortedBy { it.index }.joinToString(", ") { it.toAlgebraic() },
)

/** "Hint: knight g1 to f3." */
internal fun Texts.hintSentence(state: BoardState, hint: Highlight): String =
    string(R.string.sentence_hint, pieceWordAt(state, hint.from), hint.from.toAlgebraic(), hint.to.toAlgebraic())
