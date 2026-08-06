package com.biobox.biotech.domain.model

enum class ProjectStatus {
    PLANEADO,
    EN_PROGRESO,
    PAUSADO,
    FINALIZADO,
    CANCELADO;

    companion object {
        fun fromString(value: String?): ProjectStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PLANEADO
        }
    }
}
