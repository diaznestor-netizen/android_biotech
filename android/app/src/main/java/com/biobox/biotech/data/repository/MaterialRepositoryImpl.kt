package com.biobox.biotech.data.repository

import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.data.local.dao.MaterialDao
import com.biobox.biotech.data.mapper.filterSuccess
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntityResult
import com.biobox.biotech.data.remote.api.MaterialService
import com.biobox.biotech.domain.model.Material
import com.biobox.biotech.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRepositoryImpl @Inject constructor(
    private val materialService: MaterialService,
    private val materialDao: MaterialDao
) : MaterialRepository {

    override fun getMaterials(query: String?): Flow<List<Material>> {
        val flow = if (query.isNullOrBlank()) {
            materialDao.getAllMaterials()
        } else {
            materialDao.searchMaterials("%$query%")
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshMaterials(query: String?): ApiResult<Unit> {
        return try {
            val response = materialService.getMaterials(query)
            if (response.isSuccessful) {
                val dtos = response.body().orEmpty()
                val mappingResults = dtos.map { it.toEntityResult() }
                val entities = mappingResults.filterSuccess()
                
                if (entities.isNotEmpty()) {
                    materialDao.insertMaterials(entities)
                    if (query.isNullOrBlank()) {
                        val remoteIds = entities.mapNotNull { it.remoteId }
                        materialDao.deleteStaleMaterials(remoteIds)
                    }
                }
                ApiResult.Success(Unit)
            } else {
                ApiResult.HttpError(response.code(), response.message())
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }
}
