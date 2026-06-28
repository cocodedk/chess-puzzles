package dk.cocode.chess.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import dk.cocode.chess.core.model.PieceType

private val PROMOTION_CHOICES = listOf(
    PieceType.QUEEN to 'q',
    PieceType.ROOK to 'r',
    PieceType.BISHOP to 'b',
    PieceType.KNIGHT to 'n',
)

/** Lets the player pick a promotion piece. [flipped] true means the player's pieces are Black. */
@Composable
fun PromotionDialog(flipped: Boolean, onSelect: (PieceType) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Promote to") },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PROMOTION_CHOICES.forEach { (type, code) ->
                    TextButton(onClick = { onSelect(type) }) {
                        Text(PieceGlyph.glyph(if (flipped) code else code.uppercaseChar()), fontSize = 32.sp)
                    }
                }
            }
        },
    )
}
