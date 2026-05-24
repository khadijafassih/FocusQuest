package com.example.focusquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "focusquest_reminder"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Focus Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val mainPi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(200, NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_timer_notif)
            .setContentTitle("Time to Focus!")
            .setContentText("Your focus session is waiting. Let's go!")
            .setContentIntent(mainPi)
            .setAutoCancel(true)
            .build())
    }
}
