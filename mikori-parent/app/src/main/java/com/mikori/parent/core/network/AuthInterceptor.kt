package com.mikori.parent.core.network

import com.mikori.parent.data.local.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Añade el token Bearer del usuario a cada petición autenticada.
 */
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionStore.blockingAccessToken()

        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            original.newBuilder().header("Accept", "application/json").build()
        }

        return chain.proceed(request)
    }
}
