package com.biobox.biotech.presentation.catalogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biobox.biotech.presentation.theme.AzulOscuro
import com.biobox.biotech.presentation.theme.VerdePrincipal

data class CatalogUiItem(
    val code: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogManagementScreen() {
    var query by remember { mutableStateOf("") }
    val catalogItems = remember {
        listOf(
            CatalogUiItem("areas", "Areas", "Zonas operativas y administrativas", Icons.Default.Apartment),
            CatalogUiItem("departamentos", "Departamentos", "Clasificacion interna por area", Icons.Default.Business),
            CatalogUiItem("puestos", "Puestos", "Perfiles y posiciones organizacionales", Icons.Default.Work),
            CatalogUiItem("paises", "Paises", "Cobertura territorial y sedes", Icons.Default.Flag),
            CatalogUiItem("estados_ciudades", "Estados y ciudades", "Ubicaciones geograficas de operacion", Icons.Default.Place),
            CatalogUiItem("tipos_maquina", "Tipos de maquina", "Clasificacion principal de maquinaria", Icons.Default.PrecisionManufacturing),
            CatalogUiItem("categorias_material", "Categorias de material", "Segmentos de materiales e inventario", Icons.Default.Category),
            CatalogUiItem("unidades_medida", "Unidades de medida", "Unidades operativas y logistica", Icons.Default.Straighten),
            CatalogUiItem("prioridades", "Prioridades", "Escalas de atencion y seguimiento", Icons.Default.Speed),
            CatalogUiItem("estados_proceso", "Estados de proceso", "Flujo operativo y estados", Icons.Default.Inventory2)
        )
    }

    val filteredItems = catalogItems.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CatalogHeaderCard()
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar catalogo") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }

        items(filteredItems) { catalog ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = catalog.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = catalog.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = catalog.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Codigo backend: ${catalog.code}",
                            style = MaterialTheme.typography.labelMedium,
                            color = VerdePrincipal,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogHeaderCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Administracion de catalogos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Base reusable para altas, edicion, activacion, desactivacion y futuras restauraciones sin duplicar modulos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Preparado para backend /api/v1/catalogs",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
