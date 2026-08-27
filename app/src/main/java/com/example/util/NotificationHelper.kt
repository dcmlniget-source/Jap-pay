package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_TRANSACTIONS = "channel_jappay_tx"
    private const val CHANNEL_ADMIN = "channel_jappay_admin"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val txChannel = NotificationChannel(
                CHANNEL_TRANSACTIONS,
                "Jap Pay Transactions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time payment and money transfer notifications"
                enableVibration(true)
            }

            val adminChannel = NotificationChannel(
                CHANNEL_ADMIN,
                "Jap Pay Admin Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Account deposit status and admin announcements"
                enableVibration(true)
            }

            manager.createNotificationChannel(txChannel)
            manager.createNotificationChannel(adminChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_TRANSACTIONS
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            val manager = NotificationManagerCompat.from(context)
            val notifId = (System.currentTimeMillis() % 100000).toInt()
            manager.notify(notifId, notification)
        } catch (_: Exception) {}
    }
}
