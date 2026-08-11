package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tbasic.fitnesstracker.data.RoutineSet
import com.tbasic.fitnesstracker.data.TranslationMap

@Entity(tableName = "predefined_routines")
@TypeConverters(Converters::class)
data class PredefinedRoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val day: String,
    val durationMinutes: Int,
    val description: String,
    val sets: List<RoutineSet>,
    val translations: TranslationMap? = null
)
