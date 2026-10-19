package dk.cocode.chess.viewmodel

import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square

// The sentences a screen reader says on the puzzle screen, and the state changes that carry them.
// Every move is spoken with spokenMove.

internal fun Texts.selectionCleared() = string(R.string.sentence_selection_cleared)

internal fun Texts.noMovesSentence(square: Square) = string(R.string.sentence_no_moves, square.toAlgebraic())

internal fun Texts.wrongSentence(san: String) = string(R.string.sentence_wrong, spokenMove(san))

internal fun Texts.correctSentence(san: String, replySide: PieceColor, replySan: String) =
    string(R.string.sentence_correct, spokenMove(san), side(replySide), spokenMove(replySan))

internal fun Texts.resetSentence(prompt: String) = string(R.string.sentence_reset, prompt)

internal fun Texts.solvedSentence(san: String) = string(R.string.sentence_solved, spokenMove(san))

internal fun PuzzleUiState.withSelection(texts: Texts, square: Square, targets: Set<Square>) = copy(
    selected = square, legalTargets = targets, hint = null,
    announcement = texts.selectionSentence(this, square, targets),
)

/** Drops the selection, its targets and the hint, and says [said]. */
internal fun PuzzleUiState.withoutSelection(said: String) =
    copy(selected = null, legalTargets = emptySet(), hint = null, announcement = said)
