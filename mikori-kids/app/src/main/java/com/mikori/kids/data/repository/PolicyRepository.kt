package com.mikori.kids.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mikori.kids.core.MikoriResult
import com.mikori.kids.core.network.apiCall
import com.mikori.kids.data.remote.KidsApiService
import com.mikori.kids.data.remote.dto.PolicyData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.policyStore: DataStore<Preferences> by preferencesDataStore(name = "mikori_kids_policy")

/**
 * Obtiene y cachea la política de enforcement. El servicio de vigilancia la
 * consulta en memoria (síncrono) y la refresca periódicamente; la caché en disco
 * permite aplicar la última política conocida aunque no haya red.
 */
@Singleton
class PolicyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: KidsApiService,
    private val json: Json,
) {
    private val key = stringPreferencesKey("policy_json")

    @Volatile
    private var cached: PolicyData? = null

    /** Última política conocida (en memoria). */
    fun current(): PolicyData? = cached

    /** Carga la política persistida (llamar una vez al arrancar el servicio). */
    suspend fun loadFromCache(): PolicyData? {
        if (cached != null) return cached
        val raw = context.policyStore.data.first()[key]
        if (!raw.isNullOrBlank()) {
            cached = runCatching { json.decodeFromString(PolicyData.serializer(), raw) }.getOrNull()
        }
        return cached
    }

    /** Refresca desde el servidor y persiste. Devuelve la política vigente. */
    suspend fun refresh(): PolicyData? {
        when (val r = apiCall { api.policy() }) {
            is MikoriResult.Success -> {
                cached = r.data
                runCatching {
                    val raw = json.encodeToString(PolicyData.serializer(), r.data)
                    context.policyStore.edit { it[key] = raw }
                }
            }
            is MikoriResult.Error -> { /* mantiene la caché anterior */ }
        }
        return cached
    }
}
