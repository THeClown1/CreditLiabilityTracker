package com.kamal.creditliability.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity

data class CreditCardWithTransactions(
    @field:Embedded
    val card: CreditCardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId"
    )
    val transactions: List<TransactionEntity>
)
