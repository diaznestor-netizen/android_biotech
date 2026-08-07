package com.biobox.biotech.core.util

object AppConstants {
    // Configuración de Retrofit / OkHttp
    const val TIMEOUT_CONEXION_SEG = 10L
    const val TIMEOUT_LECTURA_SEG = 10L

    // Reglas de Negocio
    const val MAX_INTENTOS_LOGIN = 3
    const val TIEMPO_BLOQUEO_MINUTOS = 5L
    const val LONGITUD_MAX_PASSWORD = 50

    // Categorización de Logs / Analytics
    const val TAG_AUTH = "AUTH_FLOW"
    const val TAG_SECURITY = "SECURITY_EVENT"
}