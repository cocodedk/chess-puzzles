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
                    if (saved.index in 1 until puzzles.count()) loadPuzzleAt(saved.index) else announceShown()
                }
                _state.update { it.withProgress(saved, today()) }
            }
        }
    }

    fun onSquareTapped(square: Square) {
        val current = _state.value
        if (current.status != PuzzleStatus.IN_PROGRESS) return
        when (val tap = resolveTap(current.selected, current.legalTargets, square)) {
            is Tap.Select -> select(tap.square)
            is Tap.Clear -> clearSelection()
            is Tap.Move -> submit(tap.from, tap.to, null)
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
        val highlight = Highlight(hint.from, hint.to)
        _state.update {
            it.withSelection(hint.from, session.legalDestinations(hint.from).toSet())
                .copy(hint = highlight, announcement = hintSentence(it, highlight))
        }
    }

    fun onReset() {
        session.reset() // accounting keeps its counted flags, so re-solving today never re-earns
        _state.value = render().copy(announcement = "Puzzle reset. ${session.prompt()}.")
    }

    fun onNext() = stepBand(1)
    fun onPrevious() = stepBand(-1)

    private fun stepBand(by: Int) = band().let { if (it.size > 1) jumpTo(it.stepFrom(index, by)) }

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
        announceShown()
    }

    private fun announceShown() = _state.update { it.copy(announcement = session.shownSentence(it.position, it.bandSize)) }

    /** The full render recipe — the single place the clock is sampled for display. */
    private fun render() = band().let { session.toUiState(base, today(), it.indexOf(index) + 1, it.size) }

    private fun band() = bands.getValue(difficultyOf(session.puzzle.rating)) // indices, bundle order

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
        if (targets.isEmpty()) clearSelection(noMovesSentence(square))
        else _state.update { it.withSelection(square, targets) }
    }

    private fun clearSelection(said: String = SELECTION_CLEARED) = _state.update { it.withoutSelection(said) }

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
            it.withoutSelection(wrongSentence(session.lastSan))
                .copy(status = PuzzleStatus.IN_PROGRESS, feedback = Feedback.WRONG, promptText = session.prompt())
        }
    }

    private fun onContinues() {
        val played = session.lastSan
        val reply = session.applyOpponentReply()
        val said = correctSentence(played, session.playerColor.opposite(), session.lastSan)
        _state.update {
            it.withoutSelection(said).copy(
                board = session.state.board.toRows(), lastMove = Highlight(reply.from, reply.to),
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
            it.withoutSelection(solvedSentence(session.lastSan)).copy(
                board = session.state.board.toRows(), lastMove = Highlight(playerMove.from, playerMove.to),
                status = PuzzleStatus.SOLVED, feedback = Feedback.SOLVED, promptText = "Solved!",
            )
        }
    }
}
