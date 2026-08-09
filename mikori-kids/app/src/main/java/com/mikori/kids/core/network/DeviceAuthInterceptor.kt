package com.mikori.kids.core.network

import com.mikori.kids.data.local.DeviceSessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Añade el token de dispositivo (Bearer) a las peticiones autenticadas. */
class DeviceAuthInterceptor @Inject constructor(
    private val session: DeviceSessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.blockingToken()
        val builder = chain.request().newBuilder().header("Accept", "application/json")
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
