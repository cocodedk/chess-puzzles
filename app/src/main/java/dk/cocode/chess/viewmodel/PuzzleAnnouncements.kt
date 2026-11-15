package dk.cocode.chess.viewmodel

import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square

// The sentences a screen reader says on the puzzle screen, and the state changes that carry them.
// Every move is spoken with spokenMove.

internal fun Texts.selectionCleared() = string(R.string.sentence_selection_cleared)

internal fun Texts.noMovesSentence(square: Square) = string(R.string.sentence_no_moves, square.toAlgebraic())

internal fun Texts.typedMoveIllegalSentence(text: String) = string(R.string.sentence_typed_move_illegal, text.trim())

internal fun Texts.wrongSentence(san: String) = string(R.string.sentence_wrong, spokenMove(san))

internal fun Texts.correctSentence(san: String, replySide: PieceColor, replySan: String) =
    string(R.string.sentence_correct, spokenMove(san), side(replySide), spokenMove(replySan))

internal fun Texts.resetSentence(prompt: String) = string(R.string.sentence_reset, prompt)

internal fun Texts.solvedSentence(san: String) = string(R.string.sentence_solved, spokenMove(san))

internal fun PuzzleUiState.withSelection(texts: Texts, square: Square, targets: Set<Square>) = copy(
    selected = square, legalTargets = targets, hint = null, feedback = feedback.afterAnEvent(),
    announcement = texts.selectionSentence(this, square, targets),
)

/** Drops the selection, its targets, the hint and an outdated verdict on typed text, and says [said]. */
internal fun PuzzleUiState.withoutSelection(said: String) =
    copy(selected = null, legalTargets = emptySet(), hint = null, feedback = feedback.afterAnEvent(), announcement = said)

/**
 * Typed text that names no move leaves nothing on the board for its verdict to stand beside, so the next thing the
 * player does, a hint or a tap, outdates it: the line must not say it to the eye while a screen reader says the event.
 */
internal fun Feedback.afterAnEvent() = if (this == Feedback.ILLEGAL) Feedback.NONE else this
