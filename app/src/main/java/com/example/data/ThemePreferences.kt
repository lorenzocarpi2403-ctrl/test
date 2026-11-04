package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppBackgroundTheme
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppThemeConfig(
    val colorTheme: AppColorTheme = AppColorTheme.VERDE,
    val backgroundTheme: AppBackgroundTheme = AppBackgroundTheme.DEFAULT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<AppThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): AppThemeConfig {
        val colorId = prefs.getString("color_theme_id", AppColorTheme.VERDE.id)
        val bgId = prefs.getString("bg_theme_id", AppBackgroundTheme.DEFAULT.id)
        val modeName = prefs.getString("theme_mode_name", ThemeMode.SYSTEM.name)
        return AppThemeConfig(
            colorTheme = AppColorTheme.fromId(colorId),
            backgroundTheme = AppBackgroundTheme.fromId(bgId),
            themeMode = try {
                ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        )
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit().putString("color_theme_id", theme.id).apply()
        _themeConfig.value = _themeConfig.value.copy(colorTheme = theme)
    }

    fun setBackgroundTheme(bgTheme: AppBackgroundTheme) {
        prefs.edit().putString("bg_theme_id", bgTheme.id).apply()
        _themeConfig.value = _themeConfig.value.copy(backgroundTheme = bgTheme)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode_name", mode.name).apply()
        _themeConfig.value = _themeConfig.value.copy(themeMode = mode)
    }
}
