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

    val availableDifficulties: List<Difficulty> = lists.difficulties
    val availableThemes: List<PuzzleTheme> = lists.themes

    private val _state = MutableStateFlow(render())
    val state: StateFlow<PuzzleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            progress.progress.collect { saved ->
                base = saved
                _state.update { it.withProgress(saved, today(), session.puzzle.id) } // first: the shown sentence says the mark
                if (!resumed) {
                    resumed = true
                    if (saved.index in 1 until puzzles.count()) loadPuzzleAt(saved.index) else _state.update { it.withShownAnnouncement() }
                }
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

    fun onDragStart(square: Square) { if (_state.value.status == PuzzleStatus.IN_PROGRESS) select(square) }

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

    /** A move written as text, say "e2e4" or "Nf3": played as a tap would play it, or refused out loud. */
    fun onMoveTyped(text: String) {
        if (_state.value.status != PuzzleStatus.IN_PROGRESS) return
        session.moveFromText(text)?.let { submit(it.from, it.to, it.promotion) }
            ?: _state.update { it.afterIllegalText(texts, text) }
    }

    fun onNext() = stepBand(1)
    fun onPrevious() = stepBand(-1)
    fun onSharePosition(sharer: PositionSharer) = sharer.share(session.currentFen()) // the board now, FEN only

    private fun stepBand(by: Int) = band().let { if (it.size > 1) jumpTo(it.stepFrom(index, by)) }

    /** Jump to the first puzzle of the chosen band (within the theme, the first unsolved), unless that band is already showing. */
    fun onDifficultySelected(difficulty: Difficulty) {
        if (difficulty == difficultyOf(session.puzzle.rating)) return
        lists.start(difficulty, theme, base.solvedIds)?.let { jumpTo(it) }
    }

    /**
     * Show only the puzzles carrying [choice], or all of them for null. A theme starts at its first puzzle not
     * saved as solved (one got wrong last time counts as not solved); All stays on the puzzle on screen.
     * Browsing a theme never writes the saved place (see [jumpTo]).
     */
    fun onThemeSelected(choice: PuzzleTheme?) {
        if (choice == theme || (choice != null && choice !in availableThemes)) return
        val target = if (choice == null) index else lists.start(difficultyOf(session.puzzle.rating), choice, base.solvedIds) ?: return
        theme = choice
        if (target == index) relist() else jumpTo(target)
        _state.update { it.copy(announcement = "${texts.themeChosenSentence(choice, it.bandSize)} ${it.announcement}") }
    }

    /** The puzzle on screen stays; only its place in the list changes. */
    private fun relist() = band().let { list ->
        resumed = true
        _state.update { it.relisted(theme, list.indexOf(index) + 1, list.size).withShownAnnouncement() }
    }

    private fun loadPuzzleAt(target: Int) {
        index = target
        attempt = PuzzleAttempt()
        session = PuzzleSession.start(puzzles.all()[target])
        _state.value = render().withShownAnnouncement()
    }

    private fun PuzzleUiState.withShownAnnouncement() = copy(announcement = texts.shownSentence(session, this))

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

    private fun select(square: Square) = _state.update { it.selecting(texts, square, session.legalDestinations(square).toSet()) }

    private fun clearSelection() = _state.update { it.withoutSelection(texts.selectionCleared()) }

    private fun saveResult(solve: Boolean) = session.puzzle.id.let { id -> viewModelScope.launch { progress.recordResult(id, solve) } }

    private fun submit(from: Square, to: Square, promotion: PieceType?) {
        when (val result = session.submitMove(MoveIntent(from, to, promotion))) {
            // The only illegal move the UI can submit is a pawn on the last rank with no piece chosen: ask which.
            is SubmitResult.Illegal -> _state.update { it.askingPromotion(from, to) }
            is SubmitResult.Wrong -> onWrong(result.san)
            is SubmitResult.Continues -> onContinues(result)
            is SubmitResult.Solved -> onSolved(result)
        }
    }

    private fun onWrong(san: String) {
        attempt.failed = true // mistakes alone never break the streak — see jumpTo
        saveResult(solve = false) // the dot before the title turns red
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
        saveResult(solve = true) // every solve, unlike the once-a-day count below: the latest result wins
        if (accounting.countSolve(day, index)) viewModelScope.launch { progress.recordSolved(day, hintFree) }
        _state.update { it.afterSolved(texts, session, result) }
    }
}
