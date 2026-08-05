package com.biobox.biotech.domain.model

data class Mission(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val asignadoA: String,
    val asignadoPor: String? = null,
    val maquinaId: Int? = null,
    val fechaLimite: Long,
    val prioridad: MissionPriority,
    val estado: MissionStatus,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaCumplimiento: Long? = null,
    val observaciones: String? = null
)

enum class MissionPriority {
    BAJA, MEDIA, ALTA, CRITICA;

    companion object {
        fun fromString(s: String): MissionPriority = when (s.uppercase()) {
            "BAJA" -> BAJA
            "MEDIA" -> MEDIA
            "ALTA" -> ALTA
            "CRITICA" -> CRITICA
            else -> MEDIA
        }
    }
}

enum class MissionStatus {
    PENDIENTE, EN_CURSO, COMPLETADA, APROBADA, VENCIDA, CANCELADA;

    companion object {
        fun fromString(s: String): MissionStatus = when (s.uppercase()) {
            "PENDIENTE" -> PENDIENTE
            "EN_CURSO", "EN CURSO" -> EN_CURSO
            "COMPLETADA" -> COMPLETADA
            "APROBADA" -> APROBADA
            "VENCIDA" -> VENCIDA
            "CANCELADA" -> CANCELADA
            else -> PENDIENTE
        }
    }
}
