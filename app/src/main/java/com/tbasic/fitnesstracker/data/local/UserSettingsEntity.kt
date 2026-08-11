package com.tbasic.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tbasic.fitnesstracker.vm.FitnessGoal

// @Entity(tableName = "user_settings")
// data class UserSettingsEntity(
//    @PrimaryKey val id: String = "singleton",
//    val language: String = "en",
//    val email: String = "en",
//    val firstName: String = "",
//    val lastName: String = ""
// )
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: String = "singleton",
    val language: String = "en",
    val email: String = "en",
    val firstName: String = "",
    val lastName: String = "",
    val weight: Float? = null,
    val height: Float? = null,
    val location: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val goal: FitnessGoal? = null,
    val targetWeight: Float? = null
)
