package com.tbasic.fitnesstracker.vm

import androidx.lifecycle.ViewModel
import com.tbasic.fitnesstracker.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.SavedStateHandle
import javax.inject.Inject

@HiltViewModel
class EditRoutineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Početna rutina iz savedStateHandle ili default
    private val _routine = MutableStateFlow(
        savedStateHandle.get<EditableRoutine>("initialRoutine")
            ?: EditableRoutine()
    )
    val routine: StateFlow<EditableRoutine> = _routine

    private val _showSearch = MutableStateFlow(false)
    val showSearch: StateFlow<Boolean> = _showSearch

    private val _selectedSetIndex = MutableStateFlow<Int?>(null)
    val selectedSetIndex: StateFlow<Int?> = _selectedSetIndex

    fun setRoutine(routine: EditableRoutine) {
        _routine.value = routine
    }

    fun updateSetRepeat(setIndex: Int, repeat: Int) {
        _routine.update { current ->
            current.copy(
                sets = current.sets.toMutableList().also {
                    it[setIndex] = it[setIndex].copy(repeat = repeat)
                }
            )
        }
    }

    fun updateSetRestAfter(setIndex: Int, rest: Int?) {
        _routine.update { current ->
            current.copy(
                sets = current.sets.toMutableList().also {
                    it[setIndex] = it[setIndex].copy(restAfterSet = rest)
                }
            )
        }
    }

    fun updateExerciseReps(setIndex: Int, exerciseIndex: Int, reps: Int) {
        _routine.update { current ->
            current.copy(
                sets = current.sets.toMutableList().also { setsList ->
                    val exercises = setsList[setIndex].exercises.toMutableList()
                    exercises[exerciseIndex] = exercises[exerciseIndex].copy(reps = reps)
                    setsList[setIndex] = setsList[setIndex].copy(exercises = exercises)
                }
            )
        }
    }

    fun updateExerciseDuration(setIndex: Int, exerciseIndex: Int, duration: Int) {
        _routine.update { current ->
            current.copy(
                sets = current.sets.toMutableList().also { setsList ->
                    val exercises = setsList[setIndex].exercises.toMutableList()
                    exercises[exerciseIndex] = exercises[exerciseIndex].copy(durationSeconds = duration)
                    setsList[setIndex] = setsList[setIndex].copy(exercises = exercises)
                }
            )
        }
    }

    fun deleteExercise(setIndex: Int, exerciseIndex: Int) {
        _routine.update { current ->
            current.copy(
                sets = current.sets.toMutableList().also { setsList ->
                    val exercises = setsList[setIndex].exercises.toMutableList().apply {
                        removeAt(exerciseIndex)
                    }
                    setsList[setIndex] = setsList[setIndex].copy(exercises = exercises)
                }
            )
        }
    }

    fun addExercise(setIndex: Int, exercise: Exercise) {
        _routine.update { current ->
            current.copy(
                sets = current.sets.toMutableList().also { setsList ->
                    val existingExercises = setsList[setIndex].exercises
                    if (existingExercises.none { it.exercise.id == exercise.id }) {
                        val updatedExercises = existingExercises + EditableExercise(exercise)
                        setsList[setIndex] = setsList[setIndex].copy(exercises = updatedExercises)
                    }
                }
            )
        }
    }

    fun addSet() {
        _routine.update { current ->
            current.copy(sets = current.sets + EditableRoutineSet())
        }
    }

    fun moveSet(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        _routine.update { current ->
            val sets = current.sets.toMutableList()

            if (fromIndex !in sets.indices || toIndex !in sets.indices) return@update current

            val item = sets.removeAt(fromIndex)
            sets.add(toIndex, item)

            current.copy(sets = sets)
        }
    }

    fun deleteSet(index: Int) {
        _routine.update { current ->
            current.copy(sets = current.sets.toMutableList().also { it.removeAt(index) })
        }
    }

    fun moveExercise(setIndex: Int, fromIndex: Int, toIndex: Int) {
        _routine.update { current ->
            val sets = current.sets.toMutableList()
            if (setIndex !in sets.indices) return@update current

            val exercises = sets[setIndex].exercises.toMutableList()
            if (fromIndex == toIndex || fromIndex !in exercises.indices || toIndex !in exercises.indices) {
                return@update current
            }

            val item = exercises.removeAt(fromIndex)
            exercises.add(toIndex, item)

            sets[setIndex] = sets[setIndex].copy(exercises = exercises)
            current.copy(sets = sets)
        }
    }

    fun setShowSearch(show: Boolean) {
        _showSearch.value = show
    }

    fun setSelectedSetIndex(index: Int?) {
        _selectedSetIndex.value = index
    }

    fun updateName(name: String) {
        _routine.update { it.copy(name = name) }
    }

    fun updateDuration(duration: Int) {
        _routine.update { it.copy(durationMinutes = duration) }
    }

    fun updateDay(day: String) {
        _routine.update { it.copy(day = day) }
    }

    fun updateDescription(description: String) {
        _routine.update { it.copy(description = description) }
    }
}
