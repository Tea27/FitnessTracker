package com.tbasic.fitnesstracker.repository

import com.tbasic.fitnesstracker.data.BaseRoutine
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.data.local.PredefinedRoutineDao
import com.tbasic.fitnesstracker.data.local.UserRoutineDao
import com.tbasic.fitnesstracker.data.mapper.toEntity
import com.tbasic.fitnesstracker.data.mapper.toPredefinedRoutine
import com.tbasic.fitnesstracker.data.mapper.toUserRoutine
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class CombinedRoutineRepository @Inject constructor(
    private val predefinedDao: PredefinedRoutineDao,
    private val userDao: UserRoutineDao,
    private val firebase: RoutineDataSource
) : RoutineRepository {

    override suspend fun getAllRoutines(userId: String): List<BaseRoutine> {
        // prvo dohvat lokalno
        val predefinedLocal = predefinedDao.getAll().map { it.toPredefinedRoutine() }
        val userLocal = userDao.getAllForUser(userId).map { it.toUserRoutine() }

        if (predefinedLocal.isNotEmpty() && userLocal.isNotEmpty()) {
            // Ako ima lokalno, samo vrati to
            return predefinedLocal + userLocal
        }

        // ako nema lokalno, pokušaj s Firebaseom
        return try {
            val predefinedRemote = firebase.getPredefinedRoutines().values.toList()
            val userRemote = firebase.getUserRoutines(userId)

            // spremi u lokalnu bazu
            predefinedDao.clearAll()
            predefinedDao.insertAll(predefinedRemote.map { it.toEntity() })

            userDao.clearAllForUser(userId)
            userDao.insertAll(userRemote.map { it.toEntity(userId) })

            predefinedRemote + userRemote
        } catch (e: Exception) {
            // fallback na lokalno (koje može biti prazno)
            predefinedLocal + userLocal
        }
    }

    override suspend fun getUserRoutines(userId: String): List<UserRoutine> {
        val local = userDao.getAllForUser(userId).map { it.toUserRoutine() }
        if (local.isNotEmpty()) return local

        return try {
            val remote = firebase.getUserRoutines(userId)
            userDao.clearAllForUser(userId)
            userDao.insertAll(remote.map { it.toEntity(userId) })
            remote
        } catch (e: Exception) {
            local
        }
    }

    override suspend fun getPredefinedRoutines(): List<PredefinedRoutine> {
        //   if (local.isNotEmpty()) return local

        return try {
            withTimeout(5000) {
                val remote = firebase.getPredefinedRoutines().values.toList()
                if (remote.isEmpty()) {
                    return@withTimeout emptyList()
                }
                predefinedDao.insertAll(remote.map { it.toEntity() })
                remote
            }
        } catch (e: Exception) {
            val local = predefinedDao.getAll().map { it.toPredefinedRoutine() }
            local
        }
    }

    override suspend fun saveRoutine(userId: String, routine: BaseRoutine) {
        if (routine is UserRoutine) {
            firebase.saveUserRoutine(userId, routine)
            userDao.insert(routine.toEntity(userId))
        } else {
            // predefined rutine ne bi trebalo mijenjati -> ignorirati ili baciti exception
            throw UnsupportedOperationException("Predefined routines can't be saved here")
        }
    }

    override suspend fun deleteRoutine(userId: String, routineId: String) {
        firebase.deleteUserRoutine(userId, routineId)
        userDao.deleteByIdAndUserId(routineId, userId)
    }
}
