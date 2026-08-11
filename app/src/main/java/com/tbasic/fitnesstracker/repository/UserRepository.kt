package com.tbasic.fitnesstracker.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tbasic.fitnesstracker.data.AppUser
import com.tbasic.fitnesstracker.data.local.UserSettingsDao
import com.tbasic.fitnesstracker.data.local.UserSettingsEntity
import com.tbasic.fitnesstracker.vm.FitnessGoal
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val firebaseAuth: FirebaseAuth
) {

    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    // Sprema cijeli korisnički profil u Firebase pod 'users/{userId}'
    suspend fun saveUserProfile(user: AppUser) {
        usersRef.child(user.id).setValue(user).awaitFirebase()
    }

    // Spremi jezik lokalno (Room / DAO)
    suspend fun saveLanguageToLocal(lang: String) {
        val existing = userSettingsDao.get()
        if (existing == null) {
            userSettingsDao.insert(UserSettingsEntity(language = lang))
        } else {
            userSettingsDao.updateLanguage(lang)
        }
    }

    // Dohvati jezik iz lokalnog keša
    suspend fun getLanguageFromLocal(): String {
        return userSettingsDao.get()?.language ?: "en"
    }

    // Spremi jezik na Firebase (u polje language korisnika)
    suspend fun saveLanguageToRemote(lang: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        usersRef.child(uid).child("language").setValue(lang).awaitFirebase()
    }

    // Sprema jezik i lokalno i remote
    suspend fun persistLanguageEverywhere(lang: String) {
        saveLanguageToLocal(lang)
        saveLanguageToRemote(lang)
    }

//    suspend fun updatePhysicalData(
//        userId: String,
//        weight: Float?,
//        height: Float?,
//        location: String?,
//        gender: String?,
//        birthDate: String?,
//        goal: FitnessGoal?,
//        targetWeight: Float?
//    ) {
//        val updates = mutableMapOf<String, Any>()
//
//        weight?.let { updates["weight"] = it }
//        height?.let { updates["height"] = it }
//        location?.let { updates["location"] = it }
//        gender?.let { updates["gender"] = it }
//        birthDate?.let { updates["birthDate"] = it }
//        goal?.let { updates["goal"] = it.name }
//        targetWeight?.let { updates["targetWeight"] = it }
//
//        usersRef.child(userId).updateChildren(updates).awaitFirebase()
//    }

    suspend fun updatePhysicalData(
        userId: String,
        weight: Float? = null,
        height: Float? = null,
        location: String? = null,
        gender: String? = null,
        birthDate: String? = null,
        goal: FitnessGoal? = null,
        targetWeight: Float? = null
    ) {
        val updates = mutableMapOf<String, Any?>() // ➤ Any? (dozvoli null vrijednosti!)

        updates["weight"] = weight
        updates["height"] = height
        updates["location"] = location
        updates["gender"] = gender
        updates["birthDate"] = birthDate
        updates["goal"] = goal?.name
        updates["targetWeight"] = targetWeight

        usersRef.child(userId).updateChildren(updates).awaitFirebase()
    }

    suspend fun syncLanguage(): String {
        return try {
            val remoteLang = getLanguageFromRemote()
            if (!remoteLang.isNullOrEmpty()) {
                saveLanguageToLocal(remoteLang)
                remoteLang
            } else {
                getLocalOrDefault()
            }
        } catch (e: Exception) {
            getLocalOrDefault()
        }
    }

    private suspend fun getLanguageFromRemote(): String? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        val snapshot = usersRef.child(uid).child("language").get().awaitFirebase()
        return snapshot.getValue(String::class.java)
    }

    private suspend fun getLocalOrDefault(): String {
        val localLang = getLanguageFromLocal()
        return if (localLang.isEmpty()) {
            saveLanguageToLocal("en")
            "en"
        } else {
            localLang
        }
    }

    suspend fun fetchUserFromRemote(): AppUser {
        val uid = firebaseAuth.currentUser?.uid ?: throw Exception("No user logged in")
        val snapshot = usersRef.child(uid).get().awaitFirebase()

        val map = snapshot.value as? Map<String, Any> ?: throw Exception("User data not found")

        val goalStr = map["goal"] as? String
        val goalEnum = goalStr?.let { FitnessGoal.valueOf(it) }

        return AppUser(
            id = uid,
            email = map["email"] as? String ?: "",
            firstName = map["firstName"] as? String ?: "",
            lastName = map["lastName"] as? String ?: "",
            language = map["language"] as? String ?: "en",
            weight = (map["weight"] as? Number)?.toFloat(),
            height = (map["height"] as? Number)?.toFloat(),
            location = map["location"] as? String,
            gender = map["gender"] as? String,
            birthDate = map["birthDate"] as? String,
            goal = goalEnum,
            targetWeight = (map["targetWeight"] as? Number)?.toFloat()
        )
    }

    // Dohvati keširanog korisnika iz lokalne baze
    suspend fun getCachedUser(): AppUser? {
        val settings = userSettingsDao.get()
        return settings?.let {
            AppUser(
                id = firebaseAuth.currentUser?.uid ?: "unknown",
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                language = it.language,
                weight = it.weight,
                height = it.height,
                location = it.location,
                gender = it.gender,
                birthDate = it.birthDate,
                goal = it.goal,
                targetWeight = it.targetWeight
            )
        }
    }

    suspend fun cacheUser(user: AppUser) {
        val existing = userSettingsDao.get()
        if (existing == null) {
            userSettingsDao.insert(
                UserSettingsEntity(
                    id = "singleton",
                    language = user.language,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    weight = user.weight,
                    height = user.height,
                    location = user.location,
                    gender = user.gender,
                    birthDate = user.birthDate,
                    goal = user.goal,
                    targetWeight = user.targetWeight
                )
            )
        } else {
            userSettingsDao.update(
                existing.copy(
                    language = user.language,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    weight = user.weight,
                    height = user.height,
                    location = user.location,
                    gender = user.gender,
                    birthDate = user.birthDate,
                    goal = user.goal,
                    targetWeight = user.targetWeight
                )
            )
        }
    }
}

// Extension function za await Firebase Database Task u korutini
suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitFirebase(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> cont.resume(result) }
        addOnFailureListener { exception -> cont.resumeWithException(exception) }
        addOnCanceledListener { cont.cancel() }
    }
