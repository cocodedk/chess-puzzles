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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.data.ThemeMode
import dk.cocode.chess.viewmodel.Texts
import dk.cocode.chess.viewmodel.ecoLine

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
    val texts = rememberTexts()
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Column(Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            tabs()
            LazyColumn(Modifier.weight(1f, fill = true)) {
                grouped.forEach { (group, rows) ->
                    item { GroupTitle(texts.groupTitle(group)) }
                    items(rows) { opening -> OpeningRow(opening, cleanRuns[opening.id] ?: 0, texts, onOpen) }
                }
            }
            PuzzleSettings(themeMode, onThemeToggle, onAbout)
        }
    }
}

/** A group heading, set apart from the rows under it: smaller, in the accent colour, with room above. */
@Composable
private fun GroupTitle(title: String) = Text(
    title,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
        .semantics { heading() },
)

/** One item for a screen reader — "Italian Game, C50, 3 clean runs" — in place of its name, code and clean-run count. */
@Composable
private fun OpeningRow(opening: Opening, cleanCount: Int, texts: Texts, onOpen: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onOpen(opening.id) }
            .clearAndSetSemantics {
                contentDescription = texts.openingRowDescription(opening, cleanCount)
                role = Role.Button
                onClick { onOpen(opening.id); true }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(opening.name, style = MaterialTheme.typography.titleMedium)
            Text(opening.eco, style = MaterialTheme.typography.labelMedium)
        }
        if (cleanCount > 0) Text(pluralStringResource(R.plurals.opening_clean_count, cleanCount, cleanCount))
    }
}

internal fun Texts.openingRowDescription(opening: Opening, cleanCount: Int): String =
    string(R.string.opening_row_description, opening.name, ecoLine(opening.eco, cleanCount))

internal fun Texts.groupTitle(group: OpeningGroup): String = string(
    when (group) {
        OpeningGroup.WHITE -> R.string.group_white
        OpeningGroup.BLACK_VS_E4 -> R.string.group_black_e4
        OpeningGroup.BLACK_VS_D4 -> R.string.group_black_d4
    },
)
