package com.biobox.biotech.core.util

sealed class DomainError(override val message: String) : Exception(message) {
    object NetworkError : DomainError("Sin conexión a internet. Verifica tu red.")
    object TimeoutError : DomainError("El servidor tardó demasiado en responder.")
    object Unauthorized : DomainError("Credenciales incorrectas o sesión expirada.")
    object AccessDenied : DomainError("No tienes permisos para realizar esta acción.")
    object NotFound : DomainError("El recurso solicitado no fue encontrado.")
    object ServerError : DomainError("Error interno en el servidor BioTech. Intenta más tarde.")
    data class UnknownError(val code: Int, override val message: String) : DomainError("Error ($code): $message")
}