package com.tbasic.fitnesstracker.data.mapper

import android.content.Context
import com.tbasic.fitnesstracker.data.EditableRoutine
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.RoutineExercise
import com.tbasic.fitnesstracker.data.RoutineSet
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.data.local.PredefinedRoutineEntity
import com.tbasic.fitnesstracker.data.local.UserRoutineEntity
import java.util.UUID

fun UserRoutine.toEntity(userId: String): UserRoutineEntity = UserRoutineEntity(
    id = id,
    name = name,
    userId = userId,
    day = day,
    durationMinutes = durationMinutes,
    description = description,
    sets = sets,
    createdAt = createdAt,
    completed = completed,
    startedAt = startedAt,
    finishedAt = finishedAt,
    durationPerformedMillis = durationPerformedMillis
)

fun UserRoutineEntity.toUserRoutine(): UserRoutine = UserRoutine(
    id = id,
    name = name,
    day = day,
    durationMinutes = durationMinutes,
    description = description,
    sets = sets,
    createdAt = createdAt,
    completed = completed,
    startedAt = startedAt,
    finishedAt = finishedAt,
    durationPerformedMillis = durationPerformedMillis
)

fun PredefinedRoutine.toEntity(): PredefinedRoutineEntity = PredefinedRoutineEntity(
    id = id,
    name = name,
    day = day,
    durationMinutes = durationMinutes,
    description = description,
    sets = sets,
    translations = translations
)

fun PredefinedRoutineEntity.toPredefinedRoutine(): PredefinedRoutine = PredefinedRoutine(
    id = id,
    name = name,
    day = day,
    durationMinutes = durationMinutes,
    description = description,
    sets = sets,
    translations = translations
)

fun UserRoutine.toPredefinedFormat(): PredefinedRoutine {
    return PredefinedRoutine(
        id = id,
        name = name,
        day = day,
        durationMinutes = durationMinutes,
        description = description,
        sets = sets
    )
}


fun PredefinedRoutine.copyForRepeat(): PredefinedRoutine = PredefinedRoutine(
    id = UUID.randomUUID().toString(),
    name = this.name,
    day = this.day,
    durationMinutes = this.durationMinutes,
    description = this.description,
    sets = this.sets.map { set ->
        set.copy(
            exercises = set.exercises.map { it.copy(done = listOf(false)) }
        )
    }
)

fun PredefinedRoutine.localize(context: Context): PredefinedRoutine {
    val lang = context.resources.configuration.locales.get(0)?.language ?: "en"
    val t = this.translations?.get(lang) ?: return this

    return this.copy(
        name = t.name ?: name,
        day = t.day ?: day,
        description = t.description ?: description
    )
}

fun EditableRoutine.toPredefinedRoutine(routineDay: String? = null): PredefinedRoutine {
    return PredefinedRoutine(
        id = id,
        name = name,
        durationMinutes = durationMinutes,
        day = routineDay ?: day,
        description = description,
        translations = null,
        sets = sets.map { editableSet ->
            RoutineSet(
                repeat = editableSet.repeat,
                restAfterSet = editableSet.restAfterSet,
                exercises = editableSet.exercises.map { editableExercise ->
                    RoutineExercise(
                        exerciseId = editableExercise.exercise.id,
                        reps = editableExercise.reps,
                        durationSeconds = editableExercise.durationSeconds
                    )
                }
            )
        }
    )
}
