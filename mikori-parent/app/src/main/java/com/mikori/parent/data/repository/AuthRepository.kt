package com.mikori.parent.data.repository

import com.mikori.parent.core.MikoriResult
import com.mikori.parent.core.network.apiCall
import com.mikori.parent.data.local.SessionStore
import com.mikori.parent.data.remote.MikoriApiService
import com.mikori.parent.data.remote.dto.AuthData
import com.mikori.parent.data.remote.dto.ForgotPasswordRequest
import com.mikori.parent.data.remote.dto.LoginRequest
import com.mikori.parent.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: MikoriApiService,
    private val session: SessionStore,
) {
    val isLoggedIn: Flow<Boolean> = session.isLoggedInFlow
    val userName: Flow<String?> = session.userNameFlow

    suspend fun register(name: String, email: String, password: String): MikoriResult<AuthData> {
        val result = apiCall { api.register(RegisterRequest(name, email, password)) }
        persist(result)
        return result
    }

    suspend fun login(email: String, password: String): MikoriResult<AuthData> {
        val result = apiCall { api.login(LoginRequest(email, password)) }
        persist(result)
        return result
    }

    suspend fun forgotPassword(email: String): MikoriResult<String> {
        val result = apiCall { api.forgotPassword(ForgotPasswordRequest(email)) }
        return when (result) {
            is MikoriResult.Success -> MikoriResult.Success(result.data.message)
            is MikoriResult.Error -> result
        }
    }

    suspend fun logout() {
        runCatching { api.logout() }
        session.clear()
    }

    private suspend fun persist(result: MikoriResult<AuthData>) {
        if (result is MikoriResult.Success) {
            val data = result.data
            session.save(
                access = data.accessToken,
                refresh = data.refreshToken,
                name = data.user?.name,
                email = data.user?.email,
            )
        }
    }
}
