package com.biobox.biotech.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.presentation.components.buttons.BioTechButton
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.DarkBackground
import com.biobox.biotech.presentation.theme.Error
import com.biobox.biotech.presentation.theme.PrimaryCyan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success<*> -> {
                viewModel.consumeLoginState()
                onSuccess()
            }
            is UiState.Error -> errorMessage = (loginState as UiState.Error).message
            else -> Unit
        }
    }

    val canSubmit = nombre.isNotBlank() && phoneNumber.isNotBlank() &&
        password.length >= 8 && loginState !is UiState.Loading

    Scaffold(
        topBar = {
            BioTechTopBar(
                title = "CREAR CUENTA",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
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
            Text(
                text = "Regístrate",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = androidx.compose.ui.graphics.Color.White
            )

            Text(
                text = "Crea tu cuenta con número de teléfono y contraseña.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = com.biobox.biotech.presentation.theme.TextSecondaryDark,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ModernTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre",
                icon = Icons.Default.Person,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModernTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = "Apellido",
                icon = Icons.Default.Person,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModernTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email (opcional)",
                icon = Icons.Default.Phone,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModernTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) phoneNumber = it
                },
                label = "Número de teléfono",
                icon = Icons.Default.Phone,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModernTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña (mín. 8 caracteres)",
                icon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                isError = password.isNotEmpty() && password.length < 8,
                errorText = if (password.isNotEmpty() && password.length < 8) "La contraseña debe tener al menos 8 caracteres." else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (canSubmit) {
                            viewModel.register(
                                phoneNumber = phoneNumber.trim(),
                                password = password.trim(),
                                nombre = nombre.trim(),
                                apellido = apellido.trim(),
                                email = email.trim()
                            )
                        }
                    }
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Error,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BioTechButton(
                text = "CREAR CUENTA",
                onClick = {
                    errorMessage = null
                    when {
                        nombre.isBlank() -> errorMessage = "El nombre es obligatorio."
                        phoneNumber.isBlank() -> errorMessage = "El número de teléfono es obligatorio."
                        password.length < 8 -> errorMessage = "La contraseña debe tener al menos 8 caracteres."
                        else -> viewModel.register(
                            phoneNumber = phoneNumber.trim(),
                            password = password.trim(),
                            nombre = nombre.trim(),
                            apellido = apellido.trim(),
                            email = email.trim()
                        )
                    }
                },
                isLoading = loginState is UiState.Loading,
                enabled = canSubmit
            )

            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = PrimaryCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}