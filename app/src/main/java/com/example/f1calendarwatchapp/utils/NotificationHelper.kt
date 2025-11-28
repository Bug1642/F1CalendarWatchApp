// NotificationHelper

package com.example.f1calendarwatchapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.f1calendarwatchapp.R // R sınıfının projenize göre düzgün import edildiğinden emin olun

object NotificationHelper {

    // Kanal kimliği.
    const val CHANNEL_ID = "f1_race_notification_channel"
    private const val NOTIFICATION_ID = 1

    // createNotificationChannel artık public.
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "F1 Yarış Bildirimi" // Kullanıcının gördüğü isim
            val descriptionText = "Yaklaşan F1 yarışları ve seansları için hatırlatıcılar."

            // KRİTİK DÜZELTME: YÜKSEK ÖNEMLİ OLARAK TANIMLANIYOR
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Eğer kanal daha önce kapalı olarak kaydedildiyse, bu işlem onu açmaz.
            // Bu yüzden temiz kurulum kritiktir.
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // İkon adınızı kontrol edin
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}