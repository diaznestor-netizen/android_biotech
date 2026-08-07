package com.biobox.biotech.domain.notifications

enum class NotificationPriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW
}

enum class NotificationChannel {
    EMAIL,
    TEAMS,
    SLACK,
    PUSH
}

sealed class NotificationEvent {
    // Incidencias
    data class IncidentReported(val id: String, val machine: String, val priority: String, val user: String) : NotificationEvent()
    data class IncidentStatusChanged(val id: String, val newStatus: String, val user: String) : NotificationEvent()
    data class IncidentClosed(val id: String, val resolution: String, val user: String) : NotificationEvent()

    // Maquinaria
    data class MachineOutOfService(val id: String, val name: String, val reason: String) : NotificationEvent()
    data class MachineRepairCompleted(val id: String, val name: String, val technician: String) : NotificationEvent()
    data class OverdueMaintenance(val id: String, val name: String, val daysOverdue: Int) : NotificationEvent()

    // Inventario
    data class StockLow(val item: String, val currentQty: Double, val minQty: Double) : NotificationEvent()
    data class StockCritical(val item: String, val currentQty: Double) : NotificationEvent()
    data class MaterialOut(val item: String) : NotificationEvent()

    // Proyectos
    data class ProjectCreated(val id: String, val name: String, val manager: String) : NotificationEvent()
    data class ProjectDelayed(val id: String, val name: String, val delayDays: Int) : NotificationEvent()
    data class ProjectPriorityChanged(val id: String, val name: String, val newPriority: String) : NotificationEvent()

    // Seguridad
    data class PasswordRecoveryRequested(val email: String) : NotificationEvent()
    data class AccountBlocked(val email: String, val reason: String) : NotificationEvent()
    data class NewDeviceLogin(val email: String, val deviceName: String, val location: String) : NotificationEvent()
    data class OtpSent(val user: String) : NotificationEvent()
    data class OtpVerified(val user: String) : NotificationEvent()
    data class AuthExpired(val user: String) : NotificationEvent()

    // Reportes
    data class ReportGenerated(val type: String, val format: String, val user: String) : NotificationEvent()
}
