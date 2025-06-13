package com.muhaimen.healthmate.data.dataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vitals(
    var id: String = "",
    val waterIntake: Int = 0,
    val sleepHours: Float = 0f,
    val steps: Int = 0,
    val mood: String = "",
    val weight: Float = 0f,
    var date: String = ""
) : Parcelable
{
    constructor() : this(
        id = "",
        waterIntake = 0,
        sleepHours = 0f,
        steps = 0,
        mood = "",
        weight = 0f,
        date = ""
    )
}