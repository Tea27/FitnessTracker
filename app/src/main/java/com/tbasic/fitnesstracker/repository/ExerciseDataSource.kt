package com.tbasic.fitnesstracker.repository

import com.tbasic.fitnesstracker.data.Exercise

interface ExerciseDataSource {
    suspend fun loadExercisesPageSuspend(lastKey: String?): List<Exercise>
    suspend fun searchExercises(query: String): List<Exercise>
    suspend fun getExerciseById(id: String): Exercise?
}
