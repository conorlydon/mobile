package com.example.mobile

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class StoredSession(
    val accessToken: String,
    val refreshToken: String
)

private val Context.sessionDataStore by preferencesDataStore(name = "session_store")

class SessionManager(private val context: Context) {

    private object Keys {
        val ACCESS_TOKEN: Preferences.Key<String> = stringPreferencesKey("access_token")
        val REFRESH_TOKEN: Preferences.Key<String> = stringPreferencesKey("refresh_token")
    }

    suspend fun saveSession(session: StoredSession) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = session.accessToken
            prefs[Keys.REFRESH_TOKEN] = session.refreshToken
        }
    }

    suspend fun readSession(): StoredSession? {
        return context.sessionDataStore.data
            .map { prefs ->
                val access = prefs[Keys.ACCESS_TOKEN]
                val refresh = prefs[Keys.REFRESH_TOKEN]
                if (access.isNullOrBlank() || refresh.isNullOrBlank()) null
                else StoredSession(access, refresh)
            }
            .first()
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
        }
    }
}
