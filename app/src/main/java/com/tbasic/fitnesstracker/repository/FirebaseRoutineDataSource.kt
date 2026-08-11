package com.tbasic.fitnesstracker.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.UserRoutine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRoutineDataSource @Inject constructor(
    private val database: FirebaseDatabase
) : RoutineDataSource {

    private val predefinedRef = database.getReference("predefinedRoutines")
    private val userRoutinesRef = database.getReference("userRoutines")

    override suspend fun getPredefinedRoutines(): Map<String, PredefinedRoutine> = suspendCancellableCoroutine { cont ->
        try {
            predefinedRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val routines = mutableMapOf<String, PredefinedRoutine>()
                    result?.children?.forEach { snap ->
                        val routine = snap.getValue(PredefinedRoutine::class.java)
                        val key = snap.key
                        if (routine != null && key != null) {
                            routines[key] = routine.copy(id = key)
                        }
                    }
                    cont.resume(routines)
                } else {
                    cont.resumeWithException(task.exception ?: Exception("Failed to fetch predefined routines"))
                }
            }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    override suspend fun getUserRoutines(userId: String): List<UserRoutine> = suspendCancellableCoroutine { cont ->
        try {
            userRoutinesRef.child(userId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val list = mutableListOf<UserRoutine>()
                        task.result?.children?.forEach { snap ->
                            val routine = snap.getValue(UserRoutine::class.java)
                            val key = snap.key
                            if (routine != null && key != null) {
                                list.add(routine.copy(id = key))
                            }
                        }
                        cont.resume(list)
                    } else {
                        cont.resumeWithException(task.exception ?: Exception("Failed to fetch user routines"))
                    }
                }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    override suspend fun saveUserRoutine(userId: String, routine: UserRoutine) {
        try {
            userRoutinesRef.child(userId).child(routine.id).setValue(routine).await()
        } catch (e: Exception) {
            Log.e("FirebaseRoutineDS", "Failed to save user routine: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteUserRoutine(userId: String, routineId: String) {
        try {
            userRoutinesRef.child(userId).child(routineId).removeValue().await()
        } catch (e: Exception) {
            Log.e("FirebaseRoutineDS", "Failed to delete user routine: ${e.message}", e)
            throw e
        }
    }
}
