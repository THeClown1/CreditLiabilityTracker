package com.kamal.creditliability.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emi_obligations",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class EmiObligationEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val merchant: String,
    val totalAmount: Double,
    val emiAmount: Double,
    val tenure: Int,
    val remainingTenure: Int,
    val interest: Double,
    val processingFee: Double,
    val nextDueDate: Long?,
    val active: Boolean = true,
    val createdAt: Long
)