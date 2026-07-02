package dk.cocode.chess.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * "Walnut Study" palette, derived from the walnut board. All on-X pairings are WCAG AA
 * (>= 5.85:1), script-verified. Day: warm cream, no pure white (glare). Night: dim wooden
 * chess-club browns, never pure black.
 */
internal val LightColors = lightColorScheme(
    primary = Color(0xFF7A4A21),
    onPrimary = Color(0xFFFFF6EC),
    primaryContainer = Color(0xFFFFDCC0),
    onPrimaryContainer = Color(0xFF3E2408),
    secondary = Color(0xFF6C5B3F),
    onSecondary = Color(0xFFFFF6EC),
    background = Color(0xFFFAF1E6),
    onBackground = Color(0xFF211A12),
    surface = Color(0xFFFAF1E6),
    onSurface = Color(0xFF211A12),
    surfaceVariant = Color(0xFFF0E0CC),
    onSurfaceVariant = Color(0xFF50432E),
    outline = Color(0xFF837259),
    error = Color(0xFFA93C24),
    onError = Color(0xFFFFF6EC),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFE7BE92),
    onPrimary = Color(0xFF432C10),
    primaryContainer = Color(0xFF5D3F22),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFFD4BD9B),
    onSecondary = Color(0xFF382C16),
    background = Color(0xFF181310),
    onBackground = Color(0xFFEDE0D0),
    surface = Color(0xFF201914),
    onSurface = Color(0xFFEDE0D0),
    surfaceVariant = Color(0xFF41362A),
    onSurfaceVariant = Color(0xFFD7C5AE),
    outline = Color(0xFF9F8E79),
    error = Color(0xFFFFB4A5),
    onError = Color(0xFF521806),
)
