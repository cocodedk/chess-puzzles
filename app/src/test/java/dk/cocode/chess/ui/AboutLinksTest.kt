package dk.cocode.chess.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutLinksTest {
    @Test fun updatesGoToTheFdroidPageWhenTheAppIsLiveThere() {
        assertEquals(
            "https://f-droid.org/packages/dk.cocode.chess/",
            aboutUrl(AboutLink.Updates, "en", liveOnFdroid = true),
        )
    }

    @Test fun updatesGoToTheLatestGithubReleaseUntilThen() {
        assertEquals(
            "https://github.com/cocodedk/chess-puzzles/releases/latest",
            aboutUrl(AboutLink.Updates, "en", liveOnFdroid = false),
        )
    }

    @Test fun theAppIsLiveOnFdroid() {
        assertTrue(LIVE_ON_FDROID)
        assertEquals(aboutUrl(AboutLink.Updates, "en", liveOnFdroid = true), aboutUrl(AboutLink.Updates, "en"))
    }

    @Test fun thePrivacyPolicyIsThePageOnTheAppsSite() {
        assertEquals("https://chess.cocode.dk/privacy/", aboutUrl(AboutLink.Privacy, "en"))
    }

    @Test fun theDanishPrivacyPolicyIsTheDanishPageOnTheAppsSite() {
        assertEquals("https://chess.cocode.dk/da/privacy/", aboutUrl(AboutLink.Privacy, "da"))
    }

    @Test fun aLanguageTheSiteLacksGetsTheEnglishPrivacyPolicy() {
        assertEquals("https://chess.cocode.dk/privacy/", aboutUrl(AboutLink.Privacy, "fa"))
    }

    @Test fun websiteIsTheAppsSite() {
        assertEquals("https://chess.cocode.dk/", aboutUrl(AboutLink.Website, "en"))
    }

    @Test fun theDanishWebsiteIsTheDanishPageOnTheAppsSite() {
        assertEquals("https://chess.cocode.dk/da/", aboutUrl(AboutLink.Website, "da"))
    }

    @Test fun aLanguageTheSiteLacksGetsTheEnglishWebsite() {
        assertEquals("https://chess.cocode.dk/", aboutUrl(AboutLink.Website, "fa"))
    }

    @Test fun sourceAndIssuesPointAtTheAppsOwnPagesInEveryLanguage() {
        for (language in listOf("en", "da", "fa")) {
            assertEquals("https://github.com/cocodedk/chess-puzzles", aboutUrl(AboutLink.Source, language))
            assertEquals("https://github.com/cocodedk/chess-puzzles/issues", aboutUrl(AboutLink.Issues, language))
        }
    }

    @Test fun theMakersPagesAreCocodeAndLinkedIn() {
        assertEquals("https://cocode.dk", aboutUrl(AboutLink.MadeBy, "da"))
        assertEquals("https://linkedin.com/in/babakbandpey", aboutUrl(AboutLink.LinkedIn, "da"))
    }

    @Test fun everyLinkIsSecure() {
        for (live in listOf(true, false)) {
            for (language in listOf("en", "da", "fa")) {
                AboutLink.entries.forEach { assertTrue(aboutUrl(it, language, live).startsWith("https://")) }
            }
        }
    }
}
