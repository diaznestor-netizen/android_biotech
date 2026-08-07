package com.biobox.biotech.domain.notifications

import java.text.SimpleDateFormat
import java.util.*

object NotificationFormatter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun format(event: NotificationEvent): Pair<String, NotificationPriority> {
        val timestamp = dateFormat.format(Date())
        val builder = StringBuilder()
        
        builder.append("🚨 *BioTech*\n\n")

        val (module, type, priority, details) = when (event) {
            is NotificationEvent.IncidentReported -> {
                listOf(
                    "Incidencias", 
                    "Nueva incidencia registrada", 
                    NotificationPriority.HIGH,
                    "ID: ${event.id}\nMáquina: ${event.machine}\nPrioridad: ${event.priority}\nUsuario: ${event.user}"
                )
            }
            is NotificationEvent.IncidentStatusChanged -> {
                listOf(
                    "Incidencias", 
                    "Cambio de estado", 
                    NotificationPriority.NORMAL,
                    "ID: ${event.id}\nNuevo Estado: ${event.newStatus}\nUsuario: ${event.user}"
                )
            }
            is NotificationEvent.IncidentClosed -> {
                listOf(
                    "Incidencias", 
                    "Incidencia cerrada", 
                    NotificationPriority.NORMAL,
                    "ID: ${event.id}\nResolución: ${event.resolution}\nUsuario: ${event.user}"
                )
            }
            is NotificationEvent.MachineOutOfService -> {
                listOf(
                    "Maquinaria", 
                    "FUERA DE SERVICIO", 
                    NotificationPriority.CRITICAL,
                    "ID: ${event.id}\nMáquina: ${event.name}\nMotivo: ${event.reason}"
                )
            }
            is NotificationEvent.MachineRepairCompleted -> {
                listOf(
                    "Maquinaria", 
                    "Reparación completada", 
                    NotificationPriority.NORMAL,
                    "ID: ${event.id}\nMáquina: ${event.name}\nTécnico: ${event.technician}"
                )
            }
            is NotificationEvent.StockCritical -> {
                listOf(
                    "Inventario", 
                    "STOCK CRÍTICO", 
                    NotificationPriority.CRITICAL,
                    "Artículo: ${event.item}\nExistencia: ${event.currentQty}"
                )
            }
            is NotificationEvent.StockLow -> {
                listOf(
                    "Inventario", 
                    "Stock bajo", 
                    NotificationPriority.HIGH,
                    "Artículo: ${event.item}\nExistencia: ${event.currentQty}\nMínimo: ${event.minQty}"
                )
            }
            is NotificationEvent.MaterialOut -> {
                listOf(
                    "Inventario", 
                    "MATERIAL AGOTADO", 
                    NotificationPriority.CRITICAL,
                    "Artículo: ${event.item}"
                )
            }
            is NotificationEvent.ProjectCreated -> {
                listOf(
                    "Proyectos", 
                    "Nuevo proyecto creado", 
                    NotificationPriority.NORMAL,
                    "ID: ${event.id}\nNombre: ${event.name}\nResponsable: ${event.manager}"
                )
            }
            is NotificationEvent.AccountBlocked -> {
                listOf(
                    "Seguridad", 
                    "CUENTA BLOQUEADA", 
                    NotificationPriority.CRITICAL,
                    "Email: ${event.email}\nMotivo: ${event.reason}"
                )
            }
            is NotificationEvent.OtpSent -> {
                listOf(
                    "Seguridad", 
                    "Código OTP Enviado", 
                    NotificationPriority.HIGH,
                    "Se ha generado un desafío de seguridad para: ${event.user}"
                )
            }
            is NotificationEvent.OtpVerified -> {
                listOf(
                    "Seguridad", 
                    "OTP Validado", 
                    NotificationPriority.NORMAL,
                    "Acceso verificado correctamente para: ${event.user}"
                )
            }
            is NotificationEvent.AuthExpired -> {
                listOf(
                    "Seguridad", 
                    "Sesión Expirada (4h)", 
                    NotificationPriority.HIGH,
                    "Se requiere re-autenticación para: ${event.user}"
                )
            }
            is NotificationEvent.ReportGenerated -> {
                listOf(
                    "Reportes", 
                    "Reporte disponible", 
                    NotificationPriority.LOW,
                    "Tipo: ${event.type}\nFormato: ${event.format}\nGenerado por: ${event.user}"
                )
            }
            // Add more as needed
            else -> listOf("Sistema", "Evento detectado", NotificationPriority.NORMAL, "Detalles no especificados")
        }

        builder.append("*Módulo:* $module\n")
        builder.append("*Evento:* $type\n")
        builder.append("\n$details\n\n")
        builder.append("*Fecha:* $timestamp\n")
        builder.append("*Prioridad:* $priority")

        return builder.toString() to (priority as NotificationPriority)
    }
}
