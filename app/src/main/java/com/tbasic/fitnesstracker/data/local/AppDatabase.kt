package com.tbasic.fitnesstracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ExerciseEntity::class, UserRoutineEntity::class, PredefinedRoutineEntity::class, UserSettingsEntity::class, MealPlanEntity::class, GoalEntryEntity::class, CalorieEntryEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userRoutineDao(): UserRoutineDao
    abstract fun predefinedRoutineDao(): PredefinedRoutineDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun goalEntryDao(): GoalEntryDao
    abstract fun calorieEntryDao(): CalorieEntryDao
}
