package com.biobox.biotech.domain.model

data class User(
    val id: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val rol: UserRole
)

enum class UserRole {
    ADMIN, SUPERVISOR, TECNICO, ALMACEN, CONSULTA;
    
    companion object {
        fun fromString(role: String): UserRole = when(role.uppercase()) {
            "ADMIN", "ADMINISTRADOR" -> ADMIN
            "SUPERVISOR" -> SUPERVISOR
            "TÉCNICO", "TECNICO" -> TECNICO
            "ALMACÉN", "ALMACEN" -> ALMACEN
            else -> CONSULTA
        }
    }
}
