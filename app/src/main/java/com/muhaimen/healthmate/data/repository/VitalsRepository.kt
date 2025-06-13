package com.muhaimen.healthmate.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.muhaimen.healthmate.data.dataClass.Vitals
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class VitalsRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val vitalsCollection = db.collection("vitals")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addVitals(userId: String, vitals: Vitals) {
        val today = LocalDate.now().toString()
        vitals.id = today
        vitalsCollection.document(userId)
            .collection("records")
            .document(today)
            .set(vitals)
            .await()
    }

    suspend fun saveStepCount(userId: String, date: String, stepCount: Int) {
        vitalsCollection.document(userId)
            .collection("records")
            .document(date)
            .set(mapOf("steps" to stepCount), SetOptions.merge())
            .await()
    }


    fun getAllVitals(userId: String): LiveData<List<Vitals>> {
        val vitalsLiveData = MutableLiveData<List<Vitals>>()
        FirebaseFirestore.getInstance()
            .collection("vitals")
            .document(userId)
            .collection("records")
            .get()
            .addOnSuccessListener { snapshot ->
                val vitalsList = snapshot.documents.mapNotNull { it.toObject(Vitals::class.java) }
                vitalsLiveData.value = vitalsList
            }
            .addOnFailureListener { exception ->
                vitalsLiveData.value = emptyList()
                Log.e("VitalsRepository", "Error fetching vitals: ", exception)
            }
        return vitalsLiveData
    }




    suspend fun getVitals(userId: String): List<Vitals> {
        val snapshot = vitalsCollection.document(userId)
            .collection("records")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject<Vitals>() }
    }

    suspend fun updateVitals(userId: String, date: String, vitals: Vitals) {
        vitals.id = date // Ensure the ID field matches the document ID
        vitalsCollection.document(userId)
            .collection("records")
            .document(date)
            .set(vitals)
            .await()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodayVitals(userId: String): Vitals? {
        val today = LocalDate.now().toString()
        val snapshot = vitalsCollection.document(userId)
            .collection("records")
            .document(today)
            .get()
            .await()
        return snapshot.toObject<Vitals>()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getStepCount(userId: String): Int {
        val today = LocalDate.now().toString()
        val snapshot = vitalsCollection.document(userId)
            .collection("records")
            .document(today)
            .get()
            .await()
        return snapshot.getLong("steps")?.toInt() ?: 0
    }
}
