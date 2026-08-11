package com.tbasic.fitnesstracker.data

import com.google.firebase.database.PropertyName
import com.tbasic.fitnesstracker.vm.FitnessGoal

data class GoalEntryDto(
    val id: String = "", // Firebase document ID
    val userId: String = "",
    val goalType: FitnessGoal = FitnessGoal.WORKOUT_COUNT,
    val currentWeight: Float? = null,
    val targetWeight: Float? = null,
    val timePeriodWeeks: Int? = null,
    val workoutFrequency: Int? = null,
    val startDate: Long = 0L,
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var isCompleted: Boolean = false
)
