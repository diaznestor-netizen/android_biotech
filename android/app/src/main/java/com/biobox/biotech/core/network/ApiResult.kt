package com.biobox.biotech.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class HttpError(val code: Int, val message: String) : ApiResult<Nothing>
    data class NetworkError(val cause: Throwable) : ApiResult<Nothing>
    data class InvalidData(val message: String) : ApiResult<Nothing>
}

fun <T> ApiResult<T>.toResult(): Result<T> {
    return when (this) {
        is ApiResult.Success -> Result.success(data)
        is ApiResult.HttpError -> Result.failure(Exception("Error servidor ($code): $message"))
        is ApiResult.NetworkError -> Result.failure(cause)
        is ApiResult.InvalidData -> Result.failure(Exception("Datos inválidos: $message"))
    }
}
