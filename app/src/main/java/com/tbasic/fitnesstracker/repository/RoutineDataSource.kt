package com.tbasic.fitnesstracker.repository

import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.UserRoutine

interface RoutineDataSource {
    suspend fun getPredefinedRoutines(): Map<String, PredefinedRoutine>
    suspend fun getUserRoutines(userId: String): List<UserRoutine>
    suspend fun saveUserRoutine(userId: String, routine: UserRoutine)
    suspend fun deleteUserRoutine(userId: String, routineId: String)
}
