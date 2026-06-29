package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.data.Progress

/** Builds the rendered [PuzzleUiState] from the session snapshot and persisted [base] progress. */
internal fun PuzzleSession.toUiState(base: Progress): PuzzleUiState {
    val snapshot = state
    return PuzzleUiState(
        board = snapshot.board.toRows(),
        flipped = playerColor == PieceColor.BLACK,
        lastMove = Highlight(snapshot.lastMove.from, snapshot.lastMove.to),
        status = snapshot.status,
        rating = puzzle.rating,
        solvedCount = base.solvedCount,
        currentStreak = base.currentStreak,
        bestStreak = base.bestStreak,
        promptText = promptFor(playerColor),
    )
}

internal fun promptFor(color: PieceColor): String =
    (if (color == PieceColor.WHITE) "White" else "Black") + " to move — find the best move"
