package dk.cocode.chess.ui

import androidx.compose.runtime.Composable
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.viewmodel.OpeningMode
import dk.cocode.chess.viewmodel.OpeningUiState

/** One opening's board screen, in Learn or Practise, laid out via the shared [BoardScaffold]. */
@Composable
fun OpeningScreenContent(
    state: OpeningUiState,
    opening: Opening,
    onBack: () -> Unit,
    onMode: (OpeningMode) -> Unit,
    onLine: (Int) -> Unit,
    onStep: (Int) -> Unit,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    onAgain: () -> Unit,
    tabs: @Composable () -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeToggle: () -> Unit = {},
    onAbout: () -> Unit = {},
) {
    BoardScaffold(
        state = state,
        onSquareTap = onSquareTap, onDragStart = onDragStart, onDragEnd = onDragEnd,
        tabs = tabs,
        header = { stacked -> OpeningHeader(state, opening, onBack, onMode, stacked) },
        body = { stacked ->
            if (state.mode == OpeningMode.LEARN) {
                OpeningLearnPanel(state, opening, onLine, onStep, stacked)
            } else {
                OpeningPractisePanel(state, opening, onAgain)
            }
            PuzzleSettings(themeMode, onThemeToggle, onAbout)
        },
        scrollWhenStacked = true, // Learn's rows would otherwise squeeze the board on a phone
    )
}
