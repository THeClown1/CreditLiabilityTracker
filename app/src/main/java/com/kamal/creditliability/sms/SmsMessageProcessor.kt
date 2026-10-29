package com.kamal.creditliability.sms

import com.kamal.creditliability.data.entity.SmsMessageEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.repository.SmsMessageRepository
import com.kamal.creditliability.domain.service.TransactionCreationService
import com.kamal.creditliability.domain.service.TransactionSaveResult
import com.kamal.creditliability.sms.parser.TransactionParser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class SmsMessageProcessor @Inject constructor(
    private val smsFilter: SmsFilter,
    private val smsMessageRepository: SmsMessageRepository,
    private val parser: TransactionParser,
    private val cardRepository: CreditCardRepository,
    private val transactionCreationService: TransactionCreationService,
    private val thresholds: ConfidenceThresholds
) {
    suspend fun processIncoming(sender: String?, body: String, receivedAt: Long): Long? {
        val normalizedSender = sender?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val normalizedBody = body.trim()
        if (!smsFilter.shouldProcess(normalizedSender, normalizedBody)) return null
        if (smsMessageRepository.containsDuplicate(normalizedSender, normalizedBody, receivedAt)) return null

        val smsId = smsMessageRepository.insert(
            SmsMessageEntity(
                sender = normalizedSender,
                body = normalizedBody,
                receivedAt = receivedAt,
                processed = false,
                transactionId = null,
                aiProcessed = false
            )
        )
        val parsed = parser.parse(normalizedSender, normalizedBody, receivedAt)
        if (parsed == null || parsed.confidence < thresholds.review) {
            markProcessed(smsId)
            return smsId
        }
        if (parsed.confidence < thresholds.automaticSave) return smsId

        val card = cardRepository.observeCards().first().firstOrNull {
            parsed.cardLast4 != null && it.lastFourDigits == parsed.cardLast4
        }
        if (card == null) return smsId
        val transaction = TransactionEntity(
            cardId = card.id,
            amount = parsed.amount,
            merchant = parsed.merchant,
            category = parsed.category,
            transactionType = parsed.transactionType,
            transactionDate = parsed.transactionDate,
            description = "Detected from SMS",
            source = "SMS",
            smsId = smsId.toString(),
            confidence = parsed.confidence,
            createdAt = System.currentTimeMillis(),
            fingerprint = null
        )
        val result = transactionCreationService.save(transaction)
        val transactionId = when (result) {
            is TransactionSaveResult.Inserted -> result.id
            is TransactionSaveResult.Duplicate -> result.existingId
        }
        smsMessageRepository.update(
            checkNotNull(smsMessageRepository.getById(smsId)).copy(
                processed = true,
                transactionId = transactionId
            )
        )
        return smsId
    }

    private suspend fun markProcessed(id: Long) {
        smsMessageRepository.getById(id)?.let { smsMessageRepository.update(it.copy(processed = true)) }
    }
}