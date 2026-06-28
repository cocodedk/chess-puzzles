package dk.cocode.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dk.cocode.chess.ui.PuzzleScreen
import dk.cocode.chess.ui.theme.ChessTheme
import dk.cocode.chess.viewmodel.PuzzleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ChessApp
        setContent {
            ChessTheme {
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
