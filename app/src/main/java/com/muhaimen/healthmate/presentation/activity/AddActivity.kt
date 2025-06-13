package com.muhaimen.healthmate.presentation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.muhaimen.healthmate.R
import com.muhaimen.healthmate.data.dataClass.Vitals
import com.muhaimen.healthmate.data.repository.VitalsRepository
import com.muhaimen.healthmate.databinding.ActivityAddBinding
import com.muhaimen.healthmate.viewModel.AuthViewModel
import com.muhaimen.healthmate.viewModel.VitalsViewModel
import com.muhaimen.healthmate.viewModel.VitalsViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private var selectedMood: String = "OKAY" // Default mood

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var vitalsViewModel: VitalsViewModel

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val repository = VitalsRepository()
        val factory = VitalsViewModelFactory(repository)
        vitalsViewModel = ViewModelProvider(this, factory)[VitalsViewModel::class.java]

        val emojiMap = mapOf(
            R.id.emoji1 to "TERRIBLE",
            R.id.emoji2 to "BAD",
            R.id.emoji3 to "OKAY",
            R.id.emoji4 to "GOOD",
            R.id.emoji5 to "GREAT"
        )

        for ((id, mood) in emojiMap) {
            findViewById<TextView>(id).setOnClickListener {
                selectedMood = mood
                updateEmojiColors(selectedId = id)
            }
        }

        val userId = authViewModel.getCurrentUserId()
        if (userId != null) {
            // Pass userId here as required
            vitalsViewModel.fetchTodayVitals()
            vitalsViewModel.todayVitals.observe(this) { vitals ->
                vitals?.let {
                    // Set EditText text with strings
                    binding.waterIntake.setText(it.waterIntake.toString())
                    binding.sleepHours.setText(it.sleepHours.toString())
                    binding.weight.setText(it.weight.toString())

                    // Set mood from vitals and update emoji UI
                    selectedMood = it.mood.ifEmpty { "OKAY" }
                    val selectedEmojiId = emojiMap.entries.find { entry -> entry.value == selectedMood }?.key
                    selectedEmojiId?.let { id -> updateEmojiColors(selectedId = id) }
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        binding.saveButton.setOnClickListener {
            val waterIntake = binding.waterIntake.text.toString().toIntOrNull() ?: 0
            val sleepDuration = binding.sleepHours.text.toString().toFloatOrNull() ?: 0f
            val weight = binding.weight.text.toString().toFloatOrNull() ?: 0f

            val currentDate = LocalDate.now()
            val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val vitals = Vitals(
                id = formattedDate,
                date = formattedDate,
                waterIntake = waterIntake,
                sleepHours = sleepDuration,
                steps = 0, // default steps 0; you may want to fetch actual steps here
                weight = weight,
                mood = selectedMood
            )

            vitalsViewModel.addVitals(vitals)
            Toast.makeText(this, "Vitals saved successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.increaseButton.setOnClickListener {
            val currentWaterIntake = binding.waterIntake.text.toString().toIntOrNull() ?: 0
            binding.waterIntake.setText((currentWaterIntake + 1).toString())
        }

        binding.decreaseButton.setOnClickListener {
            val currentWaterIntake = binding.waterIntake.text.toString().toIntOrNull() ?: 0
            if (currentWaterIntake > 0) {
                binding.waterIntake.setText((currentWaterIntake - 1).toString())
            }
        }

        binding.increaseHrsButton.setOnClickListener {
            val currentSleepHours = binding.sleepHours.text.toString().toFloatOrNull() ?: 0f
            binding.sleepHours.setText((currentSleepHours + 0.5f).toString())
        }

        binding.decreaseHrsButton.setOnClickListener {
            val currentSleepHours = binding.sleepHours.text.toString().toFloatOrNull() ?: 0f
            if (currentSleepHours > 0) {
                binding.sleepHours.setText((currentSleepHours - 0.5f).toString())
            }
        }

        binding.increaseWeightButton.setOnClickListener {
            val currentWeight = binding.weight.text.toString().toFloatOrNull() ?: 0f
            binding.weight.setText((currentWeight + 0.1f).toString())
        }

        binding.decreaseWeightButton.setOnClickListener {
            val currentWeight = binding.weight.text.toString().toFloatOrNull() ?: 0f
            if (currentWeight > 0) {
                binding.weight.setText((currentWeight - 0.1f).toString())
            }
        }



        binding.settingsButton.setOnClickListener() {
            val intent= Intent(this, SettingsActivity::class.java)
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



    private fun updateEmojiColors(selectedId: Int) {
        val allEmojiIds = listOf(R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4, R.id.emoji5)
        for (id in allEmojiIds) {
            val emojiTextView = findViewById<TextView>(id)
            if (id == selectedId) {
                emojiTextView.setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_moodHappy))
            } else {
                emojiTextView.setTextColor(Color.parseColor("#888888")) // Gray for unselected
            }
        }
    }
}
