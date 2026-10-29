package com.kamal.creditliability.data.repository

import com.kamal.creditliability.data.entity.EmiObligationEntity
import com.kamal.creditliability.data.local.EmiObligationDao
import com.kamal.creditliability.domain.repository.EmiObligationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomEmiObligationRepository @Inject constructor(
    private val dao: EmiObligationDao
) : EmiObligationRepository {
    override fun observeAll(): Flow<List<EmiObligationEntity>> = dao.observeAll()
    override suspend fun insert(value: EmiObligationEntity): Long = dao.insert(value)
    override suspend fun update(value: EmiObligationEntity) { dao.update(value) }
    override suspend fun delete(value: EmiObligationEntity) { dao.delete(value) }
}