package com.kamal.creditliability.domain.repository

import com.kamal.creditliability.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<TransactionEntity>>
    fun observeByCard(cardId: Long): Flow<List<TransactionEntity>>
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>
    suspend fun findBySmsId(smsId: String): TransactionEntity?
    suspend fun findByFingerprint(fingerprint: String): TransactionEntity?
    suspend fun findNearMatches(cardId: Long, fromDate: Long, toDate: Long): List<TransactionEntity>
    suspend fun findPurchasesForRefund(cardId: Long, fromDate: Long, toDate: Long): List<TransactionEntity>
    suspend fun insert(transaction: TransactionEntity): Long
}