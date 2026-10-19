package dk.cocode.chess.viewmodel

import androidx.annotation.StringRes
import dk.cocode.chess.R

/**
 * The puzzle themes a player can pick, each by its Lichess tag (the Themes column of puzzles.csv), in the
 * order they are offered. The list is curated: every theme here has at least 15 puzzles in EACH of the
 * Easy, Medium and Hard bands of the bundled set, so a theme never leaves a band thin or empty (a test
 * checks it). The counts are Easy/Medium/Hard. Left out for having fewer than 15 in some band: double check
 * (8 Easy), trapped piece (9 Easy), back-rank mate (8 Hard), mate in 1 (2 Hard).
 */
enum class PuzzleTheme(val tag: String, @StringRes val label: Int) {
    FORK("fork", R.string.puzzle_theme_fork), // 404/756/781
    PIN("pin", R.string.puzzle_theme_pin), // 89/426/779
    SKEWER("skewer", R.string.puzzle_theme_skewer), // 73/122/128
    DISCOVERED_ATTACK("discoveredAttack", R.string.puzzle_theme_discovered_attack), // 127/323/418
    HANGING_PIECE("hangingPiece", R.string.puzzle_theme_hanging_piece), // 104/185/250
    SACRIFICE("sacrifice", R.string.puzzle_theme_sacrifice), // 120/582/1132
    DEFLECTION("deflection", R.string.puzzle_theme_deflection), // 86/320/474
    ATTRACTION("attraction", R.string.puzzle_theme_attraction), // 41/316/471
    PROMOTION("promotion", R.string.puzzle_theme_promotion), // 67/123/321
    MATE_IN_2("mateIn2", R.string.puzzle_theme_mate_in_2), // 645/519/42
    MATE_IN_3("mateIn3", R.string.puzzle_theme_mate_in_3), // 108/231/94
    OPENING("opening", R.string.puzzle_theme_opening), // 135/159/107
    MIDDLEGAME("middlegame", R.string.puzzle_theme_middlegame), // 1011/1605/2085
    ENDGAME("endgame", R.string.puzzle_theme_endgame), // 1264/1246/1388
}

/** A theme's name as the player reads and hears it; no theme (null) is "All". */
internal fun Texts.themeName(theme: PuzzleTheme?): String = string(theme?.label ?: R.string.puzzle_theme_all)

/** What is said when [theme] is chosen: its name and how many puzzles the band now has. */
internal fun Texts.themeChosenSentence(theme: PuzzleTheme?, count: Int): String =
    plural(R.plurals.sentence_theme_chosen, count, themeName(theme), count)
