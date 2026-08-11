package com.tbasic.fitnesstracker.data.mapper

import com.tbasic.fitnesstracker.data.GoalEntryDto
import com.tbasic.fitnesstracker.data.local.GoalEntryEntity

fun GoalEntryDto.toEntity() = GoalEntryEntity(
    id = id,
    userId = userId,
    goalType = goalType,
    currentWeight = currentWeight,
    targetWeight = targetWeight,
    timePeriodWeeks = timePeriodWeeks,
    workoutFrequency = workoutFrequency,
    startDate = startDate,
    isCompleted = this.isCompleted
)

fun GoalEntryEntity.toDto() = GoalEntryDto(
    id = id,
    userId = userId,
    goalType = goalType,
    currentWeight = currentWeight,
    targetWeight = targetWeight,
    timePeriodWeeks = timePeriodWeeks,
    workoutFrequency = workoutFrequency,
    startDate = startDate,
    isCompleted = this.isCompleted
)
