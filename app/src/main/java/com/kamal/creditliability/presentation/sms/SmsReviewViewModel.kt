package com.kamal.creditliability.presentation.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.SmsMessageEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.ParsedTransaction
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.repository.SmsMessageRepository
import com.kamal.creditliability.domain.service.TransactionCreationService
import com.kamal.creditliability.domain.service.TransactionSaveResult
import com.kamal.creditliability.sms.ConfidenceThresholds
import com.kamal.creditliability.sms.parser.TransactionParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingSmsCandidate(
    val message: SmsMessageEntity,
    val parsed: ParsedTransaction,
    val cards: List<CreditCardEntity>,
    val matchedCardId: Long?
)

@HiltViewModel
class SmsReviewViewModel @Inject constructor(
    private val smsRepository: SmsMessageRepository,
    cardRepository: CreditCardRepository,
    private val parser: TransactionParser,
    private val thresholds: ConfidenceThresholds,
    private val creationService: TransactionCreationService
) : ViewModel() {
    val candidates: StateFlow<List<PendingSmsCandidate>> = combine(
        smsRepository.observeUnprocessed(),
        cardRepository.observeCards()
    ) { messages, cards ->
        messages.mapNotNull { message ->
            val parsed = parser.parse(message.sender, message.body, message.receivedAt)
                ?.takeIf { it.confidence >= thresholds.review }
                ?: return@mapNotNull null
            val matchedId = cards.firstOrNull {
                parsed.cardLast4 != null && it.lastFourDigits == parsed.cardLast4
            }?.id
            PendingSmsCandidate(message, parsed, cards, matchedId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun approve(
        item: PendingSmsCandidate,
        cardId: Long? = item.matchedCardId,
        amount: Double = item.parsed.amount,
        merchant: String = item.parsed.merchant
    ) {
        val card = item.cards.firstOrNull { it.id == cardId } ?: return
        if (!amount.isFinite() || amount <= 0.0 || merchant.isBlank()) return
        viewModelScope.launch {
            val transaction = TransactionEntity(
                cardId = card.id,
                amount = amount,
                merchant = merchant.trim(),
                category = item.parsed.category,
                transactionType = item.parsed.transactionType,
                transactionDate = item.parsed.transactionDate,
                description = "Confirmed from SMS",
                source = "SMS",
                smsId = item.message.id.toString(),
                confidence = item.parsed.confidence,
                createdAt = System.currentTimeMillis(),
                fingerprint = null
            )
            val result = creationService.save(transaction)
            val id = when (result) {
                is TransactionSaveResult.Inserted -> result.id
                is TransactionSaveResult.Duplicate -> result.existingId
            }
            smsRepository.update(item.message.copy(processed = true, transactionId = id))
        }
    }

    fun ignore(item: PendingSmsCandidate) {
        viewModelScope.launch { smsRepository.update(item.message.copy(processed = true)) }
    }
}