package com.biobox.biotech.domain.usecase

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectValidationTest {

    @Test
    fun `validate accepts valid create project`() {
        val result = ProjectValidation.validate(project(), isCreate = true)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate rejects blank code`() {
        val result = ProjectValidation.validate(project(codigo = " "), isCreate = true)
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate rejects invalid percentage`() {
        val result = ProjectValidation.validate(project(porcentajeAvance = 101), isCreate = true)
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate rejects invalid local identifier on update`() {
        val result = ProjectValidation.validate(project(localId = "@@bad"), isCreate = false)
        assertTrue(result.isFailure)
    }

    @Test
    fun `normalize code trims uppercases and replaces whitespace`() {
        assertEquals("PRJ-001-ALFA", ProjectValidation.normalizeCode(" prj 001 alfa "))
    }

    @Test
    fun `identifier validator accepts canonical uuid like value`() {
        assertTrue(ProjectValidation.isValidIdentifier("123e4567-e89b-12d3-a456-426614174000"))
        assertFalse(ProjectValidation.isValidIdentifier("a"))
    }

    private fun project(
        localId: String = "123e4567-e89b-12d3-a456-426614174000",
        codigo: String = "PRJ-001",
        porcentajeAvance: Int = 35
    ) = Project(
        id = null,
        localId = localId,
        codigo = codigo,
        nombre = "Proyecto Uno",
        descripcion = "Demo",
        cliente = "Acme",
        responsableId = 7,
        responsableNombre = "Ana",
        usuarioCreadorId = 1,
        estado = ProjectStatus.EN_PROGRESO,
        prioridad = ProjectPriority.ALTA,
        fechaInicio = 100L,
        fechaFinEstimada = 200L,
        fechaFinReal = 300L,
        porcentajeAvance = porcentajeAvance,
        observaciones = "Obs",
        version = 1,
        syncStatus = SyncStatus.SYNCED
    )
}
