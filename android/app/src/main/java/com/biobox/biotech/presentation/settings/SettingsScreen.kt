package com.biobox.biotech.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biobox.biotech.core.datastore.SessionDataStore
import kotlinx.coroutines.launch

data class SettingsSectionItem(
    val title: String,
    val subtitle: String
)

@Composable
fun SettingsScreen(
    sessionDataStore: SessionDataStore
) {
    val coroutineScope = rememberCoroutineScope()
    val darkMode by sessionDataStore.isDarkMode.collectAsState(initial = false)

    val personalItems = listOf(
        SettingsSectionItem("Idioma", "Base preparada para preferencias individuales"),
        SettingsSectionItem("Formato de fecha y hora", "Listo para configuracion por usuario"),
        SettingsSectionItem("Zona horaria", "Preparado para sedes y operaciones internacionales"),
        SettingsSectionItem("Privacidad", "Controles de visibilidad y manejo de datos"),
        SettingsSectionItem("Tiempo de sesion", "Expiracion personal segun politica")
    )
    val globalItems = listOf(
        SettingsSectionItem("Correo del sistema", "Configuracion institucional y notificaciones"),
        SettingsSectionItem("Respaldos", "Politicas de copia automatica y manual"),
        SettingsSectionItem("Parametros de empresa", "Nombre, pais, moneda y reglas generales"),
        SettingsSectionItem("Seguridad", "Politicas de acceso y endurecimiento base")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Configuracion del sistema",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Separo preferencias personales y parametros globales para preparar BioTech como plataforma empresarial escalable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tema oscuro",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Preferencia personal persistida localmente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = {
                            coroutineScope.launch {
                                sessionDataStore.saveThemePreference(it)
                            }
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "Preferencias personales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        items(personalItems) { item ->
            SettingsInfoCard(item)
        }

        item {
            Text(
                text = "Configuracion global",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        items(globalItems) { item ->
            SettingsInfoCard(item)
        }
    }
}

@Composable
private fun SettingsInfoCard(item: SettingsSectionItem) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
