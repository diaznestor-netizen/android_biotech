package com.biobox.biotech.domain.model

data class CalendarEvent(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val tipo: EventType,
    val fechaInicio: Long,
    val fechaFin: Long? = null,
    val todoElDia: Boolean = false,
    val maquinaId: Int? = null,
    val proyecto: String? = null,
    val creadoPor: String? = null,
    val color: String? = null
)

enum class EventType {
    ACTIVIDAD, MISION, ENTREGA, INSPECCION, MANTENIMIENTO, REUNION, OTRO;

    companion object {
        fun fromString(s: String): EventType = when (s.uppercase()) {
            "ACTIVIDAD" -> ACTIVIDAD
            "MISION", "MISIÓN" -> MISION
            "ENTREGA" -> ENTREGA
            "INSPECCION", "INSPECCIÓN" -> INSPECCION
            "MANTENIMIENTO" -> MANTENIMIENTO
            "REUNION", "REUNIÓN" -> REUNION
            else -> OTRO
        }
    }
}
