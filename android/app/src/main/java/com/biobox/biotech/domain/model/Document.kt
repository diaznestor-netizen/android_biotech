package com.biobox.biotech.domain.model

data class Document(
    val id: Int,
    val titulo: String,
    val tipo: DocumentType,
    val maquinaId: Int? = null,
    val proyecto: String? = null,
    val archivoUrl: String,
    val tamano: Long = 0,
    val mimeType: String? = null,
    val fechaSubida: Long = System.currentTimeMillis(),
    val subidoPor: String? = null
)

enum class DocumentType {
    MANUAL, LISTA_MATERIALES, CERTIFICADO, REPORTE_TECNICO, PLANO, OTRO;

    companion object {
        fun fromString(s: String): DocumentType = when (s.uppercase()) {
            "MANUAL" -> MANUAL
            "LISTA_MATERIALES", "LISTA DE MATERIALES" -> LISTA_MATERIALES
            "CERTIFICADO" -> CERTIFICADO
            "REPORTE_TECNICO", "REPORTE TÉCNICO" -> REPORTE_TECNICO
            "PLANO" -> PLANO
            else -> OTRO
        }
    }
}
