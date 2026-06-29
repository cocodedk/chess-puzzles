package dk.cocode.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dk.cocode.chess.ui.PuzzleScreen
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val app = application as ChessApp
        setContent {
            ChessTheme {
                if (app.puzzles.count() == 0) {
                    EmptyState()
                } else {
                    val viewModel: PuzzleViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { PuzzleViewModel(app.puzzles, app.progress) }
                        },
                    )
                    PuzzleScreen(viewModel)
                }
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
