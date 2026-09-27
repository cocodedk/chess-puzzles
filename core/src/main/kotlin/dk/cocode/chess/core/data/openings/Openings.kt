package dk.cocode.chess.core.data.openings

import dk.cocode.chess.core.model.Opening

/** The openings catalogue: names and ECO codes as in Lichess's CC0 opening list. */
object Openings {
    val all: List<Opening> = WhiteOpenings + VsE4Openings + VsD4Openings
}
