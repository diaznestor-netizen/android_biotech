package com.biobox.biotech.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.presentation.components.buttons.BioTechButton
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.DarkBackground
import com.biobox.biotech.presentation.theme.Error
import com.biobox.biotech.presentation.theme.PrimaryCyan
import com.biobox.biotech.presentation.theme.TextSecondaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableIntStateOf(300) }
    val scope = rememberCoroutineScope()
    val secondFactorPending by viewModel.pendingSecondFactorSessionId.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timerText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val isLoginSecondFactor = !secondFactorPending.isNullOrBlank()

    LaunchedEffect(isLoginSecondFactor, currentUser) {
        if (!isLoginSecondFactor && currentUser == null) {
            errorMessage = "La sesión ha expirado. Vuelve a iniciar sesión."
            onSessionExpired()
        }
    }

    Scaffold(
        topBar = {
            BioTechTopBar(
                title = if (isLoginSecondFactor) "VERIFICACIÓN DE INICIO DE SESIÓN" else "VERIFICACIÓN DE SEGURIDAD",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PrimaryCyan
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CÓDIGO DE ACCESO",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = if (isLoginSecondFactor) {
                    "Ingresa el código de 6 dígitos enviado a tu Telegram para completar el inicio de sesión."
                } else {
                    "Ingresa el código de 6 dígitos enviado a tu Telegram vinculado."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            androidx.compose.material3.OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                modifier = Modifier.width(220.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color = Color.White
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "El código expira en: $timerText",
                color = if (timeLeft < 60) Error else TextSecondaryDark,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            BioTechButton(
                text = if (isLoginSecondFactor) "VERIFICAR ACCESO" else "VERIFICAR CÓDIGO",
                onClick = {
                    if (!isLoginSecondFactor && currentUser == null) {
                        errorMessage = "La sesión ha expirado. Vuelve a iniciar sesión."
                        onSessionExpired()
                        return@BioTechButton
                    }
                    isLoading = true
                    scope.launch {
                        val result = if (isLoginSecondFactor) {
                            viewModel.completeSecondFactor(otpCode).map { Unit }
                        } else {
                            viewModel.verifyOtp(otpCode, action = "change_password")
                        }
                        result.fold(
                            onSuccess = { onSuccess() },
                            onFailure = {
                                errorMessage = formatOtpError(it.message)
                                isLoading = false
                            }
                        )
                    }
                },
                isLoading = isLoading,
                enabled = otpCode.length == 6
            )

            if (!isLoginSecondFactor) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        timeLeft = 300
                        scope.launch { viewModel.requestOtp(action = "change_password") }
                    }
                ) {
                    Text(
                        text = "REENVIAR CÓDIGO",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan
                    )
                }
            }
        }
    }
}




private fun formatOtpError(message: String?): String {
    val raw = message.orEmpty()
    val normalized = raw.lowercase()
    return when {
        normalized.contains("token requerido") || normalized.contains("session_id") -> "La sesión ha expirado. Vuelve a iniciar sesión."
        normalized.contains("código inválido") || normalized.contains("codigo invalido") -> "Código no válido o expirado."
        normalized.contains("expirado") -> "Código no válido o expirado."
        raw.trim().startsWith("{") -> "Código no válido o expirado."
        else -> raw.ifBlank { "Código no válido o expirado." }
    }
}
