package com.kamal.creditliability.data.repository

import com.kamal.creditliability.data.entity.BillingStatementEntity
import com.kamal.creditliability.data.local.BillingStatementDao
import com.kamal.creditliability.domain.repository.BillingStatementRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomBillingStatementRepository @Inject constructor(
    private val dao: BillingStatementDao
) : BillingStatementRepository {
    override fun observeAll(): Flow<List<BillingStatementEntity>> = dao.observeAll()
    override suspend fun insert(value: BillingStatementEntity): Long = dao.insert(value)
    override suspend fun update(value: BillingStatementEntity) { dao.update(value) }
    override suspend fun delete(value: BillingStatementEntity) { dao.delete(value) }
}