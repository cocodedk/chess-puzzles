package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Square

/**
 * What [dk.cocode.chess.ui.board.ChessBoard] needs to draw a position: the pieces, whether it is
 * shown from Black's side, the current selection and its legal targets, and the last-move/hint
 * highlights. Shared by the puzzle screen ([PuzzleUiState]) and the openings screen, so the board
 * and its draw helpers depend on this rather than on either screen's full state.
 */
interface BoardState {
    val board: List<String>
    val flipped: Boolean
    val selected: Square?
    val legalTargets: Set<Square>
    val lastMove: Highlight?
    val hint: Highlight?

    /** Whether a tap on a square does anything; a screen reader is only offered a double-tap when it does. */
    val tappable: Boolean
}
