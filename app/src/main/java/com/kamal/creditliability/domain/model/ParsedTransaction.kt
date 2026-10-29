package com.kamal.creditliability.domain.model

data class ParsedTransaction(
    val bank: String?,
    val cardLast4: String?,
    val amount: Double,
    val merchant: String,
    val transactionType: String,
    val transactionDate: Long,
    val category: String,
    val confidence: Double,
    val isFinancialMessage: Boolean = true,
    val isCreditCardTransaction: Boolean = true
)