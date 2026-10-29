package com.kamal.creditliability.sms

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsFilter @Inject constructor() {
    fun shouldProcess(sender: String?, body: String): Boolean {
        if (sender.isNullOrBlank() || body.isBlank()) return false

        val text = body.lowercase()
        if (otpPattern.containsMatchIn(text)) return false

        val mentionsCard = cardPattern.containsMatchIn(text)
        val mentionsFinancialActivity = financialPattern.containsMatchIn(text)
        if (!mentionsCard || !mentionsFinancialActivity) return false

        val isPromotion = promotionPattern.containsMatchIn(text)
        val isTransactionNotice = transactionPattern.containsMatchIn(text)
        return !isPromotion || isTransactionNotice
    }

    private companion object {
        val otpPattern = Regex(
            """\b(?:otp|one[ -]time password|verification code|authentication code|login code)\b""",
            RegexOption.IGNORE_CASE
        )
        val cardPattern = Regex(
            """\b(?:credit\s*card|card|cc)\b|(?:xx|\*{2,})\s*\d{4}|(?:ending|ends in)\s*\d{4}""",
            RegexOption.IGNORE_CASE
        )
        val financialPattern = Regex(
            """\b(?:spent|purchase|used|transaction|txn|debited|credited|payment|refund|reversal|fee|charge|statement|bill|due|emi|cash withdrawal|withdrawn)\b""",
            RegexOption.IGNORE_CASE
        )
        val transactionPattern = Regex(
            """\b(?:spent|purchase|used|transaction|txn|debited|credited|payment|refund|reversal|fee|charge|emi|cash withdrawal|withdrawn)\b""",
            RegexOption.IGNORE_CASE
        )
        val promotionPattern = Regex(
            """\b(?:offer|promo(?:tion)?|discount|win|limited time|apply now|pre-approved|exclusive deal)\b""",
            RegexOption.IGNORE_CASE
        )
    }
}
