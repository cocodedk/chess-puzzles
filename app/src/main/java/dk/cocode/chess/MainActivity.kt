package dk.cocode.chess

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dk.cocode.chess.core.data.openings.Openings
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.ui.AppTab
import dk.cocode.chess.ui.ModeTabs
import dk.cocode.chess.ui.OpeningScreen
import dk.cocode.chess.ui.PuzzleScreen
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.OpeningViewModel
import dk.cocode.chess.viewmodel.PuzzleViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        val app = application as ChessApp
        // The splash bridges the async first DataStore read, so the first drawn frame already
        // has the persisted theme — no wrong-theme flash on launch.
        val mode = mutableStateOf<ThemeMode?>(null)
        lifecycleScope.launch { app.theme.mode.collect { mode.value = it } }
        splash.setKeepOnScreenCondition { mode.value == null }
        // Activity-scoped (and cycle() is internally NonCancellable) so a tapped toggle always lands.
        val toggleTheme: () -> Unit = { lifecycleScope.launch { app.theme.cycle() } }
        setContent {
            val themeMode = mode.value ?: return@setContent
            val darkTheme = themeMode.resolvesToDark(isSystemInDarkTheme())
            ChessTheme(darkTheme = darkTheme) {
                SyncWindowToTheme(darkTheme)
                AppContent(app, themeMode, toggleTheme)
            }
        }
    }

    /** The window decor must follow the in-app override, not just the system -night resources. */
    @Composable
    private fun SyncWindowToTheme(darkTheme: Boolean) {
        val background = MaterialTheme.colorScheme.background
        LaunchedEffect(darkTheme) {
            window.setBackgroundDrawable(ColorDrawable(background.toArgb()))
            // Bar colors must match the icon appearance set below; deprecated in the API 35
            // edge-to-edge world (where it is ignored) but required on API 24-34.
            @Suppress("DEPRECATION")
            window.statusBarColor = background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = background.toArgb()
            val insets = WindowCompat.getInsetsController(window, window.decorView)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Composable
private fun AppContent(app: ChessApp, themeMode: ThemeMode, onThemeToggle: () -> Unit) {
    if (app.puzzles.count() == 0) {
        EmptyState()
    } else {
        var tab by rememberSaveable { mutableStateOf(AppTab.PUZZLES) }
        val tabs: @Composable () -> Unit = { ModeTabs(selected = tab, onSelect = { tab = it }) }
        when (tab) {
            AppTab.PUZZLES -> {
                val viewModel: PuzzleViewModel = viewModel(
                    factory = viewModelFactory { initializer { PuzzleViewModel(app.puzzles, app.progress) } },
                )
                PuzzleScreen(viewModel = viewModel, themeMode = themeMode, onThemeToggle = onThemeToggle, tabs = tabs)
            }
            AppTab.OPENINGS -> {
                val viewModel: OpeningViewModel = viewModel(
                    factory = viewModelFactory { initializer { OpeningViewModel(Openings.all, app.openingProgress) } },
                )
                OpeningScreen(
                    viewModel = viewModel, openings = Openings.all,
                    themeMode = themeMode, onThemeToggle = onThemeToggle, tabs = tabs,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No puzzles available")
    }
}
