package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val rol: String,
    val activo: Boolean = true
)
