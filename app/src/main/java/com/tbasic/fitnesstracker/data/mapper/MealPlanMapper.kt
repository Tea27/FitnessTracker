package com.tbasic.fitnesstracker.data.mapper

import com.tbasic.fitnesstracker.data.MealPlanDto
import com.tbasic.fitnesstracker.data.MealPlanSelection
import com.tbasic.fitnesstracker.data.local.MealPlanEntity

fun MealPlanSelection.toEntity(userId: String, id: String, startDate: String, endDate: String): MealPlanEntity {
    return MealPlanEntity(
        id = id,
        userId = userId,
        startDate = startDate,
        endDate = endDate,
        meals = this.days
    )
}

fun MealPlanEntity.toMealPlanSelection(): MealPlanSelection {
    return MealPlanSelection(days = this.meals)
}

fun MealPlanEntity.toDto(): MealPlanDto = MealPlanDto(
    id = this.id,
    userId = this.userId,
    startDate = this.startDate,
    endDate = this.endDate,
    meals = this.meals
)

fun MealPlanDto.toEntity(): MealPlanEntity = MealPlanEntity(
    id = this.id,
    userId = this.userId,
    startDate = this.startDate,
    endDate = this.endDate,
    meals = this.meals
)
