package com.leyna.nailmanagement.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils

@Composable
fun NailManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColorArgb: Int = ThemePreferences.DEFAULT_SEED_COLOR,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> colorSchemeFromSeed(seedColorArgb, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun colorSchemeFromSeed(seedArgb: Int, isDark: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seedArgb, hsl)
    val hue = hsl[0]
    val sat = hsl[1]

    return if (isDark) {
        darkColorScheme(
            primary = hslColor(hue, sat.coerceAtLeast(0.4f), 0.80f),
            onPrimary = hslColor(hue, sat * 0.4f, 0.20f),
            primaryContainer = hslColor(hue, sat * 0.8f, 0.35f),
            onPrimaryContainer = hslColor(hue, sat * 0.2f, 0.90f),
            secondary = hslColor(hue + 30f, sat * 0.3f, 0.70f),
            onSecondary = hslColor(hue + 30f, sat * 0.2f, 0.15f),
            secondaryContainer = hslColor(hue + 30f, sat * 0.3f, 0.25f),
            onSecondaryContainer = hslColor(hue + 30f, sat * 0.2f, 0.90f),
            tertiary = hslColor(hue + 60f, sat * 0.5f, 0.70f),
            onTertiary = hslColor(hue + 60f, sat * 0.3f, 0.15f),
            tertiaryContainer = hslColor(hue + 60f, sat * 0.4f, 0.25f),
            onTertiaryContainer = hslColor(hue + 60f, sat * 0.3f, 0.90f),
            background = hslColor(hue, sat * 0.05f, 0.06f),
            onBackground = hslColor(hue, sat * 0.05f, 0.95f),
            surface = hslColor(hue, sat * 0.05f, 0.08f),
            onSurface = hslColor(hue, sat * 0.05f, 0.95f),
            surfaceVariant = hslColor(hue, sat * 0.1f, 0.18f),
            onSurfaceVariant = hslColor(hue, sat * 0.08f, 0.80f),
            surfaceContainerLowest = hslColor(hue, sat * 0.05f, 0.04f),
            surfaceContainerLow = hslColor(hue, sat * 0.06f, 0.10f),
            surfaceContainer = hslColor(hue, sat * 0.08f, 0.12f),
            surfaceContainerHigh = hslColor(hue, sat * 0.10f, 0.15f),
            surfaceContainerHighest = hslColor(hue, sat * 0.12f, 0.18f),
            outline = hslColor(hue, sat * 0.1f, 0.50f),
            outlineVariant = hslColor(hue, sat * 0.08f, 0.30f),
        )
    } else {
        lightColorScheme(
            primary = hslColor(hue, sat, 0.35f),
            onPrimary = Color.White,
            primaryContainer = hslColor(hue, sat * 0.4f, 0.88f),
            onPrimaryContainer = hslColor(hue, sat * 0.9f, 0.20f),
            secondary = hslColor(hue + 30f, sat * 0.4f, 0.45f),
            onSecondary = Color.White,
            secondaryContainer = hslColor(hue + 30f, sat * 0.15f, 0.92f),
            onSecondaryContainer = hslColor(hue + 30f, sat * 0.5f, 0.20f),
            tertiary = hslColor(hue + 60f, sat * 0.5f, 0.40f),
            onTertiary = Color.White,
            tertiaryContainer = hslColor(hue + 60f, sat * 0.2f, 0.92f),
            onTertiaryContainer = hslColor(hue + 60f, sat * 0.6f, 0.20f),
            background = hslColor(hue, sat * 0.02f, 0.99f),
            onBackground = hslColor(hue, sat * 0.1f, 0.10f),
            surface = hslColor(hue, sat * 0.02f, 0.99f),
            onSurface = hslColor(hue, sat * 0.1f, 0.10f),
            surfaceVariant = hslColor(hue, sat * 0.08f, 0.92f),
            onSurfaceVariant = hslColor(hue, sat * 0.08f, 0.30f),
            surfaceContainerLowest = hslColor(hue, sat * 0.02f, 1.0f),
            surfaceContainerLow = hslColor(hue, sat * 0.03f, 0.99f),
            surfaceContainer = hslColor(hue, sat * 0.10f, 0.95f),
            surfaceContainerHigh = hslColor(hue, sat * 0.05f, 0.97f),
            surfaceContainerHighest = hslColor(hue, sat * 0.08f, 0.94f),
            outline = hslColor(hue, sat * 0.15f, 0.60f),
            outlineVariant = hslColor(hue, sat * 0.06f, 0.80f),
        )
    }
}

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val l = lightness.coerceIn(0f, 1f)
    val argb = ColorUtils.HSLToColor(floatArrayOf(h, s, l))
    return Color(argb)
}
