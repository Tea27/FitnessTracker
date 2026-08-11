package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tbasic.fitnesstracker.vm.FitnessGoal

@Entity(tableName = "goal_entries")
@TypeConverters(Converters::class)
data class GoalEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val goalType: FitnessGoal = FitnessGoal.WORKOUT_COUNT,
    val currentWeight: Float? = null,
    val targetWeight: Float? = null,
    val timePeriodWeeks: Int? = null,
    val workoutFrequency: Int? = null,
    val startDate: Long = 0L,
    val isCompleted: Boolean = false
)
