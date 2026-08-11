package com.tbasic.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings LIMIT 1")
    suspend fun get(): UserSettingsEntity?

    @Query("UPDATE user_settings SET language = :lang WHERE id = 'singleton'")
    suspend fun updateLanguage(lang: String)

    @Query("UPDATE user_settings SET firstName = :firstName WHERE id = 'singleton'")
    suspend fun updateFirstName(firstName: String)

    @Query("UPDATE user_settings SET lastName = :lastName WHERE id = 'singleton'")
    suspend fun updateLastName(lastName: String)

    // Opcionalno: update cijelog entiteta
    @Update
    suspend fun update(settings: UserSettingsEntity)
}
