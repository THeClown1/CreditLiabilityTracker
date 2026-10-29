package com.kamal.creditliability.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cardId"]),
        Index(value = ["smsId"]),
        Index(value = ["fingerprint"]),
        Index(value = ["relatedTransactionId"])
    ]
)
data class TransactionEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val amount: Double,
    val merchant: String,
    val category: String,
    val transactionType: String,
    val transactionDate: Long,
    val description: String?,
    val source: String,
    val smsId: String?,
    val confidence: Double?,
    val createdAt: Long,
    val fingerprint: String?,
    val relatedTransactionId: Long? = null
)
