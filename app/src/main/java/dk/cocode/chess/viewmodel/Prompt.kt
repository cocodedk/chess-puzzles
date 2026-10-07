package dk.cocode.chess.viewmodel

import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Puzzle
import dk.cocode.chess.core.model.PuzzleStatus

/** What a puzzle announces as its goal. Motif tags (fork, pin, …) never are: they would spoil the solution. */
enum class Goal { MATE, DEFENSE, SAVE, MATERIAL, UPPER_HAND, BEST_MOVE }

/**
 * What a puzzle asks of the player: who moves, and the [goal] — with [movesLeft] for a mate. It is kept
 * as data and put into words when it is shown, so it follows the language the screen is in.
 */
data class Prompt(val side: PieceColor, val goal: Goal, val movesLeft: Int)

/**
 * The puzzle's announced goal. Mate is detected from the actual solution (more reliable than tags);
 * other goals come from the Lichess goal-class tags, defense before attack because defensiveMove
 * can co-occur with crushing/advantage.
 */
internal fun goalOf(endsInMate: Boolean, puzzle: Puzzle): Goal = when {
    endsInMate -> Goal.MATE
    puzzle.hasTheme("defensiveMove") -> Goal.DEFENSE
    puzzle.hasTheme("equality") -> Goal.SAVE
    puzzle.hasTheme("crushing") -> Goal.MATERIAL
    puzzle.hasTheme("advantage") -> Goal.UPPER_HAND
    else -> Goal.BEST_MOVE
}

/** E.g. "White to move — checkmate in 2". */
internal fun Texts.promptText(prompt: Prompt): String = string(R.string.prompt_to_move, side(prompt.side), goalText(prompt))

private fun Texts.goalText(prompt: Prompt): String = when (prompt.goal) {
    Goal.MATE -> string(R.string.goal_mate, prompt.movesLeft)
    Goal.DEFENSE -> string(R.string.goal_defense)
    Goal.SAVE -> string(R.string.goal_save)
    Goal.MATERIAL -> string(R.string.goal_material)
    Goal.UPPER_HAND -> string(R.string.goal_upper_hand)
    Goal.BEST_MOVE -> string(R.string.goal_best_move)
}

/** The line under the puzzle's title: what it asks, or that it is solved. */
internal fun Texts.promptLine(state: PuzzleUiState): String = when {
    state.status == PuzzleStatus.SOLVED -> string(R.string.prompt_solved)
    else -> state.prompt?.let { promptText(it) }.orEmpty()
}
