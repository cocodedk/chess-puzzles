package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.PieceColor

/** An opening and the tree merged from its lines — always opened and closed together. */
internal data class OpeningSession(val opening: Opening, val tree: OpeningTree)

/**
 * The one render recipe: the list when nothing is open, else the open opening in its current mode.
 * [learnLine] and [learnPly] say which move Learn shows; [practise] is the live drill, if any.
 */
internal fun renderOpening(
    session: OpeningSession?,
    mode: OpeningMode,
    learnLine: Int,
    learnPly: Int,
    practise: OpeningPractise?,
    cleanRuns: Map<String, Int>,
): OpeningUiState {
    val (op, t) = session ?: return OpeningUiState(cleanRuns = cleanRuns)
    val base = OpeningUiState(
        flipped = op.side == PieceColor.BLACK, openingId = op.id, lineNames = t.lineNames, cleanRuns = cleanRuns,
    )
    if (mode == OpeningMode.PRACTISE && practise != null) {
        return base.copy(
            board = practise.board.toRows(), mode = OpeningMode.PRACTISE, selected = practise.selected,
            legalTargets = practise.legalTargets, lastMove = practise.lastMove, hint = practise.hint,
            feedback = practise.feedback, clean = practise.clean, bookSan = practise.bookSan,
        )
    }
    val plies = t.plies(learnLine)
    val shown = plies.getOrNull(learnPly - 1) // null at ply 0: the start position, nothing played yet
    return base.copy(
        board = (shown?.board ?: t.start).toRows(), line = learnLine, ply = learnPly,
        moveSan = plies.map { it.san }, lastMove = shown?.move?.let { Highlight(it.from, it.to) },
    )
}
