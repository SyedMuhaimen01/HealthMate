package com.muhaimen.healthmate.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.healthmate.data.dataClass.Vitals
import com.muhaimen.healthmate.data.repository.VitalsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class VitalsViewModel(private val repository: VitalsRepository) : ViewModel() {

    private val _vitalsList = MutableLiveData<List<Vitals>>()
    val vitalsList: LiveData<List<Vitals>> get() = _vitalsList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _todayVitals = MutableLiveData<Vitals?>()
    val todayVitals: LiveData<Vitals?> get() = _todayVitals

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> get() = _stepCount

    private fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchTodayVitals() {
        val userId = getUserId() ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val vitals = repository.getTodayVitals(userId)
                _todayVitals.value = vitals
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load today's vitals"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchVitals() {
        val userId = getUserId() ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                val vitals = repository.getVitals(userId)
                _vitalsList.value = vitals
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addVitals(vitals: Vitals) {
        val userId = getUserId() ?: return
        val today = LocalDate.now().toString()
        vitals.id = today
        vitals.date = today

        viewModelScope.launch {
            try {
                repository.addVitals(userId, vitals)
                fetchVitals()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveStepCount(stepCount: Int) {
        val userId = getUserId() ?: return
        val today = LocalDate.now().toString()

        viewModelScope.launch {
            try {
                repository.saveStepCount(userId, today, stepCount)
                getStepCount()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getStepCount() {
        val userId = getUserId() ?: return
        viewModelScope.launch {
            try {
                val steps = repository.getStepCount(userId)
                _stepCount.value = steps
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getVitalsHistory(): LiveData<List<Vitals>> {
        val userId = getUserId()
            return repository.getAllVitals(userId.toString())
    }

    fun updateVitals(vitals: Vitals) {
        val userId = getUserId() ?: return
        if (vitals.id.isEmpty()) {
            _error.value = "Vitals ID (date) is missing"
            return
        }

        viewModelScope.launch {
            try {
                repository.updateVitals(userId, vitals.id, vitals)
                fetchVitals()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
