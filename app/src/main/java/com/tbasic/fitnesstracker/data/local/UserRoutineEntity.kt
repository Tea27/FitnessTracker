package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tbasic.fitnesstracker.data.RoutineSet

@Entity(tableName = "user_routines")
@TypeConverters(Converters::class)
data class UserRoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val userId: String,
    val day: String,
    val durationMinutes: Int,
    val description: String,
    val sets: List<RoutineSet>,
    val createdAt: Long,
    val completed: Boolean,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val durationPerformedMillis: Long? = null
)
