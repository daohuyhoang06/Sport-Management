package com.sportmanagement.user.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sportmanagement.user.MainActivity
import com.sportmanagement.user.R

class SportFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushNotificationRegistrar.registerToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureNotificationChannel()

        val data = message.data
        val title = message.notification?.title
            ?: data["title"]
            ?: "Sport Management"
        val body = message.notification?.body
            ?: data["body"]
            ?: data["subtitle"]
            ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = "OPEN_NOTIFICATION"
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_logo))
            .setColor(ContextCompat.getColor(this, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sport updates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Booking, matchmaking, and match reminder updates"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "sport_updates"
    }
}
