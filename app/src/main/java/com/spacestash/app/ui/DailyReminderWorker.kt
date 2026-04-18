package com.spacestash.app.ui

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)

        notificationHelper.showNotification(
            title = "SpaceStash - Nowa Misja!",
            message = "Dzisiejsze astronomiczne zdjęcie dnia (APOD) już czeka. Odkryj wszechświat! 🚀"
        )

        return Result.success()
    }
}