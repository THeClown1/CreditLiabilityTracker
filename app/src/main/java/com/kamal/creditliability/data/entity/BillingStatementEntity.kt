package com.kamal.creditliability.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "billing_statements",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId", "statementDate"])]
)
data class BillingStatementEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val statementDate: Long,
    val dueDate: Long,
    val statementAmount: Double,
    val paidAmount: Double = 0.0,
    val paymentStatus: String = "UNPAID",
    val createdAt: Long
)