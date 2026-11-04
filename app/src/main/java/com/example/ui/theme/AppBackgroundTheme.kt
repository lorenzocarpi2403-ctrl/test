package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppBackgroundTheme(
    val id: String,
    val labelItalian: String,
    val description: String,
    val swatchColor: Color,
    val bgLight: Color,
    val surfaceLight: Color,
    val surfaceVarLight: Color,
    val bgDark: Color,
    val surfaceDark: Color,
    val surfaceVarDark: Color
) {
    DEFAULT(
        id = "default",
        labelItalian = "Neutro",
        description = "Ardesia Classica",
        swatchColor = Color(0xFF64748B),
        bgLight = Color(0xFFF8FAFC),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFF1F5F9),
        bgDark = Color(0xFF0F172A),
        surfaceDark = Color(0xFF1E293B),
        surfaceVarDark = Color(0xFF334155)
    ),
    AMOLED(
        id = "amoled",
        labelItalian = "Nero AMOLED",
        description = "Nero Assoluto OLED",
        swatchColor = Color(0xFF000000),
        bgLight = Color(0xFFF1F5F9),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFE2E8F0),
        bgDark = Color(0xFF000000),
        surfaceDark = Color(0xFF0D0D0E),
        surfaceVarDark = Color(0xFF18181B)
    ),
    MIDNIGHT_BLUE(
        id = "midnight_blue",
        labelItalian = "Blu Notte",
        description = "Oceano Profondo",
        swatchColor = Color(0xFF1E3A8A),
        bgLight = Color(0xFFEFF6FF),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFDBEAFE),
        bgDark = Color(0xFF0A1128),
        surfaceDark = Color(0xFF121E42),
        surfaceVarDark = Color(0xFF1C2C5E)
    ),
    GALAXY_PURPLE(
        id = "galaxy_purple",
        labelItalian = "Viola Notte",
        description = "Ametista & Galassia",
        swatchColor = Color(0xFF581C87),
        bgLight = Color(0xFFFAF5FF),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFF3E8FF),
        bgDark = Color(0xFF140827),
        surfaceDark = Color(0xFF23103E),
        surfaceVarDark = Color(0xFF34185A)
    ),
    FOREST_GREEN(
        id = "forest_green",
        labelItalian = "Verde Bosco",
        description = "Pino & Smeraldo Scuro",
        swatchColor = Color(0xFF064E3B),
        bgLight = Color(0xFFF0FDF4),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFDCFCE7),
        bgDark = Color(0xFF041910),
        surfaceDark = Color(0xFF0B2D1F),
        surfaceVarDark = Color(0xFF12432F)
    ),
    VELVET_RED(
        id = "velvet_red",
        labelItalian = "Rosso Velluto",
        description = "Vinaccia & Rubino",
        swatchColor = Color(0xFF881337),
        bgLight = Color(0xFFFFF1F2),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFFFE4E6),
        bgDark = Color(0xFF1F060B),
        surfaceDark = Color(0xFF320E15),
        surfaceVarDark = Color(0xFF4C1520)
    ),
    ROSE_BLUSH(
        id = "rose_blush",
        labelItalian = "Rosa Blush",
        description = "Cipria & Lampone",
        swatchColor = Color(0xFF9D174D),
        bgLight = Color(0xFFFDF2F8),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFFCE7F3),
        bgDark = Color(0xFF220A1A),
        surfaceDark = Color(0xFF36122B),
        surfaceVarDark = Color(0xFF4D1C3F)
    ),
    LILAC_MIST(
        id = "lilac_mist",
        labelItalian = "Lilla & Lavanda",
        description = "Pastello Floreale",
        swatchColor = Color(0xFF7E22CE),
        bgLight = Color(0xFFF5F3FF),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFEDE9FE),
        bgDark = Color(0xFF170C2D),
        surfaceDark = Color(0xFF251545),
        surfaceVarDark = Color(0xFF372063)
    ),
    WARM_CREAM(
        id = "warm_cream",
        labelItalian = "Crema & Ambra",
        description = "Tonalità Calda e Sabbia",
        swatchColor = Color(0xFFB45309),
        bgLight = Color(0xFFFFFBEB),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFFEF3C7),
        bgDark = Color(0xFF1C1305),
        surfaceDark = Color(0xFF2E200C),
        surfaceVarDark = Color(0xFF433015)
    ),
    GRAPHITE_TITANIUM(
        id = "graphite_titanium",
        labelItalian = "Grigio Grafite",
        description = "Zinco & Titanio Cyber",
        swatchColor = Color(0xFF27272A),
        bgLight = Color(0xFFF4F4F5),
        surfaceLight = Color(0xFFFFFFFF),
        surfaceVarLight = Color(0xFFE4E4E7),
        bgDark = Color(0xFF0F0F12),
        surfaceDark = Color(0xFF1A1A20),
        surfaceVarDark = Color(0xFF282830)
    );

    companion object {
        fun fromId(id: String?): AppBackgroundTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
