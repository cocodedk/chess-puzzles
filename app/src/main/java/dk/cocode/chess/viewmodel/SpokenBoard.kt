package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Square

/** The board's FEN letter at [square], or ' ' when it is empty. */
private fun BoardState.codeAt(square: Square): Char = board[square.rank][square.file]

/** The piece on the occupied [square] in words with no colour, e.g. "knight". */
private fun BoardState.pieceWordAt(square: Square): String = spokenPiece(PieceType.fromLetter(codeAt(square))!!)

/**
 * What a screen reader says for one board square: "e4, white knight, last move". The square, its
 * contents (or "empty"), then any of selected, legal move, last move and hint, in that order.
 */
fun squareDescription(state: BoardState, square: Square): String {
    val code = state.codeAt(square)
    val contents = if (code == ' ') "empty" else "${colourOf(code)} ${state.pieceWordAt(square)}"
    val states = listOfNotNull(
        "selected".takeIf { square == state.selected },
        "legal move".takeIf { square in state.legalTargets },
        "last move".takeIf { state.lastMove.touches(square) },
        "hint".takeIf { state.hint.touches(square) },
    )
    return (listOf(square.toAlgebraic(), contents) + states).joinToString(", ")
}

private fun colourOf(code: Char) = (if (code.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK).label().lowercase()

private fun Highlight?.touches(square: Square) = this != null && (square == from || square == to)

/** "Knight g1 selected. Moves: f3, h3." — the targets in the order a1, b1 … h8. */
internal fun selectionSentence(state: BoardState, square: Square, targets: Set<Square>): String =
    "${state.pieceWordAt(square).replaceFirstChar(Char::uppercase)} ${square.toAlgebraic()} selected. Moves: " +
        targets.sortedBy { it.index }.joinToString(", ") { it.toAlgebraic() } + "."

/** "Hint: knight g1 to f3." */
internal fun hintSentence(state: BoardState, hint: Highlight): String =
    "Hint: ${state.pieceWordAt(hint.from)} ${hint.from.toAlgebraic()} to ${hint.to.toAlgebraic()}."
