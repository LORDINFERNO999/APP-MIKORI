package com.mikori.kids.core

sealed interface MikoriResult<out T> {
    data class Success<T>(val data: T) : MikoriResult<T>
    data class Error(val code: String, val message: String) : MikoriResult<Nothing>
}
