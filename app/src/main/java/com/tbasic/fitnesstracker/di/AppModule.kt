package com.tbasic.fitnesstracker.di
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tbasic.fitnesstracker.data.UserPreferences
import com.tbasic.fitnesstracker.data.local.CalorieEntryDao
import com.tbasic.fitnesstracker.data.local.ExerciseDao
import com.tbasic.fitnesstracker.data.local.GoalEntryDao
import com.tbasic.fitnesstracker.data.local.MealPlanDao
import com.tbasic.fitnesstracker.data.local.PredefinedRoutineDao
import com.tbasic.fitnesstracker.data.local.UserRoutineDao
import com.tbasic.fitnesstracker.repository.CombinedCalorieRepository
import com.tbasic.fitnesstracker.repository.CombinedExerciseRepository
import com.tbasic.fitnesstracker.repository.CombinedGoalEntryRepository
import com.tbasic.fitnesstracker.repository.CombinedMealPlanRepository
import com.tbasic.fitnesstracker.repository.CombinedRoutineRepository
import com.tbasic.fitnesstracker.repository.ExerciseDataSource
import com.tbasic.fitnesstracker.repository.ExerciseRepository
import com.tbasic.fitnesstracker.repository.FirebaseMealPlanDataSource
import com.tbasic.fitnesstracker.repository.FirebaseRepository
import com.tbasic.fitnesstracker.repository.FirebaseRoutineDataSource
import com.tbasic.fitnesstracker.repository.RoutineDataSource
import com.tbasic.fitnesstracker.repository.RoutineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideExerciseDataSource(): ExerciseDataSource = FirebaseRepository()

    @Provides
    @Singleton
    fun provideExerciseRepository(
        firebaseDataSource: ExerciseDataSource,
        dao: ExerciseDao
    ): ExerciseRepository = CombinedExerciseRepository(firebaseDataSource, dao)

    @Provides
    @Singleton
    fun provideRoutineDataSource(firebaseRoutineDataSource: FirebaseRoutineDataSource): RoutineDataSource = firebaseRoutineDataSource

    @Provides
    @Singleton
    fun provideRoutineRepository(
        routineDataSource: RoutineDataSource,
        userRoutineDao: UserRoutineDao,
        predefinedRoutineDao: PredefinedRoutineDao
    ): RoutineRepository = CombinedRoutineRepository(predefinedRoutineDao, userRoutineDao, routineDataSource)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseMealPlanDataSource(
        database: FirebaseDatabase
    ): FirebaseMealPlanDataSource = FirebaseMealPlanDataSource(database)

    @Provides
    @Singleton
    fun provideCombinedMealPlanRepository(
        firebaseMealPlanDataSource: FirebaseMealPlanDataSource,
        mealPlanDao: MealPlanDao
    ): CombinedMealPlanRepository = CombinedMealPlanRepository(mealPlanDao, firebaseMealPlanDataSource)

    @Provides
    @Singleton
    fun provideCombinedGoalEntryRepository(
        goalEntryDao: GoalEntryDao,
        firebaseDatabase: FirebaseDatabase
    ): CombinedGoalEntryRepository {
        return CombinedGoalEntryRepository(goalEntryDao, firebaseDatabase)
    }

    @Provides
    @Singleton
    fun provideCalorieRepository(
        dao: CalorieEntryDao,
        firebaseDatabase: FirebaseDatabase
    ): CombinedCalorieRepository {
        return CombinedCalorieRepository(dao, firebaseDatabase)
    }
}
