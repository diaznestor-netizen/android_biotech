package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MachineDto(
    val id: Int?,
    @SerializedName("code") val codigo: String?,
    @SerializedName("name") val nombre: String?,
    @SerializedName("production_state") val productionState: String?,
    @SerializedName("progress") val progreso: Double?,
    @SerializedName("legacy_state") val legacyState: String? = null,
    @SerializedName("client") val cliente: String? = null,
    @SerializedName("country") val pais: String? = null,
    @SerializedName("responsible_id") val responsableId: Int? = null,
    @SerializedName("project_id") val proyectoId: Int? = null,
    @SerializedName("started_at") val fechaInicio: String? = null,
    @SerializedName("finished_at") val fechaFin: String? = null,
    val observations: String? = null,
    val imagenUrl: String? = null,
    val responsable: String? = null,
    val ultimaRevision: String? = null,
    val componentes: List<ComponenteDto>? = emptyList()
)

data class ComponenteDto(
    val id: Int?,
    @SerializedName("machine_id") val machineId: Int? = null,
    @SerializedName("component_id") val componentId: Int? = null,
    @SerializedName("component_code") val codigo: String? = null,
    @SerializedName("component_name") val nombre: String?,
    val required: Boolean? = false,
    val state: String?,
    val observations: String? = null,
    val materiales: List<MaterialDto>? = emptyList()
)

data class CompletionCheckDto(
    @SerializedName("machine_id") val machineId: Int,
    val progress: Double,
    @SerializedName("components_complete") val componentsComplete: Boolean,
    @SerializedName("materials_complete") val materialsComplete: Boolean,
    @SerializedName("inspection_approved") val inspectionApproved: Boolean,
    @SerializedName("evidence_complete") val evidenceComplete: Boolean,
    @SerializedName("progress_complete") val progressComplete: Boolean,
    @SerializedName("can_be_finished") val canBeFinished: Boolean,
    @SerializedName("missing_components") val missingComponents: List<String> = emptyList(),
    @SerializedName("missing_material_ids") val missingMaterialIds: List<Int> = emptyList(),
    @SerializedName("missing_evidence") val missingEvidence: List<String> = emptyList()
)

data class UpdateComponentStateRequest(
    val state: String,
    val comment: String = ""
)

data class TransitionMachineStateRequest(
    val state: String,
    val comment: String = ""
)

data class AssemblyStepUpdateRequest(
    val category: String,
    @SerializedName("step_name") val stepName: String,
    @SerializedName("unit_number") val unitNumber: Int,
    val value: Double
)

data class MaterialDto(
    val id: Int?, val codigo: String? = "", val nombre: String? = "",
    val cantidadRequerida: Int? = 0, val cantidadDisponible: Int? = 0,
    val estado: String? = "Disponible", val descripcion: String? = null,
    val unidad: String? = null, val stock_min: Double? = 0.0, val activo: Boolean? = true
)
