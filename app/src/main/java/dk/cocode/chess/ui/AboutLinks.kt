package dk.cocode.chess.ui

/** apps.yml says the app is live on F-Droid, so updates are offered there. */
internal const val LIVE_ON_FDROID = true

/** Where the About screen's buttons lead. */
internal enum class AboutLink { Updates, Privacy, Website, Source, Issues, MadeBy, LinkedIn }

private const val REPO = "https://github.com/cocodedk/chess-puzzles"
private const val SITE = "https://chess.cocode.dk/"

/** Languages the site has a home page and a privacy page for, at `<site><code>/` and `<site><code>/privacy/`. */
private val SITE_LANGUAGES = setOf("da")

/** A site page in the app's language, or the English page when the site has no pages in that language. */
private fun sitePage(language: String, path: String = ""): String =
    if (language in SITE_LANGUAGES) "$SITE$language/$path" else "$SITE$path"

/**
 * The web address behind each About button. [Updates][AboutLink.Updates] is the F-Droid page when the app
 * is [liveOnFdroid], else the latest GitHub release; the app itself never checks for updates. The website and
 * privacy pages follow [language] (a code such as "da" from the app's current locale).
 */
internal fun aboutUrl(
    link: AboutLink,
    language: String,
    liveOnFdroid: Boolean = LIVE_ON_FDROID,
): String = when (link) {
    AboutLink.Updates ->
        if (liveOnFdroid) "https://f-droid.org/packages/dk.cocode.chess/" else "$REPO/releases/latest"
    AboutLink.Privacy -> sitePage(language, "privacy/")
    AboutLink.Website -> sitePage(language)
    AboutLink.Source -> REPO
    AboutLink.Issues -> "$REPO/issues"
    AboutLink.MadeBy -> "https://cocode.dk"
    AboutLink.LinkedIn -> "https://linkedin.com/in/babakbandpey"
}
