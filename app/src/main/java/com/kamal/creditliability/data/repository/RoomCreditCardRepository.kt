package com.kamal.creditliability.data.repository

import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.local.CreditCardDao
import com.kamal.creditliability.domain.repository.CreditCardRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomCreditCardRepository @Inject constructor(
    private val creditCardDao: CreditCardDao
) : CreditCardRepository {
    override fun observeCards(): Flow<List<CreditCardEntity>> = creditCardDao.observeAll()

    override fun observeCard(id: Long): Flow<CreditCardEntity?> = creditCardDao.observeById(id)

    override suspend fun getCard(id: Long): CreditCardEntity? = creditCardDao.getById(id)

    override suspend fun insertCard(card: CreditCardEntity): Long = creditCardDao.insert(card)

    override suspend fun updateCard(card: CreditCardEntity) {
        creditCardDao.update(card)
    }

    override suspend fun deleteCard(card: CreditCardEntity) {
        creditCardDao.delete(card)
    }
}
