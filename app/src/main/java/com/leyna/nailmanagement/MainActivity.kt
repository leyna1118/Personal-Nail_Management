package com.leyna.nailmanagement

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.leyna.nailmanagement.ui.navigation.MainNavigation
import com.leyna.nailmanagement.ui.theme.NailManagementTheme
import com.leyna.nailmanagement.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreferences = remember { ThemePreferences(this) }
            val darkTheme = isSystemInDarkTheme()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            NailManagementTheme(
                dynamicColor = themePreferences.dynamicColor,
                seedColorArgb = themePreferences.seedColorArgb
            ) {
                MainNavigation(
                    themePreferences = themePreferences,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}