package com.biobox.biotech.core.util

import retrofit2.Response

/**
 * Ejecuta una llamada de Retrofit y mapea el resultado automáticamente a Result<R>.
 */
suspend fun <T, R> safeApiCall(
    apiCall: suspend () -> Response<T>,
    transform: (T) -> R
): Result<R> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                runCatching { transform(body) }
            } else {
                Result.failure(DomainError.UnknownError(response.code(), "Respuesta vacía del servidor"))
            }
        } else {
            val errorMsg = response.errorBody()?.string()
            Result.failure(ErrorMapper.fromHttpCode(response.code(), errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(ErrorMapper.fromThrowable(e))
    }
}