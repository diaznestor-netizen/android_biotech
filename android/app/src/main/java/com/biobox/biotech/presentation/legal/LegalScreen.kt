package com.biobox.biotech.presentation.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avisos legales", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Copyright", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2024 BioBox - Grupo Vallas. Todos los derechos reservados.", fontSize = 13.sp, color = Gris700)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Términos y condiciones", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("El uso de esta aplicación está sujeto a los términos y condiciones establecidos por BioBox - Grupo Vallas. El usuario se compromete a utilizar la plataforma únicamente para los fines autorizados.", fontSize = 13.sp, color = Gris700)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Política de privacidad", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BioTech protege la información de sus usuarios mediante autenticación segura, cifrado de datos, control de permisos y auditoría de acciones. No compartimos información personal con terceros sin consentimiento.", fontSize = 13.sp, color = Gris700)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permisos de la aplicación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Cámara: Captura de evidencias fotográficas\n• Almacenamiento: Descarga de reportes\n• Notificaciones: Alertas de actividades y misiones\n• Biometría: Autenticación segura", fontSize = 13.sp, color = Gris700)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Licencias de software", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Esta aplicación utiliza componentes de software de código abierto sujetos a sus respectivas licencias (Apache 2.0, MIT).", fontSize = 13.sp, color = Gris700)
                }
            }
        }
    }
}
