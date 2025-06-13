package com.muhaimen.healthmate.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.muhaimen.healthmate.utils.ReminderWorker
import com.muhaimen.healthmate.utils.StepCounterService
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HealthMate : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("HealthMate", "App started")

        FirebaseApp.initializeApp(this)
        Log.d("HealthMate", "Firebase initialized")

        // Start step counter service
        val intent = Intent(this, StepCounterService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Log.d("HealthMate", "StepCounterService started")

        // Load reminder time from SharedPreferences
        val prefs = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)
        Log.d("HealthMate", "Loaded reminder time: $hour:$minute")

        // Schedule daily reminder
        scheduleDailyReminder(hour, minute, this)
    }

    fun scheduleDailyReminder(hour: Int, minute: Int, context: Context) {
        val now = Calendar.getInstance()
        val reminderTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (reminderTime.before(now)) {
            reminderTime.add(Calendar.DATE, 1)
        }

        val initialDelayMillis = reminderTime.timeInMillis - now.timeInMillis
        val delayMinutes = TimeUnit.MILLISECONDS.toMinutes(initialDelayMillis)
        Log.d("ReminderScheduler", "Initial delay: $delayMinutes minutes")

        val dailyWork = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_health_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWork
        )

        Log.d("ReminderScheduler", "WorkManager scheduled daily reminder")
    }
}
