package com.mikori.parent.data.repository

import com.mikori.parent.core.MikoriResult
import com.mikori.parent.core.network.apiCall
import com.mikori.parent.data.remote.MikoriApiService
import com.mikori.parent.data.remote.dto.StatsAppsData
import com.mikori.parent.data.remote.dto.StatsTodayData
import com.mikori.parent.data.remote.dto.StatsWeekData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val api: MikoriApiService,
) {
    suspend fun today(childId: Long): MikoriResult<StatsTodayData> = apiCall { api.statsToday(childId) }
    suspend fun week(childId: Long): MikoriResult<StatsWeekData> = apiCall { api.statsWeek(childId) }
    suspend fun apps(childId: Long): MikoriResult<StatsAppsData> = apiCall { api.statsApps(childId) }
}
