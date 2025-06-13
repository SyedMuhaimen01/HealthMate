package com.muhaimen.healthmate.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.healthmate.R
import com.muhaimen.healthmate.data.repository.VitalsRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null

    private var lastStepTime = 0L
    private var stepCount = 0

    private val prefs by lazy { getSharedPreferences("steps", MODE_PRIVATE) }
    private val stepKey = "step_count"
    private val dateKey = "step_date"

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelSensor == null) {
            Log.e("StepCounterService", "Accelerometer not available.")
            stopSelf()
            return
        }

        loadStepData()
        startForegroundService()
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()
        val today = getToday()

        // Reset steps if date changed
        if (prefs.getString(dateKey, "") != today) {
            stepCount = 0
            prefs.edit().putString(dateKey, today).apply()
        }

        // Step detection using magnitude threshold
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        // Threshold and time-based filtering
        if (magnitude > 11.5 && now - lastStepTime > 500) {
            stepCount++
            lastStepTime = now
            Log.d("AccelerometerStep", "Detected step: $stepCount")

            saveSteps(stepCount)
            updateNotification(stepCount)
            saveStepCountToFirestore(stepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun loadStepData() {
        val today = getToday()
        val savedDate = prefs.getString(dateKey, "")
        stepCount = if (today == savedDate) {
            prefs.getInt(stepKey, 0)
        } else {
            0
        }
    }

    private fun saveSteps(steps: Int) {
        prefs.edit()
            .putInt(stepKey, steps)
            .putString(dateKey, getToday())
            .apply()
    }

    // -----------------------------------
    // Firebase sync
    // -----------------------------------
    private fun saveStepCountToFirestore(steps: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val date = getToday()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                VitalsRepository().saveStepCount(userId, date, steps)
            } catch (e: Exception) {
                Log.e("StepCounterService", "Failed to save steps: ${e.message}")
            }
        }
    }

    private fun startForegroundService() {
        val channelId = "step_channel"
        val channelName = "Step Tracking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("HealthMate Step Tracker")
            .setContentText("Tracking steps using accelerometer...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun updateNotification(steps: Int) {
        val notification = NotificationCompat.Builder(this, "step_channel")
            .setContentTitle("HealthMate Step Tracker")
            .setContentText("Today's steps: $steps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOnlyAlertOnce(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun getToday(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
