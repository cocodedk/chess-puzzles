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

/**
 * Walnut wood, tuned for daylight. The squares sit in a narrow mid band rather than spanning
 * light-to-dark: a square at luminance 0.19 is equidistant from both piece colours, so pulling them
 * in is what lets white AND ink clear 3:1 at once. It costs checker contrast (2.69 -> 1.61:1), which
 * the eye reads easily anyway from the grid.
 */
val DayBoardPalette = BoardPalette(
    lightSquare = Color(0xFFB08A61),
    darkSquare = Color(0xFF8A6844),
    selectedTint = Color(0xE0A5E4FF),
    lastMoveTint = Color(0xCCFFE082),
    hintTint = Color(0xCC1A237E),
    marker = Color(0x73000000),
    darkPieceOutline = Color(0xFFEDEDED),
    pieceOutlineWidth = 0.07f,
    darkPieceHalo = Color.Transparent,
    darkPieceHaloWidth = 0f,
)

/**
 * The same narrow band as day, shifted one step down for a dark room. Dimming it further is what
 * used to bury the pieces: against a deep dark square a #0A1420 fill managed only 1.5-2:1, so the
 * rim and aura had to carry the whole silhouette.
 *
 * Both boards being mid-tone, the tints are shared: a mid-hue wash (the old #03A9F4 at 40-53%)
 * lands on the squares' own luminance and vanishes — 1.0:1 — so each tint is pushed to an extreme,
 * pale or deep, at high alpha. Every one clears 2:1 on all four squares.
 */
val NightBoardPalette = BoardPalette(
    lightSquare = Color(0xFFA8825A),
    darkSquare = Color(0xFF7E5E3E),
    selectedTint = Color(0xE0A5E4FF),
    lastMoveTint = Color(0xCCFFE082),
    hintTint = Color(0xCC1A237E),
    marker = Color(0x73000000),
    darkPieceOutline = Color(0xFFFAFAFA),
    pieceOutlineWidth = 0.08f,
    darkPieceHalo = Color(0x80FFE9C8),
    darkPieceHaloWidth = 0.14f,
)

/** Provided by ChessTheme so the board follows the app's day/night mode. */
val LocalBoardPalette = staticCompositionLocalOf { DayBoardPalette }
