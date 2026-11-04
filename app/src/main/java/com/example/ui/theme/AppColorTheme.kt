package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppColorTheme(
    val id: String,
    val labelItalian: String,
    val hexDescription: String,
    val swatchColor: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val containerLight: Color,
    val containerDark: Color,
    val secondary: Color
) {
    VERDE(
        id = "verde",
        labelItalian = "Verde",
        hexDescription = "Smeraldo",
        swatchColor = VerdePrimary,
        primaryLight = VerdePrimary,
        primaryDark = VerdeLight,
        containerLight = VerdeContainerLight,
        containerDark = VerdeContainerDark,
        secondary = VerdeSecondary
    ),
    BLU(
        id = "blu",
        labelItalian = "Blu",
        hexDescription = "Oceano",
        swatchColor = BluPrimary,
        primaryLight = BluPrimary,
        primaryDark = BluLight,
        containerLight = BluContainerLight,
        containerDark = BluContainerDark,
        secondary = BluSecondary
    ),
    VIOLA(
        id = "viola",
        labelItalian = "Viola",
        hexDescription = "Reale",
        swatchColor = ViolaPrimary,
        primaryLight = ViolaPrimary,
        primaryDark = ViolaLight,
        containerLight = ViolaContainerLight,
        containerDark = ViolaContainerDark,
        secondary = ViolaSecondary
    ),
    LILLA(
        id = "lilla",
        labelItalian = "Lilla",
        hexDescription = "Lavanda",
        swatchColor = LillaPrimary,
        primaryLight = LillaPrimary,
        primaryDark = LillaLight,
        containerLight = LillaContainerLight,
        containerDark = LillaContainerDark,
        secondary = LillaSecondary
    ),
    ROSA(
        id = "rosa",
        labelItalian = "Rosa",
        hexDescription = "Vivace",
        swatchColor = RosaPrimary,
        primaryLight = RosaPrimary,
        primaryDark = RosaLight,
        containerLight = RosaContainerLight,
        containerDark = RosaContainerDark,
        secondary = RosaSecondary
    ),
    ROSSO(
        id = "rosso",
        labelItalian = "Rosso",
        hexDescription = "Rubino",
        swatchColor = RossoPrimary,
        primaryLight = RossoPrimary,
        primaryDark = RossoLight,
        containerLight = RossoContainerLight,
        containerDark = RossoContainerDark,
        secondary = RossoSecondary
    ),
    NERO(
        id = "nero",
        labelItalian = "Nero",
        hexDescription = "Titanio",
        swatchColor = NeroPrimary,
        primaryLight = NeroPrimary,
        primaryDark = Color(0xFFE4E4E7),
        containerLight = NeroContainerLight,
        containerDark = NeroContainerDark,
        secondary = NeroSecondary
    );

    companion object {
        fun fromId(id: String?): AppColorTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: VERDE
        }
    }
}

enum class ThemeMode(val labelItalian: String) {
    SYSTEM("Sistema"),
    LIGHT("Chiaro"),
    DARK("Scuro"),
    AMOLED_BLACK("Nero AMOLED")
}
