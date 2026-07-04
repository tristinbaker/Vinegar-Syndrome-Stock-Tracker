package com.tristinbaker.vsalerts.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tristinbaker.vsalerts.R

const val CHANNEL_ID = "vs_stock_alerts"

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Vinegar Syndrome Alerts",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Price drop and low-stock alerts for tracked Vinegar Syndrome releases"
    }
    context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
}

fun postAlertNotification(context: Context, notificationId: Int, title: String, message: String, productUrl: String) {
    if (Build.VERSION.SDK_INT >= 33) {
        val granted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
    }

    val openIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(productUrl))
    val contentIntent = android.app.PendingIntent.getActivity(
        context,
        notificationId,
        openIntent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(contentIntent)
        .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
}
