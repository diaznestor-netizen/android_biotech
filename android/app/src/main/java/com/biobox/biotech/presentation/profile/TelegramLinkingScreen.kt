package com.biobox.biotech.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.presentation.auth.AuthViewModel
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramLinkingScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var linkingCode by remember { mutableStateOf("") }
    var botUsername by remember { mutableStateOf("BioTechBot") }
    var isLoading by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.getLinkingCode().onSuccess { response ->
            linkingCode = response.linkCode
            botUsername = response.botUsername
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            BioTechTopBar(
                title = "VINCULAR TELEGRAM",
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
            Text(
                text = "CONECTA TU CUENTA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Recibe alertas críticas, reportes y códigos OTP directamente en tu dispositivo móvil.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
                CircularProgressIndicator(color = PrimaryCyan)
            } else {
                BioTechCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = DarkSurface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PrimaryCyan
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TU CÓDIGO DE VINCULACIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = linkingCode,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = PrimaryGreen,
                            letterSpacing = 4.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedButton(
                            onClick = { clipboardManager.setText(AnnotatedString(linkingCode)) },
                            border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COPIAR CÓDIGO", color = PrimaryCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                InstructionsItem("1", "Abre Telegram y busca al bot: @$botUsername")
                InstructionsItem("2", "Presiona el botón INICIAR o escribe /start")
                InstructionsItem("3", "Envía el código mostrado arriba")

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "La vinculación se confirmará automáticamente en unos segundos.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Warning,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InstructionsItem(num: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = num, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}
