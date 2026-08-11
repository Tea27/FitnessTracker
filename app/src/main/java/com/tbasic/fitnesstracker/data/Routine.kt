package com.tbasic.fitnesstracker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class BaseRoutine {
    abstract val id: String
    abstract val name: String
    abstract val durationMinutes: Int
    abstract val day: String
    abstract val sets: List<RoutineSet>
    abstract val description: String
}

abstract class TranslatableRoutine : BaseRoutine() {
    abstract val translations: TranslationMap?
}

data class RoutineTranslation(
    val day: String = "",
    val name: String = "",
    val description: String = ""
)

typealias TranslationMap = Map<String, RoutineTranslation>

data class PredefinedRoutine(
    override val id: String = "",
    override val name: String = "",
    override val durationMinutes: Int = 0,
    override val day: String = "",
    override val sets: List<RoutineSet> = emptyList(),
    override val description: String = "",
    override val translations: TranslationMap? = null
) : TranslatableRoutine()

data class UserRoutine(
    override val id: String = "",
    override val name: String = "",
    override val durationMinutes: Int = 0,
    override val day: String = "",
    override val sets: List<RoutineSet> = emptyList(),
    override val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val durationPerformedMillis: Long? = null
) : BaseRoutine()

data class RoutineSet(
    val repeat: Int = 1,
    val restAfterSet: Int? = null,
    val exercises: List<RoutineExercise> = emptyList()
)

@Parcelize
data class RoutineExercise(
    val exerciseId: String = "",
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    // val done: Boolean = false
    val done: List<Boolean> = emptyList()
) : Parcelable

data class EditableRoutine(
    val id: String = "",
    val name: String = "",
    val day: String = "",
    val durationMinutes: Int = 0,
    val description: String = "",
    val sets: List<EditableRoutineSet> = emptyList()
)

data class EditableRoutineSet(
    val repeat: Int = 1,
    val restAfterSet: Int? = null,
    val exercises: List<EditableExercise> = emptyList()
)
