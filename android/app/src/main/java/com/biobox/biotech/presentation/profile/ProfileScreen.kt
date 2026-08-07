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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.biobox.biotech.presentation.theme.PrimaryGreen
import com.biobox.biotech.presentation.theme.TextSecondaryDark

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
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
