package com.biobox.biotech.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.indicators.GlobalSyncStatusBar
import com.biobox.biotech.presentation.components.dialogs.BioTechConfirmationDialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.presentation.common.SyncStatusViewModel
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.presentation.navigation.NavRoutes
import com.biobox.biotech.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User?,
    pendingSyncCount: Int = 0,
    onLogout: () -> Unit,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    syncViewModel: SyncStatusViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val syncState by syncViewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        val title = if (pendingSyncCount > 0) "¡Atencion: Cambios pendientes!" else "Cerrar sesion"
        val message = if (pendingSyncCount > 0) 
            "Tienes $pendingSyncCount cambios sin sincronizar con el servidor. Si cierras sesion ahora, estos datos se perderan permanentemente. ¿Deseas continuar?"
            else "¿Estas seguro que deseas cerrar la sesion actual?"
            
        BioTechConfirmationDialog(
            title = title,
            message = message,
            confirmText = if (pendingSyncCount > 0) "Cerrar de todos modos" else "Cerrar sesion",
            isDestructive = pendingSyncCount > 0,
            onConfirm = onLogout,
            onDismiss = { showLogoutDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AzulOscuro,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp)
                                    .background(VerdePrincipal, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("B", color = Blanco, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                // Text("BioTech", color = Blanco, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Text("Smart Control", color = Blanco.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                    }

                    Divider(color = Blanco.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(12.dp))

                    val menuSections = listOf(
                        MenuSection("Operaciones", listOf(
                            NavItem("Dashboard", Icons.Default.Dashboard, NavRoutes.Dashboard.route),
                            NavItem("Dashboard Analítico", Icons.Default.Analytics, NavRoutes.Analytics.route),
                            NavItem("Resúmenes", Icons.Default.Assessment, NavRoutes.Summaries.route),
                            NavItem("Calendario", Icons.Default.CalendarMonth, NavRoutes.Calendar.route),
                            NavItem("Notificaciones", Icons.Default.Notifications, NavRoutes.Notifications.route),
                        )),
                        MenuSection("Gestión", listOf(
                            NavItem("Proyectos", Icons.Default.AccountTree, NavRoutes.Projects.route),
                            NavItem("Maquinaria", Icons.Default.PrecisionManufacturing, NavRoutes.Machines.route),
                            NavItem("Materiales", Icons.Default.Inventory2, NavRoutes.Materials.route),
                            NavItem("Inventario", Icons.Default.Warehouse, NavRoutes.Inventory.route),
                            NavItem("Actividades", Icons.Default.Assignment, NavRoutes.Activities.route),
                            NavItem("Revisiones", Icons.Default.FactCheck, NavRoutes.Inspections.route),
                        )),
                        MenuSection("Seguimiento", listOf(
                            NavItem("Metas", Icons.Default.TrackChanges, NavRoutes.Goals.route),
                            NavItem("Misiones", Icons.Default.Flag, NavRoutes.Missions.route),
                            NavItem("Misiones cumplidas", Icons.Default.History, NavRoutes.CompletedMissions.route),
                            NavItem("Incidencias", Icons.Default.Report, NavRoutes.Incidents.route),
                            NavItem("Reportes", Icons.Default.Description, NavRoutes.Reports.route),
                        )),
                        MenuSection("Documentación", listOf(
                            NavItem("Documentos", Icons.Default.Folder, NavRoutes.Documents.route),
                            NavItem("Máquinas Perú", Icons.Default.LocationOn, NavRoutes.PeruMachines.route),
                            NavItem("Evidencias", Icons.Default.PhotoCamera, NavRoutes.Inspections.route),
                        )),
                        MenuSection("Administración", listOf(
                            NavItem("Usuarios", Icons.Default.People, NavRoutes.Users.route),
                            NavItem("Catalogos", Icons.Default.Category, NavRoutes.Catalogs.route),
                            NavItem("Historial", Icons.Default.History, NavRoutes.AuditLog.route),
                            NavItem("Perfil", Icons.Default.AccountCircle, NavRoutes.Profile.route),
                            NavItem("Configuración", Icons.Default.Settings, NavRoutes.Settings.route),
                        )),
                        MenuSection("Ayuda", listOf(
                            NavItem("Centro de ayuda", Icons.Default.Help, NavRoutes.HelpCenter.route),
                            NavItem("Diagnóstico", Icons.Default.BugReport, NavRoutes.Diagnostics.route),
                        )),
                    )

                    menuSections.forEach { section ->
                        Text(
                            text = section.title,
                            color = Gris500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        section.items.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationDrawerItem(
                                icon = {
                                    Icon(item.icon, contentDescription = null,
                                        tint = if (isSelected) Blanco else Blanco.copy(alpha = 0.6f))
                                },
                                label = {
                                    Text(item.label, color = if (isSelected) Blanco else Blanco.copy(alpha = 0.6f),
                                        fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium)
                                },
                                selected = isSelected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigate(item.route)
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    selectedContainerColor = VerdePrincipal.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (user != null) {
                        val initials = buildString {
                            append(user.nombre.firstOrNull() ?: 'B')
                            append(user.apellido.firstOrNull() ?: 'T')
                        }
                        Surface(
                            modifier = Modifier.padding(16.dp),
                            color = Blanco.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp)
                                        .background(VerdePrincipal, RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = initials,
                                        color = Blanco, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.nombre, color = Blanco, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(user.rol.name, color = Blanco.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { showLogoutDialog = true }) {
                                    Icon(Icons.Default.Logout, contentDescription = "Cerrar sesion", tint = Rojo)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(text = "", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AzulOscuro, titleContentColor = Blanco,
                            navigationIconContentColor = Blanco
                        ),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        }
                    )
                    GlobalSyncStatusBar(syncState)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) { content() }
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector, val route: String)
private data class MenuSection(val title: String, val items: List<NavItem>)
