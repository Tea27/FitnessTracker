package com.tbasic.fitnesstracker.repository

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.data.local.ExerciseDao
import com.tbasic.fitnesstracker.data.mapper.toEntity
import com.tbasic.fitnesstracker.data.mapper.toExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class CombinedExerciseRepository @Inject constructor(
    private val firebase: ExerciseDataSource,
    private val dao: ExerciseDao
) : ExerciseRepository {

    private val pageSize = 10
    private var lastFetchedKey: String? = null
    private var noMoreData = false

    override suspend fun loadNextPage(offset: Int): List<Exercise> {
        if (noMoreData) return emptyList()

        return try {
            withTimeout(10000) { // npr. 4 sekunde
                Log.d("pozvan try", "nesto")
                val exercises = firebase.loadExercisesPageSuspend(lastFetchedKey)

                if (exercises.isEmpty()) {
                    noMoreData = true
                    return@withTimeout emptyList()
                }

                lastFetchedKey = exercises.last().id
                dao.insertAll(exercises.map { it.toEntity() })
                exercises
            }
        } catch (e: Exception) {
            Log.e("CombinedRepo", "Firebase error, falling back to Room", e)
            val local = dao.getExercisesPaged(pageSize, offset).map { it.toExercise() }
            if (local.isEmpty()) {
                noMoreData = true
            }
            local
        }
    }

    fun buildSearchQuery(keywords: List<String>): SimpleSQLiteQuery {
        val baseQuery = StringBuilder("SELECT * FROM exercises WHERE ")
        val args = mutableListOf<String>()

        keywords.forEachIndexed { index, word ->
            if (index > 0) baseQuery.append(" AND ")
            baseQuery.append("LOWER(name) LIKE ?")
            args.add("%${word.lowercase()}%")
        }

        return SimpleSQLiteQuery(baseQuery.toString(), args.toTypedArray())
    }

    override suspend fun searchExercises(query: String): List<Exercise> {
//        return dao.searchExercises(query).map { it.toExercise() }
        val keywords = query.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (keywords.isEmpty()) return emptyList()

        val sqlQuery = buildSearchQuery(keywords)
        return dao.searchExercisesRaw(sqlQuery).map { it.toExercise() }
    }

    override fun searchExercisesWithFallback(query: String): Flow<List<Exercise>> = flow {
        // 1. Emit local immediately
        val local = searchExercises(query)
        emit(local)

        try {
            // 2. Fetch remote
            val remote = firebase.searchExercises(query)

            // 3. Save to DB/cache
            dao.insertAll(remote.map { it.toEntity() })

            // 4. Emit remote as updated result
            emit(remote)
        } catch (e: Exception) {
            // Failed remote? just keep local
            // Optionally: log the error
        }
    }

    override suspend fun getExerciseById(id: String): Exercise? {
        // 1. Lokalno
        val local = dao.getById(id)?.toExercise()
        if (local != null) return local

        // 2. Ako nema lokalno → Firebase
        return try {
            val remote = firebase.getExerciseById(id)
            remote?.let { dao.insert(it.toEntity()) } // cache ga za drugi put
            remote
        } catch (e: Exception) {
            Log.e("CombinedRepo", "Failed to fetch exercise by id", e)
            null
        }
    }
}
