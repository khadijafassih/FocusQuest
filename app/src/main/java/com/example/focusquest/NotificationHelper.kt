package com.example.focusquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_SYSTEM = "focusquest_system"
    private const val CHANNEL_SYSTEM_NAME = "System Events"

    private const val ID_BATTERY   = 301
    private const val ID_CHARGING  = 302
    private const val ID_UNPLUGGED = 303
    private const val ID_AIRPLANE  = 304

    fun postBatteryLow(context: Context) = post(
        context, ID_BATTERY,
        "Battery Low",
        "Your battery is low — save your work!"
    )

    fun postCharging(context: Context) = post(
        context, ID_CHARGING,
        "Charging",
        "Your device is now charging ⚡"
    )

    fun postUnplugged(context: Context) = post(
        context, ID_UNPLUGGED,
        "Unplugged",
        "Your device has been unplugged"
    )

    fun postAirplaneMode(context: Context, on: Boolean) = post(
        context, ID_AIRPLANE,
        if (on) "Airplane Mode On ✈️" else "Airplane Mode Off",
        if (on) "You're in airplane mode — network is disabled" else "Airplane mode has been turned off"
    )

    private fun post(context: Context, id: Int, title: String, message: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(nm)
        val tapIntent = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        nm.notify(id, NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_timer_notif)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .build())
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(CHANNEL_SYSTEM) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_SYSTEM,
                        CHANNEL_SYSTEM_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Battery, connectivity and system alerts" }
                )
            }
        }
    }
}
