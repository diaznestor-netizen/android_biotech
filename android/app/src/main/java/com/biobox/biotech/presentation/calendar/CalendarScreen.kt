package com.biobox.biotech.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.CalendarEvent
import com.biobox.biotech.domain.model.EventType
import com.biobox.biotech.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.events.collectAsState()
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    val daysInMonth = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    LaunchedEffect(currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.set(currentYear, currentMonth, daysInMonth, 23, 59, 59)
        val end = cal.timeInMillis
        viewModel.loadEvents(start, end)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 0) { currentMonth = 11; currentYear-- }
                    else currentMonth--
                }) { Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = AzulOscuro) }

                Text(
                    "${SimpleDateFormat("MMMM", Locale("es")).format(Date(currentYear, currentMonth, 1))} $currentYear",
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AzulOscuro
                )

                IconButton(onClick = {
                    if (currentMonth == 11) { currentMonth = 0; currentYear++ }
                    else currentMonth++
                }) { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AzulOscuro) }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gris600)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val rows = (daysInMonth + firstDayOfWeek + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
                    for (col in 0..6) {
                        val day = row * 7 + col - firstDayOfWeek + 1
                        if (day in 1..daysInMonth) {
                            val isSelected = day == selectedDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        if (isSelected) AzulOscuro else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Blanco else AzulOscuro
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val s = state) {
                is UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulOscuro)
                }
                is UiState.Success -> {
                    val dayEvents = s.data.filter {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it.fechaInicio
                        cal.get(Calendar.DAY_OF_MONTH) == selectedDay &&
                                cal.get(Calendar.MONTH) == currentMonth &&
                                cal.get(Calendar.YEAR) == currentYear
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Text("Eventos del día", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro) }
                        if (dayEvents.isEmpty()) {
                            item { Text("No hay eventos", color = Gris600, fontSize = 13.sp) }
                        } else {
                            items(dayEvents) { event ->
                                EventCard(event)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun EventCard(event: CalendarEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrisCard)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(12.dp).background(
                    when (event.tipo) {
                        EventType.MISION -> Rojo
                        EventType.ACTIVIDAD -> AzulOscuro
                        EventType.INSPECCION -> Naranja
                        EventType.ENTREGA -> VerdePrincipal
                        EventType.MANTENIMIENTO -> Naranja.copy(alpha = 0.7f)
                        else -> Gris600
                    }, CircleShape
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AzulOscuro)
                Text(event.tipo.name, fontSize = 11.sp, color = Gris600)
            }
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.fechaInicio)),
                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro
            )
        }
    }
}
