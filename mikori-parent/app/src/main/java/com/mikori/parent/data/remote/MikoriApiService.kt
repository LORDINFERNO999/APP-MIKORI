package com.mikori.parent.data.remote

import com.mikori.parent.data.remote.dto.AppCatalogDto
import com.mikori.parent.data.remote.dto.AppRuleDto
import com.mikori.parent.data.remote.dto.AuthData
import com.mikori.parent.data.remote.dto.ChildDto
import com.mikori.parent.data.remote.dto.PauseData
import com.mikori.parent.data.remote.dto.PauseRequest
import com.mikori.parent.data.remote.dto.ScheduleDto
import com.mikori.parent.data.remote.dto.ScheduleRequest
import com.mikori.parent.data.remote.dto.SetAppRulesRequest
import com.mikori.parent.data.remote.dto.CreateChildRequest
import com.mikori.parent.data.remote.dto.Envelope
import com.mikori.parent.data.remote.dto.ForgotPasswordRequest
import com.mikori.parent.data.remote.dto.LimitsData
import com.mikori.parent.data.remote.dto.LinkCodeData
import com.mikori.parent.data.remote.dto.LinkStatusData
import com.mikori.parent.data.remote.dto.LoginRequest
import com.mikori.parent.data.remote.dto.MessageData
import com.mikori.parent.data.remote.dto.RefreshRequest
import com.mikori.parent.data.remote.dto.RegisterRequest
import com.mikori.parent.data.remote.dto.SetLimitsRequest
import com.mikori.parent.data.remote.dto.StatsAppsData
import com.mikori.parent.data.remote.dto.StatsTodayData
import com.mikori.parent.data.remote.dto.StatsWeekData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Contrato REST con la MIKORI API (v1). Las rutas coinciden con
 * mikori-api/config/routes.php.
 */
interface MikoriApiService {

    // ── Auth ──
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Envelope<AuthData>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Envelope<AuthData>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Envelope<AuthData>

    @POST("auth/logout")
    suspend fun logout(): Envelope<MessageData>

    @POST("auth/password/forgot")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Envelope<MessageData>

    // ── Hijos ──
    @GET("children")
    suspend fun children(): Envelope<List<ChildDto>>

    @POST("children")
    suspend fun createChild(@Body body: CreateChildRequest): Envelope<ChildDto>

    @GET("children/{id}")
    suspend fun child(@Path("id") id: Long): Envelope<ChildDto>

    @DELETE("children/{id}")
    suspend fun deleteChild(@Path("id") id: Long): Envelope<MessageData>

    // ── Vinculación ──
    @POST("children/{id}/link-code")
    suspend fun generateLinkCode(@Path("id") id: Long): Envelope<LinkCodeData>

    @GET("children/{id}/link-status")
    suspend fun linkStatus(@Path("id") id: Long): Envelope<LinkStatusData>

    // ── Estadísticas ──
    @GET("children/{id}/stats/today")
    suspend fun statsToday(@Path("id") id: Long): Envelope<StatsTodayData>

    @GET("children/{id}/stats/week")
    suspend fun statsWeek(@Path("id") id: Long): Envelope<StatsWeekData>

    @GET("children/{id}/stats/apps")
    suspend fun statsApps(@Path("id") id: Long): Envelope<StatsAppsData>

    // ── Límites ──
    @GET("children/{id}/limits")
    suspend fun limits(@Path("id") id: Long): Envelope<LimitsData>

    @PUT("children/{id}/limits")
    suspend fun setLimits(@Path("id") id: Long, @Body body: SetLimitsRequest): Envelope<LimitsData>

    // ── V2: Control ──
    @GET("children/{id}/apps")
    suspend fun childApps(@Path("id") id: Long): Envelope<List<AppCatalogDto>>

    @GET("children/{id}/app-rules")
    suspend fun appRules(@Path("id") id: Long): Envelope<List<AppRuleDto>>

    @PUT("children/{id}/app-rules")
    suspend fun setAppRules(@Path("id") id: Long, @Body body: SetAppRulesRequest): Envelope<List<AppRuleDto>>

    @GET("children/{id}/schedules")
    suspend fun schedules(@Path("id") id: Long): Envelope<List<ScheduleDto>>

    @POST("children/{id}/schedules")
    suspend fun createSchedule(@Path("id") id: Long, @Body body: ScheduleRequest): Envelope<ScheduleDto>

    @DELETE("children/{id}/schedules/{sid}")
    suspend fun deleteSchedule(@Path("id") id: Long, @Path("sid") sid: Long): Envelope<MessageData>

    @POST("children/{id}/pause")
    suspend fun startPause(@Path("id") id: Long, @Body body: PauseRequest): Envelope<PauseData>

    @HTTP(method = "DELETE", path = "children/{id}/pause", hasBody = false)
    suspend fun cancelPause(@Path("id") id: Long): Envelope<MessageData>
}
