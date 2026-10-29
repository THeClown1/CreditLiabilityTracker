package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.repository.TransactionRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface TransactionSaveResult {
    data class Inserted(val id: Long) : TransactionSaveResult
    data class Duplicate(val existingId: Long) : TransactionSaveResult
}

@Singleton
class TransactionCreationService @Inject constructor(
    private val repository: TransactionRepository
) {
    private val mutex = Mutex()

    suspend fun save(candidate: TransactionEntity): TransactionSaveResult = mutex.withLock {
        require(candidate.cardId > 0L)
        require(candidate.amount.isFinite() && candidate.amount > 0.0)
        require(candidate.transactionType in ALLOWED_TYPES)

        val withFingerprint = candidate.copy(fingerprint = TransactionFingerprint.create(candidate))
        candidate.smsId?.let { repository.findBySmsId(it)?.let { found ->
            return@withLock TransactionSaveResult.Duplicate(found.id)
        } }
        repository.findByFingerprint(checkNotNull(withFingerprint.fingerprint))?.let {
            return@withLock TransactionSaveResult.Duplicate(it.id)
        }
        val day = 86_400_000L
        val possible = repository.findNearMatches(
            withFingerprint.cardId,
            withFingerprint.transactionDate - day,
            withFingerprint.transactionDate + day
        ).firstOrNull { existing ->
            existing.transactionType == withFingerprint.transactionType &&
                kotlin.math.abs(existing.amount - withFingerprint.amount) < 0.01 &&
                TransactionFingerprint.normalizeMerchant(existing.merchant) ==
                TransactionFingerprint.normalizeMerchant(withFingerprint.merchant) &&
                kotlin.math.abs(existing.transactionDate - withFingerprint.transactionDate) <= day
        }
        if (possible != null) return@withLock TransactionSaveResult.Duplicate(possible.id)

        val linked = withFingerprint.copy(
            relatedTransactionId = findOriginalPurchase(withFingerprint)
        )
        TransactionSaveResult.Inserted(repository.insert(linked))
    }

    private suspend fun findOriginalPurchase(refund: TransactionEntity): Long? {
        if (refund.transactionType != "REFUND") return null
        val window = REFUND_MATCH_WINDOW_DAYS * MILLIS_PER_DAY
        val purchases = repository.findPurchasesForRefund(
            cardId = refund.cardId,
            fromDate = (refund.transactionDate - window).coerceAtLeast(0L),
            toDate = refund.transactionDate
        ).filter { it.amount + AMOUNT_TOLERANCE >= refund.amount }
        if (purchases.isEmpty()) return null

        val refundMerchant = normalizeMerchant(refund.merchant)
        val merchantIsKnown = refundMerchant !in UNKNOWN_REFUND_MERCHANTS
        val candidates = if (merchantIsKnown) {
            purchases.filter { normalizeMerchant(it.merchant) == refundMerchant }
        } else {
            purchases.filter { kotlin.math.abs(it.amount - refund.amount) < AMOUNT_TOLERANCE }
        }
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.single().id

        val ranked = candidates.sortedWith(
            compareBy<TransactionEntity> { kotlin.math.abs(refund.transactionDate - it.transactionDate) }
                .thenBy { kotlin.math.abs(it.amount - refund.amount) }
        )
        val first = ranked[0]
        val second = ranked[1]
        val firstDateDistance = kotlin.math.abs(refund.transactionDate - first.transactionDate)
        val secondDateDistance = kotlin.math.abs(refund.transactionDate - second.transactionDate)
        val firstAmountDistance = kotlin.math.abs(first.amount - refund.amount)
        val secondAmountDistance = kotlin.math.abs(second.amount - refund.amount)
        if (firstDateDistance == secondDateDistance && firstAmountDistance == secondAmountDistance) {
            return null
        }
        return first.id
    }

    private fun normalizeMerchant(value: String): String =
        value.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")

    private companion object {
        const val REFUND_MATCH_WINDOW_DAYS = 180L
        const val MILLIS_PER_DAY = 86_400_000L
        const val AMOUNT_TOLERANCE = 0.01
        val UNKNOWN_REFUND_MERCHANTS = setOf(
            "cardrefund",
            "unknownmerchant",
            ""
        )
        val ALLOWED_TYPES = setOf(
            "PURCHASE", "PAYMENT", "REFUND", "FEE", "EMI", "CASH_WITHDRAWAL", "OTHER"
        )
    }
}