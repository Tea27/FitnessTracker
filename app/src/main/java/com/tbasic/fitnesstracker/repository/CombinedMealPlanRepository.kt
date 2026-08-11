package com.tbasic.fitnesstracker.repository

import com.tbasic.fitnesstracker.data.MealPlanSelection
import com.tbasic.fitnesstracker.data.local.MealPlanDao
import com.tbasic.fitnesstracker.data.local.MealPlanEntity
import com.tbasic.fitnesstracker.data.mapper.toDto
import com.tbasic.fitnesstracker.data.mapper.toEntity
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class CombinedMealPlanRepository @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val firebaseMealPlanDataSource: FirebaseMealPlanDataSource
) {

    suspend fun getMealPlansForUser(userId: String): List<MealPlanEntity> {
        // prvo probaj dohvatiti lokalno
        val localPlans = mealPlanDao.getMealPlansForUser(userId)
        if (localPlans.isNotEmpty()) {
            return localPlans.sortedBy { it.startDate }
        }

        // ako nema lokalno, probaj dohvatiti s Firebasea
        return try {
            withTimeout(5000) {
                val remotePlans = firebaseMealPlanDataSource.getMealPlansForUser(userId)
                // spremi u lokalnu bazu
                remotePlans.forEach {
                    mealPlanDao.insertMealPlan(it.toEntity())
                }
                remotePlans.map { it.toEntity() }.sortedBy { it.startDate }
            }
        } catch (e: Exception) {
            // fallback - vrati praznu listu ili lokalno ako nešto ima, sortirano
            localPlans.sortedBy { it.startDate }
        }
    }

    suspend fun saveMealPlan(userId: String, mealPlanSelection: MealPlanSelection, startDate: String, endDate: String) {
        // Ako plan nema ID, generiraj novi (ovo znači novi meal plan)
        val id = java.util.UUID.randomUUID().toString()

        // Pretvori u entity sa ID-em i ostalim podacima
        val mealPlanEntity = mealPlanSelection.toEntity(userId, id, startDate, endDate)
        val dto = mealPlanEntity.toDto()

        // Spremi u Firebase pod userId/id
        firebaseMealPlanDataSource.saveMealPlan(userId, dto)

        // Spremi u lokalnu bazu
        mealPlanDao.insertMealPlan(mealPlanEntity)
    }

    suspend fun deleteMealPlan(userId: String, mealPlanId: String) {
        firebaseMealPlanDataSource.deleteMealPlan(userId, mealPlanId)
        val entity = mealPlanDao.getMealPlanById(mealPlanId)
        if (entity != null) {
            mealPlanDao.deleteMealPlan(entity)
        }
    }
}
