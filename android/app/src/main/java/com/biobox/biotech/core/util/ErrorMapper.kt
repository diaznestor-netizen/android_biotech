package com.biobox.biotech.core.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object ErrorMapper {

    fun fromHttpCode(code: Int, errorBody: String? = null): DomainError {
        return when (code) {
            401 -> DomainError.Unauthorized
            403 -> DomainError.AccessDenied
            404 -> DomainError.NotFound
            in 500..599 -> DomainError.ServerError
            else -> DomainError.UnknownError(code, errorBody ?: "Error inesperado")
        }
    }

    fun fromThrowable(throwable: Throwable): DomainError {
        return when (throwable) {
            is SocketTimeoutException -> DomainError.TimeoutError
            is IOException -> DomainError.NetworkError
            is retrofit2.HttpException -> fromHttpCode(throwable.code())
            is DomainError -> throwable
            else -> DomainError.UnknownError(-1, throwable.localizedMessage ?: "Error desconocido")
        }
    }
}