package com.kamal.creditliability.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_messages",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["transactionId"]), Index(value = ["processed", "receivedAt"])]
)
data class SmsMessageEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val processed: Boolean = false,
    val transactionId: Long?,
    val aiProcessed: Boolean = false
)
