package com.kamal.creditliability.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kamal.creditliability.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE cardId = :cardId ORDER BY transactionDate DESC, id DESC")
    fun observeByCard(cardId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getById(transactionId: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE smsId = :smsId LIMIT 1")
    suspend fun findBySmsId(smsId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun findByFingerprint(fingerprint: String): TransactionEntity?

    @Query("""
        SELECT * FROM transactions
        WHERE cardId = :cardId AND transactionDate BETWEEN :fromDate AND :toDate
    """)
    suspend fun findNearMatches(cardId: Long, fromDate: Long, toDate: Long): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions
        WHERE cardId = :cardId AND transactionType = 'PURCHASE'
        AND transactionDate BETWEEN :fromDate AND :toDate
        ORDER BY transactionDate DESC
    """)
    suspend fun findPurchasesForRefund(
        cardId: Long,
        fromDate: Long,
        toDate: Long
    ): List<TransactionEntity>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Delete
    suspend fun delete(transaction: TransactionEntity): Int

    @Query("DELETE FROM transactions WHERE cardId = :cardId")
    suspend fun deleteByCard(cardId: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}