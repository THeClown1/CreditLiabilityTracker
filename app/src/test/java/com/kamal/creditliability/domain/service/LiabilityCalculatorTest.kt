package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.CardLiabilitySummary
import org.junit.Assert.assertEquals
import org.junit.Test

class LiabilityCalculatorTest {
    private val calculator = LiabilityCalculator()

    @Test
    fun purchaseAndPaymentProduceExpectedOutstanding() {
        val summary = calculator.calculate(
            creditCards = listOf(card(id = 1, limit = 100_000.0)),
            transactions = listOf(
                transaction(cardId = 1, amount = 10_000.0, type = "PURCHASE"),
                transaction(cardId = 1, amount = 3_000.0, type = "PAYMENT")
            )
        )

        assertEquals(7_000.0, summary.totalLiability, DELTA)
        assertEquals(7_000.0, summary.cards.single().outstanding, DELTA)
    }

    @Test
    fun refundReducesOutstanding() {
        val summary = calculator.calculate(
            creditCards = listOf(card(id = 1, limit = 50_000.0)),
            transactions = listOf(
                transaction(cardId = 1, amount = 5_000.0, type = "PURCHASE"),
                transaction(cardId = 1, amount = 1_250.0, type = "REFUND")
            )
        )

        assertEquals(3_750.0, summary.totalLiability, DELTA)
    }

    @Test
    fun feeEmiAndCashWithdrawalIncreaseOutstanding() {
        val summary = calculator.calculate(
            creditCards = listOf(card(id = 1, limit = 50_000.0)),
            transactions = listOf(
                transaction(cardId = 1, amount = 100.0, type = "FEE"),
                transaction(cardId = 1, amount = 2_000.0, type = "EMI"),
                transaction(cardId = 1, amount = 500.0, type = "CASH_WITHDRAWAL")
            )
        )

        assertEquals(2_600.0, summary.totalLiability, DELTA)
    }

    @Test
    fun calculatesAvailableCreditAndOverallAndCardUtilisation() {
        val summary = calculator.calculate(
            creditCards = listOf(
                card(id = 1, limit = 100_000.0),
                card(id = 2, limit = 50_000.0)
            ),
            transactions = listOf(
                transaction(cardId = 1, amount = 25_000.0, type = "PURCHASE"),
                transaction(cardId = 2, amount = 12_500.0, type = "PURCHASE")
            )
        )

        assertEquals(37_500.0, summary.totalLiability, DELTA)
        assertEquals(150_000.0, summary.totalCreditLimit, DELTA)
        assertEquals(112_500.0, summary.availableCredit, DELTA)
        assertEquals(25.0, summary.overallUtilisationPercentage, DELTA)
        assertEquals(25.0, summary.cards[0].utilisationPercentage, DELTA)
        assertEquals(25.0, summary.cards[1].utilisationPercentage, DELTA)
        assertEquals(75_000.0, summary.cards[0].availableCredit, DELTA)
    }

    @Test
    fun zeroCreditLimitReturnsZeroUtilisation() {
        val summary = calculator.calculate(
            creditCards = listOf(card(id = 1, limit = 0.0)),
            transactions = listOf(
                transaction(cardId = 1, amount = 500.0, type = "PURCHASE")
            )
        )

        assertEquals(0.0, summary.overallUtilisationPercentage, DELTA)
        assertEquals(0.0, summary.cards.single().utilisationPercentage, DELTA)
    }

    @Test
    fun noCardsReturnsZeroSummary() {
        val summary = calculator.calculate(emptyList(), emptyList())

        assertEquals(0.0, summary.totalLiability, DELTA)
        assertEquals(0.0, summary.totalCreditLimit, DELTA)
        assertEquals(0.0, summary.availableCredit, DELTA)
        assertEquals(0.0, summary.overallUtilisationPercentage, DELTA)
        assertEquals(emptyList<CardLiabilitySummary>(), summary.cards)
    }

    @Test
    fun unsupportedTransactionTypeDoesNotChangeLiability() {
        val summary = calculator.calculate(
            creditCards = listOf(card(id = 1, limit = 10_000.0)),
            transactions = listOf(
                transaction(cardId = 1, amount = 1_000.0, type = "OTHER")
            )
        )

        assertEquals(0.0, summary.totalLiability, DELTA)
    }

    private fun card(id: Long, limit: Double) = CreditCardEntity(
        id = id,
        bankName = "Test Bank",
        cardName = "Test Card",
        lastFourDigits = "1234",
        creditLimit = limit,
        billingDay = null,
        dueDay = null,
        createdAt = 0L
    )

    private fun transaction(cardId: Long, amount: Double, type: String) = TransactionEntity(
        cardId = cardId,
        amount = amount,
        merchant = "Test merchant",
        category = "OTHERS",
        transactionType = type,
        transactionDate = 0L,
        description = null,
        source = "MANUAL",
        smsId = null,
        confidence = null,
        createdAt = 0L,
        fingerprint = null
    )

    private companion object {
        const val DELTA = 0.0001
    }
}
