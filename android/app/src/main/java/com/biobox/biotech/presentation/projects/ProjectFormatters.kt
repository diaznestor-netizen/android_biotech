package com.biobox.biotech.presentation.projects

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val projectDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val projectDateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun formatProjectDate(value: Long?): String = value?.let { projectDateFormatter.format(Date(it)) } ?: "Sin fecha"
fun formatProjectDateTime(value: Long): String = projectDateTimeFormatter.format(Date(value))
fun parseProjectDate(value: String): Long? = runCatching { projectDateFormatter.parse(value)?.time }.getOrNull()
fun parseApiDate(value: String): Long? = runCatching { apiDateFormatter.parse(value)?.time }.getOrNull()
