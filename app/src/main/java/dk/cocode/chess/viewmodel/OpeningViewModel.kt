package dk.cocode.chess.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.cocode.chess.core.engine.OpeningDrill
import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.OpeningProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** [SavedStateHandle] keys the open opening and its mode are recorded under, so a fresh process
 * can reopen where the player left off. Internal so the saved-state test can plant/read them. */
internal const val OPENING_ID_KEY = "opening_id"
internal const val OPENING_MODE_KEY = "opening_mode"

/**
 * Drives the openings screens. [onOpen] switches from the list into Learn mode for one opening;
 * [onMode] toggles Learn/Practise, starting a fresh [OpeningDrill] every time Practise begins;
 * [onLine]/[onStep] step through a line in Learn; the tap/drag handlers drive Practise via
 * [OpeningPractise]. [progress] is the source of truth for [OpeningUiState.cleanRuns].
 * [savedStateHandle] records the open opening and mode, and construction reopens them — a restored
 * Practise drill simply starts fresh rather than replaying moves.
 */
class OpeningViewModel(
    private val openings: List<Opening>,
    private val progress: OpeningProgressRepository,
    private val random: Random = Random.Default,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    private var session: OpeningSession? = null
    private var mode = OpeningMode.LEARN
    private var learnLine = 0
    private var learnPly = 0
    private var practise: OpeningPractise? = null
    private var cleanRuns: Map<String, Int> = emptyMap()

    private val _state = MutableStateFlow(OpeningUiState())
    val state: StateFlow<OpeningUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progress.cleanRuns.collect { runs ->
                cleanRuns = runs
                _state.update { it.copy(cleanRuns = runs) }
            }
        }
        restoreSession()
    }

    /** Reopens the opening [savedStateHandle] recorded before the process died, in its saved mode. */
    private fun restoreSession() {
        val id = savedStateHandle.get<String>(OPENING_ID_KEY)?.takeIf { saved -> openings.any { it.id == saved } } ?: return
        val savedMode = savedStateHandle.get<String>(OPENING_MODE_KEY) // read first: open records LEARN
        open(id)
        savedMode?.let { setMode(OpeningMode.valueOf(it)) }
        publish() // reopened where it was left: the screen is read as it stands, nothing is announced
    }

    fun onOpen(id: String) = publish(openedSentence(open(id).name))

    private fun open(id: String): Opening {
        val opening = openings.first { it.id == id }
        session = OpeningSession(opening, OpeningTree(opening))
        mode = OpeningMode.LEARN
        learnLine = 0
        learnPly = 0
        practise = null
        savedStateHandle[OPENING_ID_KEY] = id
        savedStateHandle[OPENING_MODE_KEY] = mode.name
        return opening
    }

    /** The screen was left (another tab, About): what was said is old news when it comes back. */
    fun onScreenLeft() = _state.update { it.copy(announcement = "") }

    fun onBack() {
        session = null
        practise = null
        savedStateHandle.remove<String>(OPENING_ID_KEY)
        publish()
    }

    fun onMode(newMode: OpeningMode) {
        if (newMode == mode) return
        setMode(newMode)
        publish(modeSentence())
    }

    private fun setMode(newMode: OpeningMode) {
        mode = newMode
        practise = if (newMode == OpeningMode.PRACTISE) startDrill() else null // Learn has no drill to tap
        savedStateHandle[OPENING_MODE_KEY] = mode.name
    }

    private fun startDrill(): OpeningPractise? {
        val s = session ?: return null
        return OpeningPractise(OpeningDrill(s.tree, s.opening.side, random))
    }

    fun onLine(index: Int) {
        learnLine = index
        learnPly = 0
        publish(session?.tree?.lineNames?.getOrNull(index)?.let(::lineChosenSentence).orEmpty())
    }

    fun onStep(delta: Int) {
        val max = session?.tree?.plies(learnLine)?.size ?: 0
        learnPly = (learnPly + delta).coerceIn(0, max)
        publish(learnSentence())
    }

    fun onSquareTapped(square: Square) {
        val p = practise ?: return
        if (p.complete) return
        when (val tap = resolveTap(p.selected, p.legalTargets, square)) {
            is Tap.Select -> select(p, square)
            is Tap.Clear -> { p.clear(); publish(SELECTION_CLEARED) }
            is Tap.Move -> submit(p, tap.from, tap.to)
        }
    }

    fun onDragStart(square: Square) {
        val p = practise ?: return
        if (p.complete) return
        select(p, square) // show the selection and its targets while the finger is down
    }

    fun onDragEnd(target: Square) {
        val p = practise ?: return
        if (p.complete) return
        val from = p.selected ?: return
        if (target in p.legalTargets) {
            submit(p, from, target)
        } else {
            p.clear()
            publish(SELECTION_CLEARED)
        }
    }

    fun onAgain() {
        val p = practise ?: return publish()
        p.restart()
        publish(practiseSentence(p.side, p.openingSan))
    }

    private fun select(p: OpeningPractise, square: Square) {
        p.select(square)
        val shown = render()
        val said = if (p.selected == null) noMovesSentence(square) else selectionSentence(shown, square, p.legalTargets)
        _state.value = shown.copy(announcement = said)
    }

    private fun submit(p: OpeningPractise, from: Square, to: Square) {
        recordIfClean(p.submit(from, to))
        publish(p.said)
    }

    /** What is said on choosing the current mode: Learn where it was left, or a fresh Practise drill. */
    private fun modeSentence(): String {
        val p = practise
        if (mode == OpeningMode.PRACTISE && p != null) return practiseSentence(p.side, p.openingSan)
        return learnChosenSentence(learnSentence())
    }

    /** The move Learn shows now, or the start position. */
    private fun learnSentence(): String {
        val san = session?.tree?.plies(learnLine)?.getOrNull(learnPly - 1)?.san ?: return START_POSITION
        return learnMoveSentence(learnPly, san)
    }

    private fun recordIfClean(shouldRecord: Boolean) {
        val id = session?.opening?.id ?: return
        if (shouldRecord) viewModelScope.launch { progress.recordCleanRun(id) }
    }

    /** Shows the current render, saying [said] (nothing by default) to a screen reader. */
    private fun publish(said: String = "") {
        _state.value = render().copy(announcement = said)
    }

    private fun render() = renderOpening(session, mode, learnLine, learnPly, practise, cleanRuns)
}
