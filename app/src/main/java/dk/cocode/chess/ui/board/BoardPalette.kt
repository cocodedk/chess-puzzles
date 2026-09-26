package dk.cocode.chess.ui.board

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Square woods and highlight colours for one board look, plus the aura lifting ebony off walnut. */
data class BoardPalette(
    val lightSquare: Color,
    val darkSquare: Color,
    val selectedTint: Color,
    val selectedRing: Color,
    val lastMoveTint: Color,
    val hintTint: Color,
    val hintRing: Color,
    val marker: Color,
    /** Warm aura stroked around ebony pieces so they read on walnut; width is in the piece's 100-unit box. */
    val ebonyHalo: Color,
    val ebonyHaloWidth: Float,
)

/**
 * Pale maple and walnut, as in the reference. Ivory reads on maple through its dark outline, and ebony
 * on walnut through a faint warm aura, as the reference's drop-shadow glow does. The selection and the
 * last move are brass washes; the hint is a felt green, so the two never look alike.
 */
val DayBoardPalette = BoardPalette(
    lightSquare = Color(0xFFE8D0AA),
    darkSquare = Color(0xFF8A5A3B),
    selectedTint = Color(0x80FFD878),
    selectedRing = Color(0xB3FFEEBE),
    lastMoveTint = Color(0x6BDEB040),
    hintTint = Color(0x8C2E8B57),
    hintRing = Color(0xCCB4F0C8),
    marker = Color(0x99241208),   // dark enough to stand out on the night walnut, not only on maple
    ebonyHalo = Color(0x59FFE2B8),
    ebonyHaloWidth = 4f,
)

/**
 * The same woods a step darker so the maple doesn't glare in a dark room. Ebony all but vanishes into
 * dim walnut, so its aura is brighter and wider; the rest of the colours are shared with day.
 */
val NightBoardPalette = DayBoardPalette.copy(
    lightSquare = Color(0xFFC4A882),
    darkSquare = Color(0xFF6E4830),
    ebonyHalo = Color(0x80FFE9C8),
    ebonyHaloWidth = 8f,
)

/** Provided by ChessTheme so the board follows the app's day/night mode. */
val LocalBoardPalette = staticCompositionLocalOf { DayBoardPalette }
