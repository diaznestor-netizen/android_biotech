package com.biobox.biotech.domain.model

data class Goal(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val proyecto: String? = null,
    val maquinaId: Int? = null,
    val porcentajeAvance: Int = 0,
    val fechaInicio: Long,
    val fechaFin: Long? = null,
    val estado: GoalStatus,
    val actividadesCompletadas: Int = 0,
    val actividadesTotales: Int = 0
)

enum class GoalStatus {
    NO_INICIADA, EN_PROGRESO, COMPLETADA, CANCELADA;

    companion object {
        fun fromString(s: String): GoalStatus = when (s.uppercase()) {
            "NO_INICIADA", "NO INICIADA" -> NO_INICIADA
            "EN_PROGRESO", "EN PROGRESO" -> EN_PROGRESO
            "COMPLETADA" -> COMPLETADA
            "CANCELADA" -> CANCELADA
            else -> NO_INICIADA
        }
    }
}
