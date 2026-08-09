package com.mikori.parent.core

/**
 * Resultado de una operación de datos. Evita exponer excepciones a la UI.
 */
sealed interface MikoriResult<out T> {
    data class Success<T>(val data: T) : MikoriResult<T>
    data class Error(val code: String, val message: String) : MikoriResult<Nothing>
}

inline fun <T> MikoriResult<T>.onSuccess(block: (T) -> Unit): MikoriResult<T> {
    if (this is MikoriResult.Success) block(data)
    return this
}

inline fun <T> MikoriResult<T>.onError(block: (String) -> Unit): MikoriResult<T> {
    if (this is MikoriResult.Error) block(message)
    return this
}
