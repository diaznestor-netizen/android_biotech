package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Project

object ProjectValidation {
    fun validate(project: Project, isCreate: Boolean): Result<Unit> = runCatching {
        require(project.codigo.isNotBlank()) { "El código es obligatorio" }
        require(project.nombre.isNotBlank()) { "El nombre es obligatorio" }
        require(normalizeCode(project.codigo).length <= 50) { "El código excede la longitud máxima" }
        require(project.porcentajeAvance in 0..100) { "El porcentaje de avance debe estar entre 0 y 100" }
        if (!isCreate) {
            require(project.localId.isNotBlank()) { "El localId es obligatorio para editar" }
            require(isValidIdentifier(project.localId)) { "El localId es inválido" }
        }
        if (project.fechaInicio != null && project.fechaFinReal != null) {
            require(project.fechaFinReal >= project.fechaInicio) {
                "La fecha fin real no puede ser anterior a la fecha inicio"
            }
        }
    }

    fun normalizeCode(code: String): String {
        return code.trim().uppercase().replace(Regex("\\s+"), "-")
    }

    fun isValidIdentifier(value: String): Boolean {
        return value.matches(Regex("^[A-Za-z0-9-]{3,64}$"))
    }
}
