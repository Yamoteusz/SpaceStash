package com.spacestash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.spacestash.app.ui.DailyReminderWorker
import com.spacestash.app.ui.RootNavigation
import com.spacestash.app.ui.theme.SpaceStashTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- HARMONOGRAM POWIADOMIEŃ W TLE (WORKMANAGER) ---
        // Ustawiamy zadanie, które ma się odpalać co 24 godziny
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .build()

        // Zlecamy systemowi Android czuwanie nad tym zadaniem
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyApodReminder",
            ExistingPeriodicWorkPolicy.KEEP, // Jeśli zadanie już istnieje, nie nadpisuj go
            dailyWorkRequest
        )
        // ---------------------------------------------------

        enableEdgeToEdge()
        setContent {
            SpaceStashTheme {
                // Uruchamiamy zarządzanie głównym widokiem
                RootNavigation()
            }
        }
    }
}