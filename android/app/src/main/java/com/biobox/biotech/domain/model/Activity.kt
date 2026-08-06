package com.biobox.biotech.domain.model

data class Activity(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val responsable: String,
    val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    val tiempoEmpleado: Int,
    val fecha: Long,
    val evidencias: List<String> = emptyList(),
    val comentarios: String? = null,
    val estado: ActivityStatus,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ActivityStatus {
    PENDIENTE, EN_CURSO, COMPLETADA, APROBADA, RECHAZADA;

    companion object {
        fun fromString(s: String): ActivityStatus = when (s.uppercase()) {
            "PENDIENTE" -> PENDIENTE
            "EN_CURSO", "EN CURSO" -> EN_CURSO
            "COMPLETADA" -> COMPLETADA
            "APROBADA" -> APROBADA
            "RECHAZADA" -> RECHAZADA
            else -> PENDIENTE
        }
    }
}
