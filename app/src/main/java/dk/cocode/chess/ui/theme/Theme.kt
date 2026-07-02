package dk.cocode.chess.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dk.cocode.chess.ui.board.DayBoardPalette
import dk.cocode.chess.ui.board.LocalBoardPalette
import dk.cocode.chess.ui.board.NightBoardPalette

/** App theme: the Material color scheme and the board palette both follow [darkTheme]. */
@Composable
fun ChessTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBoardPalette provides if (darkTheme) NightBoardPalette else DayBoardPalette) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
    }
}
