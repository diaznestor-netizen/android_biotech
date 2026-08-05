package com.biobox.biotech.presentation.projects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectConflictComparatorTest {

    @Test
    fun `compare only returns different fields`() {
        val payload = """
            {
              "localVersion":1,
              "operation":"UPDATE",
              "serverProject":{
                "id":9,
                "local_id":"local-1",
                "codigo":"PRJ-1",
                "nombre":"Proyecto servidor",
                "descripcion":"desc",
                "cliente":"Acme",
                "responsable_nombre":"Ana",
                "estado":"EN_PROGRESO",
                "prioridad":"ALTA",
                "fecha_inicio":null,
                "fecha_fin_estimada":null,
                "fecha_fin_real":null,
                "porcentaje_avance":80,
                "observaciones":"obs",
                "version":2
              }
            }
        """.trimIndent()

        val result = ProjectConflictComparator.parse(
            payload,
            ProjectPresentationSnapshot(
                codigo = "PRJ-1",
                nombre = "Proyecto local",
                descripcion = "desc",
                cliente = "Acme",
                responsable = "Ana",
                estado = "EN_PROGRESO",
                prioridad = "ALTA",
                fechaInicio = "Sin fecha",
                fechaFinEstimada = "Sin fecha",
                fechaFinReal = "Sin fecha",
                porcentajeAvance = "30",
                observaciones = "obs"
            )
        )!!

        assertEquals(2, result.remoteVersion)
        assertEquals(2, result.differences.size)
        assertTrue(result.differences.any { it.field == "Nombre" })
        assertTrue(result.differences.any { it.field == "Avance" })
    }
}
