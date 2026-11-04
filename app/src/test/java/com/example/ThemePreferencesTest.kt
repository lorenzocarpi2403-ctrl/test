package com.example

import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AppBackgroundTheme
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.buildColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemePreferencesTest {

    @Test
    fun testAllRequestedColorsAreAvailable() {
        val requestedColors = listOf("nero", "viola", "rosa", "lilla", "rosso", "verde", "blu")
        for (colorId in requestedColors) {
            val theme = AppColorTheme.fromId(colorId)
            assertEquals(colorId, theme.id)
            assertNotNull(theme.swatchColor)
            assertNotNull(theme.primaryLight)
            assertNotNull(theme.primaryDark)
        }
    }

    @Test
    fun testAllBackgroundColorsAreAvailable() {
        val backgrounds = listOf(
            "default", "amoled", "midnight_blue", "galaxy_purple",
            "forest_green", "velvet_red", "rose_blush", "lilac_mist",
            "warm_cream", "graphite_titanium"
        )
        for (bgId in backgrounds) {
            val bg = AppBackgroundTheme.fromId(bgId)
            assertEquals(bgId, bg.id)
            assertNotNull(bg.swatchColor)
            assertNotNull(bg.bgLight)
            assertNotNull(bg.bgDark)
            assertNotNull(bg.surfaceLight)
            assertNotNull(bg.surfaceDark)
        }
    }

    @Test
    fun testFallbackToDefaultColorAndBackground() {
        val theme = AppColorTheme.fromId("unknown_color")
        assertEquals(AppColorTheme.VERDE, theme)

        val nullTheme = AppColorTheme.fromId(null)
        assertEquals(AppColorTheme.VERDE, nullTheme)

        val bg = AppBackgroundTheme.fromId("unknown_bg")
        assertEquals(AppBackgroundTheme.DEFAULT, bg)
    }

    @Test
    fun testBuildColorSchemeLightAndDarkWithCustomBackground() {
        val colorSchemeLight = buildColorScheme(
            appTheme = AppColorTheme.VIOLA,
            backgroundTheme = AppBackgroundTheme.MIDNIGHT_BLUE,
            isDark = false,
            isAmoled = false
        )
        assertEquals(AppColorTheme.VIOLA.primaryLight, colorSchemeLight.primary)
        assertEquals(AppBackgroundTheme.MIDNIGHT_BLUE.bgLight, colorSchemeLight.background)

        val colorSchemeDark = buildColorScheme(
            appTheme = AppColorTheme.ROSA,
            backgroundTheme = AppBackgroundTheme.FOREST_GREEN,
            isDark = true,
            isAmoled = false
        )
        assertEquals(AppColorTheme.ROSA.primaryDark, colorSchemeDark.primary)
        assertEquals(AppBackgroundTheme.FOREST_GREEN.bgDark, colorSchemeDark.background)
    }

    @Test
    fun testBuildColorSchemeAmoledPureBlack() {
        val colorSchemeAmoled = buildColorScheme(
            appTheme = AppColorTheme.NERO,
            backgroundTheme = AppBackgroundTheme.AMOLED,
            isDark = true,
            isAmoled = true
        )
        assertEquals(AmoledBackground, colorSchemeAmoled.background)
        assertNotNull(colorSchemeAmoled.surface)
    }
}
