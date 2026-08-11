package com.tbasic.fitnesstracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "user_prefs")

    companion object {
        val EMAIL_KEY = stringPreferencesKey("email_key")
    }

    val emailFlow: Flow<String?>
        get() = context.dataStore.data.map { prefs ->
            prefs[EMAIL_KEY]
        }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { prefs ->
            prefs[EMAIL_KEY] = email
        }
    }

    suspend fun clearEmail() {
        context.dataStore.edit { prefs ->
            prefs.remove(EMAIL_KEY)
        }
    }
}
