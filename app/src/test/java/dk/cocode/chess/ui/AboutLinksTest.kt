package dk.cocode.chess.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutLinksTest {
    @Test fun updatesGoToTheFdroidPageWhenTheAppIsLiveThere() {
        assertEquals(
            "https://f-droid.org/packages/dk.cocode.chess/",
            aboutUrl(AboutLink.Updates, liveOnFdroid = true),
        )
    }

    @Test fun updatesGoToTheLatestGithubReleaseUntilThen() {
        assertEquals(
            "https://github.com/cocodedk/chess-puzzles/releases/latest",
            aboutUrl(AboutLink.Updates, liveOnFdroid = false),
        )
    }

    @Test fun theAppIsLiveOnFdroid() {
        assertTrue(LIVE_ON_FDROID)
        assertEquals(aboutUrl(AboutLink.Updates, liveOnFdroid = true), aboutUrl(AboutLink.Updates))
    }

    @Test fun thePrivacyPolicyIsThePageOnTheAppsSite() {
        assertEquals("https://chess.cocode.dk/privacy/", aboutUrl(AboutLink.Privacy))
    }

    @Test fun websiteSourceAndIssuesPointAtTheAppsOwnPages() {
        assertEquals("https://chess.cocode.dk/", aboutUrl(AboutLink.Website))
        assertEquals("https://github.com/cocodedk/chess-puzzles", aboutUrl(AboutLink.Source))
        assertEquals("https://github.com/cocodedk/chess-puzzles/issues", aboutUrl(AboutLink.Issues))
    }

    @Test fun theMakersPagesAreCocodeAndLinkedIn() {
        assertEquals("https://cocode.dk", aboutUrl(AboutLink.MadeBy))
        assertEquals("https://linkedin.com/in/babakbandpey", aboutUrl(AboutLink.LinkedIn))
    }

    @Test fun everyLinkIsSecure() {
        for (live in listOf(true, false)) {
            AboutLink.entries.forEach { assertTrue(aboutUrl(it, live).startsWith("https://")) }
        }
    }
}
