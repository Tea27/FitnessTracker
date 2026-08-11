package com.tbasic.fitnesstracker.data

data class FirebaseMealPlan(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val createdAt: Long = 0L,
    val estimatedFor: EstimatedFor = EstimatedFor(),
    val days: List<DayMealPlanWithDates> = emptyList()
)

data class EstimatedFor(
    val start: String = "",
    val end: String = ""
)
