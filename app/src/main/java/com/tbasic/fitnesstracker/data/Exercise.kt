package com.tbasic.fitnesstracker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Exercise(
    val id: String = "",
    val name: String = "",
    val equipment: String = "",
    val bodyPart: String = "",
    val target: String = "",
    val gifFileName: String = "",
    val description: List<String> = emptyList(),
    val translations: @RawValue Map<String, Translation> = emptyMap()
) : Parcelable

@Parcelize
data class Translation(
    val name: String = "",
    val equipment: String = "",
    val bodyPart: String = "",
    val target: String = "",
    val description: List<String> = emptyList()
) : Parcelable

data class EditableExercise(
    val exercise: Exercise,
    var reps: Int = 10,
    var durationSeconds: Int = 30,
    val restSeconds: Int? = null
)
