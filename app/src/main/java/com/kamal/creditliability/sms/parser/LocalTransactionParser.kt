package com.kamal.creditliability.sms.parser

import com.kamal.creditliability.domain.model.ParsedTransaction
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalTransactionParser @Inject constructor() : TransactionParser {
    override fun parse(sender: String?, body: String, receivedAt: Long): ParsedTransaction? {
        if (body.isBlank()) return null
        val amountMatch = amountRegex.find(body) ?: return null
        val amount = amountMatch.groupValues.drop(1).firstOrNull(String::isNotBlank)
            ?.replace(",", "")?.toDoubleOrNull()?.takeIf { it > 0.0 && it.isFinite() } ?: return null
        val type = detectType(body) ?: return null
        val card = cardRegex.find(body)?.groupValues?.drop(1)?.firstOrNull(String::isNotBlank)
        val parsedDate = parseDate(body)
        val date = parsedDate ?: receivedAt
        val extractedMerchant = extractMerchant(body, amountMatch.range)
        val merchant = when (type) {
            "PAYMENT" -> "Card payment"
            "REFUND" -> extractedMerchant.takeUnless { it == "Unknown merchant" } ?: "Card refund"
            else -> extractedMerchant
        }
        val bank = detectBank(sender.orEmpty() + " " + body)
        val confidence = (
            0.50 + (if (card != null) 0.20 else 0.0) +
                (if (merchant != "Unknown merchant") 0.15 else 0.0) +
                (if (parsedDate != null) 0.10 else 0.0) +
                (if (bank != null) 0.05 else 0.0)
            ).coerceIn(0.0, 0.95)
        return ParsedTransaction(
            bank = bank,
            cardLast4 = card,
            amount = amount,
            merchant = merchant,
            transactionType = type,
            transactionDate = date,
            category = categorize(type, merchant),
            confidence = confidence
        )
    }

    private fun detectType(text: String): String? {
        val value = text.lowercase(Locale.ROOT)
        return when {
            Regex("""\b(refund|reversal|reversed|credited back)\b""").containsMatchIn(value) -> "REFUND"
            Regex("""\b(payment\s+(?:of|received|credited|towards|for)|paid\s+towards)\b""").containsMatchIn(value) -> "PAYMENT"
            Regex("""\b(cash withdrawal|cash withdrawn|atm withdrawal)\b""").containsMatchIn(value) -> "CASH_WITHDRAWAL"
            Regex("""\b(emi|installment|instalment)\b""").containsMatchIn(value) -> "EMI"
            Regex("""\b(fee|charged|annual charge|late payment charge)\b""").containsMatchIn(value) -> "FEE"
            Regex("""\b(spent|purchase|used for|transaction|txn|debited)\b""").containsMatchIn(value) -> "PURCHASE"
            else -> null
        }
    }

    private fun extractMerchant(body: String, amountRange: IntRange): String {
        val pattern = Regex("""(?i)\bat\s+([A-Z0-9&._ -]{2,40}?)(?=\s+(?:on|for|using|with|card|via)\b|[.,;]|$)""")
        val tail = body.substring((amountRange.last + 1).coerceAtMost(body.length))
        return (pattern.find(tail) ?: pattern.find(body))
            ?.groupValues?.get(1)?.trim()?.takeIf(String::isNotBlank) ?: "Unknown merchant"
    }

    private fun parseDate(body: String): Long? {
        val raw = dateRegex.find(body)?.value ?: return null
        val patterns = listOf(
            "d-MMM-uu", "dd-MMM-uu", "d-MMM-uuuu", "dd-MMM-uuuu",
            "d/MM/uuuu", "dd/MM/uuuu", "d-MM-uuuu", "dd-MM-uuuu"
        )
        for (pattern in patterns) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) { }
        }
        return null
    }

    private fun detectBank(text: String): String? = listOf("ICICI", "HDFC", "SBI", "AXIS", "KOTAK", "CITI")
        .firstOrNull { Regex("""(?i)\b$it\b""").containsMatchIn(text) }

    private fun categorize(type: String, merchant: String): String = when {
        type == "PAYMENT" -> "PAYMENT"
        type == "REFUND" -> "REFUND"
        type == "EMI" -> "EMI"
        type == "FEE" -> "FEES"
        type == "CASH_WITHDRAWAL" -> "CASH_WITHDRAWAL"
        merchant.contains("food", true) || merchant.contains("swiggy", true) ||
            merchant.contains("zomato", true) -> "FOOD"
        merchant.contains("fuel", true) || merchant.contains("petrol", true) -> "FUEL"
        merchant == "Unknown merchant" -> "OTHERS"
        else -> "SHOPPING"
    }

    private companion object {
        val amountRegex = Regex(
            """(?i)(?:INR|Rs\.?|₹)\s*([0-9][0-9,]*(?:\.\d{1,2})?)|([0-9][0-9,]*(?:\.\d{1,2})?)\s*INR"""
        )
        val cardRegex = Regex(
            """(?i)(?:x{2,}|\*{2,})\s*(\d{4})|(?:ending|ends in|card)\s*(?:no\.?\s*)?(\d{4})"""
        )
        val dateRegex = Regex(
            """(?i)\b\d{1,2}-[a-z]{3}-\d{2,4}\b|\b\d{1,2}/\d{1,2}/\d{4}\b|\b\d{1,2}-\d{1,2}-\d{4}\b"""
        )
    }
}