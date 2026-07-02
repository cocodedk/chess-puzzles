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
    pieceOutlineWidth = 0.035f,
    darkPieceHalo = Color.Transparent,
    darkPieceHaloWidth = 0f,
)

/**
 * The walnut board dimmed so it does not glare in a dark room. Highlights are brightened —
 * the day tints sink below 1.25:1 on dark wood; these stay above 1.6:1 (script-verified).
 * Piece rims are brighter and thicker: a black fill is only ~1.5:1 against the night dark
 * square, so the rim alone must carry the silhouette.
 */
val NightBoardPalette = BoardPalette(
    lightSquare = Color(0xFF8A6844),
    darkSquare = Color(0xFF46301C),
    selectedTint = Color(0x8842C6FF),
    lastMoveTint = Color(0x66FFD54F),
    hintTint = Color(0x9991A7FF),
    marker = Color(0x59FFFFFF),
    darkPieceOutline = Color(0xFFFAFAFA),
    pieceOutlineWidth = 0.06f,
    darkPieceHalo = Color(0x80FFE9C8),
    darkPieceHaloWidth = 0.14f,
)

/** Provided by ChessTheme so the board follows the app's day/night mode. */
val LocalBoardPalette = staticCompositionLocalOf { DayBoardPalette }
