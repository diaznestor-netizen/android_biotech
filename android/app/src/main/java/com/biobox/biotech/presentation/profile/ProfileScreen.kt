package com.biobox.biotech.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.theme.AzulOscuro
import com.biobox.biotech.presentation.theme.Blanco
import com.biobox.biotech.presentation.theme.DarkBackground
import com.biobox.biotech.presentation.theme.DarkSurface
import com.biobox.biotech.presentation.theme.Error
import com.biobox.biotech.presentation.theme.PrimaryGreen
import com.biobox.biotech.presentation.theme.TextSecondaryDark

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val displayUser = profile ?: user
    val editMode by viewModel.editMode.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.operationEvents.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),
                color = AzulOscuro
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${displayUser?.nombre?.firstOrNull() ?: ""}${displayUser?.apellido?.firstOrNull() ?: ""}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${displayUser?.nombre ?: ""} ${displayUser?.apellido ?: ""}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = displayUser?.rol?.name ?: "Cargo no definido",
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryGreen,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            BioTechCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DATOS PERSONALES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = if (editMode) nombre else (displayUser?.nombre ?: ""),
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        enabled = editMode,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = if (editMode) apellido else (displayUser?.apellido ?: ""),
                        onValueChange = { apellido = it },
                        label = { Text("Apellido") },
                        enabled = editMode,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = if (editMode) email else (displayUser?.email ?: ""),
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        enabled = editMode,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (editMode) {
                            Button(
                                onClick = { viewModel.saveProfile(nombre, apellido, email) },
                                enabled = !saving,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Blanco, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (saving) "Guardando..." else "GUARDAR")
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelEdit() },
                                enabled = !saving,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCELAR")
                            }
                        } else {
                            Button(
                                onClick = {
                                    nombre = displayUser?.nombre ?: ""
                                    apellido = displayUser?.apellido ?: ""
                                    email = displayUser?.email ?: ""
                                    viewModel.startEdit()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("EDITAR PERFIL")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BioTechCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SEGURIDAD Y CONTACTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CAMBIAR CONTRASEÑA")
                    }
                    OutlinedButton(
                        onClick = { showPhoneDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CAMBIAR TELÉFONO")
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    ProfileInfoRow("ESTRUCTURA OPERATIVA", "GRUPO VALLAS / BIOTECH")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CERRAR SESIÓN", color = Error, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            saving = saving,
            onConfirm = { current, new, confirm ->
                viewModel.changePassword(current, new, confirm) { showPasswordDialog = false }
            },
            onDismiss = { showPasswordDialog = false }
        )
    }
    if (showPhoneDialog) {
        ChangePhoneDialog(
            saving = saving,
            onConfirm = { phone -> viewModel.changePhone(phone) { showPhoneDialog = false } },
            onDismiss = { showPhoneDialog = false }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    saving: Boolean,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !saving,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña (mín. 8)") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !saving,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !saving,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(current, newPassword, confirmPassword) },
                enabled = !saving && current.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ChangePhoneDialog(
    saving: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Cambiar teléfono") },
        text = {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                label = { Text("Nuevo teléfono (10 dígitos)") },
                enabled = !saving,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(phone) },
                enabled = !saving && phone.length == 10
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
        }
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryDark,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
