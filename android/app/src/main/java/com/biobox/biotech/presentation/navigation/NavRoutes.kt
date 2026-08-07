package com.biobox.biotech.presentation.navigation

import android.net.Uri

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Dashboard : NavRoutes("dashboard")
    object Analytics : NavRoutes("analytics")
    object Projects : NavRoutes("projects")
    object ProjectDetail : NavRoutes("project_detail/{localId}") {
        fun createRoute(localId: String) = "project_detail/${Uri.encode(localId)}"
    }
    object ProjectForm : NavRoutes("project_form?localId={localId}&mergeConflict={mergeConflict}") {
        fun createRoute(localId: String? = null, mergeConflict: Boolean = false) = if (localId.isNullOrBlank()) {
            "project_form?localId=&mergeConflict=$mergeConflict"
        } else {
            "project_form?localId=${Uri.encode(localId)}&mergeConflict=$mergeConflict"
        }
    }
    object Machines : NavRoutes("machines")
    object MachineDetail : NavRoutes("machine_detail/{id}") {
        fun createRoute(id: Int) = "machine_detail/$id"
    }
    object NewMachine : NavRoutes("new_machine")
    object Materials : NavRoutes("materials")
    object Inventory : NavRoutes("inventory")
    object Inspections : NavRoutes("inspections")
    object Inspection : NavRoutes("inspection/{machineId}") {
        fun createRoute(machineId: Int) = "inspection/$machineId"
    }
    object Activities : NavRoutes("activities")
    object ActivityDetail : NavRoutes("activity_detail/{id}") {
        fun createRoute(id: Int) = "activity_detail/$id"
    }
    object NewActivity : NavRoutes("new_activity")
    object Goals : NavRoutes("goals")
    object GoalDetail : NavRoutes("goal_detail/{id}") {
        fun createRoute(id: Int) = "goal_detail/$id"
    }
    object NewGoal : NavRoutes("new_goal")
    object Missions : NavRoutes("missions")
    object MissionDetail : NavRoutes("mission_detail/{id}") {
        fun createRoute(id: Int) = "mission_detail/$id"
    }
    object NewMission : NavRoutes("new_mission")
    object CompletedMissions : NavRoutes("completed_missions")
    object Incidents : NavRoutes("incidents")
    object IncidentDetail : NavRoutes("incident_detail/{id}") {
        fun createRoute(id: Int) = "incident_detail/$id"
    }
    object NewIncident : NavRoutes("new_incident")
    object Reports : NavRoutes("reports")
    object Documents : NavRoutes("documents")
    object Calendar : NavRoutes("calendar")
    object Profile : NavRoutes("profile")
    object Settings : NavRoutes("settings")
    object HelpCenter : NavRoutes("help_center")
    object Legal : NavRoutes("legal")
    object Users : NavRoutes("users")
    object Catalogs : NavRoutes("catalogs")
    object UserDetail : NavRoutes("user_detail/{id}") {
        fun createRoute(id: String) = "user_detail/$id"
    }
    object PeruMachines : NavRoutes("peru_machines")
    object AuditLog : NavRoutes("audit_log")
    object Notifications : NavRoutes("notifications")
    object Summaries : NavRoutes("summaries")
    object ReauthPassword : NavRoutes("reauth_password")
    object Diagnostics : NavRoutes("diagnostics")
}
