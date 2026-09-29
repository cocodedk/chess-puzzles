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
    /** Book move played ([san] is its notation); the opponent's [reply] ([replySan]) follows and the line continues. */
    data class Correct(val playerMove: MoveStep, val reply: MoveStep, val san: String, val replySan: String) : DrillResult

    /** Book move played and the line ends here — right after it ([reply] and [replySan] null) or after [reply]. */
    data class Complete(
        val playerMove: MoveStep,
        val reply: MoveStep?,
        val clean: Boolean,
        val san: String,
        val replySan: String?,
    ) : DrillResult

    /** A legal move that isn't the book move. Not applied; the position is unchanged. [san] names
     * the book move ("Nf3") so the UI can tell the player what it was, not just highlight it;
     * [playedSan] names the move that was played instead. */
    data class Wrong(val book: MoveStep, val san: String, val playedSan: String) : DrillResult

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
    val side: PieceColor,
    private val random: Random,
) {
    private lateinit var engine: ChessEngine
    private lateinit var node: OpeningTree.Node
    private var lastMoveStep: MoveStep? = null

    val board: BoardView get() = engine.boardView()
    val lastMove: MoveStep? get() = lastMoveStep

    /** The notation of [lastMove], e.g. White's auto-played first move when [side] is BLACK. */
    val lastMoveSan: String? get() = if (lastMoveStep == null) null else node.san
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
        val san = sanOf(book.uci)
        if (intent.toUci() != book.uci) {
            clean = false
            return DrillResult.Wrong(book, san, engine.san(intent.toUci()))
        }
        advance(book.uci)
        if (complete) return DrillResult.Complete(book, reply = null, clean = clean, san = san, replySan = null)
        val reply = advance(randomChildUci())
        val replySan = node.san // advance moved `node` onto the reply
        return if (complete) {
            DrillResult.Complete(book, reply, clean, san, replySan)
        } else {
            DrillResult.Correct(book, reply, san, replySan)
        }
    }

    private fun sanOf(uci: String): String = node.children.getValue(uci).san

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
