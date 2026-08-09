package com.mikori.parent.data.repository

import com.mikori.parent.core.MikoriResult
import com.mikori.parent.core.network.apiCall
import com.mikori.parent.data.remote.MikoriApiService
import com.mikori.parent.data.remote.dto.DayLimitEntry
import com.mikori.parent.data.remote.dto.LimitsData
import com.mikori.parent.data.remote.dto.SetLimitsRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LimitsRepository @Inject constructor(
    private val api: MikoriApiService,
) {
    suspend fun limits(childId: Long): MikoriResult<LimitsData> = apiCall { api.limits(childId) }

    suspend fun setSameForAll(childId: Long, minutes: Int): MikoriResult<LimitsData> =
        apiCall { api.setLimits(childId, SetLimitsRequest(all = minutes)) }

    suspend fun setPerDay(childId: Long, days: List<DayLimitEntry>): MikoriResult<LimitsData> =
        apiCall { api.setLimits(childId, SetLimitsRequest(days = days)) }
}
