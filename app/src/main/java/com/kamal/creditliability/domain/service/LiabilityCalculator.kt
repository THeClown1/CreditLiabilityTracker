package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.CardLiabilitySummary
import com.kamal.creditliability.domain.model.LiabilitySummary
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale

@Singleton
class LiabilityCalculator @Inject constructor() {
    fun calculate(
        creditCards: List<CreditCardEntity>,
        transactions: List<TransactionEntity>
    ): LiabilitySummary {
        val transactionsByCard = transactions.groupBy(TransactionEntity::cardId)
        val cardSummaries = creditCards.map { card ->
            val outstanding = transactionsByCard[card.id]
                .orEmpty()
                .sumOf(::liabilityImpact)

            CardLiabilitySummary(
                cardId = card.id,
                outstanding = outstanding,
                creditLimit = card.creditLimit,
                availableCredit = card.creditLimit - outstanding,
                utilisationPercentage = percentage(outstanding, card.creditLimit)
            )
        }

        val totalLiability = cardSummaries.sumOf(CardLiabilitySummary::outstanding)
        val totalCreditLimit = cardSummaries.sumOf(CardLiabilitySummary::creditLimit)

        return LiabilitySummary(
            totalLiability = totalLiability,
            totalCreditLimit = totalCreditLimit,
            availableCredit = totalCreditLimit - totalLiability,
            overallUtilisationPercentage = percentage(totalLiability, totalCreditLimit),
            cards = cardSummaries
        )
    }

    private fun liabilityImpact(transaction: TransactionEntity): Double =
        when (transaction.transactionType.uppercase(Locale.ROOT)) {
            PURCHASE,
            FEE,
            EMI,
            CASH_WITHDRAWAL -> transaction.amount

            PAYMENT,
            REFUND -> -transaction.amount

            else -> 0.0
        }

    private fun percentage(outstanding: Double, creditLimit: Double): Double =
        if (creditLimit == 0.0) 0.0 else outstanding / creditLimit * 100.0

    private companion object {
        const val PURCHASE = "PURCHASE"
        const val PAYMENT = "PAYMENT"
        const val REFUND = "REFUND"
        const val FEE = "FEE"
        const val EMI = "EMI"
        const val CASH_WITHDRAWAL = "CASH_WITHDRAWAL"
    }
}
