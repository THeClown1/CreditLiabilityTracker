package com.kamal.creditliability.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.BillingStatementEntity
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.EmiObligationEntity
import com.kamal.creditliability.domain.repository.BillingStatementRepository
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.repository.EmiObligationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanceManagementState(
    val cards: List<CreditCardEntity> = emptyList(),
    val emis: List<EmiObligationEntity> = emptyList(),
    val statements: List<BillingStatementEntity> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class FinanceManagementViewModel @Inject constructor(
    cardRepository: CreditCardRepository,
    private val emiRepository: EmiObligationRepository,
    private val billingRepository: BillingStatementRepository
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)
    val state: StateFlow<FinanceManagementState> = combine(
        cardRepository.observeCards(),
        emiRepository.observeAll(),
        billingRepository.observeAll(),
        message
    ) { cards, emis, statements, currentMessage ->
        FinanceManagementState(cards, emis, statements, currentMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceManagementState())

    fun saveEmi(
        cardId: Long?,
        merchant: String,
        totalAmount: String,
        emiAmount: String,
        tenure: String,
        remainingTenure: String,
        interest: String,
        processingFee: String,
        nextDueDate: String
    ) {
        val total = totalAmount.toDoubleOrNull()
        val monthly = emiAmount.toDoubleOrNull()
        val months = tenure.toIntOrNull()
        val remaining = remainingTenure.toIntOrNull()
        val rate = interest.toDoubleOrNull()
        val fee = processingFee.toDoubleOrNull()
        val date = parseDate(nextDueDate)
        if (cardId == null || merchant.isBlank() || total == null || total <= 0.0 ||
            monthly == null || monthly <= 0.0 || months == null || months < 1 ||
            remaining == null || remaining !in 0..months ||
            rate == null || rate < 0.0 || fee == null || fee < 0.0 ||
            (nextDueDate.isNotBlank() && date == null)
        ) {
            message.value = "Check the EMI details. Amounts and tenure must be valid."
            return
        }
        viewModelScope.launch {
            runCatching {
                emiRepository.insert(
                    EmiObligationEntity(
                        cardId = cardId,
                        merchant = merchant.trim(),
                        totalAmount = total,
                        emiAmount = monthly,
                        tenure = months,
                        remainingTenure = remaining,
                        interest = rate,
                        processingFee = fee,
                        nextDueDate = date,
                        active = remaining > 0,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }.onSuccess { message.value = "EMI obligation saved." }
                .onFailure { message.value = "Unable to save the EMI obligation." }
        }
    }

    fun saveStatement(
        cardId: Long?,
        statementDate: String,
        dueDate: String,
        statementAmount: String,
        paidAmount: String
    ) {
        val statementMillis = parseDate(statementDate)
        val dueMillis = parseDate(dueDate)
        val amount = statementAmount.toDoubleOrNull()
        val paid = paidAmount.toDoubleOrNull()
        if (cardId == null || statementMillis == null || dueMillis == null ||
            dueMillis < statementMillis || amount == null || amount < 0.0 ||
            paid == null || paid < 0.0 || paid > amount
        ) {
            message.value = "Enter valid statement and due dates, and a paid amount within the bill total."
            return
        }
        val status = when {
            paid >= amount -> "PAID"
            paid > 0.0 -> "PARTIAL"
            else -> "UNPAID"
        }
        viewModelScope.launch {
            runCatching {
                billingRepository.insert(
                    BillingStatementEntity(
                        cardId = cardId,
                        statementDate = statementMillis,
                        dueDate = dueMillis,
                        statementAmount = amount,
                        paidAmount = paid,
                        paymentStatus = status,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }.onSuccess { message.value = "Statement saved." }
                .onFailure { message.value = "Unable to save the statement." }
        }
    }

    fun markStatementPaid(statement: BillingStatementEntity) {
        viewModelScope.launch {
            runCatching {
                billingRepository.update(
                    statement.copy(paidAmount = statement.statementAmount, paymentStatus = "PAID")
                )
            }.onSuccess { message.value = "Statement marked paid." }
                .onFailure { message.value = "Unable to update the statement." }
        }
    }

    fun dismissMessage() { message.value = null }

    private fun parseDate(value: String): Long? = runCatching {
        LocalDate.parse(value.trim())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}