package com.spacestash.app.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    // Unikalne ID naszego kanału
    private val CHANNEL_ID = "kosmiczny_kanal"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Kanały są wymagane tylko na Androidzie 8.0 (Oreo) i nowszych
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Kosmiczne Powiadomienia"
            val descriptionText = "Powiadomienia o misjach i zdjęciach"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Rejestracja kanału w systemie
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        // Budowanie samego powiadomienia
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Wbudowana, standardowa ikonka Androida
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Zniknie po kliknięciu

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Wysyłamy powiadomienie (używamy aktualnego czasu jako unikalnego ID, żeby nie nadpisywało poprzednich)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}