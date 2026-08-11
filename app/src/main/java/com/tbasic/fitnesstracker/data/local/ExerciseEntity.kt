package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tbasic.fitnesstracker.data.Translation

@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val equipment: String,
    val bodyPart: String,
    val target: String,
    val gifFileName: String,
    val description: List<String>,
    val translations: Map<String, Translation>
)
