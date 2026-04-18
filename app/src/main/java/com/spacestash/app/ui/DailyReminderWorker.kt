package com.spacestash.app.ui // Zmień, jeśli masz inny folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Tutaj dzieje się magia w tle!
        // Wywołujemy naszego pomocnika, którego zrobiliśmy wcześniej
        val notificationHelper = NotificationHelper(applicationContext)

        notificationHelper.showNotification(
            title = "SpaceStash - Nowa Misja!",
            message = "Dzisiejsze astronomiczne zdjęcie dnia (APOD) już czeka. Odkryj wszechświat! 🚀"
        )

        // Mówimy systemowi, że zadanie wykonano pomyślnie
        return Result.success()
    }
}