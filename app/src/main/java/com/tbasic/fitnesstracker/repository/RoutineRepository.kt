package com.tbasic.fitnesstracker.repository

import com.tbasic.fitnesstracker.data.BaseRoutine
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.UserRoutine

interface RoutineRepository {
    suspend fun getAllRoutines(userId: String): List<BaseRoutine>
    suspend fun getUserRoutines(userId: String): List<UserRoutine>
    suspend fun getPredefinedRoutines(): List<PredefinedRoutine>
    suspend fun saveRoutine(userId: String, routine: BaseRoutine)
    suspend fun deleteRoutine(userId: String, routineId: String)
}
