package dk.cocode.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.model.SubmitResult
import dk.cocode.chess.data.Progress
import dk.cocode.chess.data.ProgressRepository
import dk.cocode.chess.util.localEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the puzzle screen. Progress is the persisted source of truth (collected from [progress]);
 * recording is gated by [SolveAccounting]: each solve counts once per day, and the streak breaks
 * only when a failed puzzle is skipped unsolved. Requires a non-empty [puzzles].
 */
class PuzzleViewModel(
    private val puzzles: PuzzleRepository,
    private val progress: ProgressRepository,
    private val today: () -> Long = { localEpochDay() },
) : ViewModel() {

    private var index = 0
    private var session = PuzzleSession.start(puzzles.all()[0])
    private val accounting = SolveAccounting()
    private var attempt = PuzzleAttempt() // what the current puzzle has cost so far
    private var resumed = false
    private var base = Progress()
    private val bands: Map<Difficulty, List<Int>> =
        puzzles.all().withIndex().groupBy({ difficultyOf(it.value.rating) }, { it.index })

    /** Only the bands that actually contain puzzles, so the UI never offers a dead difficulty. */
    val availableDifficulties: List<Difficulty> = Difficulty.entries.filter { bands[it]?.isNotEmpty() == true }

    private val _state = MutableStateFlow(render())
    val state: StateFlow<PuzzleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progress.progress.collect { saved ->
                base = saved
                if (!resumed) {
                    resumed = true
                    if (saved.index in 1 until puzzles.count()) loadPuzzleAt(saved.index)
                }
                _state.update { it.withProgress(saved, today()) }
            }
        }
    }

    fun onSquareTapped(square: Square) {
        val current = _state.value
        if (current.status != PuzzleStatus.IN_PROGRESS) return
        when {
            current.selected == null -> select(square)
            square == current.selected -> clearSelection()
            square in current.legalTargets -> submit(current.selected, square, null)
            else -> select(square)
        }
    }

    fun onDragStart(square: Square) {
        if (_state.value.status == PuzzleStatus.IN_PROGRESS) select(square)
    }

    fun onDragEnd(target: Square) {
        val from = _state.value.selected ?: return
        if (target in _state.value.legalTargets) submit(from, target, null) else clearSelection()
    }

    fun onPromotionChosen(type: PieceType) {
        val pending = _state.value.pendingPromotion ?: return
        _state.update { it.copy(pendingPromotion = null) }
        submit(pending.from, pending.to, type)
    }

    fun onPromotionCancelled() {
        _state.update { it.copy(pendingPromotion = null) }
        clearSelection()
    }

    fun onHint() {
        if (_state.value.status != PuzzleStatus.IN_PROGRESS) return
        attempt.hintUsed = true
        val hint = session.hint()
        _state.update {
            it.copy(
                hint = Highlight(hint.from, hint.to),
                selected = hint.from,
                legalTargets = session.legalDestinations(hint.from).toSet(),
            )
        }
    }

    fun onReset() {
        session.reset() // accounting keeps its counted flags, so re-solving today never re-earns
        _state.value = render()
    }

    fun onNext() {
        val band = bands.getValue(difficultyOf(session.puzzle.rating))
        if (band.size > 1) jumpTo(band[(band.indexOf(index) + 1) % band.size])
    }

    /** Jump to the first puzzle of the chosen band, unless that band is already showing. */
    fun onDifficultySelected(difficulty: Difficulty) {
        if (difficulty == difficultyOf(session.puzzle.rating)) return
        bands[difficulty]?.firstOrNull()?.let { jumpTo(it) }
    }

    private fun loadPuzzleAt(target: Int) {
        index = target
        attempt = PuzzleAttempt()
        session = PuzzleSession.start(puzzles.all()[target])
        _state.value = render()
    }

    /** The full render recipe — the single place the clock is sampled for display. */
    private fun render() = session.toUiState(base, today())

    private fun jumpTo(target: Int) {
        resumed = true // a deliberate jump cancels the one-time resume to the saved index
        // Skipping a puzzle you failed and never solved breaks the streak (reads the OLD `index` —
        // the fail belongs to the puzzle being left; setIndex below persists the destination).
        if (attempt.failed && accounting.countFail(today(), index)) viewModelScope.launch { progress.recordFailed() }
        loadPuzzleAt(target)
        viewModelScope.launch { progress.setIndex(index) }
    }

    private fun select(square: Square) {
        val targets = session.legalDestinations(square).toSet()
        if (targets.isEmpty()) clearSelection()
        else _state.update { it.copy(selected = square, legalTargets = targets, hint = null) }
    }

    private fun clearSelection() =
        _state.update { it.copy(selected = null, legalTargets = emptySet(), hint = null) }

    private fun submit(from: Square, to: Square, promotion: PieceType?) {
        when (val result = session.submitMove(MoveIntent(from, to, promotion))) {
            // The only illegal move the UI can submit is a pawn reaching the last rank without a
            // promotion piece chosen yet, so surface the promotion picker.
            is SubmitResult.Illegal -> _state.update {
                it.copy(pendingPromotion = PendingPromotion(from, to), selected = null, legalTargets = emptySet())
            }
            is SubmitResult.Wrong -> onWrong()
            is SubmitResult.Continues -> onContinues()
            is SubmitResult.Solved -> onSolved(result.playerMove)
        }
    }

    private fun onWrong() {
        attempt.failed = true // mistakes alone never break the streak — see jumpTo
        session.retry() // un-lock so the player can try again (the move was never applied)
        _state.update {
            it.copy(
                status = PuzzleStatus.IN_PROGRESS, feedback = Feedback.WRONG,
                selected = null, legalTargets = emptySet(), hint = null,
                promptText = session.prompt(),
            )
        }
    }

    private fun onContinues() {
        val reply = session.applyOpponentReply()
        _state.update {
            it.copy(
                board = session.state.board.toRows(), lastMove = Highlight(reply.from, reply.to),
                selected = null, legalTargets = emptySet(), hint = null,
                feedback = Feedback.CORRECT, promptText = session.prompt(),
            )
        }
    }

    private fun onSolved(playerMove: MoveStep) {
        attempt.failed = false // solved after all — the earlier mistakes are forgiven
        val day = today() // sampled at the solve, not when the write coroutine runs
        val hintFree = !attempt.hintUsed // read now: loading a puzzle swaps `attempt` before the write runs
        if (accounting.countSolve(day, index)) viewModelScope.launch { progress.recordSolved(day, hintFree) }
        _state.update {
            it.copy(
                board = session.state.board.toRows(), lastMove = Highlight(playerMove.from, playerMove.to),
                selected = null, legalTargets = emptySet(), hint = null,
                status = PuzzleStatus.SOLVED, feedback = Feedback.SOLVED, promptText = "Solved!",
            )
        }
    }
}
