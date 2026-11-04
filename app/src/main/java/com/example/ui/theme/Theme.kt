package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun buildColorScheme(
    appTheme: AppColorTheme,
    backgroundTheme: AppBackgroundTheme = AppBackgroundTheme.DEFAULT,
    isDark: Boolean,
    isAmoled: Boolean
): ColorScheme {
    return if (isDark) {
        val bg = if (isAmoled || backgroundTheme == AppBackgroundTheme.AMOLED) AmoledBackground else backgroundTheme.bgDark
        val surface = if (isAmoled || backgroundTheme == AppBackgroundTheme.AMOLED) AmoledSurface else backgroundTheme.surfaceDark
        val surfaceVar = if (isAmoled || backgroundTheme == AppBackgroundTheme.AMOLED) AmoledSurfaceVariant else backgroundTheme.surfaceVarDark

        darkColorScheme(
            primary = appTheme.primaryDark,
            onPrimary = if (appTheme == AppColorTheme.NERO) Color.Black else Color.White,
            primaryContainer = appTheme.containerDark,
            onPrimaryContainer = appTheme.primaryDark,
            secondary = appTheme.secondary,
            onSecondary = Color.White,
            background = bg,
            surface = surface,
            surfaceVariant = surfaceVar,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark,
            onSurfaceVariant = TextSecondaryDark
        )
    } else {
        lightColorScheme(
            primary = appTheme.primaryLight,
            onPrimary = Color.White,
            primaryContainer = appTheme.containerLight,
            onPrimaryContainer = appTheme.primaryLight,
            secondary = appTheme.secondary,
            onSecondary = Color.White,
            background = backgroundTheme.bgLight,
            surface = backgroundTheme.surfaceLight,
            surfaceVariant = backgroundTheme.surfaceVarLight,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight,
            onSurfaceVariant = TextSecondaryLight
        )
    }
}

@Composable
fun ReceiptSheetTheme(
    appColorTheme: AppColorTheme = AppColorTheme.VERDE,
    backgroundTheme: AppBackgroundTheme = AppBackgroundTheme.DEFAULT,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED_BLACK -> true
    }
    val isAmoled = themeMode == ThemeMode.AMOLED_BLACK || backgroundTheme == AppBackgroundTheme.AMOLED

    val colorScheme = buildColorScheme(
        appTheme = appColorTheme,
        backgroundTheme = backgroundTheme,
        isDark = isDark,
        isAmoled = isAmoled
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
