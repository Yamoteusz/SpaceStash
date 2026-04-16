package com.spacestash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spacestash.app.ui.RootNavigation
import com.spacestash.app.ui.theme.SpaceStashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaceStashTheme {
                // Uruchamiamy zarządzanie głównym widokiem
                RootNavigation()
            }
        }
    }
}