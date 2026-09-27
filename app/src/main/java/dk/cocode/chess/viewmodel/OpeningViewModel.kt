package dk.cocode.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.cocode.chess.core.engine.OpeningDrill
import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.OpeningProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Drives the openings screens. [onOpen] switches from the list into Learn mode for one opening;
 * [onMode] toggles Learn/Practise, starting a fresh [OpeningDrill] every time Practise begins;
 * [onLine]/[onStep] step through a line in Learn; the tap/drag handlers drive Practise via
 * [OpeningPractise]. [progress] is the source of truth for [OpeningUiState.cleanRuns].
 */
class OpeningViewModel(
    private val openings: List<Opening>,
    private val progress: OpeningProgressRepository,
    private val random: Random = Random.Default,
) : ViewModel() {

    /** An opening and the tree merged from its lines — always opened and closed together. */
    private data class Session(val opening: Opening, val tree: OpeningTree)

    private var session: Session? = null
    private var mode = OpeningMode.LEARN
    private var learnLine = 0
    private var learnPly = 0
    private var practise: OpeningPractise? = null

    private val _state = MutableStateFlow(OpeningUiState())
    val state: StateFlow<OpeningUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progress.cleanRuns.collect { runs -> _state.update { it.copy(cleanRuns = runs) } }
        }
    }

    fun onOpen(id: String) {
        val opening = openings.first { it.id == id }
        session = Session(opening, OpeningTree(opening))
        mode = OpeningMode.LEARN
        learnLine = 0
        learnPly = 0
        practise = null
        _state.value = render()
    }

    fun onBack() {
        session = null
        practise = null
        _state.value = render()
    }

    fun onMode(newMode: OpeningMode) {
        if (newMode == mode) return
        mode = newMode
        if (newMode == OpeningMode.PRACTISE) practise = startDrill()
        _state.value = render()
    }

    private fun startDrill(): OpeningPractise? {
        val s = session ?: return null
        return OpeningPractise(OpeningDrill(s.tree, s.opening.side, random))
    }

    fun onLine(index: Int) {
        learnLine = index
        learnPly = 0
        _state.value = render()
    }

    fun onStep(delta: Int) {
        val max = session?.tree?.plies(learnLine)?.size ?: 0
        learnPly = (learnPly + delta).coerceIn(0, max)
        _state.value = render()
    }

    fun onSquareTapped(square: Square) {
        val p = practise ?: return
        if (p.complete) return
        when (val tap = resolveTap(p.selected, p.legalTargets, square)) {
            is Tap.Select -> p.select(square)
            is Tap.Clear -> p.clear()
            is Tap.Move -> recordIfClean(p.submit(tap.from, tap.to))
        }
        _state.value = render()
    }

    fun onDragStart(square: Square) {
        val p = practise ?: return
        if (!p.complete) p.select(square)
    }

    fun onDragEnd(target: Square) {
        val p = practise ?: return
        val from = p.selected ?: return
        if (target in p.legalTargets) recordIfClean(p.submit(from, target)) else p.clear()
        _state.value = render()
    }

    fun onAgain() {
        practise?.restart()
        _state.value = render()
    }

    private fun recordIfClean(shouldRecord: Boolean) {
        val id = session?.opening?.id ?: return
        if (shouldRecord) viewModelScope.launch { progress.recordCleanRun(id) }
    }

    /** The one render recipe: the list when nothing is open, else the open opening in its current mode. */
    private fun render(): OpeningUiState {
        val (op, t) = session ?: return OpeningUiState(cleanRuns = _state.value.cleanRuns)
        val cleanRuns = _state.value.cleanRuns
        val base = OpeningUiState(
            flipped = op.side == PieceColor.BLACK, openingId = op.id, lineNames = t.lineNames, cleanRuns = cleanRuns,
        )
        val p = practise
        if (mode == OpeningMode.PRACTISE && p != null) {
            return base.copy(
                board = p.board.toRows(), mode = OpeningMode.PRACTISE, selected = p.selected,
                legalTargets = p.legalTargets, lastMove = p.lastMove, hint = p.hint,
                feedback = p.feedback, clean = p.clean,
            )
        }
        val plies = t.plies(learnLine)
        val shown = plies.getOrNull(learnPly - 1) // null at ply 0: the start position, nothing played yet
        return base.copy(
            board = (shown?.board ?: t.start).toRows(), line = learnLine, ply = learnPly,
            moveSan = plies.map { it.san }, lastMove = shown?.move?.let { Highlight(it.from, it.to) },
        )
    }
}
