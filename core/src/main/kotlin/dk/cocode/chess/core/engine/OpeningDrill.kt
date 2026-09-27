package dk.cocode.chess.core.engine

import dk.cocode.chess.core.model.BoardView
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.util.Uci
import dk.cocode.chess.core.util.toUci
import kotlin.random.Random

/** Result of [OpeningDrill.submit]. */
sealed interface DrillResult {
    /** Book move played; the opponent's [reply] follows and the line continues. */
    data class Correct(val playerMove: MoveStep, val reply: MoveStep) : DrillResult

    /** Book move played and the line ends here — right after it ([reply] null) or after [reply]. */
    data class Complete(val playerMove: MoveStep, val reply: MoveStep?, val clean: Boolean) : DrillResult

    /** A legal move that isn't the book move. Not applied; the position is unchanged. */
    data class Wrong(val book: MoveStep) : DrillResult

    /** Not a legal chess move (or a promotion piece was required but missing). */
    data object Illegal : DrillResult
}

/**
 * Practice state machine over an [OpeningTree]: the player supplies [side]'s book moves from
 * memory, and the opponent's replies — plus, when [side] is BLACK, its opening move — are chosen at
 * random among the book's branches via [random].
 */
class OpeningDrill(
    private val tree: OpeningTree,
    private val side: PieceColor,
    private val random: Random,
) {
    private lateinit var engine: ChessEngine
    private lateinit var node: OpeningTree.Node
    private var lastMoveStep: MoveStep? = null

    val board: BoardView get() = engine.boardView()
    val lastMove: MoveStep? get() = lastMoveStep
    var clean: Boolean = true
        private set
    val complete: Boolean get() = node.children.isEmpty()

    init {
        restart()
    }

    fun legalDestinations(from: Square): List<Square> =
        if (complete) emptyList() else engine.legalDestinations(from)

    fun bookMove(): MoveStep {
        check(!complete) { "Opening line is already complete" }
        return Uci.toMoveStep(node.children.keys.single())
    }

    fun submit(intent: MoveIntent): DrillResult {
        if (complete || !engine.isLegal(intent)) return DrillResult.Illegal
        val book = bookMove()
        if (intent.toUci() != book.uci) {
            clean = false
            return DrillResult.Wrong(book)
        }
        advance(book.uci)
        if (complete) return DrillResult.Complete(book, reply = null, clean = clean)
        val reply = advance(randomChildUci())
        return if (complete) DrillResult.Complete(book, reply, clean) else DrillResult.Correct(book, reply)
    }

    /** Back to the start, with [clean] reset — re-choosing White's first move again if [side] is BLACK. */
    fun restart() {
        engine = ChessEngine()
        node = tree.root
        lastMoveStep = null
        clean = true
        if (side == PieceColor.BLACK) advance(randomChildUci())
    }

    private fun randomChildUci(): String = node.children.keys.random(random)

    private fun advance(uci: String): MoveStep {
        engine.applyUci(uci)
        node = node.children.getValue(uci)
        val step = Uci.toMoveStep(uci)
        lastMoveStep = step
        return step
    }
}
