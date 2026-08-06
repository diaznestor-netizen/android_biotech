package com.biobox.biotech.domain.model

data class Incident(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: IncidentCategory,
    val gravedad: IncidentSeverity,
    val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    val reportadoPor: String,
    val asignadoA: String? = null,
    val fechaReporte: Long = System.currentTimeMillis(),
    val fechaResolucion: Long? = null,
    val estado: IncidentStatus,
    val evidencias: List<String> = emptyList(),
    val comentarios: String? = null
)

enum class IncidentCategory {
    MECANICA, ELECTRICA, MATERIAL, SEGURIDAD, SOFTWARE, OTRO;

    companion object {
        fun fromString(s: String): IncidentCategory = when (s.uppercase()) {
            "MECANICA", "MECÁNICA" -> MECANICA
            "ELECTRICA", "ELÉCTRICA" -> ELECTRICA
            "MATERIAL" -> MATERIAL
            "SEGURIDAD" -> SEGURIDAD
            "SOFTWARE" -> SOFTWARE
            else -> OTRO
        }
    }
}

enum class IncidentSeverity {
    BAJA, MEDIA, ALTA, CRITICA;

    companion object {
        fun fromString(s: String): IncidentSeverity = when (s.uppercase()) {
            "BAJA" -> BAJA
            "MEDIA" -> MEDIA
            "ALTA" -> ALTA
            "CRITICA" -> CRITICA
            else -> MEDIA
        }
    }
}

enum class IncidentStatus {
    REPORTADO, EN_REVISION, EN_RESOLUCION, RESUELTO, CERRADO;

    companion object {
        fun fromString(s: String): IncidentStatus = when (s.uppercase()) {
            "REPORTADO" -> REPORTADO
            "EN_REVISION", "EN REVISIÓN" -> EN_REVISION
            "EN_RESOLUCION", "EN RESOLUCIÓN" -> EN_RESOLUCION
            "RESUELTO" -> RESUELTO
            "CERRADO" -> CERRADO
            else -> REPORTADO
        }
    }
}
