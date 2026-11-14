package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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
    // The keyboard's height above the navigation bar it covers: zero until the first insets arrive, and read only
    // when the board's layout is measured (BoardScaffold), where the height it leaves is known too.
    val keyboardInsets = WindowInsets.ime.exclude(WindowInsets.navigationBars)
    val density = LocalDensity.current
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
            keyboardHeight = { with(density) { keyboardInsets.getBottom(this).toDp() } },
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
    keyboardHeight: () -> Dp = { 0.dp }, // see PuzzleScreen; no keyboard unless given
) {
    var moveFieldOpen by rememberSaveable { mutableStateOf(false) }
    val typeMove: @Composable () -> Unit = {
        TypeMove(moveFieldOpen, { moveFieldOpen = !moveFieldOpen }, state.status == PuzzleStatus.IN_PROGRESS, onMoveTyped)
    }
    // `stacked` is true only when each slot has a side panel to itself. Then the settings sit under the
    // stats and the prompt above the buttons, balancing the two panels; when both slots share one
    // column the rows read top to bottom: stats, prompt, (board), buttons, settings (Share among them).
    BoardScaffold(
        state = state,
        onSquareTap = onSquareTap, onDragStart = onDragStart, onDragEnd = onDragEnd,
        tabs = tabs,
        header = { stacked ->
            PuzzleStats(state, difficulties, onDifficulty, themes, onTheme, stacked)
            if (stacked) PuzzleSettings(themeMode, onThemeToggle, onAbout) else PuzzlePrompt(state)
        },
        body = { stacked, typing ->
            if (stacked) {
                PuzzlePrompt(state)
                Spacer(Modifier.height(16.dp))
                PuzzleControls(state, onHint, onReset, onPrevious, onNext, stacked)
                SharePositionButton(onSharePosition) // not with Theme and About: that panel is already full
                typeMove() // last: on a short screen it takes a scroll
            } else {
                // While typing, the verdict and the field stay where they were in the composition: the field
                // keeps its focus, and the keyboard, and the verdict is still the live region that is announced.
                PuzzleControls(state, onHint, onReset, onPrevious, onNext, stacked, showButtons = !typing)
                typeMove()
                if (!typing) PuzzleSettings(themeMode, onThemeToggle, onAbout, onSharePosition)
            }
        },
        keyboardHeight = keyboardHeight,
        fieldOpen = moveFieldOpen,
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
