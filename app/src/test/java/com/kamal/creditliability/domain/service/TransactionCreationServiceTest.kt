package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionCreationServiceTest {
    @Test
    fun identicalFingerprintIsRejected() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        assertTrue(service.save(candidate()).isInserted())
        assertTrue(service.save(candidate(smsId = "second-sms")).isDuplicate())
        assertEquals(1, repository.rows.size)
    }

    @Test
    fun sameSmsIdIsRejectedEvenIfCandidateDiffers() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        service.save(candidate(smsId = "sms-1"))
        assertTrue(service.save(candidate(smsId = "sms-1", amount = 900.0)).isDuplicate())
        assertEquals(1, repository.rows.size)
    }

    @Test
    fun nearMatchWithinOneDayIsRejected() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        service.save(candidate(merchant = "Cafe & Co", smsId = "first"))
        assertTrue(service.save(candidate(
            merchant = "Cafe Co",
            transactionDate = DAY + 60_000,
            smsId = "second"
        )).isDuplicate())
        assertEquals(1, repository.rows.size)
    }

    @Test
    fun differentCardIsNotDeduplicated() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        service.save(candidate(cardId = 1L))
        assertTrue(service.save(candidate(cardId = 2L)).isInserted())
        assertEquals(2, repository.rows.size)
    }

    @Test
    fun refundLinksToUniqueMatchingPurchase() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        val purchaseId = (service.save(candidate(amount = 1_000.0)) as TransactionSaveResult.Inserted).id

        val result = service.save(candidate(
            amount = 1_000.0,
            merchant = "Card refund",
            type = "REFUND",
            category = "REFUND",
            transactionDate = DAY + 10 * MILLIS_PER_DAY
        )) as TransactionSaveResult.Inserted

        assertEquals(purchaseId, repository.rows.single { it.id == result.id }.relatedTransactionId)
    }

    @Test
    fun ambiguousRefundDoesNotGuessAnOriginalPurchase() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        service.save(candidate(merchant = "Shop A", amount = 500.0, smsId = "purchase-a"))
        service.save(candidate(merchant = "Shop B", amount = 500.0, smsId = "purchase-b"))

        val result = service.save(candidate(
            merchant = "Card refund",
            amount = 500.0,
            type = "REFUND",
            category = "REFUND",
            transactionDate = DAY + MILLIS_PER_DAY
        )) as TransactionSaveResult.Inserted

        assertNull(repository.rows.single { it.id == result.id }.relatedTransactionId)
    }

    @Test
    fun partialRefundLinksWhenMerchantMatches() = runBlocking {
        val repository = InMemoryTransactionRepository()
        val service = TransactionCreationService(repository)
        val purchaseId = (service.save(candidate(
            merchant = "Travel Co",
            amount = 2_000.0
        )) as TransactionSaveResult.Inserted).id

        val refund = service.save(candidate(
            merchant = "Travel Co",
            amount = 500.0,
            type = "REFUND",
            category = "REFUND",
            transactionDate = DAY + 2 * MILLIS_PER_DAY
        )) as TransactionSaveResult.Inserted

        assertEquals(purchaseId, repository.rows.single { it.id == refund.id }.relatedTransactionId)
    }

    private fun TransactionSaveResult.isInserted() = this is TransactionSaveResult.Inserted
    private fun TransactionSaveResult.isDuplicate() = this is TransactionSaveResult.Duplicate

    private fun candidate(
        cardId: Long = 1L,
        amount: Double = 125.50,
        merchant: String = "Cafe & Co",
        type: String = "PURCHASE",
        category: String = "FOOD",
        transactionDate: Long = DAY,
        smsId: String? = null
    ) = TransactionEntity(
        cardId = cardId,
        amount = amount,
        merchant = merchant,
        category = category,
        transactionType = type,
        transactionDate = transactionDate,
        description = null,
        source = if (smsId == null) "MANUAL" else "SMS",
        smsId = smsId,
        confidence = null,
        createdAt = DAY,
        fingerprint = null
    )

    private class InMemoryTransactionRepository : TransactionRepository {
        val rows = mutableListOf<TransactionEntity>()
        override fun observeTransactions(): Flow<List<TransactionEntity>> = emptyFlow()
        override fun observeByCard(cardId: Long): Flow<List<TransactionEntity>> = emptyFlow()
        override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = emptyFlow()
        override suspend fun findBySmsId(smsId: String): TransactionEntity? =
            rows.firstOrNull { it.smsId == smsId }
        override suspend fun findByFingerprint(fingerprint: String): TransactionEntity? =
            rows.firstOrNull { it.fingerprint == fingerprint }
        override suspend fun findNearMatches(cardId: Long, fromDate: Long, toDate: Long): List<TransactionEntity> =
            rows.filter { it.cardId == cardId && it.transactionDate in fromDate..toDate }
        override suspend fun findPurchasesForRefund(
            cardId: Long,
            fromDate: Long,
            toDate: Long
        ): List<TransactionEntity> = rows.filter {
            it.cardId == cardId && it.transactionType == "PURCHASE" && it.transactionDate in fromDate..toDate
        }
        override suspend fun insert(transaction: TransactionEntity): Long {
            val id = (rows.size + 1).toLong()
            rows += transaction.copy(id = id)
            return id
        }
    }

    private companion object {
        const val DAY = 1_800_000_000_000L
        const val MILLIS_PER_DAY = 86_400_000L
    }
}