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
import dk.cocode.chess.core.util.Uci

private val PROMOTION_CHOICES = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

/** Lets the player pick a promotion piece. */
@Composable
fun PromotionDialog(onSelect: (PieceType) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Promote to") },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PROMOTION_CHOICES.forEach { type ->
                    TextButton(onClick = { onSelect(type) }) {
                        Text(PieceGlyph.glyph(Uci.typeToChar(type)), fontSize = 32.sp)
                    }
                }
            }
        },
    )
}
