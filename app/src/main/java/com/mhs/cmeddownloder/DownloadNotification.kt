package com.mhs.cmeddownloder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class DownloadNotification (private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val channelId = "download_channel"
    private val notificationId = 1

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(progress: Int) {
        val notificationBuilder =
            NotificationCompat.Builder(context, channelId)
                .setContentTitle("Downloading...")
                .setContentText("$progress% complete")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setProgress(100, progress, false)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun cancelNotification() {
        notificationManager.cancel(notificationId)
    }
}