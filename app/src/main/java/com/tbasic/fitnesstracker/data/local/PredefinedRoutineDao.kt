package com.tbasic.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PredefinedRoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routines: List<PredefinedRoutineEntity>)

    @Query("SELECT * FROM predefined_routines")
    suspend fun getAll(): List<PredefinedRoutineEntity>

    @Query("DELETE FROM predefined_routines")
    suspend fun clearAll()
}
