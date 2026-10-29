package com.kamal.creditliability.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kamal.creditliability.data.entity.BillingStatementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillingStatementDao {
    @Query("SELECT * FROM billing_statements ORDER BY statementDate DESC")
    fun observeAll(): Flow<List<BillingStatementEntity>>

    @Query("SELECT * FROM billing_statements WHERE cardId = :cardId ORDER BY statementDate DESC")
    fun observeByCard(cardId: Long): Flow<List<BillingStatementEntity>>

    @Insert
    suspend fun insert(value: BillingStatementEntity): Long

    @Update
    suspend fun update(value: BillingStatementEntity): Int

    @Delete
    suspend fun delete(value: BillingStatementEntity): Int

    @Query("DELETE FROM billing_statements")
    suspend fun deleteAll()
}