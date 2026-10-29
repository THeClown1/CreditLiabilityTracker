package com.kamal.creditliability.presentation.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.CardLiabilitySummary
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.repository.TransactionRepository
import com.kamal.creditliability.domain.service.LiabilityCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val MILLIS_PER_DAY = 86_400_000L

data class CardTransactionFilters(
    val fromDateMillis: Long? = null,
    val toDateMillis: Long? = null,
    val category: String? = null,
    val transactionType: String? = null
)

data class CardDetailsUiState(
    val card: CreditCardEntity? = null,
    val liability: CardLiabilitySummary? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val filters: CardTransactionFilters = CardTransactionFilters(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CardDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cardRepository: CreditCardRepository,
    transactionRepository: TransactionRepository,
    liabilityCalculator: LiabilityCalculator
) : ViewModel() {
    private val cardId = savedStateHandle.get<Long>(CARD_ID_ARGUMENT) ?: 0L
    private val _filters = MutableStateFlow(CardTransactionFilters())
    val uiState: StateFlow<CardDetailsUiState> =
        combine(
            cardRepository.observeCard(cardId),
            transactionRepository.observeByCard(cardId),
            _filters
        ) { card, transactions, currentFilters ->
            val cardLiability = card?.let {
                liabilityCalculator.calculate(listOf(it), transactions).cards.singleOrNull()
            }
            CardDetailsUiState(
                card = card,
                liability = cardLiability,
                transactions = transactions.filter { it.matches(currentFilters) },
                filters = currentFilters,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CardDetailsUiState()
        )

    fun setFromDate(dateMillis: Long?) {
        _filters.update { it.copy(fromDateMillis = dateMillis?.toUtcDayStart()) }
    }

    fun setToDate(dateMillis: Long?) {
        _filters.update { it.copy(toDateMillis = dateMillis?.toUtcDayStart()) }
    }

    fun setCategory(category: String?) {
        _filters.update { it.copy(category = category) }
    }

    fun setTransactionType(type: String?) {
        _filters.update { it.copy(transactionType = type) }
    }

    fun clearFilters() {
        _filters.value = CardTransactionFilters()
    }

    private fun TransactionEntity.matches(filters: CardTransactionFilters): Boolean {
        val transactionDay = transactionDate / MILLIS_PER_DAY
        val fromDay = filters.fromDateMillis?.div(MILLIS_PER_DAY)
        val toDay = filters.toDateMillis?.div(MILLIS_PER_DAY)

        return (fromDay == null || transactionDay >= fromDay) &&
            (toDay == null || transactionDay <= toDay) &&
            (filters.category == null || category == filters.category) &&
            (filters.transactionType == null || transactionType == filters.transactionType)
    }

    private fun Long.toUtcDayStart(): Long = this / MILLIS_PER_DAY * MILLIS_PER_DAY

    companion object {
        const val CARD_ID_ARGUMENT = "cardId"
    }
}
