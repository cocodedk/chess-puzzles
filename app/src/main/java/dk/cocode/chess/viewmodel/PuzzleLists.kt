package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Puzzle

/**
 * Which puzzles each difficulty band holds, and which of them carry each [PuzzleTheme]: lists of puzzle
 * indices (positions in [puzzles]), in bundle order. Built once; the theme filter never touches the puzzles.
 */
internal class PuzzleLists(puzzles: List<Puzzle>) {
    private val bands: Map<Difficulty, List<Int>> = puzzles.indices.groupBy { difficultyOf(puzzles[it].rating) }
    private val themed: Map<Difficulty, Map<PuzzleTheme, List<Int>>> = bands.mapValues { (_, indices) ->
        PuzzleTheme.entries.associateWith { theme -> indices.filter { puzzles[it].hasTheme(theme.tag) } }
    }

    /** Only the bands that actually contain puzzles, so the UI never offers a dead difficulty. */
    val difficulties: List<Difficulty> = Difficulty.entries.filter { it in bands }

    /** Only the themes present in every band, so choosing one never leaves a band empty. */
    val themes: List<PuzzleTheme> = PuzzleTheme.entries.filter { theme ->
        difficulties.all { band -> themed.getValue(band).getValue(theme).isNotEmpty() }
    }

    /** The puzzles of [band], or with a [theme] only those carrying it; empty when there are none. */
    fun of(band: Difficulty, theme: PuzzleTheme?): List<Int> =
        (if (theme == null) bands[band] else themed[band]?.get(theme)).orEmpty()

    /**
     * Where a visit to [band] starts: its first puzzle; with a [theme], the first one not in [solved]
     * (or the first, when all are). Null when the list is empty.
     */
    fun start(band: Difficulty, theme: PuzzleTheme?, solved: Set<Int>): Int? {
        val list = of(band, theme)
        return (if (theme == null) null else list.firstOrNull { it !in solved }) ?: list.firstOrNull()
    }
}
