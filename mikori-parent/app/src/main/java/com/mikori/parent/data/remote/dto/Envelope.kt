package com.mikori.parent.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Sobre estándar de la MIKORI API: { "data": ..., "error": ... }
 */
@Serializable
data class Envelope<T>(
    val data: T? = null,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
)
