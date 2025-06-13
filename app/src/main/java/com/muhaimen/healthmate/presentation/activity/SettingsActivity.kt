package com.muhaimen.healthmate.presentation.activity

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.healthmate.R
import com.muhaimen.healthmate.application.HealthMate
import com.muhaimen.healthmate.databinding.ActivitySettingsBinding
import com.muhaimen.healthmate.viewModel.AuthViewModel
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences("reminder_prefs", MODE_PRIVATE)

        // Show current saved time
        val savedHour = prefs.getInt("reminder_hour", 20)
        val savedMinute = prefs.getInt("reminder_minute", 0)
        binding.reminderTimeInput.setText(String.format("%02d:%02d", savedHour, savedMinute))

        // Open TimePickerDialog on click
        binding.reminderTimeInput.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.reminderTimeInput.setText(formattedTime)

                    // Save to SharedPreferences
                    prefs.edit {
                        putInt("reminder_hour", hourOfDay)
                        putInt("reminder_minute", minute)
                    }

                    // Reschedule the reminder
                    val app = application as HealthMate
                    app.scheduleDailyReminder(hourOfDay, minute, applicationContext)
                },
                savedHour,
                savedMinute,
                true
            )
            timePickerDialog.show()
        }

        // Logout logic
        binding.logoutButton.setOnClickListener {
            authViewModel.signOut()
            finish()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.addEntryButton.setOnClickListener() {
            val intent= Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        binding.dashboardButton.setOnClickListener() {
            val intent= Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.historyButton.setOnClickListener() {
            val intent= Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}
