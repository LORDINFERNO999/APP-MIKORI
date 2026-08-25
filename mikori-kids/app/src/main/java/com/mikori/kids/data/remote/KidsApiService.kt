package com.mikori.kids.data.remote

import com.mikori.kids.data.remote.dto.AcceptedData
import com.mikori.kids.data.remote.dto.Envelope
import com.mikori.kids.data.remote.dto.MessageData
import com.mikori.kids.data.remote.dto.PolicyData
import com.mikori.kids.data.remote.dto.RedeemData
import com.mikori.kids.data.remote.dto.RedeemRequest
import com.mikori.kids.data.remote.dto.TodaySummaryData
import com.mikori.kids.data.remote.dto.UsageUploadRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface KidsApiService {

    /** Canje del código de vinculación (público, sin token). */
    @POST("link/redeem")
    suspend fun redeem(@Body body: RedeemRequest): Envelope<RedeemData>

    /** Ingesta por lotes del uso recolectado (device-auth). */
    @POST("devices/usage")
    suspend fun uploadUsage(@Body body: UsageUploadRequest): Envelope<AcceptedData>

    /** Latido de estado (device-auth). */
    @POST("devices/heartbeat")
    suspend fun heartbeat(): Envelope<MessageData>

    /** Resumen de hoy del hijo vinculado (device-auth). */
    @GET("devices/me/today")
    suspend fun today(): Envelope<TodaySummaryData>

    /** Política de enforcement (V2, device-auth). */
    @GET("devices/me/policy")
    suspend fun policy(): Envelope<PolicyData>
}
