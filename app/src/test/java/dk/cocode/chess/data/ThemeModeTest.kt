package dk.cocode.chess.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test fun systemModeFollowsTheSystemSetting() {
        assertFalse(ThemeMode.SYSTEM.resolvesToDark(systemDark = false))
        assertTrue(ThemeMode.SYSTEM.resolvesToDark(systemDark = true))
    }

    @Test fun forcedModesIgnoreTheSystemSetting() {
        assertFalse(ThemeMode.LIGHT.resolvesToDark(systemDark = true))
        assertFalse(ThemeMode.LIGHT.resolvesToDark(systemDark = false))
        assertTrue(ThemeMode.DARK.resolvesToDark(systemDark = false))
        assertTrue(ThemeMode.DARK.resolvesToDark(systemDark = true))
    }

    @Test fun nextCyclesThroughAllModes() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.SYSTEM.next())
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.next())
        assertEquals(ThemeMode.SYSTEM, ThemeMode.DARK.next())
    }
}
