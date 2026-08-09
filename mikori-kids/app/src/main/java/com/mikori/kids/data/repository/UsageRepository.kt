package com.mikori.kids.data.repository

import com.mikori.kids.core.MikoriResult
import com.mikori.kids.core.network.apiCall
import com.mikori.kids.data.local.DeviceSessionStore
import com.mikori.kids.data.remote.KidsApiService
import com.mikori.kids.data.remote.dto.TodaySummaryData
import com.mikori.kids.data.remote.dto.UsageUploadRequest
import com.mikori.kids.usage.UsageStatsCollector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    private val api: KidsApiService,
    private val collector: UsageStatsCollector,
    private val session: DeviceSessionStore,
) {
    /** Recolecta el uso de hoy y lo sube. Devuelve true si tuvo éxito. */
    suspend fun collectAndUpload(): Boolean {
        val items = collector.collectToday()
        if (items.isEmpty()) return true // nada que subir (sin permiso o sin uso)
        val result = apiCall { api.uploadUsage(UsageUploadRequest(items)) }
        return result is MikoriResult.Success
    }

    suspend fun heartbeat() {
        runCatching { api.heartbeat() }
    }

    suspend fun today(): MikoriResult<TodaySummaryData> {
        val result = apiCall { api.today() }
        if (result is MikoriResult.Success) {
            result.data.childName?.let { session.saveChildName(it) }
        }
        return result
    }
}
