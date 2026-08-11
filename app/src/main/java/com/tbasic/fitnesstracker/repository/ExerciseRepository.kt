package com.tbasic.fitnesstracker.repository

import com.tbasic.fitnesstracker.data.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    suspend fun loadNextPage(offset: Int): List<Exercise>
    suspend fun searchExercises(query: String): List<Exercise>
    fun searchExercisesWithFallback(query: String): Flow<List<Exercise>>
    suspend fun getExerciseById(id: String): Exercise?
}
