package com.tbasic.fitnesstracker.data

import kotlinx.serialization.Serializable

@Serializable
data class PhotonResponse(
    val features: List<Feature>
)

@Serializable
data class Feature(
    val properties: Properties
)

@Serializable
data class Properties(
    val name: String,
    val city: String? = null,
    val country: String? = null
)
