package com.kamal.creditliability.domain.repository

import com.kamal.creditliability.data.entity.EmiObligationEntity
import kotlinx.coroutines.flow.Flow

interface EmiObligationRepository {
    fun observeAll(): Flow<List<EmiObligationEntity>>
    suspend fun insert(value: EmiObligationEntity): Long
    suspend fun update(value: EmiObligationEntity)
    suspend fun delete(value: EmiObligationEntity)
}