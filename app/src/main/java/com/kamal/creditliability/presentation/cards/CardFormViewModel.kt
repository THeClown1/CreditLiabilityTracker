package com.kamal.creditliability.presentation.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.domain.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardFormUiState(
    val bankName: String = "",
    val cardName: String = "",
    val lastFourDigits: String = "",
    val creditLimit: String = "",
    val billingDay: String = "",
    val dueDay: String = "",
    val isLoading: Boolean = false,
    val isLoadFailed: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val message: String? = null
)

@HiltViewModel
class CardFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CreditCardRepository
) : ViewModel() {
    private val cardId = savedStateHandle.get<Long>("cardId") ?: 0L
    private var existingCard: CreditCardEntity? = null

    private val _uiState = MutableStateFlow(CardFormUiState(isLoading = cardId > 0))
    val uiState = _uiState.asStateFlow()

    init {
        if (cardId > 0) loadCard()
    }

    private fun loadCard() {
        viewModelScope.launch {
            try {
                val card = repository.getCard(cardId)
                if (card == null) {
                    _uiState.update {
                        it.copy(isLoading = false, isLoadFailed = true, message = "This card could not be found.")
                    }
                    return@launch
                }
                existingCard = card
                _uiState.value = CardFormUiState(
                    bankName = card.bankName,
                    cardName = card.cardName,
                    lastFourDigits = card.lastFourDigits,
                    creditLimit = card.creditLimit.toString(),
                    billingDay = card.billingDay?.toString().orEmpty(),
                    dueDay = card.dueDay?.toString().orEmpty()
                )
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isLoadFailed = true, message = "Unable to load this card.")
                }
            }
        }
    }

    fun onBankNameChanged(value: String) = updateField("bankName") { it.copy(bankName = value) }
    fun onCardNameChanged(value: String) = updateField("cardName") { it.copy(cardName = value) }
    fun onLastFourDigitsChanged(value: String) =
        updateField("lastFourDigits") { it.copy(lastFourDigits = value) }
    fun onCreditLimitChanged(value: String) =
        updateField("creditLimit") { it.copy(creditLimit = value) }
    fun onBillingDayChanged(value: String) =
        updateField("billingDay") { it.copy(billingDay = value) }
    fun onDueDayChanged(value: String) =
        updateField("dueDay") { it.copy(dueDay = value) }

    private fun updateField(field: String, transform: (CardFormUiState) -> CardFormUiState) {
        _uiState.update { current ->
            transform(current).copy(
                fieldErrors = current.fieldErrors - field,
                message = null
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return

        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, message = null) }
            return
        }

        val oldCard = existingCard
        val card = CreditCardEntity(
            id = oldCard?.id ?: 0L,
            bankName = state.bankName.trim(),
            cardName = state.cardName.trim(),
            lastFourDigits = state.lastFourDigits,
            creditLimit = state.creditLimit.trim().toDouble(),
            billingDay = state.billingDay.toOptionalDay(),
            dueDay = state.dueDay.toOptionalDay(),
            createdAt = oldCard?.createdAt ?: System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            try {
                if (oldCard == null) repository.insertCard(card) else repository.updateCard(card)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, message = "Unable to save this card. Please try again.")
                }
            }
        }
    }

    private fun validate(state: CardFormUiState): Map<String, String> = buildMap {
        if (state.bankName.isBlank()) put("bankName", "Enter the bank name.")
        if (state.cardName.isBlank()) put("cardName", "Enter a name for this card.")
        if (!state.lastFourDigits.matches(Regex("\\d{4}"))) {
            put("lastFourDigits", "Enter exactly 4 digits.")
        }

        val limit = state.creditLimit.trim().toDoubleOrNull()
        if (limit == null || !limit.isFinite() || limit <= 0.0) {
            put("creditLimit", "Enter a credit limit greater than 0.")
        }

        validateDay(state.billingDay, "Billing day")?.let {
            put("billingDay", it)
        }
        validateDay(state.dueDay, "Due day")?.let {
            put("dueDay", it)
        }
    }

    private fun validateDay(value: String, label: String): String? {
        if (value.isBlank()) return null
        val day = value.toIntOrNull()
        return if (day == null || day !in 1..31) "$label must be between 1 and 31." else null
    }

    private fun String.toOptionalDay(): Int? = trim().takeIf(String::isNotEmpty)?.toInt()

    companion object {
        const val CARD_ID_ARGUMENT = "cardId"
    }
}
