package dk.cocode.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.MoveIntent
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
    private val texts: Texts,
    private val today: () -> Long = { localEpochDay() },
) : ViewModel() {

    private var index = 0
    private var session = PuzzleSession.start(puzzles.all()[0])
    private val accounting = SolveAccounting()
    private var attempt = PuzzleAttempt() // what the current puzzle has cost so far
    private var resumed = false
    private var base = Progress()
    private val lists = PuzzleLists(puzzles.all())
    private var theme: PuzzleTheme? = null // session only: every start shows All
    private val solved = mutableSetOf<Int>() // puzzles solved since the app started, where a theme starts you

    val availableDifficulties: List<Difficulty> = lists.difficulties
    val availableThemes: List<PuzzleTheme> = lists.themes

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
        _state.update { it.withHint(texts, hint, session.legalDestinations(hint.from).toSet()) }
    }

    fun onReset() {
        session.reset() // accounting keeps its counted flags, so re-solving today never re-earns
        _state.value = render().copy(announcement = texts.resetSentence(texts.promptText(session.prompt())))
    }

    fun onNext() = stepBand(1)
    fun onPrevious() = stepBand(-1)
    fun onSharePosition(sharer: PositionSharer) = sharer.share(session.currentFen()) // the board now, FEN only

    private fun stepBand(by: Int) = band().let { if (it.size > 1) jumpTo(it.stepFrom(index, by)) }

    /** Jump to the first puzzle of the chosen band (within the theme, the first unsolved), unless that band is already showing. */
    fun onDifficultySelected(difficulty: Difficulty) {
        if (difficulty == difficultyOf(session.puzzle.rating)) return
        lists.start(difficulty, theme, solved)?.let { jumpTo(it) }
    }

    /**
     * Show only the puzzles carrying [choice], or all of them for null. A theme starts at its first puzzle not
     * solved yet; All stays on the puzzle on screen. Browsing a theme never writes the saved place (see [jumpTo]).
     */
    fun onThemeSelected(choice: PuzzleTheme?) {
        if (choice == theme || (choice != null && choice !in availableThemes)) return
        val target = if (choice == null) index else lists.start(difficultyOf(session.puzzle.rating), choice, solved) ?: return
        theme = choice
        if (target == index) relist() else jumpTo(target)
        _state.update { it.copy(announcement = "${texts.themeChosenSentence(choice, it.bandSize)} ${it.announcement}") }
    }

    /** The puzzle on screen stays; only its place in the list changes. */
    private fun relist() = band().let { list ->
        resumed = true
        _state.update { it.copy(theme = theme, position = list.indexOf(index) + 1, bandSize = list.size).withShownAnnouncement() }
    }

    private fun loadPuzzleAt(target: Int) {
        index = target
        attempt = PuzzleAttempt()
        session = PuzzleSession.start(puzzles.all()[target])
        _state.value = render().withShownAnnouncement()
    }

    private fun announceShown() = _state.update { it.withShownAnnouncement() }

    private fun PuzzleUiState.withShownAnnouncement() = copy(announcement = texts.shownSentence(session, position, bandSize))

    /** The full render recipe — the single place the clock is sampled for display. */
    private fun render() =
        band().let { session.toUiState(base, today(), it.indexOf(index) + 1, it.size).copy(theme = theme) }

    private fun band() = lists.of(difficultyOf(session.puzzle.rating), theme) // indices, bundle order

    private fun jumpTo(target: Int) {
        resumed = true // a deliberate jump cancels the one-time resume to the saved index
        // Skipping a puzzle you failed and never solved breaks the streak (reads the OLD `index` —
        // the fail belongs to the puzzle being left; setIndex below persists the destination).
        if (attempt.failed && accounting.countFail(today(), index)) viewModelScope.launch { progress.recordFailed() }
        loadPuzzleAt(target)
        if (theme == null) viewModelScope.launch { progress.setIndex(index) } // a theme never moves the saved place
    }

    private fun select(square: Square) {
        val targets = session.legalDestinations(square).toSet()
        if (targets.isEmpty()) clearSelection(texts.noMovesSentence(square))
        else _state.update { it.withSelection(texts, square, targets) }
    }

    private fun clearSelection(said: String = texts.selectionCleared()) = _state.update { it.withoutSelection(said) }

    private fun submit(from: Square, to: Square, promotion: PieceType?) {
        when (val result = session.submitMove(MoveIntent(from, to, promotion))) {
            // The only illegal move the UI can submit is a pawn reaching the last rank without a
            // promotion piece chosen yet, so surface the promotion picker.
            is SubmitResult.Illegal -> _state.update {
                it.copy(pendingPromotion = PendingPromotion(from, to), selected = null, legalTargets = emptySet())
            }
            is SubmitResult.Wrong -> onWrong(result.san)
            is SubmitResult.Continues -> onContinues(result)
            is SubmitResult.Solved -> onSolved(result)
        }
    }

    private fun onWrong(san: String) {
        attempt.failed = true // mistakes alone never break the streak — see jumpTo
        session.retry() // un-lock so the player can try again (the move was never applied)
        _state.update { it.afterWrong(texts, san, session.prompt()) }
    }

    private fun onContinues(result: SubmitResult.Continues) {
        val reply = session.applyOpponentReply()
        _state.update { it.afterReply(texts, session, result, reply) }
    }

    private fun onSolved(result: SubmitResult.Solved) {
        attempt.failed = false // solved after all — the earlier mistakes are forgiven
        val day = today() // sampled at the solve, not when the write coroutine runs
        val hintFree = !attempt.hintUsed // read now: loading a puzzle swaps `attempt` before the write runs
        solved += index
        if (accounting.countSolve(day, index)) viewModelScope.launch { progress.recordSolved(day, hintFree) }
        _state.update { it.afterSolved(texts, session, result) }
    }
}
