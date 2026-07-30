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
 * Neutral slate for daylight. The board is deliberately colourless: the pieces are neon
 * ([PIECE_WHITE], [PIECE_DARK]) and any warmth in the squares competes with them — on the walnut
 * board this replaced, magenta landed on the wood's own luminance at 1.04:1.
 *
 * Both squares sit low so the neon reads by luminance as well as chroma: cyan 4.4-6.2:1, magenta
 * 2.1-2.9:1. The checker step is small (1.39:1) because widening it would push one square back up
 * into the magenta.
 */
val DayBoardPalette = BoardPalette(
    lightSquare = Color(0xFF5A5A66),
    darkSquare = Color(0xFF45454F),
    selectedTint = Color(0xE0A5E4FF),
    lastMoveTint = Color(0xCCFFE082),
    hintTint = Color(0xCCD0A5FF),
    marker = Color(0x8CFFFFFF),
    darkPieceOutline = Color(0xFFEDEDED),
    pieceOutlineWidth = 0.07f,
    darkPieceHalo = Color.Transparent,
    darkPieceHaloWidth = 0f,
)

/**
 * The same slate dropped for a dark room, where the neon has the most to work with: cyan reaches
 * 7.3-9.8:1 and magenta 3.4-4.6:1.
 *
 * Both tint sets are light overlays. A dark wash is invisible here — the old deep-indigo hint and
 * black marker measured 1.09:1 and 1.21:1 on these squares — so the hint is a pale lavender and the
 * marker a white scrim, each kept off the selection's pale blue by hue.
 */
val NightBoardPalette = BoardPalette(
    lightSquare = Color(0xFF3A3A44),
    darkSquare = Color(0xFF26262E),
    selectedTint = Color(0xE0A5E4FF),
    lastMoveTint = Color(0xCCFFE082),
    hintTint = Color(0xCCD0A5FF),
    marker = Color(0x8CFFFFFF),
    darkPieceOutline = Color(0xFFFAFAFA),
    pieceOutlineWidth = 0.08f,
    darkPieceHalo = Color(0x80FFE9C8),
    darkPieceHaloWidth = 0.14f,
)

/** Provided by ChessTheme so the board follows the app's day/night mode. */
val LocalBoardPalette = staticCompositionLocalOf { DayBoardPalette }
