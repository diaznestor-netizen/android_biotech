package com.biobox.biotech.domain.model

enum class ProjectPriority {
    BAJA,
    MEDIA,
    ALTA,
    CRITICA;

    companion object {
        fun fromString(value: String?): ProjectPriority {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIA
        }
    }
}
