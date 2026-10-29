package com.kamal.creditliability.ai

import com.kamal.creditliability.domain.model.ParsedTransaction

object AiParseValidator {
    private val transactionTypes = setOf("PURCHASE", "PAYMENT", "REFUND", "FEE", "EMI", "CASH_WITHDRAWAL", "OTHER")
    private val categories = setOf("SHOPPING", "FOOD", "TRAVEL", "FUEL", "BILLS", "ENTERTAINMENT", "HEALTHCARE", "EMI", "SUBSCRIPTION", "CASH_WITHDRAWAL", "FEES", "PAYMENT", "REFUND", "OTHERS")

    fun validate(value: ParsedTransaction?): ParsedTransaction? {
        value ?: return null
        if (!value.isFinancialMessage || !value.isCreditCardTransaction) return null
        if (!value.amount.isFinite() || value.amount <= 0.0) return null
        if (value.transactionType !in transactionTypes || value.category !in categories) return null
        if (!value.confidence.isFinite() || value.confidence !in 0.0..1.0) return null
        if (value.cardLast4 != null && !value.cardLast4.matches(Regex("\\d{4}"))) return null
        if (value.merchant.isBlank() || value.merchant.length > 100) return null
        return value.copy(merchant = value.merchant.trim())
    }
}