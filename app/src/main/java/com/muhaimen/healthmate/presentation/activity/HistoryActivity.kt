package com.muhaimen.healthmate.presentation.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.muhaimen.healthmate.R
import com.muhaimen.healthmate.data.repository.VitalsRepository
import com.muhaimen.healthmate.databinding.ActivityHistoryBinding
import com.muhaimen.healthmate.presentation.adapter.HistoryAdapter
import com.muhaimen.healthmate.viewModel.VitalsViewModel
import com.muhaimen.healthmate.viewModel.VitalsViewModelFactory

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var barChart: BarChart
    private lateinit var vitalsViewModel: VitalsViewModel
    private lateinit var historyAdapter: HistoryAdapter

    companion object {
        private const val TAG = "HistoryActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation buttons
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.dashboardButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.addEntryButton.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }

        // Setup Bar Chart
        barChart = binding.trendsChart
        setupBarChart()

        // Setup RecyclerView
        historyAdapter = HistoryAdapter(emptyList())
        binding.historyRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }

        // Initialize ViewModel
        Log.d(TAG, "Initializing ViewModel...")
        val repository = VitalsRepository()
        val factory = VitalsViewModelFactory(repository)
        vitalsViewModel = viewModels<VitalsViewModel> { factory }.value

        // Observe and update data
        Log.d(TAG, "Observing vitals history...")
        vitalsViewModel.getVitalsHistory().observe(this) { vitalsList ->
            Log.d(TAG, "Received vitals list: ${vitalsList?.size} items")

            if (vitalsList == null || vitalsList.isEmpty()) {
                Log.w(TAG, "Vitals list is null or empty.")
                return@observe
            }

            try {
                // Update Bar Chart
                val entries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                vitalsList.sortedBy { it.date }.forEachIndexed { index, vitals ->
                    entries.add(BarEntry(index.toFloat(), vitals.steps.toFloat()))
                    labels.add(vitals.date)
                    Log.d(TAG, "Chart Entry - Date: ${vitals.date}, Steps: ${vitals.steps}")
                }

                val dataSet = BarDataSet(entries, "Steps Count").apply {
                    color = Color.parseColor("#4CAF50")
                    valueTextColor = Color.BLACK
                    valueTextSize = 12f
                }

                barChart.data = BarData(dataSet)
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                barChart.xAxis.labelRotationAngle = -45f
                barChart.invalidate()

                // Update RecyclerView
                val sortedList = vitalsList.sortedByDescending { it.date }
                historyAdapter.updateList(sortedList)
                Log.d(TAG, "Updated RecyclerView with ${sortedList.size} items")

            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI with vitals list", e)
            }
        }
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            setFitBars(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            animateY(1000)
        }
        Log.d(TAG, "Bar chart setup complete.")
    }
}
