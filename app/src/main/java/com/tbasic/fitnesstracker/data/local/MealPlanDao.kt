package com.tbasic.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MealPlanDao {

    @Query("SELECT * FROM meal_plan WHERE userId = :userId")
    suspend fun getMealPlansForUser(userId: String): List<MealPlanEntity>

    @Query("SELECT * FROM meal_plan WHERE id = :planId")
    suspend fun getMealPlanById(planId: String): MealPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlanEntity)

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlanEntity)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlanEntity)
}
