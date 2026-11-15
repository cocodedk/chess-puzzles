package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.board.PromotionDialog
import dk.cocode.chess.util.AndroidPositionSharer
import dk.cocode.chess.viewmodel.Difficulty
import dk.cocode.chess.viewmodel.Feedback
import dk.cocode.chess.viewmodel.PuzzleTheme
import dk.cocode.chess.viewmodel.PuzzleUiState
import dk.cocode.chess.viewmodel.PuzzleViewModel
import dk.cocode.chess.viewmodel.Texts

@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    tabs: @Composable () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val sharer = remember(context) { AndroidPositionSharer(context) }
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
    } else {
        PuzzleScreenContent(
            state = state,
            themeMode = themeMode,
            onThemeToggle = onThemeToggle,
            onSquareTap = viewModel::onSquareTapped,
            onDragStart = viewModel::onDragStart,
            onDragEnd = viewModel::onDragEnd,
            onHint = viewModel::onHint,
            onReset = viewModel::onReset,
            onNext = viewModel::onNext,
            onPrevious = viewModel::onPrevious,
            onPromotion = viewModel::onPromotionChosen,
            onPromotionCancel = viewModel::onPromotionCancelled,
            onAbout = { showAbout = true },
            onSharePosition = { viewModel.onSharePosition(sharer) },
            onMoveTyped = viewModel::onMoveTyped,
            onDifficulty = viewModel::onDifficultySelected,
            difficulties = viewModel.availableDifficulties,
            onTheme = viewModel::onThemeSelected,
            themes = viewModel.availableThemes,
            tabs = tabs,
        )
    }
}

@Composable
fun PuzzleScreenContent(
    state: PuzzleUiState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    onHint: () -> Unit,
    onReset: () -> Unit,
    onNext: () -> Unit,
    onPromotion: (PieceType) -> Unit,
    onPromotionCancel: () -> Unit,
    onPrevious: () -> Unit = {},
    onAbout: () -> Unit = {},
    onSharePosition: () -> Unit = {},
    onMoveTyped: (String) -> Unit = {},
    onDifficulty: (Difficulty) -> Unit = {},
    difficulties: List<Difficulty> = Difficulty.entries,
    onTheme: (PuzzleTheme?) -> Unit = {},
    themes: List<PuzzleTheme> = emptyList(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeToggle: () -> Unit = {},
    tabs: @Composable () -> Unit = {},
    // What pads the screen (the bars, the cutout and the keyboard), and the keyboard's height beyond the bars and the
    // cutout, which the board's layout counts back as the screen's: both read as it is measured (see BoardScaffold).
    insets: WindowInsets = WindowInsets.safeDrawing,
    keyboard: WindowInsets = WindowInsets.ime.exclude(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
) {
    // Kept here, not in the field: each layout composes the field at its own place, which would lose them. The typed
    // move keeps its caret with it, so a field made anew carries on where the last one was.
    var moveFieldOpen by rememberSaveable { mutableStateOf(false) }
    var typedMove by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    // `stacked` is true only when a slot has a side panel to itself. Beside a board between two panels the settings sit
    // under the stats and the prompt above the buttons, balancing the two panels; when both slots share one column the
    // rows read top to bottom: stats, prompt, (board), buttons, settings (Share among them). While a move is typed
    // beside the board the body is alone in its panel, so stacked too, and shows only the verdict and the field.
    BoardScaffold(
        state = state,
        onSquareTap = onSquareTap, onDragStart = onDragStart, onDragEnd = onDragEnd,
        tabs = tabs,
        header = { stacked ->
            PuzzleStats(state, difficulties, onDifficulty, themes, onTheme, stacked)
            if (stacked) PuzzleSettings(themeMode, onThemeToggle, onAbout) else PuzzlePrompt(state)
        },
        body = { stacked, typing ->
            // The verdict and the field each at one place in the composition, whatever the layout: as it flips
            // while typing, the field keeps its focus, and the keyboard, and the verdict is still the live region.
            if (stacked && !typing) {
                PuzzlePrompt(state)
                Spacer(Modifier.height(16.dp))
            }
            PuzzleControls(state, onHint, onReset, onPrevious, onNext, stacked, showButtons = !typing)
            if (stacked && !typing) SharePositionButton(onSharePosition) // not with Theme and About: that panel is full
            // Typed in a panel of its own beside the board, the field leaves out its help: the strip is too short for it.
            TypeMove(
                moveFieldOpen, { moveFieldOpen = !moveFieldOpen }, { typedMove }, { typedMove = it },
                state.status == PuzzleStatus.IN_PROGRESS, onMoveTyped, showHelp = !(stacked && typing),
            )
            if (!stacked && !typing) PuzzleSettings(themeMode, onThemeToggle, onAbout, onSharePosition)
        },
        fieldOpen = moveFieldOpen, insets = insets, keyboard = keyboard,
    )
    state.pendingPromotion?.let {
        PromotionDialog(onSelect = onPromotion, onDismiss = onPromotionCancel)
    }
}

/** The theme in use now, "Theme: Light": what a screen reader says as the theme button's state. */
internal fun Texts.themeLabel(mode: ThemeMode): String = string(
    when (mode) {
        ThemeMode.SYSTEM -> R.string.theme_auto
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    },
)

/** The theme button's text: what a tap does, which is to switch to the mode after [mode]. */
internal fun Texts.themeSwitchLabel(mode: ThemeMode): String = string(
    when (mode.next()) {
        ThemeMode.SYSTEM -> R.string.theme_to_auto
        ThemeMode.LIGHT -> R.string.theme_to_light
        ThemeMode.DARK -> R.string.theme_to_dark
    },
)

internal fun Texts.feedbackMessage(feedback: Feedback): String = when (feedback) {
    Feedback.NONE -> ""
    Feedback.CORRECT -> string(R.string.feedback_correct)
    Feedback.SOLVED -> string(R.string.feedback_solved)
    Feedback.WRONG -> string(R.string.feedback_wrong)
    Feedback.ILLEGAL -> string(R.string.feedback_illegal)
}

internal fun Texts.bandLabel(band: Difficulty): String = string(
    when (band) {
        Difficulty.EASY -> R.string.band_easy
        Difficulty.MEDIUM -> R.string.band_medium
        Difficulty.HARD -> R.string.band_hard
    },
)
