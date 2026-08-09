package com.mikori.kids.core.network

import com.mikori.kids.core.MikoriResult
import com.mikori.kids.data.remote.dto.ApiError
import com.mikori.kids.data.remote.dto.Envelope
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

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
        MikoriResult.Error("network_error", "Sin conexión con el servidor.")
    } catch (e: Exception) {
        MikoriResult.Error("unexpected_error", e.message ?: "Error inesperado.")
    }
}

private val lenientJson = Json { ignoreUnknownKeys = true }

private fun parseHttpError(e: HttpException): MikoriResult.Error {
    val raw = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
    if (!raw.isNullOrBlank()) {
        try {
            val env = lenientJson.decodeFromString(Envelope.serializer(ApiError.serializer()), raw)
            env.error?.let { return MikoriResult.Error(it.code, it.message) }
        } catch (_: Exception) { }
    }
    val msg = when (e.code()) {
        401 -> "El dispositivo no está vinculado correctamente."
        404 -> "Código no válido o no encontrado."
        410 -> "El código expiró."
        in 500..599 -> "Error del servidor. Inténtalo más tarde."
        else -> "No se pudo completar la operación."
    }
    return MikoriResult.Error("http_${e.code()}", msg)
}
