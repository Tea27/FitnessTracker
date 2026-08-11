package com.tbasic.fitnesstracker.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.tbasic.fitnesstracker.data.GoalEntryDto
import com.tbasic.fitnesstracker.data.local.GoalEntryDao
import com.tbasic.fitnesstracker.data.local.GoalEntryEntity
import com.tbasic.fitnesstracker.data.mapper.toDto
import com.tbasic.fitnesstracker.data.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CombinedGoalEntryRepository(
    private val dao: GoalEntryDao,
    private val realtimeDb: FirebaseDatabase
) {
    private val goalsRef = realtimeDb.getReference("goals")

    suspend fun uploadGoalToFirebase(goal: GoalEntryEntity) {
        try {
            goalsRef.child(goal.userId).child(goal.id).setValue(goal.toDto()).await()
        } catch (e: Exception) {
            Log.e("GoalRepo", "Upload failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun fetchGoalsFromFirebase(userId: String): List<GoalEntryEntity> {
        return try {
            val snapshot = goalsRef.child(userId).get().await()
            snapshot.children
                .mapNotNull { it.getValue(GoalEntryDto::class.java)?.toEntity() }
                .sortedByDescending { it.startDate } 
        } catch (e: Exception) {
            Log.e("GoalRepo", "Fetch failed: ${e.message}", e)
            emptyList()
        }
    }

    // FLOW koji prati sve korisnikove ciljeve iz lokalne baze
    fun getAllGoalsForUserFlow(userId: String): Flow<List<GoalEntryEntity>> =
        dao.getGoalsForUserFlow(userId)
            .map { it.sortedByDescending { goal -> goal.startDate } }
            .flowOn(Dispatchers.IO)

    // Sync down: povuci sve sa Firebase i upiši u lokalnu bazu (možeš emitovati status sa posebnim Flow ako želiš)
    suspend fun syncDown(userId: String) {
        val firebaseGoals = fetchGoalsFromFirebase(userId)
        firebaseGoals.forEach { dao.insert(it) }
    }

    // Save i sync sa Firebase
    suspend fun saveAndSync(goal: GoalEntryEntity) {
        dao.insert(goal)
        uploadGoalToFirebase(goal)
    }

    suspend fun deleteGoal(userId: String, goalId: String) {
        try {
            goalsRef.child(userId).child(goalId).removeValue().await()
            dao.deleteById(goalId)
        } catch (e: Exception) {
            Log.e("GoalRepo", "Delete failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun markGoalAsCompleted(goalId: String, userId: String) {
        dao.markGoalAsCompleted(goalId, userId, true, 100f)
        val updatedGoal = dao.getById(goalId) ?: return
        uploadGoalToFirebase(updatedGoal)
        Log.d("GoalRepo", "Goal marked as completed: $updatedGoal")
    }

    suspend fun getAllGoalsFromLocal(userId: String): List<GoalEntryEntity> {
        return dao.getGoalsForUser(userId).sortedByDescending { it.startDate }
    }

    suspend fun getLatestGoalEntryForUser(userId: String): GoalEntryEntity? {
        return dao.getLatestGoalEntryForUser(userId)
    }

    // Flow za najnoviji cilj korisnika - prvo lokalno, pa ako nije svježe, sa Firebase i update baze
    fun getLatestGoalEntryForUserFlow(userId: String): Flow<GoalEntryEntity?> = flow {
        // 1. Dohvati lokalno, ali uzmi samo nezavršene
        val localEntry = dao.getGoalsForUser(userId)
            .filter { !it.isCompleted }
            .maxByOrNull { it.startDate }

        emit(localEntry)
        Log.d("GoalRepo", "Local uncompleted: $localEntry")

        // 2. Dohvati sa Firebase, također filtriraj
        val firebaseGoals = fetchGoalsFromFirebase(userId)
        val latest = firebaseGoals
            .filter { !it.isCompleted }
            .maxByOrNull { it.startDate }

        Log.d("GoalRepo", "Firebase uncompleted: $latest")

        latest?.let {
            dao.insert(it)
            emit(it)
        }
    }.flowOn(Dispatchers.IO)
}
