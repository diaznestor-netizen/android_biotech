package com.biobox.biotech.domain.repository

import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Material
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun getMaterials(query: String? = null): Flow<List<Material>>
    suspend fun refreshMaterials(query: String? = null): ApiResult<Unit>
}
