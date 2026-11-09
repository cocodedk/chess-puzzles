package dk.cocode.chess.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val MIRRORED = mapOf("◀" to "▶", "▶" to "◀", "←" to "→")

/**
 * A left- or right-pointing [arrow] (◀ ▶ ←) as it should be drawn here: turned around in a right-to-left
 * layout, so "back" and "forward" point the way the row reads. Text does not mirror these by itself.
 */
@Composable
internal fun directional(arrow: String): String =
    if (LocalLayoutDirection.current == LayoutDirection.Ltr) arrow else MIRRORED.getValue(arrow)
