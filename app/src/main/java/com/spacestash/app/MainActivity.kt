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

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyApodReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        enableEdgeToEdge()
        setContent {
            SpaceStashTheme {
                RootNavigation()
            }
        }
    }
}