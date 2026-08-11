package com.tbasic.fitnesstracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalEntryDao {
    // Postojeće suspend funkcije za jednokratne pozive
    @Query("SELECT * FROM goal_entries WHERE userId = :userId ORDER BY startDate DESC")
    suspend fun getGoalsForUser(userId: String): List<GoalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntryEntity)

    @Delete
    suspend fun delete(goal: GoalEntryEntity)

    @Query("DELETE FROM goal_entries")
    suspend fun clearAll()

    @Query("DELETE FROM goal_entries WHERE id = :goalId")
    suspend fun deleteById(goalId: String)

    @Query("SELECT * FROM goal_entries WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestGoalEntryForUser(userId: String): GoalEntryEntity?

    @Query("SELECT * FROM goal_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): GoalEntryEntity?

    // Flow verzije za reaktivno praćenje podataka
    @Query("SELECT * FROM goal_entries WHERE userId = :userId ORDER BY startDate DESC")
    fun getGoalsForUserFlow(userId: String): Flow<List<GoalEntryEntity>>

    @Query("SELECT * FROM goal_entries WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    fun getLatestGoalEntryForUserFlow(userId: String): Flow<GoalEntryEntity?>

    @Query("UPDATE goal_entries SET isCompleted = :isCompleted, currentWeight = :currentWeight WHERE id = :goalId AND userId = :userId")
    suspend fun markGoalAsCompleted(goalId: String, userId: String, isCompleted: Boolean, currentWeight: Float?)
}
