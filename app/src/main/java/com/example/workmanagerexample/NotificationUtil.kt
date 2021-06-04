package com.example.workmanagerexample

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.workmanagerexample.MainActivity.Companion.CHANNEL_ID

fun NotificationManager.sendNotification(context: Context, title: String = "Succeed", desc : String = "Worker is completed") {

    val NOTIFY_ID = 42633687

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(desc)
        .setSmallIcon(R.drawable.ic_baseline_celebration_24)

    notify(NOTIFY_ID, notification.build())
}