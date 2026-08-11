package com.tbasic.fitnesstracker.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tbasic.fitnesstracker.data.Exercise
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRepository : ExerciseDataSource {

    private val databaseRef = FirebaseDatabase.getInstance().getReference("exercises")
    private val pageSize = 10

    private fun createQuery(lastKey: String?) = if (lastKey == null) {
        databaseRef.orderByKey().limitToFirst(pageSize)
    } else {
        databaseRef.orderByKey().startAt(lastKey).limitToFirst(pageSize + 1)
    }

    override suspend fun loadExercisesPageSuspend(lastKey: String?): List<Exercise> =
        suspendCancellableCoroutine { cont ->
            val query = createQuery(lastKey)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (cont.isActive) {
                        val result = snapshot.children.mapNotNull { it.getValue(Exercise::class.java) }
                        val finalList = if (lastKey != null) result.drop(1) else result
                        cont.resume(finalList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (cont.isActive) {
                        cont.resumeWithException(error.toException())
                    }
                }
            }

            query.addListenerForSingleValueEvent(listener)

            cont.invokeOnCancellation {
                query.removeEventListener(listener)
            }
        }

    override suspend fun searchExercises(query: String): List<Exercise> =
        suspendCancellableCoroutine { cont ->
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val all = snapshot.children.mapNotNull { it.getValue(Exercise::class.java) }
                    val normalizedQuery = query.trim().lowercase()

                    val filtered = all.filter { exercise ->
                        val name = exercise.name.trim().lowercase()

                        normalizedQuery.split(" ").all { part ->
                            name.contains(part)
                        }
                    }
                    Log.d("Filtered search FirebaseRepo", filtered.toString())
                    cont.resume(filtered)
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resumeWithException(error.toException())
                }
            })
        }

    override suspend fun getExerciseById(id: String): Exercise? {
        return try {
            val snapshot = databaseRef.child(id).get().await()
            snapshot.getValue(Exercise::class.java)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to fetch exercise by id", e)
            null
        }
    }
}
