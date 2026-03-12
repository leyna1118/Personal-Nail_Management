package com.leyna.nailmanagement.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var dynamicColor: Boolean by mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
        private set

    var seedColorArgb: Int by mutableIntStateOf(prefs.getInt(KEY_SEED_COLOR, DEFAULT_SEED_COLOR))
        private set

    val seedColor: Color
        get() = Color(seedColorArgb)

    fun updateDynamicColor(enabled: Boolean) {
        dynamicColor = enabled
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
    }

    fun updateSeedColor(argb: Int) {
        seedColorArgb = argb
        prefs.edit { putInt(KEY_SEED_COLOR, argb) }
    }

    fun updateSeedColor(color: Color) {
        updateSeedColor(color.toArgb())
    }

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_SEED_COLOR = "seed_color"
        val DEFAULT_SEED_COLOR = Color(0xFF6650a4).toArgb() // Purple40
    }
}
