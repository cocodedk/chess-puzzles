package dk.cocode.chess.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.data.ThemeMode

/** The openings catalogue grouped by side, each row showing its clean-run count. */
@Composable
fun OpeningListScreen(
    openings: List<Opening>,
    cleanRuns: Map<String, Int>,
    onOpen: (String) -> Unit,
    tabs: @Composable () -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeToggle: () -> Unit = {},
    onAbout: () -> Unit = {},
) {
    val grouped = remember(openings) { openings.groupBy { it.group } }
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            tabs()
            LazyColumn(Modifier.weight(1f, fill = true)) {
                grouped.forEach { (group, rows) ->
                    item { Text(groupTitle(group), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp)) }
                    items(rows) { opening -> OpeningRow(opening, cleanRuns[opening.id] ?: 0, onOpen) }
                }
            }
            PuzzleSettings(themeMode, onThemeToggle, onAbout)
        }
    }
}

@Composable
private fun OpeningRow(opening: Opening, cleanCount: Int, onOpen: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onOpen(opening.id) }.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(opening.name, style = MaterialTheme.typography.titleMedium)
            Text(opening.eco, style = MaterialTheme.typography.labelMedium)
        }
        if (cleanCount > 0) Text("×$cleanCount")
    }
}

internal fun groupTitle(group: OpeningGroup): String = when (group) {
    OpeningGroup.WHITE -> "As White"
    OpeningGroup.BLACK_VS_E4 -> "As Black against 1.e4"
    OpeningGroup.BLACK_VS_D4 -> "As Black against 1.d4"
}
