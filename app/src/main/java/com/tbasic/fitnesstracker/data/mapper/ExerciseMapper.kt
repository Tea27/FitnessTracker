package com.tbasic.fitnesstracker.data.mapper

import android.content.Context
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.data.local.ExerciseEntity

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    equipment = equipment,
    bodyPart = bodyPart,
    target = target,
    gifFileName = gifFileName,
    description = description,
    translations = translations
)

fun ExerciseEntity.toExercise(): Exercise = Exercise(
    id = id,
    name = name,
    equipment = equipment,
    bodyPart = bodyPart,
    target = target,
    gifFileName = gifFileName,
    description = description,
    translations = translations
)

fun Exercise.localize(context: Context): Exercise {
    val lang = context.resources.configuration.locales.get(0)?.language ?: "en"
    val t = this.translations?.get(lang) ?: return this

    return this.copy(
        name = t.name,
        target = t.target,
        equipment = t.equipment,
        bodyPart = t.bodyPart,
        description = t.description
    )
}
