package dk.cocode.chess.ui

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeLabelTest : EnglishTexts() {
    @Test fun allModesHaveDistinctLabels() {
        assertEquals("Theme: Match phone", themeLabel(ThemeMode.SYSTEM))
        assertEquals("Theme: Light", themeLabel(ThemeMode.LIGHT))
        assertEquals("Theme: Dark", themeLabel(ThemeMode.DARK))
    }

    @Test fun eachModeSaysWhichThemeATapSwitchesTo() {
        assertEquals("Switch to the light theme", themeSwitchLabel(ThemeMode.SYSTEM))
        assertEquals("Switch to the dark theme", themeSwitchLabel(ThemeMode.LIGHT))
        assertEquals("Match the phone's theme", themeSwitchLabel(ThemeMode.DARK))
    }
}
