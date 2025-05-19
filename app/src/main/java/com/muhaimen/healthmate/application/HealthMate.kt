package com.muhaimen.healthmate.application

import android.app.Application
import com.google.firebase.FirebaseApp

class HealthMate: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

}