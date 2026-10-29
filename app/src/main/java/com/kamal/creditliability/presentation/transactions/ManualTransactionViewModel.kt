package com.kamal.creditliability.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.service.TransactionCreationService
import com.kamal.creditliability.domain.service.TransactionSaveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

data class ManualTransactionUiState(
    val cardId: Long? = null,
    val amount: String = "",
    val merchant: String = "",
    val category: String = "OTHERS",
    val transactionType: String = "PURCHASE",
    val transactionDate: Long = LocalDate.now().toEpochDay() * MILLIS_PER_DAY,
    val description: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val message: String? = null
)

@HiltViewModel
class ManualTransactionViewModel @Inject constructor(
    cardRepository: CreditCardRepository,
    private val transactionCreationService: TransactionCreationService
) : ViewModel() {
    val cards: StateFlow<List<CreditCardEntity>> =
        cardRepository.observeCards().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(ManualTransactionUiState())
    val uiState = _uiState.asStateFlow()

    fun selectCard(cardId: Long) = updateField("cardId") { it.copy(cardId = cardId) }
    fun onAmountChanged(value: String) = updateField("amount") { it.copy(amount = value) }
    fun onMerchantChanged(value: String) = updateField("merchant") { it.copy(merchant = value) }
    fun onCategoryChanged(value: String) = updateField("category") { it.copy(category = value) }
    fun onTransactionTypeChanged(value: String) =
        updateField("transactionType") { it.copy(transactionType = value) }
    fun onTransactionDateChanged(value: Long) =
        updateField("transactionDate") { it.copy(transactionDate = value) }
    fun onDescriptionChanged(value: String) =
        updateField("description") { it.copy(description = value) }

    private fun updateField(field: String, transform: (ManualTransactionUiState) -> ManualTransactionUiState) {
        _uiState.update { current ->
            transform(current).copy(fieldErrors = current.fieldErrors - field, message = null)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val errors = buildMap {
            if (state.cardId == null) put("cardId", "Select a card.")
            val amount = state.amount.trim().toDoubleOrNull()
            if (amount == null || !amount.isFinite() || amount <= 0.0) {
                put("amount", "Enter an amount greater than 0.")
            }
            if (state.merchant.isBlank()) put("merchant", "Enter a merchant or payment description.")
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        val transaction = TransactionEntity(
            cardId = checkNotNull(state.cardId),
            amount = state.amount.trim().toDouble(),
            merchant = state.merchant.trim(),
            category = state.category,
            transactionType = state.transactionType,
            transactionDate = state.transactionDate,
            description = state.description.trim().ifEmpty { null },
            source = "MANUAL",
            smsId = null,
            confidence = null,
            createdAt = System.currentTimeMillis(),
            fingerprint = null
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            try {
                when (transactionCreationService.save(transaction)) {
                    is TransactionSaveResult.Inserted ->
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                    is TransactionSaveResult.Duplicate ->
                        _uiState.update {
                            it.copy(isSaving = false, message = "This transaction appears to be a duplicate.")
                        }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, message = "Unable to save this transaction. Please try again.")
                }
            }
        }
    }
}
