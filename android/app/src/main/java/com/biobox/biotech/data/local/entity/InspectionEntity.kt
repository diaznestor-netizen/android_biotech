package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_inspections")
data class InspectionEntity(
    @PrimaryKey val id: String,
    val machineId: Int,
    val itemsJson: String, // JSON representation of List<InspectionItem>
    val observaciones: String?,
    val evidencePathsJson: String, // JSON representation of List<String>
    val timestamp: Long
)
