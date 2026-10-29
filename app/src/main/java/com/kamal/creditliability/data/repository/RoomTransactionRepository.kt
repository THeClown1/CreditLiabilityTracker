package com.kamal.creditliability.data.repository

import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.data.local.TransactionDao
import com.kamal.creditliability.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()
    override fun observeByCard(cardId: Long): Flow<List<TransactionEntity>> = transactionDao.observeByCard(cardId)
    override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = transactionDao.observeRecent(limit)
    override suspend fun findBySmsId(smsId: String): TransactionEntity? = transactionDao.findBySmsId(smsId)
    override suspend fun findByFingerprint(fingerprint: String): TransactionEntity? =
        transactionDao.findByFingerprint(fingerprint)
    override suspend fun findNearMatches(cardId: Long, fromDate: Long, toDate: Long): List<TransactionEntity> =
        transactionDao.findNearMatches(cardId, fromDate, toDate)
    override suspend fun findPurchasesForRefund(
        cardId: Long,
        fromDate: Long,
        toDate: Long
    ): List<TransactionEntity> = transactionDao.findPurchasesForRefund(cardId, fromDate, toDate)
    override suspend fun insert(transaction: TransactionEntity): Long = transactionDao.insert(transaction)
}