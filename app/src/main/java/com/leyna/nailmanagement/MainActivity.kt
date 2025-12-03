package com.leyna.nailmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.leyna.nailmanagement.ui.navigation.MainNavigation
import com.leyna.nailmanagement.ui.theme.NailManagementTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NailManagementTheme {
                MainNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }
}