package dk.cocode.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.PieceColor
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

/** Drives the puzzle screen: maps board gestures to [PuzzleSession] calls and exposes [PuzzleUiState]. */
class PuzzleViewModel(
    private val puzzles: PuzzleRepository,
    private val progress: ProgressRepository,
    startIndex: Int = 0,
) : ViewModel() {

    private var index = startIndex.coerceIn(0, puzzles.count() - 1)
    private var session = PuzzleSession.start(puzzles.all()[index])
    private var solved = 0
    private var streak = 0
    private var best = 0

    private val _state = MutableStateFlow(freshState())
    val state: StateFlow<PuzzleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = progress.load()
            solved = saved.solvedCount
            streak = saved.currentStreak
            best = saved.bestStreak
            _state.update { it.copy(solvedCount = solved, currentStreak = streak, bestStreak = best) }
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
        session.reset()
        _state.value = freshState().copy(solvedCount = solved, currentStreak = streak, bestStreak = best)
    }

    fun onNext() {
        index = if (index + 1 < puzzles.count()) index + 1 else 0
        session = PuzzleSession.start(puzzles.all()[index])
        _state.value = freshState().copy(solvedCount = solved, currentStreak = streak, bestStreak = best)
    }

    private fun select(square: Square) {
        val targets = session.legalDestinations(square).toSet()
        if (targets.isEmpty()) clearSelection()
        else _state.update { it.copy(selected = square, legalTargets = targets, hint = null) }
    }

    private fun clearSelection() = _state.update { it.copy(selected = null, legalTargets = emptySet()) }

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
        streak = 0
        persist()
        _state.update {
            it.copy(
                status = PuzzleStatus.FAILED, feedback = Feedback.WRONG,
                selected = null, legalTargets = emptySet(), hint = null, currentStreak = 0,
                promptText = "Not the right move — Reset or Next",
            )
        }
    }

    private fun onContinues() {
        val reply = session.applyOpponentReply()
        _state.update {
            it.copy(
                board = session.state.board.toRows(), lastMove = Highlight(reply.from, reply.to),
                selected = null, legalTargets = emptySet(), hint = null,
                feedback = Feedback.CORRECT, promptText = promptFor(session.playerColor),
            )
        }
    }

    private fun onSolved(playerMove: MoveStep) {
        solved += 1
        streak += 1
        best = maxOf(best, streak)
        persist()
        _state.update {
            it.copy(
                board = session.state.board.toRows(), lastMove = Highlight(playerMove.from, playerMove.to),
                selected = null, legalTargets = emptySet(), hint = null,
                status = PuzzleStatus.SOLVED, feedback = Feedback.SOLVED,
                solvedCount = solved, currentStreak = streak, bestStreak = best, promptText = "Solved!",
            )
        }
    }

    private fun persist() {
        val snapshot = Progress(solved, streak, best)
        viewModelScope.launch { progress.save(snapshot) }
    }

    private fun freshState(): PuzzleUiState {
        val snapshot = session.state
        return PuzzleUiState(
            board = snapshot.board.toRows(),
            flipped = session.playerColor == PieceColor.BLACK,
            lastMove = Highlight(snapshot.lastMove.from, snapshot.lastMove.to),
            status = snapshot.status,
            rating = session.puzzle.rating,
            solvedCount = solved, currentStreak = streak, bestStreak = best,
            promptText = promptFor(session.playerColor),
        )
    }

    private fun promptFor(color: PieceColor): String =
        (if (color == PieceColor.WHITE) "White" else "Black") + " to move — find the best move"
}
