package com.mikori.parent.core.network

import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.remote.dto.ApiError
import com.mikori.parent.data.remote.dto.Envelope
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Ejecuta una llamada a la API que devuelve un Envelope<T> y la traduce a
 * MikoriResult, con manejo centralizado de errores (HTTP, red, parseo).
 */
suspend fun <T> apiCall(block: suspend () -> Envelope<T>): MikoriResult<T> {
    return try {
        val envelope = block()
        val data = envelope.data
        when {
            data != null -> MikoriResult.Success(data)
            envelope.error != null -> MikoriResult.Error(envelope.error.code, envelope.error.message)
            else -> MikoriResult.Error("empty_response", "Respuesta vacía del servidor.")
        }
    } catch (e: HttpException) {
        parseHttpError(e)
    } catch (e: IOException) {
        MikoriResult.Error("network_error", "No hay conexión con el servidor. Revisa tu internet.")
    } catch (e: Exception) {
        MikoriResult.Error("unexpected_error", e.message ?: "Ocurrió un error inesperado.")
    }
}

private val lenientJson = Json { ignoreUnknownKeys = true }

private fun parseHttpError(e: HttpException): MikoriResult.Error {
    val raw = try {
        e.response()?.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    if (!raw.isNullOrBlank()) {
        try {
            val env = lenientJson.decodeFromString(Envelope.serializer(ApiError.serializer()), raw)
            env.error?.let { return MikoriResult.Error(it.code, it.message) }
        } catch (_: Exception) {
            // cae al genérico
        }
    }
    return MikoriResult.Error("http_${e.code()}", defaultMessageFor(e.code()))
}

private fun defaultMessageFor(code: Int): String = when (code) {
    401 -> "Sesión no válida. Inicia sesión de nuevo."
    403 -> "No tienes permiso para esta acción."
    404 -> "No se encontró el recurso."
    409 -> "El recurso ya existe."
    422 -> "Los datos enviados no son válidos."
    in 500..599 -> "Error del servidor. Inténtalo más tarde."
    else -> "No se pudo completar la operación."
}
