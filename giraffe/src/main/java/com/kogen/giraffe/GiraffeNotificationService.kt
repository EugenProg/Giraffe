package com.kogen.giraffe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kz.evko.kogen_di.annotations.KoGenComponent

private const val CHANNEL_ID = "grpc_traffic_channel"

@KoGenComponent(true)
class GiraffeNotificationService(private val context: Context) {

    init {
        createNotificationChannel(context)
    }

    fun sendTrafficNotification(methodName: String, host: String, message: String, notificationId: Int) {
        val cleanBody = message.lineSequence()
            .dropWhile { it.trim().startsWith("#") }
            .joinToString("\n")

        val appIconResId = context.applicationInfo.icon

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (appIconResId != 0) appIconResId else android.R.drawable.stat_notify_sync)
            .setContentTitle(methodName)
            .setSubText(host)
            .setContentText(cleanBody.lineSequence().firstOrNull())
            .setStyle(NotificationCompat.BigTextStyle().bigText(cleanBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }


    fun createNotificationChannel(context: Context) {
        val name = "gRaffe Traffic"
        val descriptionText = "gRPC traffic interceptor alerts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}