package com.mikori.parent.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mikori_session")

/**
 * Almacén seguro de la sesión (tokens y datos básicos del usuario) en DataStore.
 */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.ACCESS] }
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { !it[Keys.ACCESS].isNullOrEmpty() }
    val userNameFlow: Flow<String?> = context.dataStore.data.map { it[Keys.USER_NAME] }

    suspend fun save(access: String, refresh: String, name: String?, email: String?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS] = access
            prefs[Keys.REFRESH] = refresh
            name?.let { prefs[Keys.USER_NAME] = it }
            email?.let { prefs[Keys.USER_EMAIL] = it }
        }
    }

    suspend fun updateTokens(access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS] = access
            prefs[Keys.REFRESH] = refresh
        }
    }

    suspend fun refreshToken(): String? = context.dataStore.data.first()[Keys.REFRESH]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /** Lectura bloqueante para el interceptor OkHttp (corre en hilo de red). */
    fun blockingAccessToken(): String? = runBlocking {
        context.dataStore.data.first()[Keys.ACCESS]
    }
}
