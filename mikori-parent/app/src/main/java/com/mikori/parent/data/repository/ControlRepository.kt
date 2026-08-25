package com.mikori.parent.data.repository

import com.mikori.parent.core.MikoriResult
import com.mikori.parent.core.network.apiCall
import com.mikori.parent.data.remote.MikoriApiService
import com.mikori.parent.data.remote.dto.AppCatalogDto
import com.mikori.parent.data.remote.dto.AppRuleDto
import com.mikori.parent.data.remote.dto.AppRuleEntry
import com.mikori.parent.data.remote.dto.PauseData
import com.mikori.parent.data.remote.dto.PauseRequest
import com.mikori.parent.data.remote.dto.ScheduleDto
import com.mikori.parent.data.remote.dto.ScheduleRequest
import com.mikori.parent.data.remote.dto.SetAppRulesRequest
import javax.inject.Inject
import javax.inject.Singleton

/** Repositorio de las funciones de control (V2). */
@Singleton
class ControlRepository @Inject constructor(
    private val api: MikoriApiService,
) {
    suspend fun childApps(childId: Long): MikoriResult<List<AppCatalogDto>> =
        apiCall { api.childApps(childId) }

    suspend fun appRules(childId: Long): MikoriResult<List<AppRuleDto>> =
        apiCall { api.appRules(childId) }

    suspend fun setAppRules(childId: Long, rules: List<AppRuleEntry>): MikoriResult<List<AppRuleDto>> =
        apiCall { api.setAppRules(childId, SetAppRulesRequest(rules)) }

    suspend fun schedules(childId: Long): MikoriResult<List<ScheduleDto>> =
        apiCall { api.schedules(childId) }

    suspend fun createSchedule(childId: Long, req: ScheduleRequest): MikoriResult<ScheduleDto> =
        apiCall { api.createSchedule(childId, req) }

    suspend fun deleteSchedule(childId: Long, sid: Long): MikoriResult<String> =
        when (val r = apiCall { api.deleteSchedule(childId, sid) }) {
            is MikoriResult.Success -> MikoriResult.Success(r.data.message)
            is MikoriResult.Error -> r
        }

    suspend fun startPause(childId: Long, minutes: Int): MikoriResult<PauseData> =
        apiCall { api.startPause(childId, PauseRequest(minutes = minutes)) }

    suspend fun cancelPause(childId: Long): MikoriResult<String> =
        when (val r = apiCall { api.cancelPause(childId) }) {
            is MikoriResult.Success -> MikoriResult.Success(r.data.message)
            is MikoriResult.Error -> r
        }
}
