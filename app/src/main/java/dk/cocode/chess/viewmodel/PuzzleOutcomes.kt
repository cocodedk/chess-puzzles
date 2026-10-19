package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.Hint
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.model.SubmitResult

// What the screen shows, and a screen reader says, after a hint or a move the player submits.

/** The hint's piece is selected, with its [targets], and the hint is highlighted. */
internal fun PuzzleUiState.withHint(texts: Texts, hint: Hint, targets: Set<Square>): PuzzleUiState {
    val highlight = Highlight(hint.from, hint.to)
    return copy(selected = hint.from, legalTargets = targets, hint = highlight, announcement = texts.hintSentence(this, highlight))
}

/** A wrong move was never applied, so the puzzle stays in progress and asks [prompt] again. */
internal fun PuzzleUiState.afterWrong(texts: Texts, san: String, prompt: Prompt) =
    withoutSelection(texts.wrongSentence(san))
        .copy(status = PuzzleStatus.IN_PROGRESS, feedback = Feedback.WRONG, prompt = prompt)

/** A correct move the opponent has just answered with [reply]; [session] already shows both. */
internal fun PuzzleUiState.afterReply(texts: Texts, session: PuzzleSession, result: SubmitResult.Continues, reply: MoveStep) =
    withoutSelection(texts.correctSentence(result.san, session.playerColor.opposite(), result.replySan)).copy(
        board = session.state.board.toRows(), lastMove = Highlight(reply.from, reply.to),
        feedback = Feedback.CORRECT, prompt = session.prompt(),
    )

/** The solving move, now on the board of [session]. */
internal fun PuzzleUiState.afterSolved(texts: Texts, session: PuzzleSession, result: SubmitResult.Solved) =
    withoutSelection(texts.solvedSentence(result.san)).copy(
        board = session.state.board.toRows(), lastMove = Highlight(result.playerMove.from, result.playerMove.to),
        status = PuzzleStatus.SOLVED, feedback = Feedback.SOLVED,
    )
