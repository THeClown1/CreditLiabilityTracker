package com.kamal.creditliability.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.domain.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val repository: CreditCardRepository
) : ViewModel() {
    val cards: StateFlow<List<CreditCardEntity>> =
        repository.observeCards().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalCreditLimit: StateFlow<Double> =
        cards.map { cardList -> cardList.sumOf(CreditCardEntity::creditLimit) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun deleteCard(card: CreditCardEntity) {
        viewModelScope.launch {
            runCatching { repository.deleteCard(card) }
                .onFailure { _errorMessage.value = "Unable to delete this card. Please try again." }
        }
    }

}
