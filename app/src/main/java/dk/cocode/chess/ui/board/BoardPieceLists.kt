package dk.cocode.chess.ui.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import dk.cocode.chess.ui.rememberTexts
import dk.cocode.chess.viewmodel.BoardState
import dk.cocode.chess.viewmodel.pieceLists

/** The first list's traversal index; the squares keep the default 0, so every list is read before them. */
private const val FIRST_LIST_INDEX = -2f

/**
 * Where every piece stands, as a screen reader reads it before the 64 squares: one item per side that
 * has pieces, White's then Black's, whichever way the board faces. The items draw nothing and take no
 * touches, and sit under the squares so exploring by touch still reads the square under the finger.
 * Sighted users never see them, so they need no setting.
 *
 * The order is set, not left to where the items happen to sit. Compose drops an item that items above it
 * cover completely: the squares cover all but the frame, which is what keeps the lists, and the lists
 * split the board's height so neither covers the other. Each click does nothing: without one, a TalkBack
 * double-tap on a list would become a finger tap on the drawing beneath it.
 */
@Composable
internal fun BoardPieceLists(state: BoardState, modifier: Modifier = Modifier) {
    val texts = rememberTexts()
    Column(modifier) {
        texts.pieceLists(state).forEachIndexed { i, list ->
            Box(
                Modifier.weight(1f).fillMaxWidth().semantics {
                    contentDescription = list
                    traversalIndex = FIRST_LIST_INDEX + i
                    onClick { true }
                },
            )
        }
    }
}
