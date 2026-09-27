package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Square

/** What a tap on a board square resolves to, given the current selection. */
sealed interface Tap {
    /** Select [square] as the new selection (nothing was selected, or an unrelated square was tapped). */
    data class Select(val square: Square) : Tap

    /** Deselect: the currently selected square was tapped again. */
    data object Clear : Tap

    /** A legal move: the tapped square is a legal target of the current selection. */
    data class Move(val from: Square, val to: Square) : Tap
}

/**
 * The tap rule shared by the puzzle and openings screens: tapping a legal target moves there,
 * tapping the current selection clears it, and any other tap selects the tapped square.
 */
fun resolveTap(selected: Square?, targets: Set<Square>, square: Square): Tap = when {
    selected == null -> Tap.Select(square)
    square == selected -> Tap.Clear
    square in targets -> Tap.Move(selected, square)
    else -> Tap.Select(square)
}
