package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tbasic.fitnesstracker.data.DayMealPlanWithDates

@Entity(tableName = "meal_plan")
@TypeConverters(Converters::class)
data class MealPlanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val startDate: String,
    val endDate: String,
    val meals: List<DayMealPlanWithDates>
)
