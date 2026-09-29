package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square

// The sentences a screen reader says on the puzzle screen, and the state changes that carry them.
// Every move is spoken with spokenMove.

internal const val SELECTION_CLEARED = "Selection cleared."

internal fun noMovesSentence(square: Square) = "No moves from ${square.toAlgebraic()}."

internal fun wrongSentence(san: String) = "You played ${spokenMove(san)}. Not the best move — try again."

internal fun correctSentence(san: String, replySide: PieceColor, replySan: String) =
    "Correct. You played ${spokenMove(san)}. ${replySide.label()} played ${spokenMove(replySan)}."

internal fun resetSentence(prompt: String) = "Puzzle reset. $prompt."

internal fun solvedSentence(san: String) = "Solved. You played ${spokenMove(san)}."

internal fun PuzzleUiState.withSelection(square: Square, targets: Set<Square>) =
    copy(selected = square, legalTargets = targets, hint = null, announcement = selectionSentence(this, square, targets))

/** Drops the selection, its targets and the hint, and says [said]. */
internal fun PuzzleUiState.withoutSelection(said: String) =
    copy(selected = null, legalTargets = emptySet(), hint = null, announcement = said)
