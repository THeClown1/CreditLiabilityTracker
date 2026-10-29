package com.kamal.creditliability.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val cardName: String,
    val lastFourDigits: String,
    val creditLimit: Double,
    val billingDay: Int?,
    val dueDay: Int?,
    val createdAt: Long
)
