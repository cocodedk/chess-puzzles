package dk.cocode.chess.ui

/** apps.yml says the app is live on F-Droid, so updates are offered there. */
internal const val LIVE_ON_FDROID = true

/** Where the About screen's buttons lead. */
internal enum class AboutLink { Updates, Privacy, Website, Source, Issues, MadeBy, LinkedIn }

private const val REPO = "https://github.com/cocodedk/chess-puzzles"

/**
 * The web address behind each About button. [Updates][AboutLink.Updates] is the F-Droid page when the app
 * is [liveOnFdroid], else the latest GitHub release; the app itself never checks for updates.
 */
internal fun aboutUrl(link: AboutLink, liveOnFdroid: Boolean = LIVE_ON_FDROID): String = when (link) {
    AboutLink.Updates ->
        if (liveOnFdroid) "https://f-droid.org/packages/dk.cocode.chess/" else "$REPO/releases/latest"
    AboutLink.Privacy -> "https://chess.cocode.dk/privacy/"
    AboutLink.Website -> "https://chess.cocode.dk/"
    AboutLink.Source -> REPO
    AboutLink.Issues -> "$REPO/issues"
    AboutLink.MadeBy -> "https://cocode.dk"
    AboutLink.LinkedIn -> "https://linkedin.com/in/babakbandpey"
}
