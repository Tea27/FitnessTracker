package com.tbasic.fitnesstracker.data

import com.tbasic.fitnesstracker.vm.FitnessGoal

data class AppUser(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val language: String = "en",
    val weight: Float? = null,
    val height: Float? = null,
    val location: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val goal: FitnessGoal? = null,
    val targetWeight: Float? = null
)
