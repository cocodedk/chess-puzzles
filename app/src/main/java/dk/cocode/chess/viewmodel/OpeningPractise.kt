package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.engine.DrillResult
import dk.cocode.chess.core.engine.OpeningDrill
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.Square

/**
 * One practise drill's live selection, hint and feedback, kept apart from [OpeningViewModel] so its
 * board-tap handling doesn't crowd the Learn-mode navigation. [submit] returns whether this move just
 * completed the line cleanly for the very first time — [OpeningDrill] refuses any further submit once
 * [complete], so that can only ever be true once per drill (until [restart]).
 */
internal class OpeningPractise(private val drill: OpeningDrill) {
    var selected: Square? = null; private set
    var legalTargets: Set<Square> = emptySet(); private set
    var hint: Highlight? = null; private set
    var feedback: OpeningFeedback = OpeningFeedback.NONE; private set
    var bookSan: String? = null; private set

    val board get() = drill.board
    val lastMove get() = drill.lastMove?.let { Highlight(it.from, it.to) }
    val clean get() = drill.clean
    val complete get() = drill.complete

    fun select(square: Square) {
        val targets = drill.legalDestinations(square).toSet()
        if (targets.isEmpty()) {
            clear()
        } else {
            selected = square
            legalTargets = targets
            hint = null
        }
    }

    fun clear() {
        selected = null
        legalTargets = emptySet()
        hint = null
    }

    fun submit(from: Square, to: Square): Boolean {
        clear()
        return when (val result = drill.submit(MoveIntent(from, to))) {
            is DrillResult.Correct -> {
                feedback = OpeningFeedback.CORRECT
                bookSan = null
                false
            }
            is DrillResult.Wrong -> {
                hint = Highlight(result.book.from, result.book.to)
                feedback = OpeningFeedback.WRONG
                bookSan = result.san
                false
            }
            is DrillResult.Complete -> {
                feedback = OpeningFeedback.COMPLETE
                bookSan = null
                result.clean
            }
            DrillResult.Illegal -> false
        }
    }

    fun restart() {
        drill.restart()
        feedback = OpeningFeedback.NONE
        bookSan = null
        clear()
    }
}
