package com.biobox.biotech.data.mapper

sealed interface MappingResult<out T> {
    data class Success<T>(val value: T) : MappingResult<T>
    data class Invalid(val reason: String, val sourceId: String? = null) : MappingResult<Nothing>
}

fun <T> List<MappingResult<T>>.filterSuccess(): List<T> {
    return this.filterIsInstance<MappingResult.Success<T>>().map { it.value }
}
