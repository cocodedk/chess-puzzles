package dk.cocode.chess.ui.board

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Square and highlight colors for one board look, plus how piece glyphs are rimmed on it. */
data class BoardPalette(
    val lightSquare: Color,
    val darkSquare: Color,
    val selectedTint: Color,
    val lastMoveTint: Color,
    val hintTint: Color,
    val marker: Color,
    val darkPieceOutline: Color,
    /** Outline stroke width as a fraction of the square size. */
    val pieceOutlineWidth: Float,
    /** Soft aura behind dark pieces so they can be spotted on dark squares; 0 width disables it. */
    val darkPieceHalo: Color,
    val darkPieceHaloWidth: Float,
)

/** Walnut wood, tuned for daylight. */
val DayBoardPalette = BoardPalette(
    lightSquare = Color(0xFFE0C29A),
    darkSquare = Color(0xFF9C6B43),
    selectedTint = Color(0x6603A9F4),
    lastMoveTint = Color(0x55FFEB3B),
    hintTint = Color(0x553F51B5),
    marker = Color(0x40000000),
    darkPieceOutline = Color(0xFFEDEDED),
    pieceOutlineWidth = 0.05f,
    darkPieceHalo = Color.Transparent,
    darkPieceHaloWidth = 0f,
)

/**
 * The walnut board for a dark room. Dimming it further is what used to bury the pieces: against a
 * deep dark square a #0A1420 fill managed only 1.5-2:1, so the rim and aura had to carry the whole
 * silhouette. These squares keep the ink pieces at 3.66:1 on dark and 8.27:1 on light — legible on
 * their own — at the cost of a board that is only a little dimmer than day.
 *
 * Because the board is now mid-tone rather than deep, the highlight tints follow the day treatment:
 * they read by hue at roughly 1.2-1.9:1, the same range the day tints already ship at. The old
 * night set was brightened for a deep board and would sink to ~1.0:1 on these squares.
 */
val NightBoardPalette = BoardPalette(
    lightSquare = Color(0xFFC9A87C),
    darkSquare = Color(0xFF8A6844),
    selectedTint = Color(0x8803A9F4),
    lastMoveTint = Color(0x77FFD54F),
    hintTint = Color(0x77303F9F),
    marker = Color(0x59000000),
    darkPieceOutline = Color(0xFFFAFAFA),
    pieceOutlineWidth = 0.06f,
    darkPieceHalo = Color(0x80FFE9C8),
    darkPieceHaloWidth = 0.14f,
)

/** Provided by ChessTheme so the board follows the app's day/night mode. */
val LocalBoardPalette = staticCompositionLocalOf { DayBoardPalette }
