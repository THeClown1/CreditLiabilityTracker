package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.TransactionEntity
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TransactionFingerprint {
    fun create(transaction: TransactionEntity): String {
        val day = Instant.ofEpochMilli(transaction.transactionDate)
            .atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        val canonical = listOf(
            transaction.cardId.toString(),
            BigDecimal.valueOf(transaction.amount).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
            normalizeMerchant(transaction.merchant),
            day,
            transaction.transactionType.uppercase(Locale.ROOT)
        ).joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun normalizeMerchant(value: String): String =
        value.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
}