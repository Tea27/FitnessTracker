package com.tbasic.fitnesstracker.data

data class MealPlanDto(
    val id: String = "",
    val userId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val meals: List<DayMealPlanWithDates> = emptyList()
)
