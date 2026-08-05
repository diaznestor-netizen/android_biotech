package com.biobox.biotech.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.presentation.auth.AuthViewModel
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.theme.AzulOscuro
import com.biobox.biotech.presentation.theme.DarkSurface
import com.biobox.biotech.presentation.theme.Error
import com.biobox.biotech.presentation.theme.PrimaryBlue
import com.biobox.biotech.presentation.theme.PrimaryCyan
import com.biobox.biotech.presentation.theme.PrimaryGreen
import com.biobox.biotech.presentation.theme.TextSecondaryDark

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onLinkTelegram: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var telegramLinked by remember { mutableStateOf(false) }
    var telegramUsername by remember { mutableStateOf<String?>(null) }
    var linkedAt by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getTelegramStatus().onSuccess { response ->
            telegramLinked = response.isLinked || response.telegramVerified
            telegramUsername = response.username
            linkedAt = response.linkedAt
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    text = "${user?.nombre?.firstOrNull() ?: ""}${user?.apellido?.firstOrNull() ?: ""}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${user?.nombre ?: ""} ${user?.apellido ?: ""}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = user?.rol?.name ?: "Cargo no definido",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryGreen,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        BioTechCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow("CONTACTO", user?.email ?: "")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                ProfileInfoRow("ESTRUCTURA OPERATIVA", "GRUPO VALLAS / BIOTECH")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BioTechCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (telegramLinked) PrimaryBlue.copy(alpha = 0.05f) else DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = PrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "TELEGRAM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = (if (telegramLinked) PrimaryGreen else Error).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (telegramLinked) "VINCULADO" else "NO VINCULADO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (telegramLinked) PrimaryGreen else Error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (telegramLinked) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Usuario: @${telegramUsername ?: "---"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Vinculado: ${linkedAt ?: "Hoy"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLinkTelegram,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VINCULAR CUENTA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
