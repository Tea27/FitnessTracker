package com.tbasic.fitnesstracker.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.tbasic.fitnesstracker.data.CalorieEntryDto
import com.tbasic.fitnesstracker.data.local.CalorieEntryDao
import com.tbasic.fitnesstracker.data.local.CalorieEntryEntity
import com.tbasic.fitnesstracker.data.mapper.toDto
import com.tbasic.fitnesstracker.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CombinedCalorieRepository(
    private val dao: CalorieEntryDao,
    private val db: FirebaseDatabase
) {
    private val ref = db.getReference("calories")

//    suspend fun insert(entry: CalorieEntryEntity) {
//        dao.insert(entry)
//        ref.child(entry.userId).child(entry.id).setValue(entry.toDto()).await()
//    }

    suspend fun getAll(userId: String): List<CalorieEntryEntity> {
        return try {
            val snap = ref.child(userId).get().await()
            val fromFirebase = snap.children.mapNotNull { it.getValue(CalorieEntryDto::class.java)?.toEntity() }

            // Ažuriraj lokalni cache (Room)
            dao.deleteAllForUser(userId) // izbriši stare za korisnika
            dao.insertAll(fromFirebase)

            fromFirebase.sortedByDescending { it.date } // sortiranje po datumu (timestamp)
        } catch (e: Exception) {
            // Ako Firebase ne uspije, koristi lokalne podatke
            dao.getAllForUser(userId).sortedByDescending { it.date }
        }
    }

    fun observeEntries(userId: String): Flow<List<CalorieEntryEntity>> {
        return dao.getAllForUserFlow(userId)
    }

    suspend fun syncFromFirebase(userId: String) {
        try {
            val snapshot = ref.child(userId).get().await()
            val firebaseEntries = snapshot.children.mapNotNull { it.getValue(CalorieEntryDto::class.java)?.toEntity() }

            dao.deleteAllForUser(userId)
            dao.insertAll(firebaseEntries)
        } catch (e: Exception) {
            Log.e("CombinedCalorieRepository", "syncFromFirebase", e)
        }
    }

    suspend fun insert(entry: CalorieEntryEntity) {
        // Lokalno odmah
        dao.insert(entry)

        // Firebase u pozadini, ne blokira UI
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                db.getReference("calories")
                    .child(entry.userId)
                    .child(entry.id)
                    .setValue(entry.toDto())
                    .await()
            } catch (e: Exception) {
               Log.e("CombinedCalorieRepository", "Firebase insert failed: ${e.message}")
            }
        }
    }

    suspend fun deleteById(userId: String, entryId: String) {
        dao.deleteById(entryId)
        ref.child(userId).child(entryId).removeValue().await()
    }
}
