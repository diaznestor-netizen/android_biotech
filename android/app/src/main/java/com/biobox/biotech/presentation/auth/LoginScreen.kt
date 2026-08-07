package com.biobox.biotech.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.BuildConfig
import com.biobox.biotech.R
import com.biobox.biotech.core.security.SecureScreen
import com.biobox.biotech.presentation.common.SyncStatusViewModel
import com.biobox.biotech.presentation.components.indicators.OfflineBanner
import com.biobox.biotech.presentation.theme.*

@Composable
fun LoginScreen(
    onLogin: (telefono: String, password: String) -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    syncViewModel: SyncStatusViewModel = hiltViewModel()
) {
    val syncState by syncViewModel.state.collectAsStateWithLifecycle()
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }
    var phoneNumberTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val phoneNumberError = remember(phoneNumber, phoneNumberTouched) {
        when {
            !phoneNumberTouched -> null
            phoneNumber.isBlank() -> "El número de teléfono es obligatorio."
            !phoneNumber.all { it.isDigit() } -> "Ingresa solo números."
            phoneNumber.length < 7 || phoneNumber.length > 15 -> "Longitud de teléfono inválida."
            else -> null
        }
    }
    val passwordError = remember(password, passwordTouched) {
        when {
            !passwordTouched -> null
            password.isBlank() -> "La contraseña es obligatoria."
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            else -> null
        }
    }
    val canSubmit = phoneNumberError == null && passwordError == null && phoneNumber.isNotBlank() && password.isNotBlank() && !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        Color(0xFF064E3B) // Dark Green touch
                    )
                )
            )
    ) {
            // Subtle Circuit Pattern (Optional - sutil overlay)
            // Can be added here as an Image with alpha

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                OfflineBanner(visible = !syncState.isServerConnected)

                Spacer(modifier = Modifier.height(60.dp))

                // Logo Section
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(1000)) + scaleIn(initialScale = 0.8f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = PrimaryGreen.copy(alpha = 0.1f),
                            modifier = Modifier.padding(16.dp).shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = PrimaryGreen
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_biotech_clean),
                                contentDescription = "Logo BioTech",
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(140.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        /*
                        Text(
                            text = "BioTech",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Industrial Operations Platform",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        */
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Login Card (Glassmorphism)
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(800))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = PrimaryBlue.copy(alpha = 0.5f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassSurface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Iniciar sesión",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // Phone Field
                            ModernTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) {
                                        phoneNumber = it
                                        phoneNumberTouched = true
                                    }
                                },
                                label = "Número de teléfono",
                                icon = Icons.Default.Phone,
                                isError = phoneNumberError != null,
                                errorText = phoneNumberError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Contraseña
                            ModernTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordTouched = true
                                },
                                label = "Contraseña",
                                icon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onPasswordToggle = { passwordVisible = !passwordVisible },
                                isError = passwordError != null,
                                errorText = passwordError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (canSubmit) onLogin(phoneNumber.trim(), password.trim())
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = onRegister,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "¿No tienes cuenta? Regístrate",
                                    color = PrimaryCyan,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberSession,
                                    onCheckedChange = { rememberSession = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryGreen,
                                        uncheckedColor = TextSecondaryDark
                                    )
                                )
                                Text(
                                    text = "Recordar sesión",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryDark
                                )
                            }

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage,
                                    color = Error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    phoneNumberTouched = true
                                    passwordTouched = true
                                    if (canSubmit) onLogin(phoneNumber.trim(), password.trim())
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(8.dp, RoundedCornerShape(18.dp)),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                enabled = !isLoading
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(PrimaryGreen, PrimaryBlue)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Text(
                                            "INICIAR SESIÓN",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(bottom = 32.dp, top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (syncState.isServerConnected) PrimaryGreen else Warning
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (syncState.isServerConnected) "Servidor Conectado" else "Modo Offline",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondaryDark
                        )
                    }
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "© 2026 BIOTECH. Todos los derechos reservados.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = "Desarrollado por Néstor Diaz Gaona",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryCyan) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondaryDark
                        )
                    }
                }
            } else null,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(16.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = PrimaryCyan,
                unfocusedLabelColor = TextSecondaryDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = Error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
