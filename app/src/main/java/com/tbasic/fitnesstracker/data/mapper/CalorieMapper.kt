package com.tbasic.fitnesstracker.data.mapper

import com.tbasic.fitnesstracker.data.CalorieEntryDto
import com.tbasic.fitnesstracker.data.local.CalorieEntryEntity

fun CalorieEntryDto.toEntity() = CalorieEntryEntity(
    id = id,
    userId = userId,
    date = date,
    calories = calories
)

fun CalorieEntryEntity.toDto() = CalorieEntryDto(
    id = id,
    userId = userId,
    date = date,
    calories = calories
)
