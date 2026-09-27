package dk.cocode.chess.ui

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Which top-level mode is showing — switched by the [ModeTabs] row atop every screen. */
enum class AppTab { PUZZLES, OPENINGS }

/** The `Puzzles | Openings` tab row shared by the puzzle and openings screens. */
@Composable
fun ModeTabs(selected: AppTab, onSelect: (AppTab) -> Unit) {
    TabRow(selectedTabIndex = selected.ordinal) {
        Tab(
            selected = selected == AppTab.PUZZLES,
            onClick = { onSelect(AppTab.PUZZLES) },
            text = { Text("Puzzles") },
        )
        Tab(
            selected = selected == AppTab.OPENINGS,
            onClick = { onSelect(AppTab.OPENINGS) },
            text = { Text("Openings") },
        )
    }
}
