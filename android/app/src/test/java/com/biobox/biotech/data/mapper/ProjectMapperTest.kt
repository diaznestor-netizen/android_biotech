package com.biobox.biotech.data.mapper

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.ProjectEntity
import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectMapperTest {

    @Test
    fun `dto maps to entity with defaults for nulls`() {
        val result = ProjectDto(
            id = 10,
            local_id = "loc-10",
            codigo = "PRJ-10",
            nombre = null
        ).toEntityResult()

        assertTrue(result is MappingResult.Success)
        val entity = (result as MappingResult.Success).value
        assertEquals("Proyecto sin nombre", entity.nombre)
        assertEquals("PLANEADO", entity.estado)
        assertEquals(SyncStatus.SYNCED, entity.syncStatus)
    }

    @Test
    fun `dto rejects missing id`() {
        val result = ProjectDto(
            id = null,
            local_id = "loc-10",
            codigo = "PRJ-10",
            nombre = "Proyecto"
        ).toEntityResult()

        assertTrue(result is MappingResult.Invalid)
    }

    @Test
    fun `entity maps to domain preserving identifiers and conflict payload`() {
        val domain = entity().toDomain()
        assertEquals("loc-1", domain.localId)
        assertEquals(9, domain.id)
        assertEquals("{\"remote\":2}", domain.conflictPayloadJson)
    }

    @Test
    fun `domain maps to entity preserving organization context`() {
        val entity = project().toEntity()
        assertEquals("org-a", entity.organizationId)
        assertEquals("tenant-a", entity.tenantId)
    }

    @Test
    fun `entity with null dates maps safely`() {
        val domain = entity(fechaInicio = null, fechaFinEstimada = null, fechaFinReal = null).toDomain()
        assertNull(domain.fechaInicio)
        assertNull(domain.fechaFinEstimada)
        assertNull(domain.fechaFinReal)
    }

    private fun entity(
        fechaInicio: Long? = 100L,
        fechaFinEstimada: Long? = 200L,
        fechaFinReal: Long? = 300L
    ) = ProjectEntity(
        localId = "loc-1",
        remoteId = 9,
        codigo = "PRJ-1",
        nombre = "Proyecto 1",
        descripcion = null,
        cliente = "Acme",
        responsableId = 1,
        responsableNombre = "Ana",
        usuarioCreadorId = 2,
        estado = "EN_PROGRESO",
        prioridad = "MEDIA",
        fechaInicio = fechaInicio,
        fechaFinEstimada = fechaFinEstimada,
        fechaFinReal = fechaFinReal,
        porcentajeAvance = 70,
        observaciones = null,
        version = 3,
        syncStatus = SyncStatus.CONFLICT,
        conflictPayloadJson = "{\"remote\":2}",
        organizationId = "org-a",
        tenantId = "tenant-a"
    )

    private fun project() = Project(
        id = 9,
        localId = "loc-1",
        codigo = "PRJ-1",
        nombre = "Proyecto 1",
        descripcion = null,
        cliente = "Acme",
        responsableId = 1,
        responsableNombre = "Ana",
        usuarioCreadorId = 2,
        estado = ProjectStatus.EN_PROGRESO,
        prioridad = ProjectPriority.MEDIA,
        fechaInicio = 100L,
        fechaFinEstimada = 200L,
        fechaFinReal = 300L,
        porcentajeAvance = 70,
        observaciones = null,
        version = 3,
        syncStatus = SyncStatus.CONFLICT,
        conflictPayloadJson = "{\"remote\":2}",
        organizationId = "org-a",
        tenantId = "tenant-a"
    )
}
