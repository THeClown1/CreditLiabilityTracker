package com.kamal.creditliability.domain.repository

import com.kamal.creditliability.data.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun observeCards(): Flow<List<CreditCardEntity>>
    fun observeCard(id: Long): Flow<CreditCardEntity?>
    suspend fun getCard(id: Long): CreditCardEntity?
    suspend fun insertCard(card: CreditCardEntity): Long
    suspend fun updateCard(card: CreditCardEntity)
    suspend fun deleteCard(card: CreditCardEntity)
}
