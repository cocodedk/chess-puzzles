package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Puzzle
import dk.cocode.chess.data.Progress
import dk.cocode.chess.data.dayStreakAsOf

/** Builds the rendered [PuzzleUiState] from the session snapshot and persisted [base] progress. */
internal fun PuzzleSession.toUiState(base: Progress, today: Long): PuzzleUiState {
    val snapshot = state
    return PuzzleUiState(
        board = snapshot.board.toRows(),
        flipped = playerColor == PieceColor.BLACK,
        lastMove = Highlight(snapshot.lastMove.from, snapshot.lastMove.to),
        status = snapshot.status,
        rating = puzzle.rating,
        promptText = prompt(),
    ).withProgress(base, today)
}

/** Copies the persisted counters into the rendered state; the daily run lapses if [today] is late. */
internal fun PuzzleUiState.withProgress(base: Progress, today: Long): PuzzleUiState = copy(
    solvedCount = base.solvedCount,
    hintFreeCount = base.hintFreeCount,
    currentStreak = base.currentStreak,
    bestStreak = base.bestStreak,
    dayStreak = base.dayStreakAsOf(today),
)

/** E.g. "White to move — checkmate in 2", counting down as the solution progresses. */
internal fun PuzzleSession.prompt(): String {
    val side = if (playerColor == PieceColor.WHITE) "White" else "Black"
    val remaining = state.totalPlayerMoves - state.playerMovesDone
    return "$side to move — ${goalText(endsInMate, remaining, puzzle)}"
}

/**
 * The puzzle's announced goal. Mate is detected from the actual solution (more reliable than tags);
 * other goals come from the Lichess goal-class tags, defense before attack because defensiveMove
 * can co-occur with crushing/advantage. Motif tags (fork, pin, …) are never announced — they would
 * spoil the solution.
 */
internal fun goalText(endsInMate: Boolean, remainingMoves: Int, puzzle: Puzzle): String = when {
    endsInMate -> "checkmate in $remainingMoves"
    puzzle.hasTheme("defensiveMove") -> "find the best defense"
    puzzle.hasTheme("equality") -> "save the game"
    puzzle.hasTheme("crushing") -> "win material"
    puzzle.hasTheme("advantage") -> "gain the upper hand"
    else -> "find the best move"
}
