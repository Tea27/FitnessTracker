package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.UUID

@Entity(tableName = "calorie_entries")
@TypeConverters(Converters::class)
data class CalorieEntryEntity(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val calories: Int = 0
) {
    companion object {
        fun create(date: Long, calories: Int, userId: String): CalorieEntryEntity {
            return CalorieEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                calories = calories
            )
        }
    }
}
