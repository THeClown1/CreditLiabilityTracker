package com.kamal.creditliability.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.FinanceSummary
import com.kamal.creditliability.domain.model.LiabilitySummary
import com.kamal.creditliability.domain.repository.BillingStatementRepository
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.repository.EmiObligationRepository
import com.kamal.creditliability.domain.repository.TransactionRepository
import com.kamal.creditliability.domain.service.FinanceCalculator
import com.kamal.creditliability.domain.service.LiabilityCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cardRepository: CreditCardRepository,
    transactionRepository: TransactionRepository,
    billingRepository: BillingStatementRepository,
    emiRepository: EmiObligationRepository,
    liabilityCalculator: LiabilityCalculator,
    financeCalculator: FinanceCalculator
) : ViewModel() {
    val cards: StateFlow<List<CreditCardEntity>> =
        cardRepository.observeCards().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val liabilitySummary: StateFlow<LiabilitySummary> =
        combine(
            cardRepository.observeCards(),
            transactionRepository.observeTransactions(),
            liabilityCalculator::calculate
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LiabilitySummary()
        )

    val financeSummary: StateFlow<FinanceSummary> =
        combine(
            transactionRepository.observeTransactions(),
            billingRepository.observeAll(),
            emiRepository.observeAll()
        ) { transactions, statements, emis ->
            financeCalculator.calculate(transactions, statements, emis)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FinanceSummary()
        )

    val recentTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.observeRecent(5)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}