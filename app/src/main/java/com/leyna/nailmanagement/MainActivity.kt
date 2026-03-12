package com.leyna.nailmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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