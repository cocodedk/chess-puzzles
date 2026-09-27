package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.ui.board.ChessBoard
import dk.cocode.chess.viewmodel.BoardState

/**
 * The three-way board layout (a full-height board between, beside, or below panels of content)
 * shared by the puzzle and openings screens, so the `when (boardLayout(...))` lives in one place.
 * [tabs] draws at the very top of whichever panel/column comes first. [header] is the left panel /
 * top-of-screen slot, [body] the right panel / bottom-of-screen slot; each is told whether it must
 * lay its own rows out as a stacked column — only true when it stands alone beside the board in its
 * own panel ([BoardLayout.BETWEEN_PANELS]), never when it shares a panel or a column with the other.
 */
@Composable
fun BoardScaffold(
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    tabs: @Composable () -> Unit,
    header: @Composable ColumnScope.(stacked: Boolean) -> Unit,
    body: @Composable ColumnScope.(stacked: Boolean) -> Unit,
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            when (boardLayout(maxWidth, maxHeight)) {
                BoardLayout.BETWEEN_PANELS -> Row(Modifier.fillMaxSize()) {
                    SidePanel { tabs(); header(true) }
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
                    SidePanel { body(true) }
                }
                BoardLayout.BESIDE_PANEL -> Row(Modifier.fillMaxSize()) {
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
                    SidePanel {
                        tabs()
                        header(false)
                        Spacer(Modifier.height(16.dp))
                        body(false)
                    }
                }
                // STACKED. An `else`: as the lambda's last expression an exhaustive `when` hides an unreachable throw.
                else -> Column(
                    Modifier.fillMaxSize().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    tabs()
                    header(false)
                    Spacer(Modifier.height(8.dp))
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.weight(1f, fill = false))
                    Spacer(Modifier.height(8.dp))
                    body(false)
                }
            }
        }
    }
}

/** One side of a board as tall as the screen: its rows centred, scrolling if the screen is too short. */
@Composable
private fun RowScope.SidePanel(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    content = content,
)

/** Where the board goes, given the room a board as tall as the screen leaves beside it. */
internal enum class BoardLayout { BETWEEN_PANELS, BESIDE_PANEL, STACKED }

internal fun boardLayout(width: Dp, height: Dp): BoardLayout = when {
    width - height >= NARROW_PANEL * 2 -> BoardLayout.BETWEEN_PANELS
    width - height >= WIDE_PANEL -> BoardLayout.BESIDE_PANEL   // a 16:9 phone on its side
    else -> BoardLayout.STACKED
}

/** A side panel of stacked bands and buttons: wide enough for the five stats and the Theme and About links. */
private val NARROW_PANEL = 190.dp

/** A single panel keeping its rows: wide enough for the three bands side by side. */
private val WIDE_PANEL = 280.dp
