package com.tbasic.fitnesstracker.data

data class CalorieEntryDto(
    val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val calories: Int = 0
)
