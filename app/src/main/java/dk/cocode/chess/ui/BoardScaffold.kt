package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
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
 * own panel ([BoardLayout.BETWEEN_PANELS], and [body] in either typing layout of the panels), never when it
 * shares a panel or a column with the other.
 * In [BoardLayout.UPRIGHT] the board is as wide as the screen and the rows below it scroll; in
 * [BoardLayout.STACKED] it is the largest square that fits the height the rows leave, unscrolled, unless
 * [scrollWhenStacked] (for rows too many to leave the board room) asks for the full width and scrolling there too.
 * The screen is padded with [insets]: the system bars, the cutout and the keyboard. With the keyboard up
 * ([keyboard]: its height beyond the bars, none unless given) under an open move field ([fieldOpen]),
 * [BoardLayout.TYPING] takes the place of either, and a panel family's typing layout its own (the board and [body]
 * alone), which [body] is told as its second argument. Both insets are read as the layout is measured (see
 * [BoxWithBoardLayout]).
 */
@Composable
fun BoardScaffold(
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    tabs: @Composable () -> Unit,
    header: @Composable ColumnScope.(stacked: Boolean) -> Unit,
    body: @Composable ColumnScope.(stacked: Boolean, typing: Boolean) -> Unit,
    scrollWhenStacked: Boolean = false,
    fieldOpen: Boolean = false,
    insets: WindowInsets = WindowInsets.safeDrawing,
    keyboard: WindowInsets = WindowInsets(0),
) {
    Surface(color = MaterialTheme.colorScheme.background) { // and the content colour for it, as a Scaffold gives
        BoxWithBoardLayout(insets, keyboard, fieldOpen) { layout ->
            BoardContent(layout, state, onSquareTap, onDragStart, onDragEnd, tabs, header, body, scrollWhenStacked)
        }
    }
}

/** The board and its rows in [layout], as [BoardScaffold] describes. */
@Composable
private fun BoardContent(
    layout: BoardLayout,
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    tabs: @Composable () -> Unit,
    header: @Composable ColumnScope.(stacked: Boolean) -> Unit,
    body: @Composable ColumnScope.(stacked: Boolean, typing: Boolean) -> Unit,
    scrollWhenStacked: Boolean,
) {
    val typing = layout.typing
    when (layout) {
        // Each panel family in one branch with its typing layout, which only hides rows, so the move field
        // keeps its place in the composition, and its focus, as the keyboard comes and goes.
        BoardLayout.BETWEEN_PANELS, BoardLayout.BETWEEN_PANELS_TYPING -> Row(Modifier.fillMaxSize()) {
            if (!typing) SidePanel { tabs(); header(true) }
            ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
            SidePanel(typing) { body(true, typing) }
        }
        BoardLayout.BESIDE_PANEL, BoardLayout.BESIDE_PANEL_TYPING -> Row(Modifier.fillMaxSize()) {
            ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
            SidePanel(typing) {
                if (!typing) {
                    tabs()
                    header(false)
                    Spacer(Modifier.height(16.dp))
                }
                body(typing, typing) // stacked while typing: the panel is then its own
            }
        }
        // The rest, UPRIGHT, STACKED and TYPING, in one branch so the move field keeps its focus, and the keyboard
        // stays up, as the layout flips between them.
        else -> {
            val fullWidth = layout == BoardLayout.UPRIGHT || layout == BoardLayout.STACKED && scrollWhenStacked
            // Both outside the `when`, so a flip to TYPING and back keeps the place. TYPING has its own, as its
            // rows fit at most fonts, and a shared one would be pulled back to the top.
            val scroll = rememberScrollState()
            val typingScroll = rememberScrollState()
            Column(
                Modifier.fillMaxSize().then(
                    when {
                        fullWidth -> Modifier.verticalScroll(scroll).padding(vertical = 8.dp)
                        // No padding: every dp the keyboard leaves goes to the board. The board's weight shares
                        // out the screen's height (the scroll's minimum), so this scrolls only once the field's
                        // rows outgrow the screen at the largest fonts, the board gone by then.
                        typing -> Modifier.verticalScroll(typingScroll)
                        else -> Modifier.padding(vertical = 16.dp)
                    },
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!typing) {
                    tabs()
                    header(false)
                    Spacer(Modifier.height(8.dp))
                }
                val board = when {
                    fullWidth -> Modifier.fillMaxWidth()
                    typing -> Modifier.weight(1f, fill = false).squareWithin()
                    else -> Modifier.weight(1f, fill = false)
                }
                ChessBoard(state, onSquareTap, onDragStart, onDragEnd, board)
                Spacer(Modifier.height(8.dp)) // part of Hint's 44dp clearance under the board (see PuzzleControls)
                body(false, typing)
            }
        }
    }
}

/**
 * One side of a board as tall as the screen: its rows centred, scrolling if the screen is too short. While a move is
 * [typing] it drops its padding, as every dp the keyboard leaves goes to the verdict and the field.
 */
@Composable
private fun RowScope.SidePanel(typing: Boolean = false, content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
        .padding(vertical = if (typing) 0.dp else 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    content = content,
)

/**
 * The largest square the room allows, down to nothing: at the largest fonts the move field leaves the board no room,
 * and `aspectRatio` alone, which never measures to nothing, would draw it full width over the field's rows.
 */
private fun Modifier.squareWithin() = layout { measurable, constraints ->
    val side = minOf(constraints.maxWidth, constraints.maxHeight)
    val square = measurable.measure(Constraints.fixed(side, side))
    layout(side, side) { square.place(0, 0) }
}
