package com.mikori.kids.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mikori_kids_device")

/**
 * Sesión del dispositivo: UID estable, token de dispositivo y datos del hijo vinculado.
 */
@Singleton
class DeviceSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DEVICE_UID = stringPreferencesKey("device_uid")
        val DEVICE_TOKEN = stringPreferencesKey("device_token")
        val CHILD_ID = longPreferencesKey("child_id")
        val CHILD_NAME = stringPreferencesKey("child_name")
    }

    val isLinkedFlow: Flow<Boolean> = context.dataStore.data.map { !it[Keys.DEVICE_TOKEN].isNullOrEmpty() }

    suspend fun saveLink(token: String, childId: Long) {
        context.dataStore.edit {
            it[Keys.DEVICE_TOKEN] = token
            it[Keys.CHILD_ID] = childId
        }
    }

    suspend fun saveChildName(name: String) {
        context.dataStore.edit { it[Keys.CHILD_NAME] = name }
    }

    suspend fun clear() {
        val uid = deviceUid() // conserva el UID del dispositivo
        context.dataStore.edit {
            it.clear()
            it[Keys.DEVICE_UID] = uid
        }
    }

    /** UID estable del dispositivo; se genera una vez y persiste. */
    suspend fun deviceUid(): String {
        val current = context.dataStore.data.first()[Keys.DEVICE_UID]
        if (!current.isNullOrEmpty()) return current
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { it[Keys.DEVICE_UID] = generated }
        return generated
    }

    /** Lectura bloqueante del token para el interceptor OkHttp. */
    fun blockingToken(): String? = runBlocking { context.dataStore.data.first()[Keys.DEVICE_TOKEN] }
}
