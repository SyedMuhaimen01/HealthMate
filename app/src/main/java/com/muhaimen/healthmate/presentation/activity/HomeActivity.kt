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
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.muhaimen.healthmate.R
import com.muhaimen.healthmate.data.repository.VitalsRepository
import com.muhaimen.healthmate.databinding.ActivityHomeBinding
import com.muhaimen.healthmate.viewModel.AuthViewModel
import com.muhaimen.healthmate.viewModel.VitalsViewModel
import com.muhaimen.healthmate.viewModel.VitalsViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var vitalsViewModel: VitalsViewModel

    private var selectedMood: String = "OKAY"

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
                updateEmojiVisibility(selectedId = id)
            }
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.addEntryButton.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }

        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        val userId = authViewModel.getCurrentUserId()
        if (userId != null) {
            vitalsViewModel.fetchTodayVitals()
            vitalsViewModel.todayVitals.observe(this) { vitals ->
                vitals?.let {
                    binding.waterIntake.text = it.waterIntake.toString()
                    binding.sleepHours.text = it.sleepHours.toString()
                    binding.weight.text = it.weight.toString()
                    binding.steps.text = it.steps.toString()
                    // Set mood from vitals and update emoji UI
                    selectedMood = it.mood ?: "OKAY"
                    val selectedEmojiId = emojiMap.entries.find { entry -> entry.value == selectedMood }?.key
                    selectedEmojiId?.let { id -> updateEmojiVisibility(selectedId = id) }
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmojiVisibility(selectedId: Int) {
        val allEmojiIds = listOf(R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4, R.id.emoji5)
        for (id in allEmojiIds) {
            val emojiTextView = findViewById<TextView>(id)
            emojiTextView.visibility = if (id == selectedId) {
                TextView.VISIBLE
            } else {
                TextView.INVISIBLE
            }
        }
    }
}
