package com.kamal.creditliability.domain.repository

import com.kamal.creditliability.data.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

interface SmsMessageRepository {
    fun observeUnprocessed(): Flow<List<SmsMessageEntity>>
    suspend fun getById(id: Long): SmsMessageEntity?
    suspend fun containsDuplicate(sender: String, body: String, receivedAt: Long): Boolean
    suspend fun insert(message: SmsMessageEntity): Long
    suspend fun update(message: SmsMessageEntity)
}
