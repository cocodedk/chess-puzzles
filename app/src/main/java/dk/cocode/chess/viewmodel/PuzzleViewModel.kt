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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the puzzle screen. Progress is the persisted source of truth (collected from [progress]);
 * solves/fails record atomically and are counted at most once per puzzle. Requires a non-empty
 * [puzzles] (the caller guarantees this).
 */
class PuzzleViewModel(
    private val puzzles: PuzzleRepository,
    private val progress: ProgressRepository,
) : ViewModel() {

    private var index = 0
    private var session = PuzzleSession.start(puzzles.all()[0])
    private val counted = mutableSetOf<Int>()
    private var resumed = false
    private var base = Progress()
    private val bands: Map<Difficulty, List<Int>> =
        puzzles.all().withIndex().groupBy({ difficultyOf(it.value.rating) }, { it.index })

    /** Only the bands that actually contain puzzles, so the UI never offers a dead difficulty. */
    val availableDifficulties: List<Difficulty> = Difficulty.entries.filter { bands[it]?.isNotEmpty() == true }

    private val _state = MutableStateFlow(session.toUiState(base))
    val state: StateFlow<PuzzleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progress.progress.collect { saved ->
                base = saved
                if (!resumed) {
                    resumed = true
                    if (saved.index in 1 until puzzles.count()) loadPuzzleAt(saved.index)
                }
                _state.update {
                    it.copy(
                        solvedCount = saved.solvedCount,
                        currentStreak = saved.currentStreak,
                        bestStreak = saved.bestStreak,
                    )
                }
            }
        }
    }

    fun onSquareTapped(square: Square) {
        val current = _state.value
        if (current.status != PuzzleStatus.IN_PROGRESS) return
        when {
            current.selected == null -> select(square)
            square == current.selected -> clearSelection()
            square in current.legalTargets -> attempt(current.selected, square, null)
            else -> select(square)
        }
    }

    fun onDragStart(square: Square) {
        if (_state.value.status == PuzzleStatus.IN_PROGRESS) select(square)
    }

    fun onDragEnd(target: Square) {
        val from = _state.value.selected ?: return
        if (target in _state.value.legalTargets) attempt(from, target, null) else clearSelection()
    }

    fun onPromotionChosen(type: PieceType) {
        val pending = _state.value.pendingPromotion ?: return
        _state.update { it.copy(pendingPromotion = null) }
        attempt(pending.from, pending.to, type)
    }

    fun onPromotionCancelled() {
        _state.update { it.copy(pendingPromotion = null) }
        clearSelection()
    }

    fun onHint() {
        if (_state.value.status != PuzzleStatus.IN_PROGRESS) return
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
        session.reset() // a reset puzzle keeps its `counted` flag, so re-solving never re-earns progress
        _state.value = session.toUiState(base)
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
        session = PuzzleSession.start(puzzles.all()[target])
        _state.value = session.toUiState(base)
    }

    private fun jumpTo(target: Int) {
        resumed = true // a deliberate jump cancels the one-time resume to the saved index
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

    private fun attempt(from: Square, to: Square, promotion: PieceType?) {
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
        recordOnce { progress.recordFailed() } // an error breaks the streak, counted once
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
        recordOnce { progress.recordSolved() }
        _state.update {
            it.copy(
                board = session.state.board.toRows(), lastMove = Highlight(playerMove.from, playerMove.to),
                selected = null, legalTargets = emptySet(), hint = null,
                status = PuzzleStatus.SOLVED, feedback = Feedback.SOLVED, promptText = "Solved!",
            )
        }
    }

    private fun recordOnce(action: suspend () -> Unit) {
        if (counted.add(index)) viewModelScope.launch { action() } // each puzzle is counted once
    }
}
