package com.biobox.biotech.core.util

/**
 * Funciones de utilidad para protección de datos e higiene de memoria.
 */
object SecurityUtils {

    /**
     * Ofusca un número telefónico para evitar registrar PII en texto plano.
     * Ejemplo: "5512345678" -> "551****678"
     */
    fun ofuscarTelefono(telefono: String): String {
        val limpio = telefono.trim()
        return if (limpio.length >= 7) {
            val inicio = limpio.take(3)
            val fin = limpio.takeLast(3)
            val asteriscos = "*".repeat(limpio.length - 6)
            "$inicio$asteriscos$fin"
        } else {
            "***"
        }
    }

    /**
     * Limpia arreglos de caracteres en memoria sobrescribiéndolos con ceros.
     */
    fun limpiarMemoria(vararg arrays: CharArray) {
        for (array in arrays) {
            array.fill('\u0000')
        }
    }
}