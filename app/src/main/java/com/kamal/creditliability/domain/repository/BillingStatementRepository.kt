package com.kamal.creditliability.domain.repository

import com.kamal.creditliability.data.entity.BillingStatementEntity
import kotlinx.coroutines.flow.Flow

interface BillingStatementRepository {
    fun observeAll(): Flow<List<BillingStatementEntity>>
    suspend fun insert(value: BillingStatementEntity): Long
    suspend fun update(value: BillingStatementEntity)
    suspend fun delete(value: BillingStatementEntity)
}