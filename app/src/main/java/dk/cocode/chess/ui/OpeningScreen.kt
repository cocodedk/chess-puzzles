package dk.cocode.chess.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.viewmodel.OpeningViewModel

/**
 * Routes between the openings list and one opening's Learn/Practise screen, plus About — the
 * openings analogue of [PuzzleScreen].
 */
@Composable
fun OpeningScreen(
    viewModel: OpeningViewModel,
    openings: List<Opening>,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    tabs: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val openingId = state.openingId
    when {
        showAbout -> AboutScreen(onBack = { showAbout = false })
        openingId == null -> OpeningListScreen(
            openings = openings, cleanRuns = state.cleanRuns, onOpen = viewModel::onOpen,
            tabs = tabs, themeMode = themeMode, onThemeToggle = onThemeToggle, onAbout = { showAbout = true },
        )
        else -> {
            BackHandler(onBack = viewModel::onBack) // the phone's Back returns to the list, as the arrow does
            // Its live regions are rebuilt on return, which would read the last sentence again.
            DisposableEffect(viewModel) { onDispose(viewModel::onScreenLeft) }
            OpeningScreenContent(
                state = state, opening = remember(openingId) { openings.first { it.id == openingId } },
                onBack = viewModel::onBack, onMode = viewModel::onMode,
                onLine = viewModel::onLine, onStep = viewModel::onStep,
                onSquareTap = viewModel::onSquareTapped, onDragStart = viewModel::onDragStart,
                onDragEnd = viewModel::onDragEnd, onAgain = viewModel::onAgain,
                tabs = tabs, themeMode = themeMode, onThemeToggle = onThemeToggle, onAbout = { showAbout = true },
            )
        }
    }
}
