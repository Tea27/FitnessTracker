package com.tbasic.fitnesstracker.di

import android.app.Application
import androidx.room.Room
import com.tbasic.fitnesstracker.data.local.AppDatabase
import com.tbasic.fitnesstracker.data.local.CalorieEntryDao
import com.tbasic.fitnesstracker.data.local.ExerciseDao
import com.tbasic.fitnesstracker.data.local.GoalEntryDao
import com.tbasic.fitnesstracker.data.local.MealPlanDao
import com.tbasic.fitnesstracker.data.local.PredefinedRoutineDao
import com.tbasic.fitnesstracker.data.local.UserRoutineDao
import com.tbasic.fitnesstracker.data.local.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "exercise_db").build()

    @Provides
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideUserRoutineDao(db: AppDatabase): UserRoutineDao = db.userRoutineDao()

    @Provides
    fun providePredefinedRoutineDao(db: AppDatabase): PredefinedRoutineDao = db.predefinedRoutineDao()

    @Provides
    fun provideUserSettingsDao(db: AppDatabase): UserSettingsDao = db.userSettingsDao()

    @Provides
    fun provideMealPlanDao(db: AppDatabase): MealPlanDao = db.mealPlanDao()

    @Provides
    fun provideGoalEntryDao(db: AppDatabase): GoalEntryDao = db.goalEntryDao()

    @Provides
    fun provideCalorieEntryDao(db: AppDatabase): CalorieEntryDao = db.calorieEntryDao()
}
