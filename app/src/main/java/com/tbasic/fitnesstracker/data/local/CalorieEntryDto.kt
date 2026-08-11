package com.tbasic.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalorieEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CalorieEntryEntity)

    @Query("DELETE FROM calorie_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM calorie_entries WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<CalorieEntryEntity>)

    @Query("SELECT * FROM calorie_entries WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllForUser(userId: String): List<CalorieEntryEntity>

    @Query("SELECT * FROM calorie_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllForUserFlow(userId: String): Flow<List<CalorieEntryEntity>>
}
