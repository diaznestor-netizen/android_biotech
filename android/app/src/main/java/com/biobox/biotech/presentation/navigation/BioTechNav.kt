package com.biobox.biotech.presentation.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.core.security.BiometricAuth
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.presentation.activities.ActivityListScreen
import com.biobox.biotech.presentation.analytics.AnalyticsScreen
import com.biobox.biotech.presentation.analytics.AnalyticsViewModel
import com.biobox.biotech.presentation.audit.AuditLogScreen
import com.biobox.biotech.presentation.auth.AuthViewModel
import com.biobox.biotech.presentation.auth.LoginScreen
import com.biobox.biotech.presentation.auth.OtpVerificationScreen
import com.biobox.biotech.presentation.calendar.CalendarScreen
import com.biobox.biotech.presentation.catalogs.CatalogManagementScreen
import com.biobox.biotech.presentation.components.navigation.BioTechBottomBar
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.components.navigation.NavigationItem
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.dashboard.DashboardScreen
import com.biobox.biotech.presentation.debug.DiagnosticScreen
import com.biobox.biotech.presentation.documents.DocumentListScreen
import com.biobox.biotech.presentation.goals.GoalListScreen
import com.biobox.biotech.presentation.help.HelpCenterScreen
import com.biobox.biotech.presentation.home.HomeScreen
import com.biobox.biotech.presentation.incidents.IncidentListScreen
import com.biobox.biotech.presentation.inspections.InspectionListScreen
import com.biobox.biotech.presentation.legal.LegalScreen
import com.biobox.biotech.presentation.machines.MachineListScreen
import com.biobox.biotech.presentation.materials.MaterialListScreen
import com.biobox.biotech.presentation.missions.MissionListScreen
import com.biobox.biotech.presentation.notifications.NotificationsScreen
import com.biobox.biotech.presentation.peru.PeruMachinesScreen
import com.biobox.biotech.presentation.profile.ProfileScreen
import com.biobox.biotech.presentation.profile.TelegramLinkingScreen
import com.biobox.biotech.presentation.projects.ProjectDetailScreen
import com.biobox.biotech.presentation.projects.ProjectFormScreen
import com.biobox.biotech.presentation.projects.ProjectListScreen
import com.biobox.biotech.presentation.reports.ReportsScreen
import com.biobox.biotech.presentation.settings.SettingsScreen
import com.biobox.biotech.presentation.splash.SplashScreen
import com.biobox.biotech.presentation.summaries.SummaryScreen
import com.biobox.biotech.presentation.theme.DarkBackground
import com.biobox.biotech.presentation.theme.PrimaryGreen
import com.biobox.biotech.presentation.users.UserListScreen

@Composable
fun BioTechNav(biometricAuth: BiometricAuth? = null) {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val loginState by authViewModel.loginState.collectAsStateWithLifecycle()
    val sessionState by authViewModel.sessionValidationState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val lastReAuthTime by authViewModel.lastReAuthTime.collectAsStateWithLifecycle()
    val pendingSecondFactorSessionId by authViewModel.pendingSecondFactorSessionId.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success<*>) {
            rootNavController.navigate(NavRoutes.Dashboard.route) {
                popUpTo(NavRoutes.Login.route) { inclusive = true }
            }
            authViewModel.consumeLoginState()
        }
    }

    LaunchedEffect(lastReAuthTime) {
        if (authViewModel.isReAuthRequired(System.currentTimeMillis())) {
            rootNavController.navigate(NavRoutes.OtpVerification.route)
        }
    }

    LaunchedEffect(pendingSecondFactorSessionId) {
        if (!pendingSecondFactorSessionId.isNullOrBlank()) {
            rootNavController.navigate(NavRoutes.OtpVerification.route)
        }
    }

    NavHost(navController = rootNavController, startDestination = NavRoutes.Splash.route) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                sessionState = sessionState,
                onValidateSession = authViewModel::validateSession,
                onNavigate = { hasSession ->
                    val targetRoute = if (hasSession) {
                        if (authViewModel.isReAuthRequired(System.currentTimeMillis())) NavRoutes.OtpVerification.route
                        else NavRoutes.Dashboard.route
                    } else NavRoutes.Login.route
                    rootNavController.navigate(targetRoute) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLogin = authViewModel::login,
                isLoading = loginState is UiState.Loading,
                errorMessage = (loginState as? UiState.Error)?.message
            )
        }

        composable(NavRoutes.OtpVerification.route) {
            OtpVerificationScreen(
                viewModel = authViewModel,
                onSuccess = {
                    rootNavController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.OtpVerification.route) { inclusive = true }
                    }
                },
                onBack = {
                    authViewModel.clearPendingSecondFactor()
                    rootNavController.popBackStack()
                },
                onSessionExpired = {
                    authViewModel.clearPendingSecondFactor()
                    rootNavController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.OtpVerification.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Dashboard.route) {
            MainShell(
                user = currentUser,
                authViewModel = authViewModel,
                onLogout = {
                    rootNavController.navigate(NavRoutes.Login.route) {
                        popUpTo(rootNavController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                rootNavController = rootNavController
            )
        }
    }

}

@Composable
private fun MainShell(
    user: User?,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    rootNavController: androidx.navigation.NavController
) {
    val shellNavController = rememberNavController()
    val context = LocalContext.current
    val sessionDataStore = remember(context) { SessionDataStore(context) }
    val backStackEntry by shellNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavRoutes.Dashboard.route
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()
    val pendingSyncCount by authViewModel.pendingSyncCount.collectAsStateWithLifecycle()

    val bottomItems = listOf(
        NavigationItem("Inicio", NavRoutes.Dashboard.route, Icons.Default.Dashboard, Icons.Default.Dashboard),
        NavigationItem("Máquinas", NavRoutes.Machines.route, Icons.Default.PrecisionManufacturing, Icons.Default.PrecisionManufacturing),
        NavigationItem("Proyectos", NavRoutes.Projects.route, Icons.Default.Inventory2, Icons.Default.Inventory2),
        NavigationItem("Misiones", NavRoutes.Missions.route, Icons.Default.Flag, Icons.Default.Flag),
        NavigationItem("Perfil", NavRoutes.Profile.route, Icons.Default.AccountCircle, Icons.Default.AccountCircle)
    )

    HomeScreen(
        user = user,
        pendingSyncCount = pendingSyncCount,
        onLogout = {
            authViewModel.logout()
            onLogout()
        },
        currentRoute = currentRoute,
        onNavigate = { route ->
            when {
                route.startsWith("machine_detail/") || route.startsWith("activity_detail/") ||
                route.startsWith("goal_detail/") || route.startsWith("mission_detail/") ||
                route.startsWith("incident_detail/") || route.startsWith("user_detail/") ||
                route.startsWith("project_detail/") || route.startsWith("project_form") ||
                route.startsWith("inspection/") -> shellNavController.navigate(route)
                else -> shellNavController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(shellNavController.graph.findStartDestination().id) { saveState = true }
                }
            }
        },
        content = {
            Scaffold(
                bottomBar = {
                    BioTechBottomBar(
                        items = bottomItems,
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            shellNavController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(shellNavController.graph.findStartDestination().id) { saveState = true }
                            }
                        }
                    )
                },
                containerColor = DarkBackground
            ) { innerPadding ->
                NavHost(
                    navController = shellNavController,
                    startDestination = NavRoutes.Dashboard.route,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    composable(NavRoutes.Dashboard.route) { DashboardScreen() }
                    composable(NavRoutes.Analytics.route) { AnalyticsScreen(viewModel = analyticsViewModel) }
                    composable(NavRoutes.Projects.route) {
                        ProjectListScreen(
                            onProjectClick = { localId ->
                                shellNavController.navigate(NavRoutes.ProjectDetail.createRoute(localId))
                            },
                            onCreateProject = {
                                shellNavController.navigate(NavRoutes.ProjectForm.createRoute())
                            },
                            onEditProject = { localId ->
                                shellNavController.navigate(NavRoutes.ProjectForm.createRoute(localId))
                            }
                        )
                    }
                    composable(
                        route = NavRoutes.ProjectDetail.route,
                        arguments = listOf(navArgument("localId") { type = NavType.StringType })
                    ) {
                        ProjectDetailScreen(
                            onBack = { shellNavController.popBackStack() },
                            onEdit = { localId ->
                                shellNavController.navigate(NavRoutes.ProjectForm.createRoute(localId))
                            },
                            onMergeConflict = { localId ->
                                shellNavController.navigate(NavRoutes.ProjectForm.createRoute(localId, mergeConflict = true))
                            }
                        )
                    }
                    composable(
                        route = NavRoutes.ProjectForm.route,
                        arguments = listOf(
                            navArgument("localId") {
                                type = NavType.StringType
                                defaultValue = ""
                                nullable = true
                            },
                            navArgument("mergeConflict") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) {
                        ProjectFormScreen(
                            onBack = { shellNavController.popBackStack() }
                        )
                    }
                    composable(NavRoutes.Machines.route) {
                        MachineListScreen(
                            onMachineClick = { machineId ->
                                shellNavController.navigate(NavRoutes.MachineDetail.createRoute(machineId))
                            }
                        )
                    }
                    composable(NavRoutes.Materials.route) { MaterialListScreen() }
                    composable(NavRoutes.Inventory.route) { MaterialListScreen() }
                    composable(NavRoutes.Inspections.route) { 
                        PlaceholderScreen(title = "REVISIONES PENDIENTES", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) })
                    }
                    composable(
                        route = NavRoutes.MachineDetail.route,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { 
                        // Note: This route might be redundant if MachineDetail is handled above
                        // but we'll ensure it has a consistent look.
                        PlaceholderScreen(title = "DETALLE DE MÁQUINA", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) })
                    }
                    composable(NavRoutes.Activities.route) {
                        ActivityListScreen(
                            onActivityClick = { id -> shellNavController.navigate(NavRoutes.ActivityDetail.createRoute(id)) },
                            onCreateActivity = { shellNavController.navigate(NavRoutes.NewActivity.route) }
                        )
                    }
                    composable(NavRoutes.ActivityDetail.route, arguments = listOf(navArgument("id") { type = NavType.IntType })) { 
                        PlaceholderScreen(title = "DETALLE DE ACTIVIDAD", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) })
                    }
                    composable(NavRoutes.NewActivity.route) { PlaceholderScreen(title = "NUEVA ACTIVIDAD", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) }) }
                    composable(NavRoutes.Goals.route) {
                        GoalListScreen(
                            onGoalClick = { id -> shellNavController.navigate(NavRoutes.GoalDetail.createRoute(id)) },
                            onCreateGoal = { shellNavController.navigate(NavRoutes.NewGoal.route) }
                        )
                    }
                    composable(NavRoutes.GoalDetail.route, arguments = listOf(navArgument("id") { type = NavType.IntType })) { 
                        PlaceholderScreen(title = "DETALLE DE META", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) })
                    }
                    composable(NavRoutes.NewGoal.route) { PlaceholderScreen(title = "NUEVA META", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) }) }
                    composable(NavRoutes.Missions.route) {
                        MissionListScreen(
                            onMissionClick = { id -> shellNavController.navigate(NavRoutes.MissionDetail.createRoute(id)) },
                            onCreateMission = { shellNavController.navigate(NavRoutes.NewMission.route) },
                            onCompletedMissions = { shellNavController.navigate(NavRoutes.CompletedMissions.route) }
                        )
                    }
                    composable(NavRoutes.MissionDetail.route, arguments = listOf(navArgument("id") { type = NavType.IntType })) { 
                        PlaceholderScreen(title = "DETALLE DE MISIÓN", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) })
                    }
                    composable(NavRoutes.NewMission.route) { PlaceholderScreen(title = "NUEVA MISIÓN", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) }) }
                    composable(NavRoutes.CompletedMissions.route) { PlaceholderScreen(title = "MISIONES CUMPLIDAS", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) }) }
                    composable(NavRoutes.Incidents.route) {
                        IncidentListScreen(
                            onIncidentClick = { id -> shellNavController.navigate(NavRoutes.IncidentDetail.createRoute(id)) },
                            onCreateIncident = { shellNavController.navigate(NavRoutes.NewIncident.route) }
                        )
                    }
                    composable(NavRoutes.IncidentDetail.route, arguments = listOf(navArgument("id") { type = NavType.IntType })) { 
                        PlaceholderScreen(title = "DETALLE DE INCIDENCIA", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) })
                    }
                    composable(NavRoutes.NewIncident.route) { PlaceholderScreen(title = "NUEVA INCIDENCIA", onAction = { shellNavController.navigate(NavRoutes.Dashboard.route) }) }
                    composable(NavRoutes.Reports.route) { ReportsScreen() }
                    composable(NavRoutes.Documents.route) { DocumentListScreen(onDocumentClick = { }) }
                    composable(NavRoutes.Calendar.route) { CalendarScreen() }
                    composable(NavRoutes.Profile.route) { 
                        ProfileScreen(
                            user = user, 
                            onLogout = onLogout,
                            onLinkTelegram = { shellNavController.navigate(NavRoutes.TelegramLinking.route) }
                        ) 
                    }
                    composable(NavRoutes.TelegramLinking.route) {
                        TelegramLinkingScreen(
                            onBack = { shellNavController.popBackStack() }
                        )
                    }
                    composable(NavRoutes.Settings.route) { SettingsScreen(sessionDataStore = sessionDataStore) }
                    composable(NavRoutes.HelpCenter.route) {
                        HelpCenterScreen(onNavigateToLegal = { shellNavController.navigate(NavRoutes.Legal.route) })
                    }
                    composable(NavRoutes.Legal.route) { LegalScreen() }
                    composable(NavRoutes.Users.route) { UserListScreen() }
                    composable(NavRoutes.Catalogs.route) { CatalogManagementScreen() }
                    composable(NavRoutes.PeruMachines.route) { PeruMachinesScreen() }
                    composable(NavRoutes.AuditLog.route) { AuditLogScreen() }
                    composable(NavRoutes.Notifications.route) { NotificationsScreen() }
                    composable(NavRoutes.Summaries.route) { SummaryScreen(viewModel = analyticsViewModel) }
                    composable(NavRoutes.Diagnostics.route) { DiagnosticScreen() }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    onAction: (() -> Unit)? = null
) {
    Scaffold(
        topBar = { BioTechTopBar(title = title) },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            EmptyState(
                title = title,
                description = "No hay registros para mostrar en esta sección.",
                action = onAction?.let {
                    {
                        Button(
                            onClick = it,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("VOLVER AL INICIO")
                        }
                    }
                }
            )
        }
    }
}









