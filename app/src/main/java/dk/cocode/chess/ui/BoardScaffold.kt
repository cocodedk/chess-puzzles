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
import androidx.compose.foundation.layout.fillMaxWidth
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
 * The board layout (a full-height board between or beside panels of content, or one between rows above
 * and below it) shared by the puzzle and openings screens, so the `when (boardLayout(...))` lives in one place.
 * [tabs] draws at the very top of whichever panel/column comes first. [header] is the left panel /
 * top-of-screen slot, [body] the right panel / bottom-of-screen slot; each is told whether it must
 * lay its own rows out as a stacked column — only true when it stands alone beside the board in its
 * own panel ([BoardLayout.BETWEEN_PANELS]), never when it shares a panel or a column with the other.
 * In [BoardLayout.UPRIGHT] the board is as wide as the screen and the rows below it scroll; in
 * [BoardLayout.STACKED] it is the largest square that fits the height the rows leave, unscrolled, unless
 * [scrollWhenStacked] (for rows too many to leave the board room) asks for the full width and scrolling there too.
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
    scrollWhenStacked: Boolean = false,
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            when (val layout = boardLayout(maxWidth, maxHeight)) {
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
                // UPRIGHT and STACKED. An `else`: as the lambda's last expression an exhaustive `when` hides
                // an unreachable throw.
                else -> {
                    val scrolls = layout == BoardLayout.UPRIGHT || scrollWhenStacked
                    val scroll = rememberScrollState() // outside the `if`: a flip to STACKED and back keeps the place
                    Column(
                        if (scrolls) Modifier.fillMaxSize().verticalScroll(scroll).padding(vertical = 8.dp)
                        else Modifier.fillMaxSize().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        tabs()
                        header(false)
                        Spacer(Modifier.height(8.dp))
                        val board = if (scrolls) Modifier.fillMaxWidth() else Modifier.weight(1f, fill = false)
                        ChessBoard(state, onSquareTap, onDragStart, onDragEnd, board)
                        Spacer(Modifier.height(8.dp)) // part of Hint's 44dp clearance under the board (see PuzzleControls)
                        body(false)
                    }
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

/**
 * Where the board goes, given the room a board as tall as the screen leaves beside it. With no room, rows go
 * above and below it: on an upright phone ([UPRIGHT_RATIO] or taller) the board takes the full width and the
 * rows scroll; on a near-square screen, upright or not, a board that wide would push its own bottom off the
 * screen, so it shrinks to fit the height the rows leave instead, unless they are too many to leave it room
 * (see [BoardScaffold]'s `scrollWhenStacked`).
 */
internal enum class BoardLayout { BETWEEN_PANELS, BESIDE_PANEL, UPRIGHT, STACKED }

internal fun boardLayout(width: Dp, height: Dp): BoardLayout = when {
    width - height >= NARROW_PANEL * 2 -> BoardLayout.BETWEEN_PANELS
    width - height >= WIDE_PANEL -> BoardLayout.BESIDE_PANEL   // a 16:9 phone on its side
    height >= width * UPRIGHT_RATIO -> BoardLayout.UPRIGHT      // a phone upright
    else -> BoardLayout.STACKED                                 // near-square: a tablet, a foldable's inner screen
}

/** A side panel of stacked menus and buttons: wide enough for the five stats and the Theme and About links. */
private val NARROW_PANEL = 190.dp

/** A single panel keeping its rows: wide enough for the Level and Theme menus side by side. */
private val WIDE_PANEL = 280.dp

/**
 * How much taller than wide an upright phone is, as opposed to a near-square tablet or foldable: every phone
 * is at least 1.5 even after the system bars, and from 1.3 up a full-width board plus the rows above it fit
 * on a wide screen.
 */
private const val UPRIGHT_RATIO = 1.3f
