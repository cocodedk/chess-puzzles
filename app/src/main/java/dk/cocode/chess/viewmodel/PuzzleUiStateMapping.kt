package dk.cocode.chess.viewmodel

import dk.cocode.chess.R
import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.data.Progress
import dk.cocode.chess.data.dayStreakAsOf

/** Builds the rendered [PuzzleUiState] from the session snapshot and persisted [base] progress. */
internal fun PuzzleSession.toUiState(base: Progress, today: Long, position: Int, bandSize: Int): PuzzleUiState {
    val snapshot = state
    return PuzzleUiState(
        board = snapshot.board.toRows(),
        flipped = playerColor == PieceColor.BLACK,
        lastMove = Highlight(snapshot.lastMove.from, snapshot.lastMove.to),
        status = snapshot.status,
        rating = puzzle.rating,
        position = position,
        bandSize = bandSize,
        prompt = prompt(),
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

/** Who moves and the goal, the mate's count going down as the solution progresses. */
internal fun PuzzleSession.prompt(): Prompt =
    Prompt(playerColor, goalOf(endsInMate, puzzle), state.totalPlayerMoves - state.playerMovesDone)

/** What is said when a puzzle is shown: its place, rating, the opponent's first move, then the prompt. */
internal fun Texts.shownSentence(session: PuzzleSession, position: Int, bandSize: Int): String = string(
    R.string.sentence_puzzle_shown, position, bandSize, session.puzzle.rating,
    side(session.playerColor.opposite()), spokenMove(session.setupSan), promptText(session.prompt()),
)
