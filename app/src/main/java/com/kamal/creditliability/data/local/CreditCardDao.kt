package com.kamal.creditliability.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kamal.creditliability.data.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Transaction
    @Query("SELECT * FROM credit_cards WHERE id = :cardId LIMIT 1")
    fun observeWithTransactions(cardId: Long): Flow<CreditCardWithTransactions?>

    @Query("SELECT * FROM credit_cards ORDER BY bankName COLLATE NOCASE, cardName COLLATE NOCASE")
    fun observeAll(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :cardId LIMIT 1")
    fun observeById(cardId: Long): Flow<CreditCardEntity?>

    @Query("SELECT * FROM credit_cards WHERE id = :cardId LIMIT 1")
    suspend fun getById(cardId: Long): CreditCardEntity?

    @Insert
    suspend fun insert(card: CreditCardEntity): Long

    @Update
    suspend fun update(card: CreditCardEntity): Int

    @Delete
    suspend fun delete(card: CreditCardEntity): Int

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAll()
}
