package com.tbasic.fitnesstracker.repository

import com.google.firebase.database.FirebaseDatabase
import com.tbasic.fitnesstracker.data.MealPlanDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseMealPlanDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {

    private val mealPlansRef = database.getReference("mealPlans")

    suspend fun getMealPlansForUser(userId: String): List<MealPlanDto> = suspendCoroutine { cont ->
        mealPlansRef.child(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val list = mutableListOf<MealPlanDto>()
                task.result?.children?.forEach { snap ->
                    val plan = snap.getValue(MealPlanDto::class.java)
                    val key = snap.key
                    if (plan != null && key != null) {
                        list.add(plan.copy(id = key))
                    }
                }
                cont.resume(list)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Failed to fetch meal plans"))
            }
        }
    }

    suspend fun saveMealPlan(userId: String, mealPlan: MealPlanDto) {
        mealPlansRef.child(userId).child(mealPlan.id).setValue(mealPlan).await()
    }

    suspend fun deleteMealPlan(userId: String, mealPlanId: String) {
        mealPlansRef.child(userId).child(mealPlanId).removeValue().await()
    }
}
