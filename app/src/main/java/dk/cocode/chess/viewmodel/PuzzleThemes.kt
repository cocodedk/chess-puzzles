package dk.cocode.chess.viewmodel

import androidx.annotation.StringRes
import dk.cocode.chess.R

/**
 * The puzzle themes a player can pick, each by its Lichess tag (the Themes column of puzzles.csv), in the
 * order they are offered. The list is curated: every theme here has at least 15 puzzles in EACH of the
 * Easy, Medium and Hard bands of the bundled set, so a theme never leaves a band thin or empty (a test
 * checks it). The counts are Easy/Medium/Hard. Left out for having fewer than 15 in some band: double check
 * (2 Easy), back-rank mate and mate in 1 (none Hard), promotion (7 Easy), attraction (8 Easy).
 */
enum class PuzzleTheme(val tag: String, @StringRes val label: Int) {
    FORK("fork", R.string.puzzle_theme_fork), // 95/221/244
    PIN("pin", R.string.puzzle_theme_pin), // 30/114/223
    SKEWER("skewer", R.string.puzzle_theme_skewer), // 24/31/38
    DISCOVERED_ATTACK("discoveredAttack", R.string.puzzle_theme_discovered_attack), // 40/93/124
    HANGING_PIECE("hangingPiece", R.string.puzzle_theme_hanging_piece), // 31/52/73
    SACRIFICE("sacrifice", R.string.puzzle_theme_sacrifice), // 36/140/319
    DEFLECTION("deflection", R.string.puzzle_theme_deflection), // 21/100/109
    MATE_IN_2("mateIn2", R.string.puzzle_theme_mate_in_2), // 168/114/20
    MATE_IN_3("mateIn3", R.string.puzzle_theme_mate_in_3), // 26/60/36
    OPENING("opening", R.string.puzzle_theme_opening), // 44/43/28
    MIDDLEGAME("middlegame", R.string.puzzle_theme_middlegame), // 278/428/586
    ENDGAME("endgame", R.string.puzzle_theme_endgame), // 324/325/344
}

/** A theme's name as the player reads and hears it; no theme (null) is "All". */
internal fun Texts.themeName(theme: PuzzleTheme?): String = string(theme?.label ?: R.string.puzzle_theme_all)

/** What is said when [theme] is chosen: its name and how many puzzles the band now has. */
internal fun Texts.themeChosenSentence(theme: PuzzleTheme?, count: Int): String =
    plural(R.plurals.sentence_theme_chosen, count, themeName(theme), count)
