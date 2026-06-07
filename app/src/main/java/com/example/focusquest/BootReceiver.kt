package com.example.focusquest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = UserPreferencesManager(context)
            if (prefs.getReminderEnabled()) {
                ReminderScheduler.schedule(context, prefs.getReminderHour(), prefs.getReminderMinute())
            }
        }
    }
}
