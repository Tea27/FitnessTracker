package com.tbasic.fitnesstracker.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tbasic.fitnesstracker.data.RoutineSet
import com.tbasic.fitnesstracker.data.UserRoutine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RoutinePlayerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(RoutinePlayerState())
    val uiState: StateFlow<RoutinePlayerState> = _uiState

    fun start(routine: UserRoutine) {
        val expandedSets = routine.sets.flatMap { set ->
            List(set.repeat) {
                set.copy(
                    exercises = set.exercises.map {
                        it.copy(done = listOf(false))
                    }
                )
            }
        }

        _uiState.value = RoutinePlayerState(
            routine = routine.copy(sets = expandedSets),
            originalSets = routine.sets,
            startedAt = System.currentTimeMillis()
        )
    }

    fun setTimer(seconds: Int) {
        _uiState.value = _uiState.value.copy(
            timerSecondsLeft = seconds,
            showTimer = seconds > 0
        )
    }

    fun skipExercise() {
        goToNextExercise(skipDone = true)
    }

    fun tickTimer() {
        val current = _uiState.value
        if (current.timerSecondsLeft > 0) {
            _uiState.value = current.copy(timerSecondsLeft = current.timerSecondsLeft - 1)
        } else {
            _uiState.value = current.copy(showTimer = false)
        }
    }

    fun tickRest() {
        val current = _uiState.value
        if (current.restSecondsLeft > 0) {
            _uiState.value = current.copy(restSecondsLeft = current.restSecondsLeft - 1)
        } else {
            _uiState.value = current.copy(isResting = false)
        }
    }

    fun cancelTimer() {
        _uiState.value = _uiState.value.copy(showTimer = false, timerSecondsLeft = 0)
    }

    fun markCurrentExerciseAsDone() {
        val state = _uiState.value
        val sets = state.routine?.sets ?: return
        if (state.currentSetIndex >= sets.size) return
        val set = sets[state.currentSetIndex]
        if (state.currentExerciseIndex >= set.exercises.size) return

        val newExercises = set.exercises.mapIndexed { index, exercise ->
            if (index == state.currentExerciseIndex) {
                val updatedDone = exercise.done.toMutableList()
                Log.d("test123 updated done", updatedDone.toString())
                if (updatedDone.isNotEmpty()) updatedDone[0] = true
                Log.d("test123 exercise  done", exercise.toString())

                exercise.copy(done = updatedDone)
            } else {
                exercise
            }
        }

        val newSet = set.copy(exercises = newExercises)
        val newSets = sets.toMutableList()
        newSets[state.currentSetIndex] = newSet

        _uiState.value = state.copy(routine = state.routine?.copy(sets = newSets))
    }

    fun goToNextExercise(skipDone: Boolean = false) {
        if (!skipDone) markCurrentExerciseAsDone()
        cancelTimer()

        val state = _uiState.value
        val sets = state.routine?.sets ?: return
        val originalSets = state.originalSets ?: return
        var setIndex = state.currentSetIndex
        var exerciseIndex = state.currentExerciseIndex

        val currentSet = sets.getOrNull(setIndex) ?: return

        if (exerciseIndex < currentSet.exercises.lastIndex) {
            exerciseIndex++
            _uiState.value = state.copy(
                currentExerciseIndex = exerciseIndex,
                isResting = false,
                restSecondsLeft = 0
            )
        } else {
            val originalSetIndex = calculateOriginalSetIndex(setIndex, originalSets)

            if (setIndex < sets.lastIndex) {
                setIndex++
                exerciseIndex = 0
                val rest = originalSets.getOrNull(originalSetIndex)?.restAfterSet ?: 0
                _uiState.value = state.copy(
                    currentSetIndex = setIndex,
                    currentExerciseIndex = exerciseIndex,
                    isResting = rest > 0,
                    restSecondsLeft = rest
                )
            } else {
                val rest = originalSets.getOrNull(originalSetIndex)?.restAfterSet ?: 0
                _uiState.value = state.copy(
                    isResting = rest > 0,
                    restSecondsLeft = rest,
                    isFinished = true,
                    finishedAt = System.currentTimeMillis(),
                    durationPerformedMillis = System.currentTimeMillis() - (state.startedAt ?: System.currentTimeMillis())
                )
            }
        }
    }

    private fun calculateOriginalSetIndex(expandedIndex: Int, originalSets: List<RoutineSet>): Int {
        var count = 0
        for (i in originalSets.indices) {
            count += originalSets[i].repeat
            if (expandedIndex < count) return i
        }
        return originalSets.lastIndex
    }

    fun skipRest() {
        _uiState.value = _uiState.value.copy(isResting = false, restSecondsLeft = 0)
    }

    fun rebuildOriginalSets(): List<RoutineSet> {
        val state = _uiState.value
        val original = state.originalSets ?: return emptyList()
        val performed = state.routine?.sets ?: return original

        return original.mapIndexed { originalSetIndex, originalSet ->
            val startIndex = calculateStartIndex(originalSetIndex, original)
            val relevantExpanded = performed.subList(startIndex, startIndex + originalSet.repeat)

            val updatedExercises = originalSet.exercises.mapIndexed { i, exercise ->
                val allDone = relevantExpanded.mapNotNull { it.exercises.getOrNull(i)?.done?.firstOrNull() }
                exercise.copy(done = allDone)
            }

            originalSet.copy(exercises = updatedExercises)
        }
    }

    private fun calculateStartIndex(index: Int, sets: List<RoutineSet>): Int {
        return sets.take(index).sumOf { it.repeat }
    }
}

data class RoutinePlayerState(
    val routine: UserRoutine? = null,
    val originalSets: List<RoutineSet>? = null,
    val currentSetIndex: Int = 0,
    val currentExerciseIndex: Int = 0,
    val timerSecondsLeft: Int = 0,
    val restSecondsLeft: Int = 0,
    val isResting: Boolean = false,
    val showTimer: Boolean = false,
    val isFinished: Boolean = false,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val durationPerformedMillis: Long = 0
)
