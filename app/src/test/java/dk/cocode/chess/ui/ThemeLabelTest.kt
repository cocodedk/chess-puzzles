package dk.cocode.chess.ui

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeLabelTest : EnglishTexts() {
    @Test fun eachModeHasADistinctStateLabel() {
        assertEquals("Theme: Match phone", themeLabel(ThemeMode.SYSTEM))
        assertEquals("Theme: Light", themeLabel(ThemeMode.LIGHT))
        assertEquals("Theme: Dark", themeLabel(ThemeMode.DARK))
    }

    @Test fun theButtonTextSaysWhichThemeATapSwitchesTo() {
        assertEquals("Use light theme", themeSwitchLabel(ThemeMode.SYSTEM))
        assertEquals("Use dark theme", themeSwitchLabel(ThemeMode.LIGHT))
        assertEquals("Match phone theme", themeSwitchLabel(ThemeMode.DARK))
    }
}
