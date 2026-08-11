package com.tbasic.fitnesstracker.utils

import com.tbasic.fitnesstracker.BuildConfig
import com.tbasic.fitnesstracker.data.PhotonResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

suspend fun fetchPhotonSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
    if (query.length < 3) return@withContext emptyList() // minimalno 3 znaka za pretragu
    val client = OkHttpClient()
    val url = "${BuildConfig.PHOTON_URL}?q=$query&limit=5"
    val request = Request.Builder()
        .url(url)
        .header("User-Agent", BuildConfig.PHOTON_USER_AGENT)
        .build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) return@withContext emptyList()

    val body = response.body?.string() ?: return@withContext emptyList()
    val json = Json { ignoreUnknownKeys = true }
    val photonResponse = json.decodeFromString<PhotonResponse>(body)

    return@withContext photonResponse.features.map { feature ->
        val p = feature.properties
        listOfNotNull(p.name, p.city, p.country).joinToString(", ")
    }
}
