package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.BillingStatementEntity
import com.kamal.creditliability.data.entity.EmiObligationEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceCalculatorTest {
    private val calculator = FinanceCalculator()
    private val zone = ZoneId.systemDefault()
    private val now = date("2026-10-15")

    @Test
    fun separatesCurrentBillFromNetUnbilledTransactions() {
        val result = calculator.calculate(
            transactions = listOf(
                transaction(id = 1, type = "PURCHASE", amount = 900.0, date = "2026-10-04"),
                transaction(id = 2, type = "PURCHASE", amount = 600.0, date = "2026-10-08"),
                transaction(id = 3, type = "PAYMENT", amount = 200.0, date = "2026-10-10")
            ),
            statements = listOf(
                BillingStatementEntity(
                    cardId = 1,
                    statementDate = date("2026-10-05"),
                    dueDate = date("2026-10-25"),
                    statementAmount = 2_000.0,
                    paidAmount = 0.0,
                    paymentStatus = "UNPAID",
                    createdAt = now
                )
            ),
            emis = emptyList(),
            nowMillis = now
        )

        assertEquals(1_800.0, result.currentBill, 0.001)
        assertEquals(400.0, result.unbilledAmount, 0.001)
    }

    @Test
    fun postStatementPaymentReducesBillButIsNotSpending() {
        val result = calculator.calculate(
            transactions = listOf(
                transaction(id = 1, type = "PURCHASE", amount = 800.0, date = "2026-10-08"),
                transaction(id = 2, type = "PAYMENT", amount = 300.0, date = "2026-10-10")
            ),
            statements = listOf(
                BillingStatementEntity(
                    cardId = 1,
                    statementDate = date("2026-10-05"),
                    dueDate = date("2026-10-25"),
                    statementAmount = 1_000.0,
                    createdAt = now
                )
            ),
            emis = emptyList(),
            nowMillis = now
        )

        assertEquals(700.0, result.currentBill, 0.001)
        assertEquals(800.0, result.totalMonthlySpending, 0.001)
        assertEquals(800.0, result.monthlySpending.single().amount, 0.001)
    }

    @Test
    fun linkedRefundReducesCategorySpendAndActiveEmisAreSummed() {
        val result = calculator.calculate(
            transactions = listOf(
                transaction(id = 10, type = "PURCHASE", amount = 1_000.0, date = "2026-10-08"),
                transaction(
                    id = 11,
                    type = "REFUND",
                    amount = 250.0,
                    date = "2026-10-12",
                    relatedTransactionId = 10
                )
            ),
            statements = emptyList(),
            emis = listOf(
                emi(active = true, remaining = 4, amount = 350.0),
                emi(active = false, remaining = 0, amount = 500.0)
            ),
            nowMillis = now
        )

        assertEquals(750.0, result.totalMonthlySpending, 0.001)
        assertEquals("SHOPPING", result.monthlySpending.single().category)
        assertEquals(350.0, result.monthlyEmi, 0.001)
    }

    private fun date(value: String): Long =
        LocalDate.parse(value).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun transaction(
        id: Long,
        type: String,
        amount: Double,
        date: String,
        relatedTransactionId: Long? = null
    ) = TransactionEntity(
        id = id,
        cardId = 1,
        amount = amount,
        merchant = "Shop",
        category = "SHOPPING",
        transactionType = type,
        transactionDate = date(date),
        description = null,
        source = "TEST",
        smsId = null,
        confidence = null,
        createdAt = now,
        fingerprint = null,
        relatedTransactionId = relatedTransactionId
    )

    private fun emi(active: Boolean, remaining: Int, amount: Double) = EmiObligationEntity(
        cardId = 1,
        merchant = "Phone",
        totalAmount = amount * 12,
        emiAmount = amount,
        tenure = 12,
        remainingTenure = remaining,
        interest = 0.0,
        processingFee = 0.0,
        nextDueDate = null,
        active = active,
        createdAt = now
    )
}