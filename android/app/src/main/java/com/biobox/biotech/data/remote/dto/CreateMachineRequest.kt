package com.biobox.biotech.data.remote.dto

data class CreateMachineRequest(
    val codigo: String,
    val nombre: String,
    val modelo: String? = null,
    val id_tipo: Int? = null,
    val id_responsable: Int? = null,
    val descripcion: String? = null
)
