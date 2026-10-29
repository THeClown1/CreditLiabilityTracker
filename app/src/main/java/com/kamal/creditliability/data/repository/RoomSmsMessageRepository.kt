package com.kamal.creditliability.data.repository

import com.kamal.creditliability.data.entity.SmsMessageEntity
import com.kamal.creditliability.data.local.SmsMessageDao
import com.kamal.creditliability.domain.repository.SmsMessageRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomSmsMessageRepository @Inject constructor(
    private val dao: SmsMessageDao
) : SmsMessageRepository {
    override fun observeUnprocessed(): Flow<List<SmsMessageEntity>> = dao.observeUnprocessed()
    override suspend fun getById(id: Long): SmsMessageEntity? = dao.getById(id)
    override suspend fun containsDuplicate(sender: String, body: String, receivedAt: Long): Boolean =
        dao.findDuplicateId(sender, body, receivedAt) != null
    override suspend fun insert(message: SmsMessageEntity): Long = dao.insert(message)
    override suspend fun update(message: SmsMessageEntity) { dao.update(message) }
}