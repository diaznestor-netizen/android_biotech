package com.biobox.biotech.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.cards.KPIWidget
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val reportTypes = listOf(
        ReportType("Excel global", Icons.Default.TableChart, "global", PrimaryGreen)
    )

    Scaffold(
        topBar = {
            BioTechTopBar(title = "REPORTES OPERATIVOS")
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "PANEL DE GENERACIÓN",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryDark,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(reportTypes) { report ->
                    KPIWidget(
                        label = report.label.uppercase(),
                        value = "Generar",
                        icon = report.icon,
                        color = report.color,
                        modifier = Modifier.clickable {
                            viewModel.downloadGlobalExcel()
                        }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            }

            state.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    color = Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        color = Error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            state.downloadedFile?.let { file ->
                BioTechCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    containerColor = Success.copy(alpha = 0.12f),
                    elevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "REPORTE DESCARGADO",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Success
                                )
                                Text(
                                    file.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

data class ReportType(val label: String, val icon: ImageVector, val key: String, val color: Color)
