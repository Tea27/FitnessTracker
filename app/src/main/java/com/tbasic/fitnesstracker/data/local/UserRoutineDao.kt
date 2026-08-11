package com.tbasic.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserRoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: UserRoutineEntity)

    @Query("SELECT * FROM user_routines WHERE userId = :userId")
    suspend fun getAllForUser(userId: String): List<UserRoutineEntity>

    @Query("DELETE FROM user_routines WHERE id = :routineId")
    suspend fun deleteById(routineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routines: List<UserRoutineEntity>)

    @Query("DELETE FROM user_routines WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)

    @Query("DELETE FROM user_routines WHERE id = :routineId AND userId = :userId")
    suspend fun deleteByIdAndUserId(routineId: String, userId: String)
}
