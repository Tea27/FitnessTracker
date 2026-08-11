package com.tbasic.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises LIMIT :limit OFFSET :offset")
    suspend fun getExercisesPaged(limit: Int, offset: Int): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    @Query("DELETE FROM exercises")
    suspend fun clearAll()

//    @Query("SELECT * FROM exercises WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'")
//    suspend fun searchExercises(query: String): List<ExerciseEntity>
    @RawQuery
    suspend fun searchExercisesRaw(query: SupportSQLiteQuery): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity)
}
